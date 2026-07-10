class_name TouchControls
extends Control

var active_touches := {}
var controls := {
	"move_left": Rect2(24, 572, 72, 72),
	"move_right": Rect2(104, 572, 72, 72),
	"crouch": Rect2(64, 652, 72, 52),
	"jump": Rect2(1028, 602, 76, 76),
	"fire": Rect2(1120, 572, 76, 76),
	"sprint": Rect2(934, 626, 70, 54),
	"dash": Rect2(860, 642, 62, 48)
}

func _ready() -> void:
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	queue_redraw()

func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed: active_touches[event.index] = action_at(event.position)
		else: active_touches.erase(event.index)
		sync_actions()
	if event is InputEventScreenDrag:
		active_touches[event.index] = action_at(event.position)
		sync_actions()

func action_at(point: Vector2) -> String:
	for action in controls:
		if controls[action].has_point(point): return action
	return ""

func sync_actions() -> void:
	for action in controls: Input.action_release(action)
	for action in active_touches.values():
		if action != "": Input.action_press(action)
	queue_redraw()

func _draw() -> void:
	for action in controls:
		var box: Rect2 = controls[action]
		var active: bool = action in active_touches.values()
		var fill := Color(1.0, 0.84, 0.42, 0.46) if active else Color(0.04, 0.08, 0.13, 0.24)
		draw_style_box(make_box(fill), box)
		draw_string(ThemeDB.fallback_font, box.position + Vector2(box.size.x * 0.5, box.size.y * 0.58), label_for(action), HORIZONTAL_ALIGNMENT_CENTER, box.size.x, 14, Color(1,1,1,0.72))

func make_box(color: Color) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.border_color = Color(1,1,1,0.28)
	box.set_border_width_all(2)
	box.set_corner_radius_all(18)
	return box

func label_for(action: String) -> String:
	if action == "move_left": return "◀"
	if action == "move_right": return "▶"
	if action == "crouch": return "CROUCH"
	if action == "jump": return "JUMP"
	if action == "fire": return "FIRE"
	if action == "dash": return "DASH"
	return "RUN"
