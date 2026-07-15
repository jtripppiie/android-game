class_name MovingTrailPlatform
extends AnimatableBody2D

@export var travel := Vector2(220, 0)
@export var cycle_seconds := 2.8
var origin := Vector2.ZERO
var clock := 0.0

func _ready() -> void:
	origin = position
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(150, 24)
	collision.shape = shape
	add_child(collision)
	var art := Sprite2D.new()
	art.texture = load("res://assets/route_platform_moving.png")
	art.scale = Vector2(150.0 / art.texture.get_width(), 0.23)
	art.position = Vector2(0, 5)
	add_child(art)

func _physics_process(delta: float) -> void:
	clock += delta
	var pct := (sin(clock * TAU / cycle_seconds) + 1.0) * 0.5
	position = origin + travel * pct
