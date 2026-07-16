class_name ReviewNotebook
extends Control

var context_provider: Callable
var nearest_id_provider: Callable
var panel: PanelContainer
var input: LineEdit
var priority: CheckButton
var selected_tag := "FEEL"
var target_label: Label
var status_label: Label

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	build_panel()

func build_panel() -> void:
	panel = PanelContainer.new()
	panel.set_anchors_preset(Control.PRESET_TOP_RIGHT)
	panel.position = Vector2(-466, 70)
	panel.size = Vector2(454, 166)
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
	title.text = "REVIEW NOTE · game paused · scene stays visible"
	title.add_theme_font_size_override("font_size", 13)
	title.add_theme_color_override("font_color", Color("#84d5e8"))
	rows.add_child(title)
	target_label = Label.new()
	target_label.add_theme_font_size_override("font_size", 12)
	target_label.add_theme_color_override("font_color", Color("#ffda79"))
	rows.add_child(target_label)
	var tags := HBoxContainer.new()
	rows.add_child(tags)
	for tag in ["FEEL", "JUMP", "SPACE", "ART", "BUG"]:
		var tag_button := Button.new()
		tag_button.text = tag
		tag_button.custom_minimum_size = Vector2(64, 26)
		tag_button.pressed.connect(select_tag.bind(tag))
		tags.add_child(tag_button)
	input = LineEdit.new()
	input.placeholder_text = "Short note: what happened, and what should change?"
	input.max_length = 220
	rows.add_child(input)
	var actions := HBoxContainer.new()
	rows.add_child(actions)
	priority = CheckButton.new()
	priority.text = "FIX FIRST"
	priority.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	actions.add_child(priority)
	for spec in [["MIC", start_voice_note], ["COPY ALL", copy_notes], ["CANCEL", close], ["SAVE", save_note]]:
		var button := Button.new()
		button.text = spec[0]
		button.pressed.connect(spec[1])
		actions.add_child(button)
	status_label = Label.new()
	status_label.add_theme_font_size_override("font_size", 11)
	status_label.add_theme_color_override("font_color", Color("#84d5e8"))
	rows.add_child(status_label)
	AndroidBridge.voice_note_received.connect(_on_voice_note)
	AndroidBridge.voice_note_failed.connect(_on_voice_error)

func toggle() -> void:
	if panel.visible: close()
	else: open()

func open() -> void:
	panel.visible = true
	target_label.text = "NEAREST: " + (String(nearest_id_provider.call()) if nearest_id_provider.is_valid() else "UNKNOWN")
	status_label.text = "%d notes saved locally" % note_count()
	get_tree().paused = true
	input.grab_focus()

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
	file.store_line("[%s] [%s]%s %s\n%s\n" % [Time.get_datetime_string_from_system(), selected_tag, " [FIX FIRST]" if priority.button_pressed else "", clean, context])
	close()

func copy_notes() -> void:
	if not FileAccess.file_exists("user://debug-review-notes.txt"):
		status_label.text = "No saved notes yet"
		return
	var file := FileAccess.open("user://debug-review-notes.txt", FileAccess.READ)
	DisplayServer.clipboard_set(file.get_as_text())
	status_label.text = "All notes copied"

func select_tag(tag: String) -> void:
	selected_tag = tag
	input.placeholder_text = "%s note for %s" % [tag, target_label.text.trim_prefix("NEAREST: ")]
	input.grab_focus()

func note_count() -> int:
	if not FileAccess.file_exists("user://debug-review-notes.txt"): return 0
	var file := FileAccess.open("user://debug-review-notes.txt", FileAccess.READ)
	var count := 0
	for line in file.get_as_text().split("\n"):
		if line.begins_with("["): count += 1
	return count

func start_voice_note() -> void:
	input.placeholder_text = "Listening…"
	AndroidBridge.start_voice_note()

func _on_voice_note(text: String) -> void:
	input.text = text.left(input.max_length)
	input.placeholder_text = "What feels wrong?"
	input.grab_focus()

func _on_voice_error(message: String) -> void:
	input.placeholder_text = message
