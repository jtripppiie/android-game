class_name TrailBoss
extends Area2D

signal defeated
signal feedback(message: String)

enum State { TELL, ATTACK, RECOVER, DEFEATED }
@export var max_health := 8
@export var boss_name := "TRAIL GUARDIAN"
@export_range(0, 4) var boss_variant := 0
const TELL_SECONDS := 1.25
const ATTACK_SECONDS := 0.72
const RECOVER_SECONDS := 1.35
var health := 8
var state := State.TELL
var state_timer := 0.0
var rest_position := Vector2.ZERO
var target_x := 0.0
var player: AlaskaRunner
var art: Sprite2D

func _ready() -> void:
	rest_position = position
	health = max_health
	player = get_tree().get_first_node_in_group("player") as AlaskaRunner
	var collision := CollisionShape2D.new()
	var shape := CircleShape2D.new()
	shape.radius = 58.0
	collision.shape = shape
	add_child(collision)
	body_entered.connect(_on_body_entered)
	build_art()
	feedback.emit("BOSS · READ THE TELL · FIRE DURING RECOVERY")
	queue_redraw()

func _physics_process(delta: float) -> void:
	if state == State.DEFEATED: return
	if not is_instance_valid(player): player = get_tree().get_first_node_in_group("player") as AlaskaRunner
	state_timer += delta
	if state == State.TELL:
		var tell_motion := Vector2(sin(state_timer * 18.0) * 5.0, 0.0)
		if boss_variant == 1: tell_motion.y = -absf(sin(state_timer * 7.0)) * 34.0 # salmon leap
		elif boss_variant == 3: tell_motion.y = sin(state_timer * 12.0) * 24.0 # eagle hover
		elif boss_variant == 4: tell_motion.x *= 2.2 # polar-bear ground shake
		position = rest_position + tell_motion
		if state_timer >= TELL_SECONDS:
			state = State.ATTACK
			state_timer = 0.0
			target_x = player.global_position.x if is_instance_valid(player) else rest_position.x - 260.0
			feedback.emit("CHARGE · MOVE")
	elif state == State.ATTACK:
		var pct := minf(1.0, state_timer / ATTACK_SECONDS)
		var reach: float = [300.0, 250.0, 390.0, 440.0, 520.0][boss_variant]
		position.x = lerpf(rest_position.x, maxf(rest_position.x - reach, target_x), sin(pct * PI))
		if boss_variant == 0: position.y = rest_position.y - sin(pct * PI) * 90.0 # sun arc
		elif boss_variant == 1: position.y = rest_position.y - sin(pct * PI) * 150.0 # salmon jump
		elif boss_variant == 3: position.y = rest_position.y + sin(pct * PI) * 120.0 # eagle dive
		if state_timer >= ATTACK_SECONDS:
			state = State.RECOVER
			state_timer = 0.0
			position = rest_position
			feedback.emit("WEAK · FIRE")
	elif state == State.RECOVER and state_timer >= RECOVER_SECONDS:
		state = State.TELL
		state_timer = 0.0
		feedback.emit("BOSS WINDUP")
	queue_redraw()
	if is_instance_valid(art):
		art.frame = int(Time.get_ticks_msec() / 145.0) % maxi(1, art.hframes)
		art.flip_h = true

func snowball_hit(_projectile: Node) -> void:
	if state != State.RECOVER:
		feedback.emit("ARMORED · WAIT FOR WEAK")
		return
	health -= 1
	feedback.emit("%s HIT · %d/%d" % [boss_name, health, max_health])
	if health <= 0:
		state = State.DEFEATED
		monitoring = false
		defeated.emit()
		queue_free()

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner and state == State.ATTACK: body.take_hit(global_position.x)

func _draw() -> void:
	if is_instance_valid(art):
		if state == State.TELL: draw_arc(Vector2.ZERO, 78.0, 0.0, TAU, 40, Color(1.0, 0.38, 0.33, 0.82), 6.0)
		elif state == State.RECOVER: draw_arc(Vector2.ZERO, 78.0, 0.0, TAU, 40, Color(1.0, 0.85, 0.47, 0.92), 7.0)
		return
	var body_color := Color("#ffda79") if state == State.RECOVER else Color("#6c5142")
	var variant_colors := [Color("#e8944e"), Color("#db6b68"), Color("#79573c"), Color("#526f94"), Color("#e9f3f5")]
	if state != State.RECOVER: body_color = variant_colors[boss_variant]
	draw_circle(Vector2.ZERO, 58.0, Color("#1f2226"))
	draw_circle(Vector2.ZERO, 51.0, body_color)
	draw_circle(Vector2(-22, -42), 18.0, body_color)
	draw_circle(Vector2(22, -42), 18.0, body_color)
	var eye_color := Color("#ff6254") if state == State.TELL else Color.WHITE
	draw_circle(Vector2(-17, -12), 5.0, eye_color)
	draw_circle(Vector2(17, -12), 5.0, eye_color)
	if state == State.TELL: draw_arc(Vector2.ZERO, 70.0, 0.0, TAU, 40, Color(1.0, 0.38, 0.33, 0.72), 5.0)
	elif state == State.RECOVER: draw_arc(Vector2.ZERO, 70.0, 0.0, TAU, 40, Color(1.0, 0.85, 0.47, 0.85), 6.0)

func build_art() -> void:
	art = Sprite2D.new()
	var files := ["boss_laser_emitter.png", "wildlife_salmon_swim.png", "wildlife_moose_walk.png", "wildlife_eagle_fly.png", "wildlife_polar_bear_walk.png"]
	art.texture = load("res://assets/%s" % files[boss_variant])
	art.hframes = 1 if boss_variant == 0 else 6
	var scales := [0.30, 0.58, 0.62, 0.58, 0.64]
	art.scale = Vector2.ONE * scales[boss_variant]
	art.position.y = -30.0
	add_child(art)
