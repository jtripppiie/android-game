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
var history_label: Label
var paused_before_open := false
var tag_buttons := {}
var quick_buttons: Array[Button] = []
var last_saved_note := ""

const NOTES_PATH := "user://debug-review-notes.txt"
const MAX_NOTE_LENGTH := 220
const QUICK_NOTES := [
	["TOO HARD", "This section feels too hard because "],
	["TOO SMALL", "This item is too small to read or judge: "],
	["BLOCKS VIEW", "This blocks the route or hides important action: "],
	["WRONG ART", "The art, scale, or facing looks wrong because "]
]

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	build_panel()

func build_panel() -> void:
	panel = PanelContainer.new()
	panel.set_anchors_preset(Control.PRESET_TOP_RIGHT)
	panel.position = Vector2(-492, 88)
	panel.size = Vector2(480, 292)
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
	rows.add_theme_constant_override("separation", 4)
	panel.add_child(rows)
	var title := Label.new()
	title.text = "REVIEW NOTE · PAUSED · SCENE REMAINS VISIBLE"
	title.add_theme_font_size_override("font_size", note_font(16))
	title.add_theme_color_override("font_color", Color("#84d5e8"))
	rows.add_child(title)
	target_label = Label.new()
	target_label.add_theme_font_size_override("font_size", note_font(16))
	target_label.add_theme_color_override("font_color", Color("#ffda79"))
	rows.add_child(target_label)
	var tags := HBoxContainer.new()
	rows.add_child(tags)
	for tag in ["FEEL", "JUMP", "SPACE", "ART", "BUG"]:
		var tag_button := Button.new()
		tag_button.text = tag
		tag_button.toggle_mode = true
		tag_button.button_pressed = tag == selected_tag
		tag_button.custom_minimum_size = Vector2(78, 30)
		tag_button.add_theme_font_size_override("font_size", note_font(15))
		tag_button.pressed.connect(select_tag.bind(tag))
		tag_buttons[tag] = tag_button
		tags.add_child(tag_button)
	var quick_row := HBoxContainer.new()
	quick_row.add_theme_constant_override("separation", 4)
	rows.add_child(quick_row)
	for spec in QUICK_NOTES:
		var quick := Button.new()
		quick.text = spec[0]
		quick.tooltip_text = spec[1]
		quick.custom_minimum_size = Vector2(108, 28)
		quick.add_theme_font_size_override("font_size", note_font(14))
		quick.pressed.connect(apply_quick_note.bind(String(spec[1])))
		quick_buttons.append(quick)
		quick_row.add_child(quick)
	input = LineEdit.new()
	input.placeholder_text = "Short note: what happened, and what should change?"
	input.max_length = MAX_NOTE_LENGTH
	input.custom_minimum_size.y = 34
	input.add_theme_font_size_override("font_size", note_font(16))
	input.text_submitted.connect(func(_submitted): save_note())
	rows.add_child(input)
	var actions := HBoxContainer.new()
	rows.add_child(actions)
	priority = CheckButton.new()
	priority.text = "FIX FIRST"
	priority.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	actions.add_child(priority)
	for spec in [["MIC", start_voice_note], ["UNDO", undo_last_note], ["COPY", copy_notes], ["CANCEL", close], ["SAVE", save_note]]:
		var button := Button.new()
		button.text = spec[0]
		button.custom_minimum_size = Vector2(68, 32)
		button.add_theme_font_size_override("font_size", note_font(15))
		if spec[0] == "MIC" and not AndroidBridge.voice_available():
			button.disabled = true
			button.tooltip_text = "Voice notes require the Android bridge"
		button.pressed.connect(spec[1])
		actions.add_child(button)
	status_label = Label.new()
	status_label.add_theme_font_size_override("font_size", note_font(15))
	status_label.add_theme_color_override("font_color", Color("#84d5e8"))
	rows.add_child(status_label)
	history_label = Label.new()
	history_label.clip_text = true
	history_label.add_theme_font_size_override("font_size", note_font(15))
	history_label.add_theme_color_override("font_color", Color(0.76, 0.86, 0.89, 0.88))
	rows.add_child(history_label)
	AndroidBridge.voice_note_received.connect(_on_voice_note)
	AndroidBridge.voice_note_failed.connect(_on_voice_error)

func toggle() -> void:
	if panel.visible: close()
	else: open()

func open() -> void:
	paused_before_open = get_tree().paused
	panel.visible = true
	target_label.text = "NEAREST: " + (String(nearest_id_provider.call()) if nearest_id_provider.is_valid() else "UNKNOWN")
	refresh_note_status()
	get_tree().paused = true
	input.grab_focus()

func close() -> void:
	panel.visible = false
	input.clear()
	priority.button_pressed = false
	get_tree().paused = paused_before_open

