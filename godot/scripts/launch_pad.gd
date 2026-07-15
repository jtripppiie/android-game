class_name AuroraLaunchPad
extends Area2D

@export var launch_velocity := 780.0
var cooldown := 0.0

func _ready() -> void:
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(92, 18)
	collision.shape = shape
	collision.position = Vector2(0, -8)
	add_child(collision)
	body_entered.connect(_on_body_entered)
	var art := Sprite2D.new()
	art.texture = load("res://assets/arctic_launch_pad.png")
	art.scale = Vector2(96.0 / art.texture.get_width(), 0.22)
	art.position = Vector2(0, -14)
	add_child(art)

func _physics_process(delta: float) -> void:
	cooldown = maxf(0.0, cooldown - delta)

func _on_body_entered(body: Node) -> void:
	if body is AlaskaRunner and cooldown <= 0.0 and body.velocity.y >= 0.0:
		body.velocity.y = -launch_velocity
		body.chain_action(20)
		body.action_feedback.emit("AURORA LAUNCH · AIR ROUTE")
		cooldown = 0.22
