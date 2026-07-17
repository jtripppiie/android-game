extends Node

var world: AlaskaStage
var ui := CanvasLayer.new()
var transition_locked := false
var splash_root: Control
var splash_prompt: Label
var splash_skippable := false
var splash_dismissing := false
var current_screen := "startup"
var verification_harness: AndroidVerificationHarness

const SPLASH_MINIMUM_SECONDS := 1.25
const SPLASH_TOTAL_SECONDS := 4.0
const SPLASH_FADE_SECONDS := 0.35

func _ready() -> void:
	add_child(ui)
	var smoke_stage := -1
	var touch_audit := false
	var lifecycle_audit := false
	var system_audit := false
	var pause_audit := false
	var mechanics_audit := false
	process_mode = Node.PROCESS_MODE_ALWAYS
	for argument in OS.get_cmdline_user_args():
		if argument.begins_with("--stage-smoke="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--autoplay-audit="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--visual-audit="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--geometry-audit="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--debug-overlay-audit="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument == "--touch-audit": touch_audit = true
		elif argument == "--lifecycle-audit": lifecycle_audit = true
		elif argument == "--system-audit": system_audit = true
		elif argument == "--pause-audit": pause_audit = true
		elif argument == "--mechanics-audit": mechanics_audit = true
	var verification_scenario := AndroidBridge.verification_scenario()
	if not verification_scenario.is_empty():
		verification_harness = AndroidVerificationHarness.new()
		add_child(verification_harness)
		verification_harness.run.call_deferred(
			self,
			verification_scenario,
			AndroidBridge.verification_stage()
		)
		return
	if mechanics_audit:
		var mechanics := MechanicsAuditor.new()
		add_child(mechanics)
		mechanics.run.call_deferred(self)
		return
	if pause_audit:
		run_pause_audit.call_deferred()
		return
	if system_audit:
		run_system_audit.call_deferred()
		return
	if lifecycle_audit:
		run_lifecycle_audit.call_deferred()
		return
	if touch_audit:
		run_touch_audit.call_deferred()
		return
	if smoke_stage >= 0: start_stage(clampi(smoke_stage, 0, 4))
	else: show_launch_splash()

func show_launch_splash() -> void:
	clear_ui()
	splash_skippable = false
	splash_dismissing = false
	splash_root = Control.new()
	splash_root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	splash_root.mouse_filter = Control.MOUSE_FILTER_STOP
	ui.add_child(splash_root)
	var art := TextureRect.new()
	art.texture = load("res://assets/boot_splash.png")
	art.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	art.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
	art.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_COVERED
	art.mouse_filter = Control.MOUSE_FILTER_IGNORE
	splash_root.add_child(art)
	var shade := ColorRect.new()
	shade.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	shade.offset_top = -112
	shade.offset_bottom = 0
	shade.color = Color(0.01, 0.04, 0.08, 0.58)
	shade.mouse_filter = Control.MOUSE_FILTER_IGNORE
	splash_root.add_child(shade)
	splash_prompt = Label.new()
	splash_prompt.text = "TAP TO BEGIN"
	splash_prompt.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	splash_prompt.offset_top = -88
	splash_prompt.offset_bottom = -24
	splash_prompt.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	splash_prompt.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	splash_prompt.add_theme_font_size_override("font_size", 28)
	splash_prompt.add_theme_color_override("font_color", Color("#fff0a8"))
	splash_prompt.add_theme_constant_override("outline_size", 6)
	splash_prompt.add_theme_color_override("font_outline_color", Color(0.01, 0.04, 0.08, 0.95))
	splash_prompt.modulate.a = 0.0
	splash_prompt.mouse_filter = Control.MOUSE_FILTER_IGNORE
	splash_root.add_child(splash_prompt)
	reveal_splash_prompt.call_deferred()

func reveal_splash_prompt() -> void:
	await get_tree().create_timer(SPLASH_MINIMUM_SECONDS).timeout
	if not is_instance_valid(splash_root) or splash_dismissing: return
	splash_skippable = true
	var prompt_tween := create_tween()
	prompt_tween.tween_property(splash_prompt, "modulate:a", 1.0, 0.25)
	await get_tree().create_timer(SPLASH_TOTAL_SECONDS - SPLASH_MINIMUM_SECONDS).timeout
	dismiss_launch_splash()

func _input(event: InputEvent) -> void:
	if not is_instance_valid(splash_root) or not splash_skippable: return
	if event is InputEventScreenTouch and event.pressed:
		dismiss_launch_splash()
		get_viewport().set_input_as_handled()
	elif event is InputEventMouseButton and event.pressed:
		dismiss_launch_splash()
		get_viewport().set_input_as_handled()
	elif event is InputEventKey and event.pressed and not event.echo:
		dismiss_launch_splash()
		get_viewport().set_input_as_handled()

func dismiss_launch_splash() -> void:
	if splash_dismissing or not is_instance_valid(splash_root): return
	splash_dismissing = true
	splash_skippable = false
	var fade := create_tween()
	fade.tween_property(splash_root, "modulate:a", 0.0, SPLASH_FADE_SECONDS)
	await fade.finished
	splash_root = null
	splash_prompt = null
	show_menu()

func run_lifecycle_audit() -> void:
	start_stage(0)
	await get_tree().process_frame
	assert(get_tree().get_nodes_in_group("player").size() == 1)
	assert(get_tree().get_nodes_in_group("active_stage").size() == 1)
	assert(is_instance_valid(world.hud))
	world.hud.audit_layout()
	assert(is_instance_valid(world.notebook))
	world.notebook.audit_layout()
	world.audit_debug_overlay()
	assert(is_instance_valid(world.boss_node))
	assert(not world.boss_node.activated)
	var dormant_boss_timer := world.boss_node.state_timer
	for frame in range(4):
		await get_tree().physics_frame
	assert(is_equal_approx(world.boss_node.state_timer, dormant_boss_timer))
	assert(get_tree().get_nodes_in_group("boss_hazard").is_empty())
	var first_runner := get_tree().get_first_node_in_group("player") as AlaskaRunner
	first_runner.ring_chain = 4
	first_runner.ring_chain_timer = 1.0
	first_runner.ring_rush_timer = 3.0
	first_runner.was_on_floor = true
	first_runner.respawn()
	assert(first_runner.ring_chain == 0)
	assert(first_runner.ring_chain_timer == 0.0 and first_runner.ring_rush_timer == 0.0)
	assert(not first_runner.was_on_floor)
	show_map()
	await get_tree().process_frame
	start_stage(1)
	await get_tree().process_frame
	assert(get_tree().get_nodes_in_group("player").size() == 1)
	assert(get_tree().get_nodes_in_group("active_stage").size() == 1)
	print("LIFECYCLE AUDIT PASS · clean respawn · map transition · one world · one runner")
	get_tree().quit(0)

func run_system_audit() -> void:
	var old_unlocked := GameSession.unlocked_stage
	var old_selected := GameSession.selected_stage
	var old_total := GameSession.total_score
	var old_best := GameSession.best_scores.duplicate()
	var old_completed := GameSession.completed_runs.duplicate()
	var old_times := GameSession.best_times.duplicate()
	var old_stars := GameSession.best_stars.duplicate()
	var old_muted := GameSession.muted
	GameSession.unlocked_stage = 0
	GameSession.total_score = 0
	GameSession.best_scores = [0, 0, 0, 0, 0]
	GameSession.complete_stage(0, 100)
	assert(GameSession.total_score == 100)
	assert(GameSession.best_scores[0] == 100)
	assert(GameSession.unlocked_stage == 1)
	assert(GameSession.star_rating(0, 300, 40.0, 0) == 3)
	assert(GameSession.star_rating(0, 0, 90.0, 2) == 0)
	GameSession.complete_stage(0, 50)
	assert(GameSession.total_score == 150)
	assert(GameSession.best_scores[0] == 100)
	show_menu()
	assert(current_screen == "menu" and ui.get_child_count() == 2)
	show_map()
	assert(current_screen == "map" and ui.get_child_count() == 2)
	var map_panel := ui.get_child(1) as VBoxContainer
	var stage_two_button := map_panel.get_child(2) as Button
	assert(not stage_two_button.disabled)
	stage_two_button.pressed.emit()
	assert(current_screen == "stage")
	assert(GameSession.selected_stage == 1)
	assert(is_instance_valid(world) and world.stage_index == 1)
	show_map()
	show_customize()
	assert(current_screen == "customize" and ui.get_child_count() == 2)
	show_accessibility()
	assert(current_screen == "accessibility" and ui.get_child_count() == 2)
	var accessibility_panel := ui.get_child(1) as VBoxContainer
	var mute_toggle := accessibility_panel.get_child(1) as CheckButton
	mute_toggle.toggled.emit(not old_muted)
	assert(GameSession.muted == not old_muted)
	mute_toggle.toggled.emit(old_muted)
	assert(GameSession.muted == old_muted)
	assert(FeedbackService.profile_for("LAND") == Vector3.ZERO)
	assert(FeedbackService.profile_for("HIT · COMBO LOST").z >= 50.0)
	assert(FeedbackService.profile_for("BOSS WINDUP").x > 0.0)
	assert(FeedbackService.profile_for("BOSS WINDUP").z == 0.0)
	start_stage(0)
	world.player.score = 320
	world.run_elapsed = 40.0
	world.damage_taken = 0
	world.key_collected = true
	world.survivors_found = 2
	world.boss_defeated = true
	world.finish_level(world.player)
	assert(get_tree().paused)
	assert(is_instance_valid(world.stage_complete_overlay))
	assert(world.stage_complete_overlay.rating_label.text.contains("3/3"))
	assert(GameSession.best_stars[0] == 3)
	world.stage_complete_overlay.replay_requested.emit()
	await get_tree().process_frame
	assert(not get_tree().paused)
	assert(get_tree().get_nodes_in_group("active_stage").size() == 1)
	assert(get_tree().get_nodes_in_group("player").size() == 1)
	show_map()
	GameSession.unlocked_stage = old_unlocked
	GameSession.selected_stage = old_selected
	GameSession.total_score = old_total
	GameSession.best_scores = old_best
	GameSession.completed_runs = old_completed
	GameSession.best_times = old_times
	GameSession.best_stars = old_stars
	GameSession.save_profile()
	print("SYSTEM AUDIT PASS · score ownership · 3-star result · saved progression · clean replay · bound UI actions")
	get_tree().quit(0)

func run_pause_audit() -> void:
	start_stage(0)
	await get_tree().physics_frame
	Input.action_press("move_right")
	Input.action_press("sprint")
	for frame in range(6):
		await get_tree().physics_frame
	var moving_x := world.player.global_position.x
	assert(moving_x > 190.0)
	world.hud.post_message("PAUSE CLOCK", 2, 4.0)
	var paused_message_time := world.hud.message_time_left
	world.toggle_pause_panel()
	assert(get_tree().paused and world.pause_panel.visible)
	assert(not Input.is_action_pressed("move_right"))
	for frame in range(12):
		await get_tree().process_frame
	assert(is_equal_approx(world.player.global_position.x, moving_x))
	assert(is_equal_approx(world.hud.message_time_left, paused_message_time))
	world.resume_run()
	Input.action_press("move_right")
	Input.action_press("sprint")
	for frame in range(6):
		await get_tree().physics_frame
	assert(world.player.global_position.x > moving_x)
	Input.action_release("move_right")
	Input.action_release("sprint")
	print("PAUSE AUDIT PASS · player frozen · controls frozen · resume restored")
	get_tree().quit(0)

func run_touch_audit() -> void:
	var original_touch_scale := GameSession.touch_scale
	GameSession.touch_scale = 1.0
	var controls := TouchControls.new()
	controls.size = Vector2(1280, 720)
	add_child(controls)
	controls.review_mode = true
	controls.layout_controls()
	var right: Rect2 = controls.controls["move_right"]
	var jump: Rect2 = controls.controls["jump"]
	var dpad_up: Rect2 = controls.controls["dpad_up"]
	var review_note: Rect2 = controls.controls["debug_note"]
	var review_ids: Rect2 = controls.controls["debug_ids"]
	assert(jump.size == Vector2(132, 132))
	var objective_hud := Rect2(526, 10, 512, 74)
	var pause_hud := Rect2(1050, 10, 216, 74)
	assert(review_note.position.y >= 92.0 and review_ids.position.y >= 92.0)
	assert(review_note.intersection(pause_hud).get_area() == 0.0)
	assert(review_ids.intersection(objective_hud).get_area() == 0.0)
	var move_press := InputEventScreenTouch.new()
	move_press.index = 1; move_press.position = right.get_center(); move_press.pressed = true
	controls._input(move_press)
	var jump_press := InputEventScreenTouch.new()
	jump_press.index = 2; jump_press.position = jump.get_center(); jump_press.pressed = true
	controls._input(jump_press)
	assert(Input.is_action_pressed("move_right") and Input.is_action_pressed("sprint") and Input.is_action_pressed("jump"))
	var jump_release := InputEventScreenTouch.new()
	jump_release.index = 2; jump_release.position = jump.get_center(); jump_release.pressed = false
	controls._input(jump_release)
	assert(Input.is_action_pressed("move_right") and Input.is_action_pressed("sprint") and not Input.is_action_pressed("jump"))
	var move_release := InputEventScreenTouch.new()
	move_release.index = 1; move_release.position = right.get_center(); move_release.pressed = false
	controls._input(move_release)
	assert(not Input.is_action_pressed("move_right") and not Input.is_action_pressed("sprint"))
	var up_press := InputEventScreenTouch.new()
	up_press.index = 3; up_press.position = dpad_up.get_center(); up_press.pressed = true
	controls._input(up_press)
	assert(Input.is_action_pressed("jump"))
	var up_release := InputEventScreenTouch.new()
	up_release.index = 3; up_release.position = dpad_up.get_center(); up_release.pressed = false
	controls._input(up_release)
	assert(not Input.is_action_pressed("jump"))
	var shallow_drift := InputEventScreenTouch.new()
	shallow_drift.index = 8
	shallow_drift.position = controls.dpad_bounds.get_center() + Vector2(70, -20)
	shallow_drift.pressed = true
	controls._input(shallow_drift)
	assert(Input.is_action_pressed("move_right") and not Input.is_action_pressed("jump"))
	var shallow_release := InputEventScreenTouch.new()
	shallow_release.index = 8
	shallow_release.position = shallow_drift.position
	shallow_release.pressed = false
	controls._input(shallow_release)
	var diagonal := InputEventScreenTouch.new()
	diagonal.index = 4
	diagonal.position = controls.dpad_bounds.get_center() + Vector2(52, -52)
	diagonal.pressed = true
	controls._input(diagonal)
	assert(Input.is_action_pressed("move_right") and Input.is_action_pressed("jump"))
	var drift := InputEventScreenDrag.new()
	drift.index = 4
	drift.position = Vector2(controls.dpad_bounds.end.x + 18, controls.dpad_bounds.get_center().y)
	controls._input(drift)
	assert(Input.is_action_pressed("move_right") and not Input.is_action_pressed("jump"))
	var diagonal_release := InputEventScreenTouch.new()
	diagonal_release.index = 4; diagonal_release.position = drift.position; diagonal_release.pressed = false
	controls._input(diagonal_release)
	var owned_jump := InputEventScreenTouch.new()
	owned_jump.index = 5; owned_jump.position = jump.get_center(); owned_jump.pressed = true
	controls._input(owned_jump)
	assert(Input.is_action_pressed("jump"))
	var cross_drag := InputEventScreenDrag.new()
	cross_drag.index = 5
	cross_drag.position = controls.dpad_bounds.get_center() + Vector2(52, 0)
	controls._input(cross_drag)
	assert(not Input.is_action_pressed("jump") and not Input.is_action_pressed("move_right"))
	var cross_release := InputEventScreenTouch.new()
	cross_release.index = 5; cross_release.position = cross_drag.position; cross_release.pressed = false
	controls._input(cross_release)
	var first_dpad := InputEventScreenTouch.new()
	first_dpad.index = 6
	first_dpad.position = controls.dpad_bounds.get_center() + Vector2(52, 0)
	first_dpad.pressed = true
	controls._input(first_dpad)
	var competing_dpad := InputEventScreenTouch.new()
	competing_dpad.index = 7
	competing_dpad.position = controls.dpad_bounds.get_center() + Vector2(-52, 0)
	competing_dpad.pressed = true
	controls._input(competing_dpad)
	assert(Input.is_action_pressed("move_right") and not Input.is_action_pressed("move_left"))
	var first_release := InputEventScreenTouch.new()
	first_release.index = 6; first_release.position = first_dpad.position; first_release.pressed = false
	controls._input(first_release)
	assert(not Input.is_action_pressed("move_right") and not Input.is_action_pressed("move_left"))
	var competing_release := InputEventScreenTouch.new()
	competing_release.index = 7; competing_release.position = competing_dpad.position; competing_release.pressed = false
	controls._input(competing_release)
	for scale in [0.85, 1.15]:
		GameSession.touch_scale = scale
		controls.layout_controls()
		assert(is_equal_approx(controls.dpad_bounds.size.x, TouchControls.DPAD_SIZE * scale))
		assert(Rect2(Vector2.ZERO, controls.size).encloses(controls.dpad_bounds))
		var scaled_jump: Rect2 = controls.controls["jump"]
		var scaled_fire: Rect2 = controls.controls["fire"]
		var scaled_dash: Rect2 = controls.controls["dash"]
		assert(scaled_jump.intersection(scaled_fire).get_area() == 0.0)
		assert(scaled_jump.intersection(scaled_dash).get_area() == 0.0)
	GameSession.touch_scale = original_touch_scale
	print("TOUCH AUDIT PASS · ownership · deliberate vertical threshold · no cross-control drag · single dpad thumb · diagonal · drift")
	get_tree().quit(0)

func clear_ui() -> void:
	# Detach immediately so a newly built screen cannot overlap or receive
	# input alongside the screen waiting for deletion at frame end.
	for child in ui.get_children():
		ui.remove_child(child)
		child.queue_free()

func show_menu() -> void:
	dispose_world()
	current_screen = "menu"
	transition_locked = false
	clear_ui()
	add_backdrop("background_midnight_sun.png")
	var view := get_viewport().get_visible_rect().size
	var frame := PanelContainer.new()
	frame.name = "MainMenuFrame"
	var frame_width := minf(900.0, view.x - 96.0)
	var frame_height := minf(620.0, view.y - 64.0)
	frame.position = Vector2((view.x - frame_width) * 0.5, (view.y - frame_height) * 0.5)
	frame.size = Vector2(frame_width, frame_height)
	frame.add_theme_stylebox_override("panel", menu_panel_style())
	ui.add_child(frame)
	var panel := VBoxContainer.new()
	panel.name = "MainMenuActions"
	panel.alignment = BoxContainer.ALIGNMENT_CENTER
	panel.add_theme_constant_override("separation", 22)
	frame.add_child(panel)
	var title := Label.new()
	title.text = "YOU RUSH · ALASKA"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 54 if GameSession.large_text else 48)
	title.custom_minimum_size.y = 76
	title.add_theme_color_override("font_color", Color("#fff4d2"))
	title.add_theme_constant_override("outline_size", 8)
	title.add_theme_color_override("font_outline_color", Color(0.02, 0.08, 0.14, 0.9))
	panel.add_child(title)
	var subtitle := Label.new()
	subtitle.text = "FIVE TRAILS · KEYS · RESCUES · WILDLIFE BOSSES"
	subtitle.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	subtitle.add_theme_font_size_override("font_size", 21 if GameSession.large_text else 19)
	subtitle.add_theme_color_override("font_color", Color("#9ee8f5"))
	subtitle.custom_minimum_size.y = 34
	panel.add_child(subtitle)
	for spec in [
		["PLAY ALASKA MAP", show_map],
		["CUSTOMIZE RUNNER", show_customize],
		["ACCESSIBILITY", show_accessibility]
	]:
		var button := add_button(panel, String(spec[0]), spec[1])
		button.custom_minimum_size.y = 88
	var status := Label.new()
	status.text = "ALASKA %s · STAGES %d/5 · LIFETIME %d" % [
		GameSession.APP_VERSION,
		GameSession.unlocked_stage + 1,
		GameSession.total_score
	]
	status.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	status.add_theme_font_size_override("font_size", 22)
	status.add_theme_color_override("font_color", Color("#fff0a8"))
	status.custom_minimum_size.y = 32
	panel.add_child(status)


