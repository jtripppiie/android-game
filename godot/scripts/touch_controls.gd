class_name TouchControls
extends Control

const MIN_SIZE := Vector2(104, 84)
const SAFE_MARGIN := Vector2(34, 30)

var active_touches := {}
var review_mode := false
var controls := {}

func _ready() -> void:
	review_mode = GameSession.review_mode
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	resized.connect(layout_controls)
	call_deferred("layout_controls")

func layout_controls() -> void:
	var view := size
	if view.x < 600.0 or view.y < 360.0: view = get_viewport_rect().size
	var bottom := view.y - SAFE_MARGIN.y
	controls = {
		"move_left": Rect2(Vector2(SAFE_MARGIN.x, bottom - 116), Vector2(92, 86)),
		"move_right": Rect2(Vector2(SAFE_MARGIN.x + 184, bottom - 116), Vector2(92, 86)),
		"dpad_up": Rect2(Vector2(SAFE_MARGIN.x + 92, bottom - 202), Vector2(92, 86)),
		"dpad_down": Rect2(Vector2(SAFE_MARGIN.x + 92, bottom - 100), Vector2(92, 70)),
		"jump": Rect2(Vector2(view.x - SAFE_MARGIN.x - 124, bottom - 134), Vector2(124, 124)),
		"fire": Rect2(Vector2(view.x - SAFE_MARGIN.x - 226, bottom - 234), Vector2(94, 94)),
		"dash": Rect2(Vector2(view.x - SAFE_MARGIN.x - 238, bottom - 106), Vector2(98, 76))
	}
	if review_mode:
		controls["debug_note"] = Rect2(Vector2(view.x - SAFE_MARGIN.x - 104, SAFE_MARGIN.y), Vector2(104, 52))
		controls["debug_ids"] = Rect2(Vector2(view.x - SAFE_MARGIN.x - 218, SAFE_MARGIN.y), Vector2(104, 52))
	queue_redraw()

func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed: active_touches[event.index] = action_at(event.position)
		else: active_touches.erase(event.index)
		sync_actions()
	elif event is InputEventScreenDrag:
		active_touches[event.index] = action_at(event.position)
		sync_actions()
	elif event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
		if event.pressed: active_touches[-1] = action_at(event.position)
		else: active_touches.erase(-1)
		sync_actions()
	elif event is InputEventMouseMotion and -1 in active_touches and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		active_touches[-1] = action_at(event.position)
		sync_actions()

func action_at(point: Vector2) -> String:
	for action in controls:
		if (controls[action] as Rect2).has_point(point): return action
	return ""

func sync_actions() -> void:
	for action in controls: Input.action_release(input_action_for(action))
	Input.action_release("sprint")
	var moving := false
	for action in active_touches.values():
		if action != "":
			Input.action_press(input_action_for(action))
			if action in ["move_left", "move_right"]: moving = true
	if moving: Input.action_press("sprint")
	queue_redraw()

func _exit_tree() -> void:
	for action in controls: Input.action_release(input_action_for(action))
	Input.action_release("sprint")

func _draw() -> void:
	if controls.is_empty(): return
	var dpad_bounds: Rect2 = controls["move_left"]
	for direction in ["move_right", "dpad_up", "dpad_down"]:
		dpad_bounds = dpad_bounds.merge(controls[direction])
	draw_style_box(make_box(Color(0.01, 0.04, 0.08, 0.82), Color("#84d5e8"), 32), dpad_bounds.grow(12))
	draw_circle(dpad_bounds.get_center(), 34.0, Color(0.12, 0.30, 0.38, 0.96))
	for action in controls:
		var box: Rect2 = controls[action]
		var active: bool = action in active_touches.values()
		var fill := control_color(action, active)
		var border := Color.WHITE if active else control_border(action)
		var radius := 62 if action == "jump" else 47 if action == "fire" else 22
		draw_style_box(make_box(fill, border, radius), box)
		var font_size := 17 if action in ["move_left", "move_right", "dpad_up", "dpad_down"] else 19
		if action == "jump": font_size = 22
		var text_color := control_text_color(action, active)
		var baseline := box.position + Vector2(0, box.size.y * 0.62 + font_size * 0.25)
		draw_string(ThemeDB.fallback_font, baseline, label_for(action), HORIZONTAL_ALIGNMENT_CENTER, box.size.x, font_size, text_color)

func make_box(color: Color, border: Color, radius: int) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.border_color = border
	box.set_border_width_all(3)
	box.set_corner_radius_all(radius)
	return box

func label_for(action: String) -> String:
	if action == "move_left": return "LEFT"
	if action == "move_right": return "RIGHT"
	if action == "dpad_up": return "UP"
	if action == "dpad_down": return "DOWN"
	if action == "jump": return "JUMP"
	if action == "fire": return "SNOW"
	if action == "dash": return "DASH"
	if action == "debug_note": return "NOTE"
	if action == "debug_ids": return "IDS"
	return "RUN"

func input_action_for(control: String) -> String:
	if control == "dpad_up": return "jump"
	if control == "dpad_down": return "crouch"
	return control

func control_color(action: String, active: bool) -> Color:
	if action == "jump": return Color("#ffe27a") if active else Color(0.78, 0.43, 0.06, 0.88)
	if action == "fire": return Color("#baf6ff") if active else Color(0.02, 0.38, 0.52, 0.88)
	if action == "dash": return Color("#e1c4ff") if active else Color(0.34, 0.17, 0.52, 0.88)
	if action in ["debug_note", "debug_ids"]: return Color(0.03, 0.12, 0.18, 0.94)
	return Color(0.02, 0.10, 0.17, 0.86)

func control_border(action: String) -> Color:
	if action == "jump": return Color("#fff0a8")
	if action == "fire": return Color("#84eaff")
	if action == "dash": return Color("#d7adff")
	return Color("#84d5e8")

func control_text_color(action: String, active: bool) -> Color:
	if active or action == "jump": return Color("#071326")
	return Color.WHITE
