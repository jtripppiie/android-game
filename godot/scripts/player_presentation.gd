class_name PlayerPresentation
extends Node2D

# Measured from the six 362x724 source frames. Each value places that frame's
# lowest opaque shoe pixel on the CharacterBody2D origin, which is also the
# capsule's contact line. This prevents run-cycle bobbing and removes the
# temptation to "fix" collision by eye.
const FRAME_GROUND_Y := [
	-85.0, -89.8, -86.7, -81.6, -90.1, -89.1
]

@onready var runner_sprite: Sprite2D = $RunnerSprite
@onready var photo_head: Sprite2D = $PhotoHead
@onready var ground_shadow: Polygon2D = $GroundShadow


func _ready() -> void:
	apply_photo_head()


func update_state(facing: float, state: String, horizontal_speed: float, grounded: bool) -> void:
	runner_sprite.flip_h = facing < 0.0
	if state == "idle":
		runner_sprite.frame = 0
	elif state == "crouch":
		runner_sprite.frame = 1
	elif state in ["jump", "dash"]:
		runner_sprite.frame = 3
	elif state == "stomp":
		runner_sprite.frame = 2
	elif state == "fall":
		runner_sprite.frame = 4
	else:
		var fps := 13.0 if state == "sprint" else 9.0
		if horizontal_speed < 80.0:
			fps = 7.0
		runner_sprite.frame = int(Time.get_ticks_msec() / (1000.0 / fps)) % runner_sprite.hframes
	runner_sprite.position.y = FRAME_GROUND_Y[runner_sprite.frame]
	photo_head.position.y = runner_sprite.position.y + 8.0
	ground_shadow.visible = grounded


func apply_photo_head() -> void:
	var texture := GameSession.player_photo_texture()
	photo_head.texture = texture
	photo_head.visible = texture != null