func menu_panel_style() -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.008, 0.035, 0.075, 0.95)
	style.border_color = Color("#5ce1e6")
	style.set_border_width_all(4)
	style.set_corner_radius_all(24)
	style.content_margin_left = 34
	style.content_margin_right = 34
	style.content_margin_top = 26
	style.content_margin_bottom = 26
	style.shadow_color = Color(0.0, 0.0, 0.0, 0.72)
	style.shadow_size = 16
	style.shadow_offset = Vector2(0, 10)
	return style

func show_map() -> void:
	dispose_world()
	current_screen = "map"
	transition_locked = false
	clear_ui()
	add_backdrop("background_dark_winter.png")
	var panel := VBoxContainer.new()
	var view := get_viewport().get_visible_rect().size
	var panel_width := minf(1040.0, view.x - 80.0)
	panel.position = Vector2((view.x - panel_width) * 0.5, 28)
	panel.size = Vector2(panel_width, view.y - 48)
	panel.add_theme_constant_override("separation", 12)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "ALASKA EXPEDITION MAP"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 40)
	title.custom_minimum_size.y = 66
	panel.add_child(title)
	for index in range(GameSession.STAGES.size()):
		var stage = GameSession.STAGES[index]
		var unlocked := index <= GameSession.unlocked_stage
		var time_text := (
			" · %.1fs" % GameSession.best_times[index]
			if GameSession.best_times[index] > 0.0
			else ""
		)
		var label := "%d · %s · BEST %d · ★ %d/3%s%s" % [
			index + 1,
			stage.name,
			GameSession.best_scores[index],
			GameSession.best_stars[index],
			time_text,
			"" if unlocked else " · LOCKED"
		]
		var button := add_button(panel, label, start_stage.bind(index))
		button.disabled = not unlocked
	add_button(panel, "BACK", show_menu)

