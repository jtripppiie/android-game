class_name AlaskaGameHud
extends CanvasLayer

signal pause_requested

const VIEW_SIZE := Vector2(1280, 720)
const BAR_HEIGHT := 96.0
const CARD_HEIGHT := 74.0
const PROGRESS_HEIGHT := 8.0
const MESSAGE_DEFAULT_SECONDS := 2.4

var root: Control
var top_bar: Panel
var runner_card: PanelContainer
var objective_card: PanelContainer
var health_label: Label
var score_label: Label
var objective_label: Label
var state_label: Label
var message_label: Label
var message_panel: PanelContainer
var progress_fill: ColorRect
var progress_track: ColorRect
var progress_marker: ColorRect
var pause_button: Button
var message_queue: Array[Dictionary] = []
var message_time_left := 0.0
var message_priority := -1
var last_message := ""
var stage_length := 5710.0
var last_snapshot: Array = []
var last_world_x := 0.0
var last_progress_pixels := -1

func _ready() -> void:
	layer = 10
	process_mode = Node.PROCESS_MODE_ALWAYS
	build()

func build() -> void:
	root = Control.new()
	root.name = "HudRoot"
	root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	root.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(root)
	build_top_bar()
	build_left_status()
	build_objective_status()
	build_pause_button()
	build_progress_track()
	build_message_toast()
	root.resized.connect(layout_for_viewport)
	layout_for_viewport.call_deferred()

func build_top_bar() -> void:
	top_bar = Panel.new()
	top_bar.name = "TopBar"
	top_bar.position = Vector2.ZERO
	top_bar.size = Vector2(VIEW_SIZE.x, BAR_HEIGHT)
	top_bar.mouse_filter = Control.MOUSE_FILTER_IGNORE
	top_bar.add_theme_stylebox_override("panel", panel_style(
		Color(0.012, 0.038, 0.068, 0.93),
		Color(0.22, 0.56, 0.68, 0.72),
		0,
		0,
		2
	))
	root.add_child(top_bar)

func build_left_status() -> void:
	runner_card = PanelContainer.new()
	runner_card.name = "RunnerStatus"
	runner_card.position = Vector2(14, 10)
	runner_card.size = Vector2(430, CARD_HEIGHT)
	runner_card.mouse_filter = Control.MOUSE_FILTER_IGNORE
	runner_card.add_theme_stylebox_override("panel", panel_style(
		Color(0.018, 0.075, 0.115, 0.94),
		Color("#4ddbb8"),
		16,
		16,
		2
	))
	root.add_child(runner_card)
	var columns := HBoxContainer.new()
	columns.add_theme_constant_override("separation", 16)
	runner_card.add_child(columns)
	health_label = readable_label(ui_font(23), Color("#fff4d2"))
	health_label.custom_minimum_size = Vector2(134, 64)
	health_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	columns.add_child(health_label)
	var stats := VBoxContainer.new()
	stats.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	stats.add_theme_constant_override("separation", 0)
	columns.add_child(stats)
	score_label = readable_label(ui_font(20), Color.WHITE)
	state_label = readable_label(ui_font(17), Color("#9ee8f5"))
	score_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	state_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	stats.add_child(score_label)
	stats.add_child(state_label)

func build_objective_status() -> void:
	objective_card = PanelContainer.new()
	objective_card.name = "ObjectiveStatus"
	objective_card.position = Vector2(456, 10)
	objective_card.size = Vector2(582, CARD_HEIGHT)
	objective_card.mouse_filter = Control.MOUSE_FILTER_IGNORE
	objective_card.add_theme_stylebox_override("panel", panel_style(
		Color(0.035, 0.075, 0.115, 0.95),
		Color("#ffda79"),
		16,
		16,
		2
	))
	root.add_child(objective_card)
	objective_label = readable_label(ui_font(21), Color("#fff0a8"))
	objective_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	objective_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	objective_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	objective_card.add_child(objective_label)

func build_pause_button() -> void:
	pause_button = Button.new()
	pause_button.name = "PauseButton"
	pause_button.text = "PAUSE"
	pause_button.position = Vector2(1050, 10)
	pause_button.size = Vector2(216, CARD_HEIGHT)
	pause_button.process_mode = Node.PROCESS_MODE_ALWAYS
	pause_button.focus_mode = Control.FOCUS_ALL
	pause_button.add_theme_font_size_override("font_size", ui_font(22))
	pause_button.add_theme_color_override("font_color", Color.WHITE)
	pause_button.add_theme_color_override("font_hover_color", Color("#071326"))
	pause_button.add_theme_color_override("font_pressed_color", Color("#071326"))
	var normal := panel_style(
		Color(0.055, 0.13, 0.18, 0.96),
		Color("#84d5e8"),
		16,
		16,
		3
	)
	var hover := normal.duplicate()
	hover.bg_color = Color("#ffda79")
	hover.border_color = Color.WHITE
	pause_button.add_theme_stylebox_override("normal", normal)
	pause_button.add_theme_stylebox_override("hover", hover)
	pause_button.add_theme_stylebox_override("pressed", hover)
	pause_button.add_theme_stylebox_override("focus", hover)
	pause_button.pressed.connect(func(): pause_requested.emit())
	root.add_child(pause_button)

