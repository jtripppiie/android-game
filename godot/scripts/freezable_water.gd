class_name FreezableWater
extends Area2D

@export var size := Vector2(150, 36)
var frozen := false
var collision: CollisionShape2D
var art: Sprite2D

func _ready() -> void:
	add_to_group("reactive_terrain")
	collision = CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = size
	collision.shape = shape
	add_child(collision)
	body_entered.connect(_on_body_entered)
	art = Sprite2D.new()
	art.texture = load("res://assets/glacial_water_surface.png")
	art.scale = Vector2(size.x / art.texture.get_width(), maxf(0.12, size.y / art.texture.get_height()))
	add_child(art)
	var current_tween := create_tween().set_loops()
	current_tween.tween_property(art, "position:x", 5.0, 0.75).set_trans(Tween.TRANS_SINE)
	current_tween.tween_property(art, "position:x", -5.0, 0.75).set_trans(Tween.TRANS_SINE)

func snowball_hit(_projectile: Node) -> void:
	if frozen:
		return
	frozen = true
	art.visible = false
	collision.set_deferred("disabled", true)
	var bridge := ReactiveIce.new()
	bridge.position = position - Vector2(0, size.y * 0.5)
	bridge.size = Vector2(size.x, 22)
	bridge.shattered.connect(_on_bridge_shattered)
	get_parent().add_child.call_deferred(bridge)
	queue_redraw()

func _on_bridge_shattered(_at: Vector2) -> void:
	frozen = false
	art.visible = true
	collision.set_deferred("disabled", false)
	queue_redraw()

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner:
		body.action_feedback.emit("CURRENT · ROUTE LOST")
		body.respawn()