func start_stage(index: int) -> void:
	if transition_locked or is_instance_valid(world): return
	transition_locked = true
	current_screen = "stage"
	GameSession.selected_stage = index
	clear_ui()
	world = AlaskaStage.new()
	world.stage_completed.connect(_on_stage_completed)
	world.exit_requested.connect(show_map)
	world.restart_requested.connect(restart_stage)
	world.advance_requested.connect(advance_stage)
	add_child(world)
	transition_locked = false

func _on_stage_completed(stage: int, score: int, elapsed_seconds: float, damage_taken: int) -> void:
	GameSession.complete_stage(stage, score, elapsed_seconds, damage_taken)


func restart_stage(index: int) -> void:
	get_tree().paused = false
	dispose_world()
	transition_locked = false
	start_stage(clampi(index, 0, GameSession.STAGES.size() - 1))


func advance_stage(index: int) -> void:
	get_tree().paused = false
	dispose_world()
	transition_locked = false
	GameSession.selected_stage = clampi(index, 0, GameSession.unlocked_stage)
	GameSession.save_profile()
	start_stage(GameSession.selected_stage)

func dispose_world() -> void:
	if not is_instance_valid(world): return
	if world.get_parent() == self: remove_child(world)
	world.queue_free()
	world = null

func show_customize() -> void:
	current_screen = "customize"
	clear_ui()
	add_backdrop("background_midnight_sun.png")
	var panel := VBoxContainer.new()
	var view := get_viewport().get_visible_rect().size
	var panel_width := minf(860.0, view.x - 96.0)
	panel.position = Vector2((view.x - panel_width) * 0.5, 58)
	panel.size = Vector2(panel_width, view.y - 92)
	panel.add_theme_constant_override("separation", 20)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "CUSTOMIZE RUNNER"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 42)
	title.custom_minimum_size.y = 74
	panel.add_child(title)
	var status := Label.new()
	status.text = "DEFAULT RUNNER" if GameSession.photo_path.is_empty() else "PHOTO READY · " + GameSession.photo_path.get_file()
	status.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	panel.add_child(status)
	add_button(panel, "CHOOSE LOCAL PHOTO", func(): open_photo_picker(status))
	add_button(panel, "RESET PHOTO", func(): GameSession.set_photo_path(""); status.text = "DEFAULT RUNNER")
	add_button(panel, "BACK", show_menu)