func save_note() -> void:
	var clean := input.text.strip_edges()
	if clean.is_empty():
		status_label.text = "TYPE A NOTE FIRST"
		input.grab_focus()
		return
	var context := String(context_provider.call()) if context_provider.is_valid() else "context unavailable"
	var mode := FileAccess.READ_WRITE if FileAccess.file_exists(NOTES_PATH) else FileAccess.WRITE_READ
	var file := FileAccess.open(NOTES_PATH, mode)
	if file == null:
		status_label.text = "SAVE FAILED · %s" % error_string(FileAccess.get_open_error())
		return
	file.seek_end()
	var entry := "[%s] [%s]%s %s\n%s\n---\n" % [
		Time.get_datetime_string_from_system(),
		selected_tag,
		" [FIX FIRST]" if priority.button_pressed else "",
		clean,
		context
	]
	file.store_string(entry)
	file.flush()
	last_saved_note = clean
	close()

func copy_notes() -> void:
	if not FileAccess.file_exists(NOTES_PATH):
		status_label.text = "No saved notes yet"
		return
	var file := FileAccess.open(NOTES_PATH, FileAccess.READ)
	if file == null:
		status_label.text = "COPY FAILED"
		return
	DisplayServer.clipboard_set(file.get_as_text())
	status_label.text = "All notes copied"

func select_tag(tag: String) -> void:
	selected_tag = tag
	for key in tag_buttons:
		(tag_buttons[key] as Button).button_pressed = key == tag
	input.placeholder_text = "%s note for %s" % [tag, target_label.text.trim_prefix("NEAREST: ")]
	input.grab_focus()

func note_count() -> int:
	if not FileAccess.file_exists(NOTES_PATH): return 0
	var file := FileAccess.open(NOTES_PATH, FileAccess.READ)
	if file == null: return 0
	var count := 0
	for line in file.get_as_text().split("\n"):
		if line.begins_with("["): count += 1
	return count

func note_font(base_size: int) -> int:
	return base_size + 2 if GameSession.large_text else base_size

func apply_quick_note(template: String) -> void:
	if input.text.strip_edges().is_empty():
		input.text = template
	elif not input.text.ends_with(" "):
		input.text += " "
	input.caret_column = input.text.length()
	input.grab_focus()

func refresh_note_status() -> void:
	var count := note_count()
	status_label.text = "%d NOTE%s SAVED LOCALLY" % [count, "" if count == 1 else "S"]
	history_label.text = recent_note_summary()

func recent_note_summary() -> String:
	if not FileAccess.file_exists(NOTES_PATH): return "RECENT: none yet"
	var file := FileAccess.open(NOTES_PATH, FileAccess.READ)
	if file == null: return "RECENT: unavailable"
	var headers: Array[String] = []
	for line in file.get_as_text().split("\n"):
		if line.begins_with("["): headers.append(line)
	if headers.is_empty(): return "RECENT: none yet"
	var recent := headers[headers.size() - 1]
	if recent.length() > 66: recent = recent.left(63) + "…"
	return "RECENT: " + recent

func undo_last_note() -> void:
	if not FileAccess.file_exists(NOTES_PATH):
		status_label.text = "NO NOTE TO UNDO"
		return
	var read_file := FileAccess.open(NOTES_PATH, FileAccess.READ)
	if read_file == null:
		status_label.text = "UNDO FAILED"
		return
	var text := read_file.get_as_text()
	var lines := text.split("\n")
	var last_header := -1
	for index in range(lines.size()):
		if lines[index].begins_with("["): last_header = index
	if last_header < 0:
		status_label.text = "NO NOTE TO UNDO"
		return
	var remaining := ""
	for index in range(last_header):
		remaining += lines[index] + "\n"
	var write_file := FileAccess.open(NOTES_PATH, FileAccess.WRITE)
	if write_file == null:
		status_label.text = "UNDO FAILED"
		return
	write_file.store_string(remaining)
	write_file.flush()
	status_label.text = "LAST NOTE REMOVED"
	history_label.text = recent_note_summary()

func start_voice_note() -> void:
	input.placeholder_text = "Listening…"
	AndroidBridge.start_voice_note()

func _on_voice_note(text: String) -> void:
	input.text = text.left(input.max_length)
	input.placeholder_text = "What feels wrong?"
	input.grab_focus()

func _on_voice_error(message: String) -> void:
	input.placeholder_text = message

func audit_layout() -> void:
	assert(panel.size.x <= 480.0 and panel.size.y <= 292.0)
	assert(tag_buttons.size() == 5)
	assert(quick_buttons.size() == QUICK_NOTES.size())
	assert(input.max_length == MAX_NOTE_LENGTH)
	assert(input.get_theme_font_size("font_size") >= 16)
	assert(status_label.get_theme_font_size("font_size") >= 15)
	assert(input.placeholder_text.length() > 0)
