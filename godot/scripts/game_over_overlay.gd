class_name GameOverOverlay
extends PanelContainer

signal restart_requested
signal map_requested


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	set_anchors_preset(Control.PRESET_CENTER)
	offset_left = -230.0
	offset_top = -126.0
	offset_right = 230.0
	offset_bottom = 126.0
	mouse_filter = Control.MOUSE_FILTER_STOP
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.015, 0.05, 0.085, 0.98)
	style.border_color = Color("#ffda79")
	style.set_border_width_all(3)
	style.set_corner_radius_all(18)
	style.content_margin_left = 24
	style.content_margin_right = 24
	style.content_margin_top = 20
	style.content_margin_bottom = 20
	add_theme_stylebox_override("panel", style)
	var rows := VBoxContainer.new()
	rows.add_theme_constant_override("separation", 12)
	add_child(rows)
	var title := Label.new()
	title.text = "RUN ENDED"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 30 if not GameSession.large_text else 34)
	title.add_theme_color_override("font_color", Color("#fff0a8"))
	title.add_theme_constant_override("outline_size", 4)
	title.add_theme_color_override("font_outline_color", Color("#071326"))
	rows.add_child(title)
	var explanation := Label.new()
	explanation.text = "No progress was erased.\nRestart the stage cleanly or return to the map."
	explanation.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	explanation.add_theme_font_size_override("font_size", 19 if not GameSession.large_text else 22)
	explanation.add_theme_color_override("font_color", Color("#f7fcff"))
	rows.add_child(explanation)
	var actions := HBoxContainer.new()
	actions.alignment = BoxContainer.ALIGNMENT_CENTER
	actions.add_theme_constant_override("separation", 14)
	rows.add_child(actions)
	actions.add_child(action_button("RESTART", func() -> void: restart_requested.emit()))
	actions.add_child(action_button("MAP", func() -> void: map_requested.emit()))


func action_button(text: String, callback: Callable) -> Button:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(178, 64)
	button.add_theme_font_size_override("font_size", 22)
	button.pressed.connect(callback)
	return button