func show_accessibility() -> void:
	current_screen = "accessibility"
	clear_ui()
	add_backdrop("background_dark_winter.png")
	var panel := VBoxContainer.new()
	var view := get_viewport().get_visible_rect().size
	var panel_width := minf(860.0, view.x - 96.0)
	panel.position = Vector2((view.x - panel_width) * 0.5, 20)
	panel.size = Vector2(panel_width, view.y - 34)
	panel.add_theme_constant_override("separation", 7)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "ACCESSIBILITY"
	title.add_theme_font_size_override("font_size", 42)
	title.custom_minimum_size.y = 56
	panel.add_child(title)
	for spec in [["MUTE AUDIO", "muted"], ["HAPTICS", "haptics"], ["LARGE TEXT", "large_text"], ["REDUCED MOTION", "reduced_motion"], ["HIGH CONTRAST", "high_contrast"], ["REVIEW MODE · IDS + NOTES", "review_mode"]]:
		var toggle := CheckButton.new()
		toggle.text = spec[0]
		toggle.custom_minimum_size.y = 58
		toggle.add_theme_font_size_override("font_size", 25)
		toggle.button_pressed = GameSession.get(spec[1])
		toggle.toggled.connect(set_accessibility.bind(String(spec[1])))
		panel.add_child(toggle)
	add_touch_size_control(panel)
	add_button(panel, "BACK", show_menu)

