class_name RunnerCamera
extends Camera2D

const LOOKAHEAD_DISTANCE := 92.0
const LOOKAHEAD_RESPONSE := 5.5
const TRAUMA_DECAY := 2.6
const MAX_SHAKE_PIXELS := 9.0

var base_position := Vector2.ZERO
var trauma := 0.0
var target: CharacterBody2D


func _ready() -> void:
	base_position = position
	target = get_parent() as CharacterBody2D


func _process(delta: float) -> void:
	if not is_instance_valid(target):
		return
	var movement_ratio := clampf(target.velocity.x / 780.0, -1.0, 1.0)
	var desired_x := base_position.x
	if not GameSession.reduced_motion:
		desired_x += movement_ratio * LOOKAHEAD_DISTANCE
	position.x = lerpf(position.x, desired_x, minf(1.0, LOOKAHEAD_RESPONSE * delta))
	position.y = base_position.y
	trauma = maxf(0.0, trauma - TRAUMA_DECAY * delta)
	if GameSession.reduced_motion or trauma <= 0.0:
		offset = Vector2.ZERO
		return
	var strength := trauma * trauma * MAX_SHAKE_PIXELS
	var phase := Time.get_ticks_msec() * 0.031
	offset = Vector2(sin(phase * 1.7), cos(phase * 2.3)) * strength


func add_trauma(amount: float) -> void:
	if GameSession.reduced_motion:
		return
	trauma = clampf(trauma + amount, 0.0, 1.0)


func reset_feedback() -> void:
	trauma = 0.0
	offset = Vector2.ZERO
	position = base_position
