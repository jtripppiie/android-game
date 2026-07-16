class_name BossHazard
extends Area2D

var kind := "sun"
var velocity := Vector2.ZERO
var fall_acceleration := 0.0
var life := 3.2
var art: Sprite2D

func setup(hazard_kind: String, origin: Vector2, initial_velocity: Vector2) -> void:
	kind = hazard_kind
	global_position = origin
	velocity = initial_velocity
	fall_acceleration = 520.0 if kind in ["splash", "snow"] else 0.0

func _ready() -> void:
	add_to_group("boss_hazard")
	# Hazards scan player bodies but do not occupy the player-projectile layer.
	collision_layer = 0
	collision_mask = 1
	var collision := CollisionShape2D.new()
	if kind == "shockwave":
		var box := RectangleShape2D.new()
		box.size = Vector2(62, 34)
		collision.shape = box
	else:
		var circle := CircleShape2D.new()
		circle.radius = 18.0 if kind == "snow" else 14.0
		collision.shape = circle
	add_child(collision)
	body_entered.connect(_on_body_entered)
	if kind == "snow": build_snow_art()
	queue_redraw()

func _physics_process(delta: float) -> void:
	velocity.y += fall_acceleration * delta
	position += velocity * delta
	if is_instance_valid(art): art.rotation = atan2(velocity.y, velocity.x)
	life -= delta
	if life <= 0.0 or global_position.y > 820.0: queue_free()

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner:
		body.take_hit(global_position.x)
		queue_free()

func build_snow_art() -> void:
	art = Sprite2D.new()
	var atlas := AtlasTexture.new()
	atlas.atlas = load("res://assets/trail_objects_atlas.png")
	var cell_width := atlas.atlas.get_width() / 3.0
	atlas.region = Rect2(cell_width * 2.0, 0, cell_width, atlas.atlas.get_height())
	art.texture = atlas
	art.scale = Vector2.ONE * 0.09
	add_child(art)

func _draw() -> void:
	if kind == "snow": return
	if kind == "sun":
		draw_circle(Vector2.ZERO, 16, Color("#ffda79"))
		draw_arc(Vector2.ZERO, 23, 0, TAU, 24, Color(1, 0.45, 0.22, 0.9), 5)
	elif kind == "splash":
		draw_circle(Vector2.ZERO, 15, Color("#84d5e8"))
		draw_arc(Vector2.ZERO, 22, PI, TAU, 18, Color("#eafcff"), 4)
	elif kind == "shockwave":
		draw_colored_polygon(PackedVector2Array([Vector2(-34,14),Vector2(-18,-18),Vector2(0,5),Vector2(17,-24),Vector2(34,14)]), Color("#ffda79"))
		draw_polyline(PackedVector2Array([Vector2(-34,14),Vector2(-18,-18),Vector2(0,5),Vector2(17,-24),Vector2(34,14)]), Color("#3b2d27"), 4)
	else:
		draw_colored_polygon(PackedVector2Array([Vector2(-26,0),Vector2(18,-9),Vector2(28,0),Vector2(18,9)]), Color("#e9f3f5"))
		draw_line(Vector2(-20,0), Vector2(20,0), Color("#526f94"), 3)
