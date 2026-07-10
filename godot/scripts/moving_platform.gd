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
	queue_redraw()

func _physics_process(delta: float) -> void:
	clock += delta
	var pct := (sin(clock * TAU / cycle_seconds) + 1.0) * 0.5
	position = origin + travel * pct

func _draw() -> void:
	draw_rect(Rect2(-75, -12, 150, 24), Color("#7b5135"))
	draw_line(Vector2(-65,-5), Vector2(65,-5), Color("#d9b680"), 3)
