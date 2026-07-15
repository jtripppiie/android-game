class_name AuroraTrickRing
extends Area2D

func _ready() -> void:
	var collision := CollisionShape2D.new()
	var shape := CircleShape2D.new()
	shape.radius = 24.0
	collision.shape = shape
	add_child(collision)
	body_entered.connect(_on_body_entered)
	queue_redraw()

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner:
		body.collect_aurora_ring()
		queue_free()

func _draw() -> void:
	draw_arc(Vector2.ZERO, 25.0, 0.0, TAU, 32, Color(0.30, 0.86, 0.72, 0.34), 9.0)
	draw_arc(Vector2.ZERO, 22.0, 0.0, TAU, 32, Color("#84d5e8"), 4.0)
	draw_arc(Vector2.ZERO, 22.0, -1.2, 0.5, 12, Color.WHITE, 1.5)

