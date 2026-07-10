class_name AlaskaRunner
extends CharacterBody2D

signal fired(origin: Vector2, direction: float)
signal checkpoint_reached(position: Vector2)
signal action_feedback(message: String)

const WALK_SPEED := 260.0
const SPRINT_SPEED := 430.0
const ACCELERATION := 1800.0
const AIR_ACCELERATION := 1050.0
const FRICTION := 2100.0
const GRAVITY := 1550.0
const JUMP_SPEED := 610.0
const COYOTE_TIME := 0.11
const JUMP_BUFFER := 0.13
const DASH_SPEED := 720.0
const DASH_SECONDS := 0.16
const DASH_COOLDOWN := 0.70
const STOMP_SPEED := 880.0

var coyote := 0.0
var jump_buffer := 0.0
var fire_cooldown := 0.0
var facing := 1.0
var state := "idle"
var spawn_point := Vector2.ZERO
var coins := 0
var keys := 0
var health := 3
var dash_timer := 0.0
var dash_cooldown := 0.0
var invulnerability := 0.0
var was_on_floor := false
var combo := 0
var sprite: Sprite2D

func _ready() -> void:
	spawn_point = global_position
	add_to_group("player")
	var shape := CollisionShape2D.new()
	var capsule := CapsuleShape2D.new()
	capsule.radius = 22.0
	capsule.height = 76.0
	shape.shape = capsule
	shape.position = Vector2(0, -39)
	add_child(shape)
	sprite = Sprite2D.new()
	sprite.texture = load("res://assets/runner_overhaul.png")
	sprite.hframes = 6
	sprite.scale = Vector2(0.34, 0.34)
	sprite.position = Vector2(0, -55)
	add_child(sprite)
	var camera := Camera2D.new()
	camera.position = Vector2(220, -95)
	camera.position_smoothing_enabled = true
	camera.position_smoothing_speed = 6.5
	camera.limit_left = 0
	camera.limit_top = -260
	camera.limit_right = 6200
	camera.limit_bottom = 760
	add_child(camera)

func _physics_process(delta: float) -> void:
	fire_cooldown = maxf(0.0, fire_cooldown - delta)
	dash_cooldown = maxf(0.0, dash_cooldown - delta)
	invulnerability = maxf(0.0, invulnerability - delta)
	dash_timer = maxf(0.0, dash_timer - delta)
	if is_on_floor(): coyote = COYOTE_TIME
	else:
		coyote = maxf(0.0, coyote - delta)
		velocity.y += GRAVITY * delta
	if Input.is_action_just_pressed("jump"): jump_buffer = JUMP_BUFFER
	else: jump_buffer = maxf(0.0, jump_buffer - delta)
	if jump_buffer > 0.0 and coyote > 0.0:
		velocity.y = -JUMP_SPEED
		jump_buffer = 0.0
		coyote = 0.0
	if Input.is_action_just_released("jump") and velocity.y < -210.0: velocity.y *= 0.52
	var axis := Input.get_axis("move_left", "move_right")
	if Input.is_action_just_pressed("dash") and dash_cooldown <= 0.0:
		dash_timer = DASH_SECONDS
		dash_cooldown = DASH_COOLDOWN
		velocity = Vector2(facing * DASH_SPEED, 0.0)
		action_feedback.emit("TRAIL DASH")
	if not is_on_floor() and Input.is_action_just_pressed("crouch") and dash_timer <= 0.0:
		velocity.y = STOMP_SPEED
		state = "stomp"
		action_feedback.emit("ICE STOMP")
	if dash_timer > 0.0:
		velocity.x = facing * DASH_SPEED
		velocity.y = 0.0
		move_and_slide()
		update_animation(delta, axis)
		return
	var target_speed := SPRINT_SPEED if Input.is_action_pressed("sprint") else WALK_SPEED
	if axis != 0.0:
		facing = signf(axis)
		velocity.x = move_toward(velocity.x, axis * target_speed, (ACCELERATION if is_on_floor() else AIR_ACCELERATION) * delta)
	else:
		velocity.x = move_toward(velocity.x, 0.0, FRICTION * delta)
	if Input.is_action_pressed("crouch") and is_on_floor(): velocity.x = move_toward(velocity.x, 0.0, FRICTION * 1.5 * delta)
	if Input.is_action_pressed("fire") and fire_cooldown <= 0.0:
		fire_cooldown = 0.20
		fired.emit(global_position + Vector2(facing * 32, -52), facing)
	move_and_slide()
	if is_on_floor() and not was_on_floor and velocity.y >= 0.0:
		action_feedback.emit("PERFECT LAND" if absf(velocity.x) > 280.0 else "LAND")
	was_on_floor = is_on_floor()
	if global_position.y > 850: respawn()
	update_animation(delta, axis)

func update_animation(delta: float, axis: float) -> void:
	if dash_timer > 0.0: state = "dash"
	elif not is_on_floor() and velocity.y > 700.0: state = "stomp"
	elif not is_on_floor(): state = "jump" if velocity.y < 0.0 else "fall"
	elif Input.is_action_pressed("crouch"): state = "crouch"
	elif absf(velocity.x) > 320.0: state = "sprint"
	elif absf(velocity.x) > 35.0: state = "run"
	else: state = "idle"
	sprite.flip_h = facing < 0.0
	if state == "idle": sprite.frame = 0
	elif state == "crouch": sprite.frame = 1
	elif state == "jump" or state == "dash": sprite.frame = 3
	elif state == "stomp": sprite.frame = 2
	elif state == "fall": sprite.frame = 4
	else:
		var fps := 13.0 if state == "sprint" else 9.0
		sprite.frame = int(Time.get_ticks_msec() / (1000.0 / fps)) % 6

func set_checkpoint(point: Vector2) -> void:
	spawn_point = point
	checkpoint_reached.emit(point)

func take_hit(from_x: float) -> void:
	if invulnerability > 0.0 or dash_timer > 0.0: return
	health -= 1
	combo = 0
	invulnerability = 1.0
	velocity = Vector2(signf(global_position.x - from_x) * 320.0, -360.0)
	action_feedback.emit("HIT · COMBO LOST")
	if health <= 0: respawn()

func enemy_defeated(stomp: bool) -> void:
	combo += 1
	if stomp:
		velocity.y = -420.0
		action_feedback.emit("STOMP COMBO x%d" % combo)
	else: action_feedback.emit("HIT COMBO x%d" % combo)

func respawn() -> void:
	health = 3
	combo = 0
	invulnerability = 1.2
	velocity = Vector2.ZERO
	global_position = spawn_point
