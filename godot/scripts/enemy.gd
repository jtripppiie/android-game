class_name TrailEnemy
extends CharacterBody2D

@export var patrol_distance := 180.0
@export var speed := 95.0
@export var kind := "wolf"
var origin_x := 0.0
var direction := -1.0
var attack_cooldown := 0.0
var player: Node2D

func _ready() -> void:
	origin_x = global_position.x
	player = get_tree().get_first_node_in_group("player")
	add_to_group("enemy")
	var collision := CollisionShape2D.new()
	var shape := CircleShape2D.new()
	shape.radius = 34.0 if kind == "bear" else 22.0
	collision.shape = shape
	add_child(collision)

func _physics_process(delta: float) -> void:
	attack_cooldown = maxf(0.0, attack_cooldown - delta)
	if not is_instance_valid(player): player = get_tree().get_first_node_in_group("player")
	var distance := player.global_position.x - global_position.x if is_instance_valid(player) else 9999.0
	if kind == "eagle":
		direction = signf(distance)
		velocity.x = direction * speed * 1.45
		velocity.y = sin(Time.get_ticks_msec() * 0.004) * 90.0
	elif kind == "salmon":
		direction = signf(distance)
		velocity.x = direction * speed * 1.20
		velocity.y += 900.0 * delta
		if is_on_floor() and attack_cooldown <= 0.0:
			velocity.y = -520.0
			attack_cooldown = 1.4
	elif kind == "wolf" and absf(distance) < 230.0:
		direction = signf(distance)
		velocity.x = direction * speed * 1.75
		if is_on_floor() and absf(distance) < 150.0 and attack_cooldown <= 0.0:
			velocity.y = -420.0
			attack_cooldown = 1.25
	elif kind == "bear" and absf(distance) < 320.0:
		direction = signf(distance)
		velocity.x = direction * speed * 1.28
	else: velocity.x = direction * speed
	if kind != "eagle" and kind != "salmon": velocity.y += 1500.0 * delta
	move_and_slide()
	if is_instance_valid(player) and global_position.distance_to(player.global_position) < (46.0 if kind == "bear" else 38.0):
		if player.velocity.y > 520.0 and player.global_position.y < global_position.y - 12.0:
			player.enemy_defeated(true)
			queue_free()
			return
		player.take_hit(global_position.x)
	if absf(global_position.x - origin_x) > patrol_distance: direction *= -1.0
	queue_redraw()

func _draw() -> void:
	var scale_factor := 1.34 if kind == "bear" else 0.78 if kind == "wolf" else 0.62 if kind == "salmon" else 0.84
	if kind == "eagle":
		draw_colored_polygon(PackedVector2Array([Vector2(-46,0),Vector2(-10,-18),Vector2(0,-4),Vector2(10,-18),Vector2(46,0),Vector2(10,8),Vector2(0,2),Vector2(-10,8)]), Color("#6c5142"))
		return
	if kind == "salmon":
		draw_colored_polygon(PackedVector2Array([Vector2(-32,0),Vector2(-20,-17),Vector2(18,-14),Vector2(34,0),Vector2(18,14),Vector2(-20,17)]), Color("#ff6254"))
		draw_colored_polygon(PackedVector2Array([Vector2(-28,0),Vector2(-50,-22),Vector2(-50,22)]), Color("#ff8b72"))
		return
	draw_circle(Vector2(0, 4), 29.0 * scale_factor, Color("#3c2e2b"))
	draw_circle(Vector2(-18, -14), 12.0, Color("#4e3a32"))
	draw_circle(Vector2(18, -14), 12.0, Color("#4e3a32"))
	draw_circle(Vector2(0, -2), 22.0, Color("#6c5142"))
	draw_circle(Vector2(-8, -7), 3.0, Color.WHITE)
	draw_circle(Vector2(8, -7), 3.0, Color.WHITE)
