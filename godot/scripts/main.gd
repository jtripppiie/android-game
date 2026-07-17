extends Node

var world: AlaskaStage
var ui := CanvasLayer.new()
var transition_locked := false
var splash_root: Control
var splash_prompt: Label
var splash_skippable := false
var splash_dismissing := false
var current_screen := "startup"

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
	process_mode = Node.PROCESS_MODE_ALWAYS
	for argument in OS.get_cmdline_user_args():
		if argument.begins_with("--stage-smoke="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--autoplay-audit="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--visual-audit="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--geometry-audit="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument == "--touch-audit": touch_audit = true
		elif argument == "--lifecycle-audit": lifecycle_audit = true
		elif argument == "--system-audit": system_audit = true
		elif argument == "--pause-audit": pause_audit = true
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
	shade.position = Vector2(0, -112)
	shade.size = Vector2(1280, 112)
	shade.color = Color(0.01, 0.04, 0.08, 0.58)
	shade.mouse_filter = Control.MOUSE_FILTER_IGNORE
	splash_root.add_child(shade)
	splash_prompt = Label.new()
	splash_prompt.text = "TAP TO BEGIN"
	splash_prompt.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	splash_prompt.position = Vector2(0, -88)
	splash_prompt.size = Vector2(1280, 64)
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
	var old_total := GameSession.total_score
	var old_best := GameSession.best_scores.duplicate()
	GameSession.unlocked_stage = 0
	GameSession.total_score = 0
	GameSession.best_scores = [0, 0, 0, 0, 0]
	GameSession.complete_stage(0, 100)
	assert(GameSession.total_score == 100)
	assert(GameSession.best_scores[0] == 100)
	assert(GameSession.unlocked_stage == 1)
	GameSession.complete_stage(0, 50)
	assert(GameSession.total_score == 150)
	assert(GameSession.best_scores[0] == 100)
	show_menu()
	assert(current_screen == "menu" and ui.get_child_count() == 2)
	show_map()
	assert(current_screen == "map" and ui.get_child_count() == 2)
	show_customize()
	assert(current_screen == "customize" and ui.get_child_count() == 2)
	show_accessibility()
	assert(current_screen == "accessibility" and ui.get_child_count() == 2)
	GameSession.unlocked_stage = old_unlocked
	GameSession.total_score = old_total
	GameSession.best_scores = old_best
	GameSession.save_profile()
	print("SYSTEM AUDIT PASS · score ownership · immediate UI replacement · screen flow")
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
	world.toggle_pause_panel()
	assert(get_tree().paused and world.pause_panel.visible)
	assert(not Input.is_action_pressed("move_right"))
	for frame in range(12):
		await get_tree().process_frame
	assert(is_equal_approx(world.player.global_position.x, moving_x))
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
	var controls := TouchControls.new()
	controls.size = Vector2(1280, 720)
	add_child(controls)
	controls.layout_controls()
	var right: Rect2 = controls.controls["move_right"]
	var jump: Rect2 = controls.controls["jump"]
	var dpad_up: Rect2 = controls.controls["dpad_up"]
	assert(jump.size == Vector2(124, 124))
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
	print("TOUCH AUDIT PASS · circular dpad · diagonal input · drift margin · simultaneous move+jump")
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
	var panel := VBoxContainer.new()
	panel.position = Vector2(250, 54)
	panel.size = Vector2(780, 612)
	panel.add_theme_constant_override("separation", 20)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "YOU RUSH · ALASKA"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 54 if GameSession.large_text else 48)
	title.custom_minimum_size.y = 92
	title.add_theme_color_override("font_color", Color("#fff4d2"))
	title.add_theme_constant_override("outline_size", 8)
	title.add_theme_color_override("font_outline_color", Color(0.02, 0.08, 0.14, 0.9))
	panel.add_child(title)
	add_button(panel, "PLAY ALASKA MAP", show_map)
	add_button(panel, "CUSTOMIZE RUNNER", show_customize)
	add_button(panel, "ACCESSIBILITY", show_accessibility)
	var status := Label.new()
	status.text = "ALASKA 5.3.2 · STAGES %d/5 · LIFETIME %d" % [GameSession.unlocked_stage + 1, GameSession.total_score]
	status.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	status.add_theme_font_size_override("font_size", 22)
	panel.add_child(status)

func show_map() -> void:
	dispose_world()
	current_screen = "map"
	transition_locked = false
	clear_ui()
	add_backdrop("background_dark_winter.png")
	var panel := VBoxContainer.new()
	panel.position = Vector2(170, 36)
	panel.size = Vector2(940, 654)
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
		var label := "%d · %s · BEST %d%s" % [index + 1, stage.name, GameSession.best_scores[index], "" if unlocked else " · LOCKED"]
		var button := add_button(panel, label, func(): start_stage(index))
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
	add_child(world)
	transition_locked = false

func _on_stage_completed(stage: int, score: int) -> void:
	GameSession.complete_stage(stage, score)
	show_map()

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
	panel.position = Vector2(240, 72)
	panel.size = Vector2(800, 576)
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
	add_button(panel, "RESET PHOTO", func(): GameSession.photo_path = ""; status.text = "DEFAULT RUNNER"; GameSession.save_profile())
	add_button(panel, "BACK", show_menu)

func show_accessibility() -> void:
	current_screen = "accessibility"
	clear_ui()
	add_backdrop("background_dark_winter.png")
	var panel := VBoxContainer.new()
	panel.position = Vector2(240, 42)
	panel.size = Vector2(800, 636)
	panel.add_theme_constant_override("separation", 10)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "ACCESSIBILITY"
	title.add_theme_font_size_override("font_size", 42)
	title.custom_minimum_size.y = 68
	panel.add_child(title)
	for spec in [["MUTE AUDIO", "muted"], ["HAPTICS", "haptics"], ["LARGE TEXT", "large_text"], ["REDUCED MOTION", "reduced_motion"], ["HIGH CONTRAST", "high_contrast"], ["REVIEW MODE · IDS + NOTES", "review_mode"]]:
		var toggle := CheckButton.new()
		toggle.text = spec[0]
		toggle.custom_minimum_size.y = 66
		toggle.add_theme_font_size_override("font_size", 25)
		toggle.button_pressed = GameSession.get(spec[1])
		toggle.toggled.connect(func(value): GameSession.set(spec[1], value); GameSession.save_profile())
		panel.add_child(toggle)
	add_button(panel, "BACK", show_menu)

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
	picker.file_selected.connect(func(path): GameSession.photo_path = path; GameSession.save_profile(); status.text = "PHOTO READY · " + path.get_file(); picker.queue_free())
	picker.canceled.connect(func(): picker.queue_free())
	add_child(picker)
	picker.popup_centered_ratio(0.8)

func add_backdrop(asset_name: String) -> void:
	var backdrop := TextureRect.new()
	backdrop.texture = load("res://assets/%s" % asset_name)
	backdrop.position = Vector2.ZERO
	backdrop.size = Vector2(1280, 720)
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