func build_progress_track() -> void:
	progress_track = ColorRect.new()
	progress_track.name = "StageProgressTrack"
	progress_track.position = Vector2(0, BAR_HEIGHT - PROGRESS_HEIGHT)
	progress_track.size = Vector2(VIEW_SIZE.x, PROGRESS_HEIGHT)
	progress_track.color = Color(0.015, 0.075, 0.105, 1.0)
	progress_track.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(progress_track)
	progress_fill = ColorRect.new()
	progress_fill.name = "StageProgressFill"
	progress_fill.position = progress_track.position
	progress_fill.size = Vector2.ZERO
	progress_fill.color = Color("#65f0c9")
	progress_fill.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(progress_fill)
	progress_marker = ColorRect.new()
	progress_marker.name = "StageProgressMarker"
	progress_marker.position = Vector2(-12, BAR_HEIGHT - 12)
	progress_marker.size = Vector2(13, 13)
	progress_marker.rotation = PI / 4.0
	progress_marker.pivot_offset = progress_marker.size * 0.5
	progress_marker.color = Color("#fff0a8")
	progress_marker.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(progress_marker)

func build_message_toast() -> void:
	message_panel = PanelContainer.new()
	message_panel.name = "MessageToast"
	message_panel.position = Vector2(340, 108)
	message_panel.size = Vector2(600, 58)
	message_panel.mouse_filter = Control.MOUSE_FILTER_IGNORE
	message_panel.visible = false
	message_panel.add_theme_stylebox_override("panel", panel_style(
		Color(0.015, 0.055, 0.09, 0.92),
		Color("#ffda79"),
		14,
		14,
		3
	))
	root.add_child(message_panel)
	message_label = readable_label(ui_font(20), Color("#fff4d2"))
	message_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	message_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	message_label.clip_text = true
	message_panel.add_child(message_label)

func panel_style(
	fill: Color,
	border: Color,
	top_radius: int,
	bottom_radius: int,
	border_width: int
) -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = fill
	style.border_color = border
	style.set_border_width_all(border_width)
	style.corner_radius_top_left = top_radius
	style.corner_radius_top_right = top_radius
	style.corner_radius_bottom_left = bottom_radius
	style.corner_radius_bottom_right = bottom_radius
	style.content_margin_left = 12
	style.content_margin_right = 12
	style.content_margin_top = 4
	style.content_margin_bottom = 4
	style.shadow_color = Color(0.0, 0.0, 0.0, 0.46)
	style.shadow_size = 6
	style.shadow_offset = Vector2(0, 4)
	style.anti_aliasing = true
	return style

func readable_label(font_size: int, color: Color) -> Label:
	var label := Label.new()
	label.add_theme_font_size_override("font_size", font_size)
	label.add_theme_color_override("font_color", color)
	label.add_theme_constant_override("outline_size", 3)
	label.add_theme_color_override("font_outline_color", Color(0.01, 0.03, 0.05, 0.92))
	return label

func ui_font(base_size: int) -> int:
	return base_size + 4 if GameSession.large_text else base_size

func update_snapshot(
	health: int,
	score: int,
	best_score: int,
	aurora: int,
	combo: int,
	player_state: String,
	route: String,
	has_key: bool,
	rescues: int,
	boss_defeated: bool,
	world_x: float
) -> void:
	var snapshot := [health, score, best_score, aurora, combo, player_state, route, has_key, rescues, boss_defeated]
	if snapshot != last_snapshot:
		last_snapshot = snapshot
		health_label.text = "HEALTH %d/3" % health
		health_label.add_theme_color_override(
			"font_color",
			Color("#fff4d2") if health >= 2 else Color("#ff9b86")
		)
		score_label.text = "SCORE %d  ·  BEST %d" % [score, best_score]
		var combo_text := "x%d" % combo if combo > 1 else "x0"
		var route_text := "MID" if route == "PRECISION" else route
		state_label.text = "AUR %d    COMBO %s    %s · %s" % [
			aurora,
			combo_text,
			player_state.to_upper(),
			route_text
		]
		objective_label.text = objective_text(has_key, rescues, boss_defeated)
	last_world_x = world_x
	update_progress()


