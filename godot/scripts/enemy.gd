class_name TrailEnemy
extends CharacterBody2D

@export var patrol_distance := 180.0
@export var speed := 95.0
@export var kind := "wolf"
var origin_x := 0.0
var direction := -1.0
var attack_cooldown := 0.0
var player: Node2D
var art: Sprite2D
var ledge_ray: RayCast2D

func _ready() -> void:
	origin_x = global_position.x
	player = get_tree().get_first_node_in_group("player")
	add_to_group("enemy")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(92, 68) if kind == "bear" else Vector2(74, 46) if kind == "wolf" else Vector2(82, 42) if kind == "eagle" else Vector2(66, 34)
	collision.shape = shape
	collision.position = Vector2(0, -shape.size.y * 0.5)
	add_child(collision)
	art = Sprite2D.new()
	var files := {"bear":"wildlife_bear_walk.png", "wolf":"wildlife_wolf_run.png", "eagle":"wildlife_eagle_fly.png", "salmon":"wildlife_salmon_swim.png"}
	art.texture = load("res://assets/%s" % files.get(kind, "wildlife_wolf_run.png"))
	art.hframes = 6
	art.scale = Vector2.ONE * float({"bear":0.48, "wolf":0.30, "eagle":0.34, "salmon":0.25}.get(kind, 0.30))
	art.position.y = float({"bear":-68.0, "wolf":-32.0, "eagle":-80.0, "salmon":-34.0}.get(kind, -32.0))
	add_child(art)
	if kind in ["bear", "wolf"]:
		ledge_ray = RayCast2D.new()
		ledge_ray.position = Vector2(direction * 36.0, -12.0)
		ledge_ray.target_position = Vector2(direction * 28.0, 76.0)
		ledge_ray.enabled = true
		add_child(ledge_ray)

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
	if is_instance_valid(ledge_ray) and is_on_floor():
		ledge_ray.position = Vector2(direction * 36.0, -12.0)
		ledge_ray.target_position = Vector2(direction * 28.0, 76.0)
		ledge_ray.force_raycast_update()
		if not ledge_ray.is_colliding():
			direction *= -1.0
			velocity.x = direction * absf(velocity.x)
	if kind != "eagle" and kind != "salmon": velocity.y += 1500.0 * delta
	move_and_slide()
	art.frame = int(Time.get_ticks_msec() / (115.0 if absf(velocity.x) > speed * 1.2 else 155.0)) % 6
	art.flip_h = direction > 0.0
	if is_instance_valid(player) and global_position.distance_to(player.global_position) < (46.0 if kind == "bear" else 38.0):
		if is_stomp_contact(player):
			player.enemy_defeated(true)
			queue_free()
			return
		player.take_hit(global_position.x)
	if absf(global_position.x - origin_x) > patrol_distance: direction *= -1.0

func is_stomp_contact(runner: AlaskaRunner) -> bool:
	# A normal jump landing must read as a stomp, not an inexplicable death.
	# Permit the late apex as well as descent; side/underside contact still hurts.
	return (
		not runner.is_on_floor()
		and runner.global_position.y <= global_position.y - 18.0
		and runner.velocity.y >= -180.0
	)
