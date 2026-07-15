class_name FreezableWater
extends Area2D

@export var size := Vector2(150, 36)

func _ready() -> void:
	add_to_group("reactive_terrain")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = size
	collision.shape = shape
	add_child(collision)
	body_entered.connect(_on_body_entered)
	queue_redraw()

func snowball_hit(_projectile: Node) -> void:
	var bridge := ReactiveIce.new()
	bridge.position = position - Vector2(0, size.y * 0.5)
	bridge.size = Vector2(size.x, 22)
	get_parent().add_child.call_deferred(bridge)
	queue_free()

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner:
		body.action_feedback.emit("CURRENT · ROUTE LOST")
		body.respawn()

func _draw() -> void:
	draw_rect(Rect2(-size * 0.5, size), Color("#125c82"), true)
	for x in range(int(-size.x * 0.5), int(size.x * 0.5), 28):
		draw_arc(Vector2(x, -4), 14, PI, TAU, 12, Color("#84d5e8"), 3)

