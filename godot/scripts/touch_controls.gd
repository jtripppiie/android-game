class_name TouchControls
extends Control

const SAFE_MARGIN := Vector2(34, 30)
const DPAD_SIZE := 180.0
const DPAD_DRIFT_MARGIN := 24.0
const DPAD_DEAD_ZONE := 0.16

var active_touches := {}
var review_mode := false
var controls := {}
var dpad_bounds := Rect2()
var dpad_vector := Vector2.ZERO
var touch_pressed_actions := {}

func _ready() -> void:
	add_to_group("touch_controls")
	review_mode = GameSession.review_mode
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	resized.connect(layout_controls)
	call_deferred("layout_controls")

func layout_controls() -> void:
	var view := size
	if view.x < 600.0 or view.y < 360.0: view = get_viewport_rect().size
	var bottom := view.y - SAFE_MARGIN.y
	dpad_bounds = Rect2(Vector2(SAFE_MARGIN.x, bottom - DPAD_SIZE), Vector2(DPAD_SIZE, DPAD_SIZE))
	var center := dpad_bounds.get_center()
	var arrow_offset := 52.0
	var arrow_size := Vector2(52, 52)
	controls = {
		"move_left": Rect2(center + Vector2(-arrow_offset, 0) - arrow_size * 0.5, arrow_size),
		"move_right": Rect2(center + Vector2(arrow_offset, 0) - arrow_size * 0.5, arrow_size),
		"dpad_up": Rect2(center + Vector2(0, -arrow_offset) - arrow_size * 0.5, arrow_size),
		"dpad_down": Rect2(center + Vector2(0, arrow_offset) - arrow_size * 0.5, arrow_size),
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
		if event.pressed: active_touches[event.index] = event.position
		else: active_touches.erase(event.index)
		sync_actions()
	elif event is InputEventScreenDrag:
		active_touches[event.index] = event.position
		sync_actions()
	elif event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
		if event.pressed: active_touches[-1] = event.position
		else: active_touches.erase(-1)
		sync_actions()
	elif event is InputEventMouseMotion and -1 in active_touches and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		active_touches[-1] = event.position
		sync_actions()

func action_at(point: Vector2) -> String:
	if dpad_touch_contains(point): return "dpad"
	for action in controls:
		if action in ["move_left", "move_right", "dpad_up", "dpad_down"]: continue
		if (controls[action] as Rect2).has_point(point): return action
	return ""

func sync_actions() -> void:
	var desired_actions := {}
	var moving := false
	dpad_vector = Vector2.ZERO
	for point in active_touches.values():
		if dpad_touch_contains(point):
			var vector := dpad_input_vector(point)
			dpad_vector = vector
			if vector.x < -DPAD_DEAD_ZONE:
				desired_actions["move_left"] = true
				moving = true
			elif vector.x > DPAD_DEAD_ZONE:
				desired_actions["move_right"] = true
				moving = true
			if vector.y < -DPAD_DEAD_ZONE: desired_actions["jump"] = true
			elif vector.y > DPAD_DEAD_ZONE: desired_actions["crouch"] = true
			continue
		var action := action_at(point)
		if action != "":
			desired_actions[input_action_for(action)] = true
			if action in ["move_left", "move_right"]: moving = true
	if moving: desired_actions["sprint"] = true
	apply_touch_action_changes(desired_actions)
	queue_redraw()

func apply_touch_action_changes(desired_actions: Dictionary) -> void:
	# ScreenDrag events can arrive every frame. Releasing and pressing every
	# action on each event turns a hold into repeated just-pressed edges.
	for action in touch_pressed_actions:
		if not desired_actions.has(action): Input.action_release(action)
	for action in desired_actions:
		if not touch_pressed_actions.has(action): Input.action_press(action)
	touch_pressed_actions = desired_actions

func release_all_touches() -> void:
	active_touches.clear()
	sync_actions()

func _exit_tree() -> void:
	release_all_touches()

func _draw() -> void:
	if controls.is_empty(): return
	draw_dpad()
	for action in controls:
		if action in ["move_left", "move_right", "dpad_up", "dpad_down"]: continue
		var box: Rect2 = controls[action]
		var active := Input.is_action_pressed(input_action_for(action))
		var fill := control_color(action, active)
		var border := Color.WHITE if active else control_border(action)
		var radius := 62 if action == "jump" else 47 if action == "fire" else 22
		draw_style_box(make_box(fill, border, radius), box)
		var font_size := 17 if action in ["move_left", "move_right", "dpad_up", "dpad_down"] else 19
		if action == "jump": font_size = 22
		var text_color := control_text_color(action, active)
		var baseline := box.position + Vector2(0, box.size.y * 0.62 + font_size * 0.25)
		draw_string(ThemeDB.fallback_font, baseline, label_for(action), HORIZONTAL_ALIGNMENT_CENTER, box.size.x, font_size, text_color)

func dpad_touch_contains(point: Vector2) -> bool:
	return point.distance_to(dpad_bounds.get_center()) <= DPAD_SIZE * 0.5 + DPAD_DRIFT_MARGIN

func dpad_input_vector(point: Vector2) -> Vector2:
	var half := maxf(1.0, DPAD_SIZE * 0.5)
	var vector := (point - dpad_bounds.get_center()) / half
	return vector.limit_length(1.0)

func draw_dpad() -> void:
	var center := dpad_bounds.get_center()
	var radius := DPAD_SIZE * 0.5
	draw_circle(center + Vector2(0, 5), radius + 3, Color(0, 0, 0, 0.28))
	draw_circle(center, radius, Color(0.035, 0.09, 0.13, 0.78))
	draw_circle(center + Vector2(0, -radius * 0.18), radius * 0.66, Color(1, 1, 1, 0.07))
	draw_arc(center, radius, 0, TAU, 64, Color(0.52, 0.84, 0.91, 0.82), 3.0)
	draw_dpad_arrow(center + Vector2(-52, 0), Vector2.LEFT, Input.is_action_pressed("move_left"))
	draw_dpad_arrow(center + Vector2(52, 0), Vector2.RIGHT, Input.is_action_pressed("move_right"))
	draw_dpad_arrow(center + Vector2(0, -52), Vector2.UP, Input.is_action_pressed("jump"))
	draw_dpad_arrow(center + Vector2(0, 52), Vector2.DOWN, Input.is_action_pressed("crouch"))
	var knob := center + dpad_vector * radius * 0.46
	draw_circle(knob, 14, Color(1.0, 0.84, 0.42, 0.92))
	draw_circle(knob, 6, Color("#18202a"))

func draw_dpad_arrow(at: Vector2, direction: Vector2, active: bool) -> void:
	draw_circle(at, 23, Color(1.0, 0.84, 0.42, 0.92) if active else Color(1, 1, 1, 0.62))
	var tip := at + direction * 12.0
	var back := at - direction * 8.0
	var side := Vector2(-direction.y, direction.x) * 8.0
	draw_colored_polygon(PackedVector2Array([tip, back + side, back - side]), Color("#18202a"))

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
