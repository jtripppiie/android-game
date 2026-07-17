class_name StageCompleteOverlay
extends PanelContainer

signal next_requested
signal replay_requested
signal map_requested

var title_label: Label
var score_label: Label
var objective_label: Label
var rating_label: Label
var next_button: Button


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	set_anchors_preset(Control.PRESET_CENTER)
	offset_left = -280
	offset_top = -202
	offset_right = 280
	offset_bottom = 202
	mouse_filter = Control.MOUSE_FILTER_STOP
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.012, 0.045, 0.075, 0.985)
	style.border_color = Color("#4ddbb8")
	style.set_border_width_all(3)
	style.set_corner_radius_all(20)
	style.content_margin_left = 28
	style.content_margin_right = 28
	style.content_margin_top = 22
	style.content_margin_bottom = 22
	add_theme_stylebox_override("panel", style)
	var rows := VBoxContainer.new()
	rows.add_theme_constant_override("separation", 12)
	add_child(rows)
	title_label = Label.new()
	title_label.text = "EXPEDITION COMPLETE"
	title_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title_label.add_theme_font_size_override("font_size", 34 if not GameSession.large_text else 38)
	title_label.add_theme_color_override("font_color", Color("#fff0a8"))
	title_label.add_theme_constant_override("outline_size", 4)
	title_label.add_theme_color_override("font_outline_color", Color("#071326"))
	rows.add_child(title_label)
	score_label = Label.new()
	score_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	score_label.add_theme_font_size_override("font_size", 24 if not GameSession.large_text else 27)
	score_label.add_theme_color_override("font_color", Color.WHITE)
	rows.add_child(score_label)
	rating_label = Label.new()
	rating_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	rating_label.add_theme_font_size_override("font_size", 21 if not GameSession.large_text else 24)
	rating_label.add_theme_color_override("font_color", Color("#fff0a8"))
	rows.add_child(rating_label)
	objective_label = Label.new()
	objective_label.text = "KEY FOUND  ·  RESCUES 2/2  ·  BOSS DEFEATED"
	objective_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	objective_label.add_theme_font_size_override("font_size", 18 if not GameSession.large_text else 21)
	objective_label.add_theme_color_override("font_color", Color("#84d5e8"))
	rows.add_child(objective_label)
	var hint := Label.new()
	hint.text = "Progress has been saved."
	hint.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	hint.add_theme_font_size_override("font_size", 17)
	hint.add_theme_color_override("font_color", Color(0.80, 0.89, 0.92))
	rows.add_child(hint)
	var actions := HBoxContainer.new()
	actions.alignment = BoxContainer.ALIGNMENT_CENTER
	actions.add_theme_constant_override("separation", 10)
	rows.add_child(actions)
	next_button = action_button("NEXT STAGE", func() -> void: next_requested.emit())
	actions.add_child(next_button)
	actions.add_child(action_button("REPLAY", func() -> void: replay_requested.emit()))
	actions.add_child(action_button("MAP", func() -> void: map_requested.emit()))


func configure(
	stage: int,
	score: int,
	previous_best: int,
	elapsed_seconds: float,
	damage_taken: int
) -> void:
	var new_best := score > previous_best
	title_label.text = "%s COMPLETE" % GameSession.STAGES[stage].name
	score_label.text = "SCORE %d  ·  BEST %d%s" % [
		score,
		maxi(score, previous_best),
		"  ·  NEW BEST" if new_best else ""
	]
	var stars := GameSession.star_rating(stage, score, elapsed_seconds, damage_taken)
	var best_time: float = GameSession.best_times[stage]
	rating_label.text = "%s  %d/3  ·  TIME %.1fs  ·  HITS %d%s" % [
		"★".repeat(stars) + "☆".repeat(3 - stars),
		stars,
		elapsed_seconds,
		damage_taken,
		"  ·  BEST %.1fs" % best_time if best_time > 0.0 else ""
	]
	if stage >= GameSession.STAGES.size() - 1:
		next_button.text = "CAMPAIGN MAP"


func action_button(text: String, callback: Callable) -> Button:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(154, 66)
	button.add_theme_font_size_override("font_size", 20)
	button.pressed.connect(callback)
	return button
