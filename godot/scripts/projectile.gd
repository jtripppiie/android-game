class_name SnowballProjectile
extends Area2D

var velocity := Vector2.ZERO
var life := 1.6

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
	queue_redraw()

func _physics_process(delta: float) -> void:
	velocity.y += 260.0 * delta
	position += velocity * delta
	life -= delta
	if life <= 0.0: queue_free()

func _draw() -> void:
	draw_circle(Vector2.ZERO, 10.0, Color("#eaf8ff"))
	draw_circle(Vector2(-3, -3), 3.0, Color.WHITE)

func _on_body_entered(body: Node) -> void:
	if body.is_in_group("enemy"):
		var player := get_tree().get_first_node_in_group("player")
		if is_instance_valid(player): player.enemy_defeated(false)
		body.queue_free()
	queue_free()
