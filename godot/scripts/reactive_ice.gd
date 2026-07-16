class_name ReactiveIce
extends StaticBody2D

signal shattered(position: Vector2)

@export var size := Vector2(150, 24)
@export var hits_to_break := 2
var hits := 0
var art: Sprite2D

func _ready() -> void:
	add_to_group("reactive_terrain")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = size
	collision.shape = shape
	add_child(collision)
	art = Sprite2D.new()
	art.texture = load("res://assets/route_platform_ice.png")
	art.scale = Vector2(size.x / art.texture.get_width(), maxf(0.10, size.y / art.texture.get_height()))
	add_child(art)
	queue_redraw()

func snowball_hit(_projectile: Node) -> void:
	hits += 1
	spawn_hit_flash()
	if hits >= hits_to_break:
		shattered.emit(global_position)
		queue_free()
	else:
		queue_redraw()

func _draw() -> void:
	var rect := Rect2(-size * 0.5, size)
	if hits > 0:
		draw_polyline(PackedVector2Array([
			Vector2(-24, -size.y * 0.5), Vector2(-5, 3), Vector2(12, -5),
			Vector2(28, size.y * 0.5)
		]), Color("#245f7a"), 3.0)

func spawn_hit_flash() -> void:
	var flash := Sprite2D.new()
	flash.texture = load("res://assets/laser_ice_impact.png")
	flash.scale = Vector2.ONE * 0.18
	flash.position = Vector2(randf_range(-size.x * 0.3, size.x * 0.3), -8)
	flash.z_index = 3
	add_child(flash)
	var tween := create_tween()
	tween.tween_property(flash, "scale", Vector2.ONE * 0.30, 0.16)
	tween.parallel().tween_property(flash, "modulate:a", 0.0, 0.20)
	tween.tween_callback(flash.queue_free)
