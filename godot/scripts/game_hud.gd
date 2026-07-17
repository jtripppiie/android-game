class_name AlaskaGameHud
extends CanvasLayer

signal pause_requested

const VIEW_SIZE := Vector2(1280, 720)
const BAR_HEIGHT := 82.0
const MESSAGE_DEFAULT_SECONDS := 2.4

var root: Control
var health_label: Label
var score_label: Label
var objective_label: Label
var state_label: Label
var message_label: Label
var message_panel: PanelContainer
var progress_fill: ColorRect
var pause_button: Button
var message_queue: Array[Dictionary] = []
var message_time_left := 0.0
var message_priority := -1
var last_message := ""
var stage_length := 5710.0

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

func build_top_bar() -> void:
	var bar := Panel.new()
	bar.name = "TopBar"
	bar.position = Vector2.ZERO
	bar.size = Vector2(VIEW_SIZE.x, BAR_HEIGHT)
	bar.mouse_filter = Control.MOUSE_FILTER_IGNORE
	bar.add_theme_stylebox_override("panel", panel_style(
		Color(0.012, 0.038, 0.068, 0.93),
		Color(0.22, 0.56, 0.68, 0.72),
		0,
		0,
		2
	))
	root.add_child(bar)

func build_left_status() -> void:
	var card := PanelContainer.new()
	card.name = "RunnerStatus"
	card.position = Vector2(14, 10)
	card.size = Vector2(420, 62)
	card.mouse_filter = Control.MOUSE_FILTER_IGNORE
	card.add_theme_stylebox_override("panel", panel_style(
		Color(0.02, 0.08, 0.12, 0.82),
		Color(0.31, 0.76, 0.75, 0.48),
		12,
		12,
		1
	))
	root.add_child(card)
	var columns := HBoxContainer.new()
	columns.add_theme_constant_override("separation", 18)
	card.add_child(columns)
	health_label = readable_label(ui_font(20), Color("#fff4d2"))
	health_label.custom_minimum_size = Vector2(142, 54)
	health_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	columns.add_child(health_label)
	var stats := VBoxContainer.new()
	stats.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	stats.add_theme_constant_override("separation", 0)
	columns.add_child(stats)
	score_label = readable_label(ui_font(18), Color.WHITE)
	state_label = readable_label(ui_font(16), Color("#84d5e8"))
	score_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	state_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	stats.add_child(score_label)
	stats.add_child(state_label)

func build_objective_status() -> void:
	var card := PanelContainer.new()
	card.name = "ObjectiveStatus"
	card.position = Vector2(446, 10)
	card.size = Vector2(608, 62)
	card.mouse_filter = Control.MOUSE_FILTER_IGNORE
	card.add_theme_stylebox_override("panel", panel_style(
		Color(0.03, 0.075, 0.11, 0.86),
		Color(0.95, 0.76, 0.32, 0.58),
		12,
		12,
		1
	))
	root.add_child(card)
	objective_label = readable_label(ui_font(18), Color("#fff0a8"))
	objective_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	objective_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	objective_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	card.add_child(objective_label)

func build_pause_button() -> void:
	pause_button = Button.new()
	pause_button.name = "PauseButton"
	pause_button.text = "PAUSE"
	pause_button.position = Vector2(1066, 10)
	pause_button.size = Vector2(200, 62)
	pause_button.process_mode = Node.PROCESS_MODE_ALWAYS
	pause_button.focus_mode = Control.FOCUS_ALL
	pause_button.add_theme_font_size_override("font_size", ui_font(20))
	pause_button.add_theme_color_override("font_color", Color.WHITE)
	pause_button.add_theme_color_override("font_hover_color", Color("#071326"))
	pause_button.add_theme_color_override("font_pressed_color", Color("#071326"))
	var normal := panel_style(
		Color(0.055, 0.13, 0.18, 0.96),
		Color("#84d5e8"),
		13,
		13,
		2
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
	var track := ColorRect.new()
	track.name = "StageProgressTrack"
	track.position = Vector2(0, BAR_HEIGHT - 4)
	track.size = Vector2(VIEW_SIZE.x, 4)
	track.color = Color(0.02, 0.10, 0.14, 0.96)
	track.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(track)
	progress_fill = ColorRect.new()
	progress_fill.name = "StageProgressFill"
	progress_fill.position = track.position
	progress_fill.size = Vector2.ZERO
	progress_fill.color = Color("#4ddbb8")
	progress_fill.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(progress_fill)

func build_message_toast() -> void:
	message_panel = PanelContainer.new()
	message_panel.name = "MessageToast"
	message_panel.position = Vector2(380, 92)
	message_panel.size = Vector2(520, 48)
	message_panel.mouse_filter = Control.MOUSE_FILTER_IGNORE
	message_panel.visible = false
	message_panel.add_theme_stylebox_override("panel", panel_style(
		Color(0.015, 0.055, 0.09, 0.92),
		Color("#ffda79"),
		12,
		12,
		2
	))
	root.add_child(message_panel)
	message_label = readable_label(ui_font(18), Color("#fff4d2"))
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
	return style

func readable_label(font_size: int, color: Color) -> Label:
	var label := Label.new()
	label.add_theme_font_size_override("font_size", font_size)
	label.add_theme_color_override("font_color", color)
	label.add_theme_constant_override("outline_size", 3)
	label.add_theme_color_override("font_outline_color", Color(0.01, 0.03, 0.05, 0.92))
	return label

func ui_font(base_size: int) -> int:
	return base_size + 3 if GameSession.large_text else base_size

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
	health_label.text = "HP %d/3" % health
	score_label.text = "SCORE %d    BEST %d" % [score, best_score]
	var combo_text := "x%d" % combo if combo > 1 else "x0"
	var route_text := "MID" if route == "PRECISION" else route
	state_label.text = "AUR %d    COMBO %s    %s · %s" % [
		aurora,
		combo_text,
		player_state.to_upper(),
		route_text
	]
	objective_label.text = objective_text(has_key, rescues, boss_defeated)
	var progress := clampf(world_x / maxf(1.0, stage_length), 0.0, 1.0)
	progress_fill.size = Vector2(VIEW_SIZE.x * progress, 4)

func objective_text(has_key: bool, rescues: int, boss_defeated: bool) -> String:
	var key_text := "KEY FOUND" if has_key else "FIND KEY"
	var rescue_text := "RESCUE %d/2" % rescues
	var boss_text := "BOSS DONE" if boss_defeated else "BOSS NEXT"
	return "%s   ·   %s   ·   %s" % [key_text, rescue_text, boss_text]

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
	assert(pause_button != null and progress_fill != null and message_panel != null)
	assert(Rect2(14, 10, 420, 62).intersection(Rect2(446, 10, 608, 62)).get_area() == 0.0)
	assert(Rect2(446, 10, 608, 62).intersection(Rect2(1066, 10, 200, 62)).get_area() == 0.0)
	assert(pause_button.text.contains("PAUSE"))
	assert(score_label.get_theme_font_size("font_size") >= 18)
	assert(state_label.get_theme_font_size("font_size") >= 16)
	assert(objective_label.get_theme_font_size("font_size") >= 18)
	post_message("LOW", 1, 2.0)
	post_message("URGENT", 3, 2.0)
	assert(message_label.text == "URGENT")
	assert(message_queue.size() == 1)
	clear_messages()
