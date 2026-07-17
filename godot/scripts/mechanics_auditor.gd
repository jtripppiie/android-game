class_name MechanicsAuditor
extends Node

var controller: Node
var world: AlaskaStage
var player: AlaskaRunner


func run(main_controller: Node) -> void:
	controller = main_controller
	controller.start_stage(0)
	await controller.get_tree().process_frame
	await controller.get_tree().physics_frame
	world = controller.world as AlaskaStage
	player = world.player
	await settle_on_floor()
	var start_x := player.global_position.x
	Input.action_press("move_right")
	Input.action_press("sprint")
	await physics_frames(18)
	Input.action_release("move_right")
	Input.action_release("sprint")
	var right_distance := player.global_position.x - start_x
	assert(right_distance > 70.0)
	player.velocity.x = 0.0
	var left_start_x := player.global_position.x
	Input.action_press("move_left")
	await physics_frames(18)
	Input.action_release("move_left")
	var left_distance := left_start_x - player.global_position.x
	assert(left_distance > 45.0)

	player.respawn()
	await settle_on_floor()
	var short_floor_y := player.global_position.y
	Input.action_press("jump")
	await physics_frames(2)
	Input.action_release("jump")
	var short_apex := await measure_jump_apex(short_floor_y)
	var short_height := short_floor_y - short_apex

	player.respawn()
	await settle_on_floor()
	var full_floor_y := player.global_position.y
	Input.action_press("jump")
	await physics_frames(30)
	Input.action_release("jump")
	var full_apex := await measure_jump_apex(full_floor_y)
	var full_height := full_floor_y - full_apex
	assert(short_height >= 55.0)
	assert(full_height >= 220.0)
	assert(full_height >= short_height + 120.0)

	player.respawn()
	await settle_on_floor()
	await tap("jump", 2)
	await physics_frames(8)
	var before_air_jump_velocity := player.velocity.y
	await tap("jump", 1)
	await physics_frames(1)
	assert(player.air_jumps_left == 0)
	assert(player.velocity.y < before_air_jump_velocity)
	var after_air_jump_velocity := player.velocity.y
	await tap("jump", 1)
	await physics_frames(1)
	assert(player.air_jumps_left == 0)
	assert(player.velocity.y > after_air_jump_velocity)

	player.respawn()
	await settle_on_floor()
	var dash_start_x := player.global_position.x
	await tap("dash", 1)
	await physics_frames(8)
	var dash_distance := player.global_position.x - dash_start_x
	assert(dash_distance > 75.0)

	player.respawn()
	await settle_on_floor()
	await tap("jump", 2)
	await physics_frames(7)
	assert(player.queue_stomp())
	assert(player.state == "stomp" or player.velocity.y >= AlaskaRunner.STOMP_SPEED * 0.85)

	player.respawn()
	await settle_on_floor()
	Input.action_press("fire")
	await physics_frames(2)
	Input.action_release("fire")
	var projectiles := 0
	for child in world.get_children():
		if child is SnowballProjectile:
			projectiles += 1
	assert(projectiles == 1)

	player.health = 1
	player.invulnerability = 0.0
	player.take_hit(player.global_position.x + 100.0)
	await controller.get_tree().process_frame
	assert(controller.get_tree().paused)
	assert(is_instance_valid(world.game_over_overlay))
	controller.restart_stage(0)
	await controller.get_tree().process_frame
	await controller.get_tree().physics_frame
	assert(not controller.get_tree().paused)
	assert(controller.get_tree().get_nodes_in_group("player").size() == 1)
	assert(controller.get_tree().get_nodes_in_group("active_stage").size() == 1)
	print(
		"MECHANICS AUDIT PASS right=%.1f left=%.1f short_jump=%.1f full_jump=%.1f dash=%.1f air_jump=one third_jump=blocked stomp=true snowball=1 game_over=true clean_restart=true" %
		[right_distance, left_distance, short_height, full_height, dash_distance]
	)
	controller.get_tree().quit(0)


func measure_jump_apex(floor_y: float) -> float:
	var apex := player.global_position.y
	for _frame in range(120):
		await controller.get_tree().physics_frame
		apex = minf(apex, player.global_position.y)
		if player.is_on_floor() and player.global_position.y >= floor_y - 2.0:
			return apex
	assert(false, "Player did not land within mechanics-audit window")
	return apex


func settle_on_floor() -> void:
	for _frame in range(120):
		await controller.get_tree().physics_frame
		if player.is_on_floor():
			return
	assert(false, "Player did not settle on authored terrain")


func tap(action: String, held_frames: int) -> void:
	Input.action_press(action)
	await physics_frames(held_frames)
	Input.action_release(action)


func physics_frames(count: int) -> void:
	for _frame in range(count):
		await controller.get_tree().physics_frame
