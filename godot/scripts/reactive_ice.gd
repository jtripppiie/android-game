class_name ReactiveIce
extends StaticBody2D

signal shattered(position: Vector2)

@export var size := Vector2(150, 24)
@export var hits_to_break := 2
var hits := 0

func _ready() -> void:
	add_to_group("reactive_terrain")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = size
	collision.shape = shape
	add_child(collision)
	queue_redraw()

func snowball_hit(_projectile: Node) -> void:
	hits += 1
	if hits >= hits_to_break:
		shattered.emit(global_position)
		queue_free()
	else:
		queue_redraw()

func _draw() -> void:
	var rect := Rect2(-size * 0.5, size)
	draw_rect(rect, Color("#7ed7ed") if hits == 0 else Color("#b8eff8"), true)
	draw_rect(rect, Color.WHITE, false, 3.0)
	if hits > 0:
		draw_polyline(PackedVector2Array([
			Vector2(-24, -size.y * 0.5), Vector2(-5, 3), Vector2(12, -5),
			Vector2(28, size.y * 0.5)
		]), Color("#245f7a"), 3.0)