func set_accessibility(value: bool, property_name: String) -> void:
	var allowed := ["muted", "haptics", "large_text", "reduced_motion", "high_contrast", "review_mode"]
	if property_name not in allowed:
		push_error("Refused unknown accessibility property: %s" % property_name)
		return
	GameSession.set(property_name, value)
	GameSession.save_profile()


func add_touch_size_control(parent: VBoxContainer) -> void:
	var row := HBoxContainer.new()
	row.custom_minimum_size.y = 58
	row.add_theme_constant_override("separation", 10)
	parent.add_child(row)
	var label := Label.new()
	label.text = "TOUCH SIZE"
	label.custom_minimum_size.x = 250
	label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	label.add_theme_font_size_override("font_size", 25)
	row.add_child(label)
	for spec in [["SMALL", 0.85], ["STANDARD", 1.0], ["LARGE", 1.15]]:
		var button := Button.new()
		button.text = String(spec[0])
		button.toggle_mode = true
		button.button_pressed = is_equal_approx(GameSession.touch_scale, float(spec[1]))
		button.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		button.add_theme_font_size_override("font_size", 20)
		button.pressed.connect(set_touch_scale.bind(float(spec[1])))
		row.add_child(button)


func set_touch_scale(value: float) -> void:
	GameSession.touch_scale = clampf(value, 0.85, 1.15)
	GameSession.save_profile()
	show_accessibility()

