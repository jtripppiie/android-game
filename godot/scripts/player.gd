class_name AlaskaRunner
extends CharacterBody2D

signal fired(origin: Vector2, direction: float)
signal checkpoint_reached(position: Vector2)
signal action_feedback(message: String)
signal defeated
signal damaged(health_remaining: int)

const WALK_SPEED := 330.0
const SPRINT_SPEED := 540.0
const ACCELERATION := 3200.0
const AIR_ACCELERATION := 1350.0
const FRICTION := 2500.0
const AIR_FRICTION := 360.0
const GRAVITY := 1550.0
const RISE_GRAVITY_SCALE := 0.92
const APEX_GRAVITY_SCALE := 0.72
const FALL_GRAVITY_SCALE := 1.18
const MAX_FALL_SPEED := 1180.0
const JUMP_SPEED := 900.0
const COYOTE_TIME := 0.18
const JUMP_BUFFER := 0.20
const DASH_SPEED := 780.0
const DASH_SECONDS := 0.16
const DASH_COOLDOWN := 0.70
const STOMP_SPEED := 880.0
const SHORT_JUMP_CUT := 0.52
const FALL_LIMIT_Y := 850.0

var coyote := 0.0
var jump_buffer := 0.0
var fire_cooldown := 0.0
var facing := 1.0
var state := "idle"
var spawn_point := Vector2.ZERO
var coins := 0
var health := 3
var dash_timer := 0.0
var dash_cooldown := 0.0
var invulnerability := 0.0
var was_on_floor := false
var combo := 0
var combo_timer := 0.0
var score := 0
var ring_chain := 0
var ring_chain_timer := 0.0
var ring_rush_timer := 0.0
var air_jumps_left := 1
var controls_enabled := true
var stomp_impact_pending := false

@onready var presentation: PlayerPresentation = $Presentation
@onready var camera: RunnerCamera = $Camera2D
@onready var effects: RunnerEffects = $RunnerEffects

func _ready() -> void:
	spawn_point = global_position
	add_to_group("player")
	camera.position_smoothing_enabled = not GameSession.reduced_motion

func _physics_process(delta: float) -> void:
	if not controls_enabled:
		velocity = Vector2.ZERO
		return
	fire_cooldown = maxf(0.0, fire_cooldown - delta)
	dash_cooldown = maxf(0.0, dash_cooldown - delta)
	invulnerability = maxf(0.0, invulnerability - delta)
	dash_timer = maxf(0.0, dash_timer - delta)
	ring_chain_timer = maxf(0.0, ring_chain_timer - delta)
	ring_rush_timer = maxf(0.0, ring_rush_timer - delta)
	combo_timer = maxf(0.0, combo_timer - delta)
	if combo_timer <= 0.0: combo = 0
	if ring_chain_timer <= 0.0: ring_chain = 0
	var axis := Input.get_axis("move_left", "move_right")
	# Read the current steering intent before dash. Previously a same-frame
	# left+dash press launched in the runner's stale facing direction.
	if not is_zero_approx(axis):
		facing = signf(axis)
	if is_on_floor():
		coyote = COYOTE_TIME
		air_jumps_left = 1
	else:
		coyote = maxf(0.0, coyote - delta)
		var gravity_scale := RISE_GRAVITY_SCALE
		if absf(velocity.y) < 120.0:
			gravity_scale = APEX_GRAVITY_SCALE
		elif velocity.y > 0.0:
			gravity_scale = FALL_GRAVITY_SCALE
		velocity.y = minf(MAX_FALL_SPEED, velocity.y + GRAVITY * gravity_scale * delta)
	if Input.is_action_just_pressed("jump"): jump_buffer = JUMP_BUFFER
	else: jump_buffer = maxf(0.0, jump_buffer - delta)
	if jump_buffer > 0.0 and coyote > 0.0:
		velocity.y = -JUMP_SPEED
		jump_buffer = 0.0
		coyote = 0.0
	elif jump_buffer > 0.0 and not is_on_floor() and air_jumps_left > 0:
		velocity.y = -JUMP_SPEED * 0.88
		jump_buffer = 0.0
		air_jumps_left -= 1
		action_feedback.emit("AIR JUMP")
	if Input.is_action_just_released("jump") and velocity.y < -210.0:
		velocity.y *= SHORT_JUMP_CUT
	if Input.is_action_just_pressed("dash") and dash_cooldown <= 0.0:
		dash_timer = DASH_SECONDS
		dash_cooldown = DASH_COOLDOWN
		velocity = Vector2(facing * DASH_SPEED, clampf(velocity.y, -110.0, 110.0))
		camera.add_trauma(0.06)
		action_feedback.emit("TRAIL DASH")
	if not is_on_floor() and Input.is_action_just_pressed("crouch") and dash_timer <= 0.0:
		queue_stomp()
	if dash_timer > 0.0:
		velocity.x = facing * DASH_SPEED
		velocity.y += GRAVITY * 0.16 * delta
		move_and_slide()
		update_animation(axis)
		return
	var target_speed := SPRINT_SPEED if Input.is_action_pressed("sprint") else WALK_SPEED
	if ring_rush_timer > 0.0: target_speed *= 1.18
	if axis != 0.0:
		var acceleration := ACCELERATION if is_on_floor() else AIR_ACCELERATION
		if is_on_floor() and not is_zero_approx(velocity.x) and signf(velocity.x) != signf(axis):
			acceleration *= 1.28
		velocity.x = move_toward(velocity.x, axis * target_speed, acceleration * delta)
	else:
		var deceleration := FRICTION if is_on_floor() else AIR_FRICTION
		velocity.x = move_toward(velocity.x, 0.0, deceleration * delta)
	if Input.is_action_pressed("crouch") and is_on_floor(): velocity.x = move_toward(velocity.x, 0.0, FRICTION * 1.5 * delta)
	if Input.is_action_pressed("fire") and fire_cooldown <= 0.0:
		fire_cooldown = 0.20
		fired.emit(global_position + Vector2(facing * 32, -52), facing)
	move_and_slide()
	if is_on_floor() and not was_on_floor and velocity.y >= 0.0:
		var impact_strength := 1.45 if stomp_impact_pending else 1.0 if absf(velocity.x) > 280.0 else 0.65
		effects.emit_snow(impact_strength)
		camera.add_trauma(0.30 if stomp_impact_pending else 0.16 if impact_strength >= 1.0 else 0.08)
		if stomp_impact_pending:
			action_feedback.emit("STOMP LANDING")
		elif absf(velocity.x) > 280.0:
			action_feedback.emit("PERFECT LAND")
		stomp_impact_pending = false
	was_on_floor = is_on_floor()
	if global_position.y > FALL_LIMIT_Y:
		action_feedback.emit("ROUTE LOST · CHECKPOINT")
		respawn(true)
	modulate = Color(1, 1, 1, 0.42 if invulnerability > 0.0 and int(invulnerability * 16.0) % 2 == 0 else 1.0)
	update_animation(axis)

