class_name ReviewNotebook
extends Control

var context_provider: Callable
var panel: PanelContainer
var input: LineEdit
var priority: CheckButton

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	build_panel()

func build_panel() -> void:
	panel = PanelContainer.new()
	panel.position = Vector2(842, 12)
	panel.size = Vector2(426, 112)
	panel.visible = false
	panel.process_mode = Node.PROCESS_MODE_ALWAYS
	panel.mouse_filter = Control.MOUSE_FILTER_STOP
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.03, 0.07, 0.12, 0.97)
	style.border_color = Color("#4ddbb8")
	style.set_border_width_all(2)
	style.set_corner_radius_all(12)
	panel.add_theme_stylebox_override("panel", style)
	add_child(panel)
	var rows := VBoxContainer.new()
	panel.add_child(rows)
	var title := Label.new()
	title.text = "QUICK NOTE · frame + visible IDs attach"
	title.add_theme_font_size_override("font_size", 13)
	title.add_theme_color_override("font_color", Color("#84d5e8"))
	rows.add_child(title)
	input = LineEdit.new()
	input.placeholder_text = "What feels wrong?"
	input.max_length = 220
	rows.add_child(input)
	var actions := HBoxContainer.new()
	rows.add_child(actions)
	priority = CheckButton.new()
	priority.text = "FIX FIRST"
	priority.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	actions.add_child(priority)
	for spec in [["COPY", copy_notes], ["CANCEL", close], ["SAVE", save_note]]:
		var button := Button.new()
		button.text = spec[0]
		button.pressed.connect(spec[1])
		actions.add_child(button)

func toggle() -> void:
	if panel.visible: close()
	else: open()

func open() -> void:
	panel.visible = true
	get_tree().paused = true

func close() -> void:
	panel.visible = false
	input.clear()
	priority.button_pressed = false
	get_tree().paused = false

func save_note() -> void:
	var clean := input.text.strip_edges()
	if clean.is_empty(): return
	var context := String(context_provider.call()) if context_provider.is_valid() else "context unavailable"
	var mode := FileAccess.READ_WRITE if FileAccess.file_exists("user://debug-review-notes.txt") else FileAccess.WRITE_READ
	var file := FileAccess.open("user://debug-review-notes.txt", mode)
	if file == null: return
	file.seek_end()
	file.store_line("[%s]%s %s\n%s\n" % [Time.get_datetime_string_from_system(), " [FIX FIRST]" if priority.button_pressed else "", clean, context])
	close()

func copy_notes() -> void:
	if not FileAccess.file_exists("user://debug-review-notes.txt"): return
	var file := FileAccess.open("user://debug-review-notes.txt", FileAccess.READ)
	DisplayServer.clipboard_set(file.get_as_text())