func layout_for_viewport() -> void:
	var view := root.size
	if view.x < VIEW_SIZE.x:
		view = VIEW_SIZE
	top_bar.size = Vector2(view.x, BAR_HEIGHT)
	runner_card.position = Vector2(14, 10)
	runner_card.size = Vector2(430, CARD_HEIGHT)
	pause_button.position = Vector2(view.x - 230, 10)
	pause_button.size = Vector2(216, CARD_HEIGHT)
	objective_card.position = Vector2(456, 10)
	objective_card.size = Vector2(maxf(360.0, pause_button.position.x - 470.0), CARD_HEIGHT)
	progress_track.size = Vector2(view.x, PROGRESS_HEIGHT)
	message_panel.position = Vector2((view.x - 600.0) * 0.5, 108)
	last_progress_pixels = -1
	update_progress()


func update_progress() -> void:
	if not is_instance_valid(progress_track) or not is_instance_valid(progress_fill):
		return
	var pixels := roundi(progress_track.size.x * clampf(last_world_x / maxf(1.0, stage_length), 0.0, 1.0))
	if pixels == last_progress_pixels:
		return
	last_progress_pixels = pixels
	progress_fill.size = Vector2(pixels, PROGRESS_HEIGHT)
	progress_marker.position.x = clampf(
		float(pixels) - progress_marker.size.x * 0.5,
		-progress_marker.size.x,
		progress_track.size.x - progress_marker.size.x
	)

func objective_text(has_key: bool, rescues: int, boss_defeated: bool) -> String:
	var key_text := "KEY ✓" if has_key else "KEY —"
	var rescue_text := "RESCUES %d/2" % rescues
	var boss_text := "BOSS ✓" if boss_defeated else "BOSS —"
	return "%s    %s    %s" % [key_text, rescue_text, boss_text]

func post_message(text: String, priority := 1, seconds := MESSAGE_DEFAULT_SECONDS) -> void:
	var clean := text.strip_edges()
	if clean.is_empty(): return
	if clean == last_message and message_time_left > 0.25:
		message_time_left = maxf(message_time_left, seconds)
		return
	var entry := {
		"text": clean,
		"priority": priority,
		"seconds": maxf(0.8, seconds)
	}
	if message_time_left <= 0.0:
		show_message_entry(entry)
	elif priority > message_priority:
		message_queue.push_front({
			"text": message_label.text,
			"priority": message_priority,
			"seconds": minf(1.4, message_time_left)
		})
		show_message_entry(entry)
	else:
		enqueue_message(entry)

func enqueue_message(entry: Dictionary) -> void:
	for existing in message_queue:
		if String(existing.text) == String(entry.text): return
	message_queue.append(entry)
	if message_queue.size() > 4:
		message_queue.pop_front()

func show_message_entry(entry: Dictionary) -> void:
	last_message = String(entry.text)
	message_priority = int(entry.priority)
	message_time_left = float(entry.seconds)
	message_label.text = last_message
	message_panel.visible = true
	message_panel.modulate = Color.WHITE

func clear_messages() -> void:
	message_queue.clear()
	message_time_left = 0.0
	message_priority = -1
	last_message = ""
	message_panel.visible = false

func _process(delta: float) -> void:
	# The HUD remains interactive while paused, but gameplay feedback should not
	# expire while the player is reading a pause menu or writing a field note.
	if get_tree().paused: return
	if message_time_left <= 0.0: return
	message_time_left = maxf(0.0, message_time_left - delta)
	if message_time_left > 0.0:
		message_panel.modulate.a = clampf(message_time_left * 2.2, 0.35, 1.0)
		return
	if not message_queue.is_empty():
		show_message_entry(message_queue.pop_front())
	else:
		message_priority = -1
		message_panel.visible = false

func audit_layout() -> void:
	clear_messages()
	assert(health_label != null and score_label != null and objective_label != null)
	assert(pause_button != null and progress_fill != null and progress_marker != null and message_panel != null)
	assert(runner_card.get_rect().intersection(objective_card.get_rect()).get_area() == 0.0)
	assert(objective_card.get_rect().intersection(pause_button.get_rect()).get_area() == 0.0)
	assert(pause_button.text.contains("PAUSE"))
	assert(score_label.get_theme_font_size("font_size") >= 20)
	assert(state_label.get_theme_font_size("font_size") >= 17)
	assert(objective_label.get_theme_font_size("font_size") >= 21)
	post_message("LOW", 1, 2.0)
	post_message("URGENT", 3, 2.0)
	assert(message_label.text == "URGENT")
	assert(message_queue.size() == 1)
	clear_messages()
