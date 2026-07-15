class_name TrailBoss
extends Area2D

signal defeated
signal feedback(message: String)

enum State { TELL, ATTACK, RECOVER, DEFEATED }
const MAX_HEALTH := 8
const TELL_SECONDS := 1.25
const ATTACK_SECONDS := 0.72
const RECOVER_SECONDS := 1.35
var health := MAX_HEALTH
var state := State.TELL
var state_timer := 0.0
var rest_position := Vector2.ZERO
var target_x := 0.0
var player: AlaskaRunner

func _ready() -> void:
	rest_position = position
	player = get_tree().get_first_node_in_group("player") as AlaskaRunner
	var collision := CollisionShape2D.new()
	var shape := CircleShape2D.new()
	shape.radius = 58.0
	collision.shape = shape
	add_child(collision)
	body_entered.connect(_on_body_entered)
	feedback.emit("BOSS · READ THE TELL · FIRE DURING RECOVERY")
	queue_redraw()

func _physics_process(delta: float) -> void:
	if state == State.DEFEATED: return
	if not is_instance_valid(player): player = get_tree().get_first_node_in_group("player") as AlaskaRunner
	state_timer += delta
	if state == State.TELL:
		position = rest_position + Vector2(sin(state_timer * 18.0) * 5.0, 0.0)
		if state_timer >= TELL_SECONDS:
			state = State.ATTACK
			state_timer = 0.0
			target_x = player.global_position.x if is_instance_valid(player) else rest_position.x - 260.0
			feedback.emit("CHARGE · MOVE")
	elif state == State.ATTACK:
		var pct := minf(1.0, state_timer / ATTACK_SECONDS)
		position.x = lerpf(rest_position.x, maxf(rest_position.x - 360.0, target_x), sin(pct * PI))
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

func snowball_hit(_projectile: Node) -> void:
	if state != State.RECOVER:
		feedback.emit("ARMORED · WAIT FOR WEAK")
		return
	health -= 1
	feedback.emit("BOSS HIT · %d/%d" % [health, MAX_HEALTH])
	if health <= 0:
		state = State.DEFEATED
		monitoring = false
		defeated.emit()
		queue_free()

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner and state == State.ATTACK: body.take_hit(global_position.x)

func _draw() -> void:
	var body_color := Color("#ffda79") if state == State.RECOVER else Color("#6c5142")
	draw_circle(Vector2.ZERO, 58.0, Color("#1f2226"))
	draw_circle(Vector2.ZERO, 51.0, body_color)
	draw_circle(Vector2(-22, -42), 18.0, body_color)
	draw_circle(Vector2(22, -42), 18.0, body_color)
	var eye_color := Color("#ff6254") if state == State.TELL else Color.WHITE
	draw_circle(Vector2(-17, -12), 5.0, eye_color)
	draw_circle(Vector2(17, -12), 5.0, eye_color)
	if state == State.TELL: draw_arc(Vector2.ZERO, 70.0, 0.0, TAU, 40, Color(1.0, 0.38, 0.33, 0.72), 5.0)
	elif state == State.RECOVER: draw_arc(Vector2.ZERO, 70.0, 0.0, TAU, 40, Color(1.0, 0.85, 0.47, 0.85), 6.0)
