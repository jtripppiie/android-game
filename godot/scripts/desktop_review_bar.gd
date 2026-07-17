class_name DesktopReviewBar
extends Control

signal review_toggled(enabled: bool)
signal ids_requested
signal note_requested

var registry: ReviewRegistry
var player: AlaskaRunner
var active := false
var panel: PanelContainer
var context_label: Label
var ids_button: Button
var note_button: Button
var review_button: Button


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	build()


func configure(owner_registry: ReviewRegistry, runner: AlaskaRunner, enabled: bool) -> void:
	registry = owner_registry
	player = runner
	set_active(enabled)


func build() -> void:
	panel = PanelContainer.new()
	panel.name = "DesktopReviewBar"
	panel.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	panel.offset_left = 250
	panel.offset_top = -58
	panel.offset_right = -250
	panel.offset_bottom = -12
	panel.mouse_filter = Control.MOUSE_FILTER_STOP
	panel.add_theme_stylebox_override("panel", panel_style())
	add_child(panel)
	var row := HBoxContainer.new()
	row.add_theme_constant_override("separation", 8)
	panel.add_child(row)
	context_label = Label.new()
	context_label.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	context_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	context_label.add_theme_font_size_override("font_size", 16)
	context_label.add_theme_color_override("font_color", Color("#eafcff"))
	context_label.add_theme_constant_override("outline_size", 3)
	context_label.add_theme_color_override("font_outline_color", Color("#071326"))
	row.add_child(context_label)
	ids_button = small_button("F10 · IDS")
	ids_button.pressed.connect(func(): ids_requested.emit())
	row.add_child(ids_button)
	note_button = small_button("N · NOTE")
	note_button.pressed.connect(func(): note_requested.emit())
	row.add_child(note_button)
	review_button = small_button("F1 · REVIEW")
	review_button.pressed.connect(func(): review_toggled.emit(not active))
	row.add_child(review_button)


func set_active(enabled: bool) -> void:
	active = enabled
	if not is_instance_valid(panel):
		return
	ids_button.visible = active
	note_button.visible = active
	review_button.text = "F1 · EXIT" if active else "F1 · REVIEW"
	update_context()


func _process(_delta: float) -> void:
	update_context()


func update_context() -> void:
	if not is_instance_valid(context_label):
		return
	if not active:
		context_label.text = "COMPUTER RUN · A/D MOVE · SPACE JUMP · E DASH · F SNOW"
		return
	var nearest := (
		registry.nearest(player)
		if registry != null and is_instance_valid(player)
		else "NO TARGET"
	)
	context_label.text = "REVIEW MODE · %s" % nearest


func small_button(text: String) -> Button:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(104, 34)
	button.add_theme_font_size_override("font_size", 15)
	button.add_theme_color_override("font_color", Color.WHITE)
	button.add_theme_color_override("font_pressed_color", Color("#071326"))
	var normal := StyleBoxFlat.new()
	normal.bg_color = Color(0.04, 0.13, 0.18, 0.96)
	normal.border_color = Color("#84d5e8")
	normal.set_border_width_all(2)
	normal.set_corner_radius_all(9)
	button.add_theme_stylebox_override("normal", normal)
	var hover := normal.duplicate()
	hover.bg_color = Color("#ffda79")
	hover.border_color = Color.WHITE
	button.add_theme_stylebox_override("hover", hover)
	button.add_theme_stylebox_override("pressed", hover)
	button.add_theme_stylebox_override("focus", hover)
	return button


func panel_style() -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.01, 0.04, 0.075, 0.94)
	style.border_color = Color("#4ddbb8")
	style.set_border_width_all(2)
	style.set_corner_radius_all(12)
	style.content_margin_left = 12
	style.content_margin_right = 8
	style.content_margin_top = 5
	style.content_margin_bottom = 5
	style.shadow_color = Color(0, 0, 0, 0.52)
	style.shadow_size = 6
	style.shadow_offset = Vector2(0, 4)
	return style


func audit_layout() -> void:
	assert(panel.offset_bottom <= -12.0)
	assert(panel.offset_top >= -58.0)
	assert(context_label.get_theme_font_size("font_size") >= 16)
	assert(ids_button.custom_minimum_size.y >= 34.0)
	assert(note_button.custom_minimum_size.y >= 34.0)
