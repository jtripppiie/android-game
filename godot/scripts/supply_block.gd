class_name AuroraSupplyBlock
extends Area2D

signal opened(at: Vector2)
var spent := false

func _ready() -> void:
	add_to_group("reactive_terrain")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(52, 52)
	collision.shape = shape
	add_child(collision)
	body_entered.connect(_on_body_entered)
	var art := Sprite2D.new()
	art.name = "Art"
	art.texture = load("res://assets/aurora_supply_block.png")
	art.scale = Vector2(56.0 / art.texture.get_width(), 56.0 / art.texture.get_height())
	add_child(art)

func snowball_hit(_projectile: Node) -> void:
	open()

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner and body.velocity.y < 0.0:
		body.velocity.y = 90.0
		open()

func open() -> void:
	if spent:
		return
	spent = true
	opened.emit(global_position)
	var art := get_node("Art") as Sprite2D
	art.modulate = Color(0.35, 0.48, 0.58, 0.58)
	set_deferred("monitoring", false)

