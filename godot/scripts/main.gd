extends Node

var world: AlaskaStage
var ui := CanvasLayer.new()

func _ready() -> void:
	add_child(ui)
	var smoke_stage := -1
	for argument in OS.get_cmdline_user_args():
		if argument.begins_with("--stage-smoke="): smoke_stage = int(argument.get_slice("=", 1))
		elif argument.begins_with("--autoplay-audit="): smoke_stage = int(argument.get_slice("=", 1))
	if smoke_stage >= 0: start_stage(clampi(smoke_stage, 0, 4))
	else: show_menu()

func clear_ui() -> void:
	for child in ui.get_children(): child.queue_free()

func show_menu() -> void:
	if is_instance_valid(world): world.queue_free()
	clear_ui()
	add_backdrop("background_midnight_sun.png")
	var panel := VBoxContainer.new()
	panel.position = Vector2(390, 110)
	panel.size = Vector2(500, 500)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "YOU RUSH · ALASKA"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 42 if GameSession.large_text else 34)
	title.add_theme_color_override("font_color", Color("#fff4d2"))
	title.add_theme_constant_override("outline_size", 8)
	title.add_theme_color_override("font_outline_color", Color(0.02, 0.08, 0.14, 0.9))
	panel.add_child(title)
	add_button(panel, "PLAY ALASKA MAP", show_map)
	add_button(panel, "CUSTOMIZE RUNNER", show_customize)
	add_button(panel, "ACCESSIBILITY", show_accessibility)
	var status := Label.new()
	status.text = "ENGINE 5.0 · STAGES %d/5 · TOTAL %d" % [GameSession.unlocked_stage + 1, GameSession.total_score]
	status.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	panel.add_child(status)

func show_map() -> void:
	clear_ui()
	add_backdrop("background_dark_winter.png")
	var panel := VBoxContainer.new()
	panel.position = Vector2(250, 52)
	panel.size = Vector2(780, 620)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "ALASKA EXPEDITION MAP"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 30)
	panel.add_child(title)
	for index in range(GameSession.STAGES.size()):
		var stage = GameSession.STAGES[index]
		var unlocked := index <= GameSession.unlocked_stage
		var label := "%d · %s · BEST %d%s" % [index + 1, stage.name, GameSession.best_scores[index], "" if unlocked else " · LOCKED"]
		var button := add_button(panel, label, func(): start_stage(index))
		button.disabled = not unlocked
	add_button(panel, "BACK", show_menu)

func start_stage(index: int) -> void:
	GameSession.selected_stage = index
	clear_ui()
	world = AlaskaStage.new()
	world.stage_completed.connect(_on_stage_completed)
	world.exit_requested.connect(show_map)
	add_child(world)

func _on_stage_completed(stage: int, score: int) -> void:
	GameSession.complete_stage(stage, score)
	show_map()

func show_customize() -> void:
	clear_ui()
	add_backdrop("background_midnight_sun.png")
	var panel := VBoxContainer.new()
	panel.position = Vector2(340, 130)
	panel.size = Vector2(600, 450)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "CUSTOMIZE RUNNER"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 30)
	panel.add_child(title)
	var status := Label.new()
	status.text = "DEFAULT RUNNER" if GameSession.photo_path.is_empty() else "PHOTO READY · " + GameSession.photo_path.get_file()
	status.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	panel.add_child(status)
	add_button(panel, "CHOOSE LOCAL PHOTO", func(): open_photo_picker(status))
	add_button(panel, "RESET PHOTO", func(): GameSession.photo_path = ""; status.text = "DEFAULT RUNNER"; GameSession.save_profile())
	add_button(panel, "BACK", show_menu)

func show_accessibility() -> void:
	clear_ui()
	add_backdrop("background_dark_winter.png")
	var panel := VBoxContainer.new()
	panel.position = Vector2(390, 100)
	panel.size = Vector2(500, 520)
	ui.add_child(panel)
	var title := Label.new()
	title.text = "ACCESSIBILITY"
	title.add_theme_font_size_override("font_size", 30)
	panel.add_child(title)
	for spec in [["MUTE AUDIO", "muted"], ["HAPTICS", "haptics"], ["LARGE TEXT", "large_text"], ["REDUCED MOTION", "reduced_motion"], ["HIGH CONTRAST", "high_contrast"]]:
		var toggle := CheckButton.new()
		toggle.text = spec[0]
		toggle.button_pressed = GameSession.get(spec[1])
		toggle.toggled.connect(func(value): GameSession.set(spec[1], value); GameSession.save_profile())
		panel.add_child(toggle)
	add_button(panel, "BACK", show_menu)

func add_button(parent: Control, label: String, callback: Callable) -> Button:
	var button := Button.new()
	button.text = label
	button.custom_minimum_size = Vector2(0, 52)
	button.add_theme_font_size_override("font_size", 20 if GameSession.large_text else 17)
	button.add_theme_color_override("font_color", Color("#f7fcff"))
	button.add_theme_color_override("font_hover_color", Color("#fff0a8"))
	var normal := StyleBoxFlat.new()
	normal.bg_color = Color(0.025, 0.105, 0.16, 0.90)
	normal.border_color = Color("#4ddbb8")
	normal.set_border_width_all(2)
	normal.set_corner_radius_all(12)
	button.add_theme_stylebox_override("normal", normal)
	var hover := normal.duplicate()
	hover.bg_color = Color(0.08, 0.26, 0.32, 0.96)
	hover.border_color = Color("#ffda79")
	button.add_theme_stylebox_override("hover", hover)
	button.add_theme_stylebox_override("pressed", hover)
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