func update_animation(_axis: float) -> void:
	if dash_timer > 0.0: state = "dash"
	elif not is_on_floor() and velocity.y > 700.0: state = "stomp"
	elif not is_on_floor(): state = "jump" if velocity.y < 0.0 else "fall"
	elif Input.is_action_pressed("crouch"): state = "crouch"
	elif absf(velocity.x) > 320.0: state = "sprint"
	elif absf(velocity.x) > 35.0: state = "run"
	else: state = "idle"
	presentation.update_state(facing, state, absf(velocity.x), is_on_floor())

func set_checkpoint(point: Vector2) -> void:
	spawn_point = point
	checkpoint_reached.emit(point)

func queue_jump() -> void:
	jump_buffer = JUMP_BUFFER


func queue_stomp() -> bool:
	if is_on_floor() or dash_timer > 0.0 or not controls_enabled:
		return false
	velocity.y = STOMP_SPEED
	state = "stomp"
	stomp_impact_pending = true
	action_feedback.emit("ICE STOMP")
	return true


func take_hit(from_x: float) -> void:
	if not controls_enabled or invulnerability > 0.0 or dash_timer > 0.0:
		return
	health -= 1
	damaged.emit(health)
	combo = 0
	invulnerability = 1.0
	effects.emit_hit()
	camera.add_trauma(0.32)
	velocity = Vector2(signf(global_position.x - from_x) * 320.0, -360.0)
	action_feedback.emit("HIT · COMBO LOST")
	if health <= 0:
		controls_enabled = false
		velocity = Vector2.ZERO
		action_feedback.emit("RUN ENDED · RESTART OR MAP")
		defeated.emit()

func enemy_defeated(stomp: bool) -> void:
	chain_action(42 if stomp else 24)
	if stomp:
		velocity.y = -420.0
		action_feedback.emit("STOMP COMBO x%d" % combo)
	else: action_feedback.emit("HIT COMBO x%d" % combo)

func collect_aurora_ring() -> void:
	coins += 1
	ring_chain = ring_chain + 1 if ring_chain_timer > 0.0 else 1
	ring_chain_timer = 1.65
	ring_rush_timer = minf(4.2, 1.8 + ring_chain * 0.38)
	velocity.y = minf(velocity.y, -115.0 - mini(3, ring_chain) * 22.0)
	chain_action(18 + ring_chain * 4)
	action_feedback.emit("AURORA RING x%d · SPEED SURGE" % ring_chain)

func respawn(apply_score_penalty := false) -> void:
	if apply_score_penalty:
		score = maxi(0, score - 25)
	health = 3
	combo = 0
	combo_timer = 0.0
	dash_timer = 0.0
	dash_cooldown = 0.25
	fire_cooldown = 0.0
	jump_buffer = 0.0
	coyote = 0.0
	air_jumps_left = 1
	facing = 1.0
	ring_chain = 0
	ring_chain_timer = 0.0
	ring_rush_timer = 0.0
	was_on_floor = false
	stomp_impact_pending = false
	state = "idle"
	invulnerability = 1.2
	controls_enabled = true
	velocity = Vector2.ZERO
	global_position = spawn_point
	effects.clear()
	camera.reset_feedback()
	action_feedback.emit("TRAIL RECOVERY · TRY AGAIN")


func set_controls_enabled(enabled: bool) -> void:
	controls_enabled = enabled
	if not enabled:
		velocity = Vector2.ZERO

func chain_action(base_score: int) -> void:
	combo += 1
	combo_timer = maxf(2.15, 2.8 - maxf(0.0, combo - 1) * 0.045)
	var multiplier := 4 if combo >= 10 else 3 if combo >= 7 else 2 if combo >= 4 else 1
	score += base_score * multiplier