func add_button(parent: Control, label: String, callback: Callable) -> Button:
	var button := Button.new()
	button.text = label
	button.custom_minimum_size = Vector2(0, 82)
	button.add_theme_font_size_override("font_size", 30 if GameSession.large_text else 26)
	button.add_theme_color_override("font_color", Color("#f7fcff"))
	button.add_theme_color_override("font_hover_color", Color("#fff0a8"))
	button.add_theme_color_override("font_pressed_color", Color("#071326"))
	button.add_theme_color_override("font_disabled_color", Color("#d5e4e8"))
	button.add_theme_color_override("font_focus_color", Color("#fff0a8"))
	var normal := StyleBoxFlat.new()
	normal.bg_color = Color(0.025, 0.105, 0.16, 0.90)
	normal.border_color = Color("#4ddbb8")
	normal.set_border_width_all(2)
	normal.set_corner_radius_all(16)
	button.add_theme_stylebox_override("normal", normal)
	var hover := normal.duplicate()
	hover.bg_color = Color(0.08, 0.26, 0.32, 0.96)
	hover.border_color = Color("#ffda79")
	button.add_theme_stylebox_override("hover", hover)
	button.add_theme_stylebox_override("pressed", hover)
	var disabled := normal.duplicate()
	disabled.bg_color = Color(0.08, 0.12, 0.15, 0.96)
	disabled.border_color = Color("#78919a")
	button.add_theme_stylebox_override("disabled", disabled)
	button.add_theme_stylebox_override("focus", hover)
	button.pressed.connect(callback)
	parent.add_child(button)
	return button

