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
		"move_left": Rect2(Vector2(SAFE_MARGIN.x, bottom - 128), Vector2(124, 108)),
		"move_right": Rect2(Vector2(SAFE_MARGIN.x + 136, bottom - 128), Vector2(124, 108)),
		"crouch": Rect2(Vector2(SAFE_MARGIN.x + 78, bottom - 226), Vector2(104, 76)),
		"jump": Rect2(Vector2(view.x - SAFE_MARGIN.x - 140, bottom - 144), Vector2(140, 124)),
		"fire": Rect2(Vector2(view.x - SAFE_MARGIN.x - 258, bottom - 246), Vector2(112, 94)),
		"dash": Rect2(Vector2(view.x - SAFE_MARGIN.x - 266, bottom - 126), MIN_SIZE)
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
	for action in controls: Input.action_release(action)
	Input.action_release("sprint")
	var moving := false
	for action in active_touches.values():
		if action != "":
			Input.action_press(action)
			if action in ["move_left", "move_right"]: moving = true
	if moving: Input.action_press("sprint")
	queue_redraw()

func _exit_tree() -> void:
	for action in controls: Input.action_release(action)
	Input.action_release("sprint")

func _draw() -> void:
	if controls.is_empty(): return
	var left_group: Rect2 = controls["move_left"]
	var right_group: Rect2 = controls["move_right"]
	draw_style_box(make_box(Color(0.01, 0.04, 0.08, 0.64), Color(0.55, 0.92, 1.0, 0.55), 24), left_group.merge(right_group).grow(10))
	for action in controls:
		var box: Rect2 = controls[action]
		var active: bool = action in active_touches.values()
		var fill := Color(1.0, 0.78, 0.20, 0.96) if active else Color(0.02, 0.10, 0.17, 0.88)
		var border := Color("#fff0a8") if active else Color("#84d5e8")
		draw_style_box(make_box(fill, border, 22), box)
		var font_size := 34 if action in ["move_left", "move_right"] else 22
		var text_color := Color("#071326") if active else Color.WHITE
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
	if action == "move_left": return "◀"
	if action == "move_right": return "▶"
	if action == "crouch": return "CROUCH"
	if action == "jump": return "JUMP"
	if action == "fire": return "FIRE"
	if action == "dash": return "DASH"
	if action == "debug_note": return "NOTE"
	if action == "debug_ids": return "IDS"
	return "RUN"
