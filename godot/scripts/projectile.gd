class_name SnowballProjectile
extends Area2D

var velocity := Vector2.ZERO
var life := 1.6
var art: Sprite2D

func setup(origin: Vector2, direction: float) -> void:
	global_position = origin
	velocity = Vector2(direction * 720.0, -70.0)

func _ready() -> void:
	var collision := CollisionShape2D.new()
	var shape := CircleShape2D.new()
	shape.radius = 9.0
	collision.shape = shape
	add_child(collision)
	body_entered.connect(_on_body_entered)
	area_entered.connect(_on_area_entered)
	art = Sprite2D.new()
	var atlas := AtlasTexture.new()
	atlas.atlas = load("res://assets/trail_objects_atlas.png")
	var cell_width := atlas.atlas.get_width() / 3.0
	atlas.region = Rect2(cell_width * 2.0, 0, cell_width, atlas.atlas.get_height())
	art.texture = atlas
	art.scale = Vector2.ONE * 0.055
	art.rotation = atan2(velocity.y, velocity.x)
	add_child(art)

func _physics_process(delta: float) -> void:
	velocity.y += 260.0 * delta
	position += velocity * delta
	if is_instance_valid(art): art.rotation = atan2(velocity.y, velocity.x)
	life -= delta
	if life <= 0.0: queue_free()

func _on_body_entered(body: Node) -> void:
	if body.has_method("snowball_hit"):
		body.snowball_hit(self)
	elif body.is_in_group("enemy"):
		var player := get_tree().get_first_node_in_group("player")
		if is_instance_valid(player): player.enemy_defeated(false)
		body.queue_free()
	queue_free()

func _on_area_entered(area: Area2D) -> void:
	if area.has_method("snowball_hit"):
		area.snowball_hit(self)
		queue_free()