func open_photo_picker(status: Label) -> void:
	var picker := FileDialog.new()
	picker.use_native_dialog = true
	picker.file_mode = FileDialog.FILE_MODE_OPEN_FILE
	picker.access = FileDialog.ACCESS_FILESYSTEM
	picker.filters = PackedStringArray(["*.png, *.jpg, *.jpeg, *.webp ; Images"])
	picker.file_selected.connect(func(path):
		if GameSession.set_photo_path(path):
			status.text = "PHOTO READY · " + path.get_file()
		else:
			status.text = "PHOTO COULD NOT BE LOADED · MAX 20 MB"
		picker.queue_free()
	)
	picker.canceled.connect(func(): picker.queue_free())
	add_child(picker)
	picker.popup_centered_ratio(0.8)

func add_backdrop(asset_name: String) -> void:
	var backdrop := TextureRect.new()
	backdrop.texture = load("res://assets/%s" % asset_name)
	backdrop.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	backdrop.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
	backdrop.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_COVERED
	backdrop.modulate = Color(0.58, 0.66, 0.72)
	backdrop.mouse_filter = Control.MOUSE_FILTER_IGNORE
	ui.add_child(backdrop)

func _unhandled_input(event: InputEvent) -> void:
	if is_instance_valid(splash_root): return
	if not event.is_action_pressed("ui_cancel"): return
	if is_instance_valid(world):
		if is_instance_valid(world.notebook) and world.notebook.panel.visible:
			world.notebook.close()
		else:
			world.toggle_pause_panel()
		get_viewport().set_input_as_handled()
		return
	if current_screen in ["map", "customize", "accessibility"]:
		show_menu()
	elif current_screen == "menu":
		get_tree().quit()
	get_viewport().set_input_as_handled()

func _notification(what: int) -> void:
	if what == NOTIFICATION_APPLICATION_PAUSED and is_instance_valid(world):
		world.pause_for_background()
