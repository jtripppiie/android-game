class_name GameplayAuditor
extends Node

var stage: AlaskaStage
var autoplay := false
var visual := false
var geometry := false
var debug_overlay := false
var elapsed := 0.0
var next_jump := 0.35
var last_jump := -1.0
var release_dash := false
var jumps := 0
var hits := 0
var last_health := 3
var maximum_x := 190.0
var last_x := 190.0
var last_progress_time := 0.0
var visual_capture_x := 500.0
var visual_capture_pending := false
var visual_files: Array[String] = []
var last_progress_report := 0
var deaths := 0
var death_pending := false


func configure(owner: AlaskaStage) -> void:
	stage = owner
	for argument in OS.get_cmdline_user_args():
		if argument.begins_with("--autoplay-audit="):
			autoplay = true
		elif argument.begins_with("--visual-audit="):
			autoplay = true
			visual = true
		elif argument.begins_with("--geometry-audit="):
			geometry = true
		elif argument.begins_with("--debug-overlay-audit="):
			debug_overlay = true
	set_process(autoplay or visual)
	if autoplay:
		process_mode = Node.PROCESS_MODE_ALWAYS
		stage.player.defeated.connect(func(): death_pending = true)
		print("AUTOPLAY START stage=%d player_ready=%s" % [stage.stage_index, is_instance_valid(stage.player)])
	if geometry:
		run_geometry.call_deferred()
	elif debug_overlay:
		run_debug_overlay.call_deferred()


func _process(delta: float) -> void:
	if death_pending:
		recover_from_audit_death()
	if autoplay:
		run_autoplay(delta)
	if (
		visual
		and not visual_capture_pending
		and is_instance_valid(stage.player)
		and stage.player.global_position.x >= visual_capture_x
	):
		capture_visual.call_deferred()


func run_autoplay(delta: float) -> void:
	if not is_instance_valid(stage.player):
		return
	var player := stage.player
	elapsed += delta
	var elapsed_second := floori(elapsed)
	if elapsed_second >= last_progress_report + 10:
		last_progress_report = elapsed_second
		print(
			"AUTOPLAY PROGRESS stage=%d time=%d x=%.1f health=%d key=%s rescues=%d boss=%s" %
			[
				stage.stage_index,
				elapsed_second,
				player.global_position.x,
				player.health,
				stage.key_collected,
				stage.survivors_found,
				stage.boss_defeated
			]
		)
	if player.health < last_health:
		hits += last_health - player.health
	last_health = player.health
	maximum_x = maxf(maximum_x, player.global_position.x)
	if absf(player.global_position.x - last_x) > 8.0:
		last_x = player.global_position.x
		last_progress_time = elapsed
	var objective := target_objective()
	var boss_engaged := (
		not stage.boss_defeated
		and objective == null
		and is_instance_valid(stage.boss_node)
		and player.global_position.x > stage.boss_node.rest_position.x - 700.0
	)
	var objective_dx := objective.global_position.x - player.global_position.x if is_instance_valid(objective) else INF
	var target_direction := 1.0
	if is_instance_valid(objective):
		target_direction = signf(objective_dx)
	elif boss_engaged:
		target_direction = signf((stage.boss_node.rest_position.x - 250.0) - player.global_position.x)
	var waiting_for_boss := (
		boss_engaged
		and absf((stage.boss_node.rest_position.x - 250.0) - player.global_position.x) < 90.0
	)
	if is_instance_valid(objective) and absf(objective_dx) < 28.0:
		Input.action_release("move_right")
		Input.action_release("move_left")
	elif waiting_for_boss:
		Input.action_release("move_right")
		Input.action_release("move_left")
		player.facing = 1.0
	elif target_direction < 0.0:
		Input.action_release("move_right")
		Input.action_press("move_left")
	else:
		Input.action_press("move_right")
		Input.action_release("move_left")
	if is_instance_valid(objective) and absf(objective_dx) < 180.0:
		Input.action_release("sprint")
	else:
		Input.action_press("sprint")
	Input.action_press("fire")
	if release_dash:
		Input.action_release("dash")
		release_dash = false
	var stuck := elapsed - last_progress_time > 1.3
	if (
		is_instance_valid(objective)
		and player.is_on_floor()
		and absf(objective_dx) < 175.0
		and elapsed - last_jump > 0.30
	):
		next_jump = minf(next_jump, elapsed)
	if (
		not boss_engaged
		and player.is_on_floor()
		and jump_needed(target_direction)
		and elapsed - last_jump > 0.30
	):
		next_jump = minf(next_jump, elapsed)
	for hazard in stage.get_tree().get_nodes_in_group("boss_hazard"):
		if (
			player.is_on_floor()
			and hazard is Node2D
			and absf(hazard.global_position.x - player.global_position.x) < 250.0
			and elapsed - last_jump > 0.30
		):
			next_jump = minf(next_jump, elapsed)
			break
	if elapsed >= next_jump:
		player.queue_jump()
		jumps += 1
		last_jump = elapsed
		next_jump = INF
	if stuck and not boss_engaged:
		player.queue_jump()
		Input.action_press("dash")
		release_dash = true
		jumps += 1
		last_jump = elapsed
		last_progress_time = elapsed
		next_jump = elapsed + 0.5
	if elapsed > 120.0:
		var boss_health := stage.boss_node.health if is_instance_valid(stage.boss_node) else 0
		print(
			"AUTOPLAY FAIL stage=%d max_x=%.1f complete=false key=%s rescues=%d boss=%s boss_hp=%d jumps=%d hits=%d deaths=%d time=%.2f reason=timeout" %
			[stage.stage_index, maximum_x, stage.key_collected, stage.survivors_found, stage.boss_defeated, boss_health, jumps, hits, deaths, elapsed]
		)
		release_inputs()
		stage.get_tree().quit(2)


func complete(score: int) -> void:
	print(
		"AUTOPLAY PASS stage=%d max_x=%.1f complete=true key=%s rescues=%d boss=%s jumps=%d hits=%d deaths=%d time=%.2f score=%d failure_reason=none" %
		[stage.stage_index, maximum_x, stage.key_collected, stage.survivors_found, stage.boss_defeated, jumps, hits, deaths, elapsed, score]
	)
	release_inputs()
	stage.get_tree().quit(0)


func recover_from_audit_death() -> void:
	death_pending = false
	deaths += 1
	stage.get_tree().paused = false
	if is_instance_valid(stage.game_over_overlay):
		stage.game_over_overlay.queue_free()
		stage.game_over_overlay = null
	stage.player.respawn()
	last_health = stage.player.health
	last_progress_time = elapsed
	next_jump = elapsed + 0.35
	print(
		"AUTOPLAY RECOVERY stage=%d death=%d checkpoint_x=%.1f" %
		[stage.stage_index, deaths, stage.player.global_position.x]
	)


func capture_visual() -> void:
	visual_capture_pending = true
	var capture_at := visual_capture_x
	visual_capture_x += 800.0
	if DisplayServer.get_name() == "headless":
		print(
			"VISUAL AUDIT NOT CAPTURED stage=%d x=%d reason=headless-display-no-render-target" %
			[stage.stage_index, roundi(capture_at)]
		)
		visual_capture_pending = false
		return
	await RenderingServer.frame_post_draw
	var image := stage.get_viewport().get_texture().get_image()
	var path := "user://visual-audit-s%d-x%d.png" % [stage.stage_index, roundi(capture_at)]
	var result := image.save_png(path)
	if result == OK:
		visual_files.append(path)
		print("VISUAL AUDIT CAPTURE stage=%d x=%d path=%s" % [stage.stage_index, roundi(capture_at), path])
	else:
		push_error("Visual audit capture failed: %s" % error_string(result))
	visual_capture_pending = false


func run_geometry() -> void:
	await stage.get_tree().physics_frame
	var keys := 0
	var survivors := 0
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		if String(item.get_meta("kind", "")) == "key":
			keys += 1
		elif String(item.get_meta("kind", "")) == "survivor":
			survivors += 1
	assert(keys == 1)
	assert(survivors == 2)
	assert(stage.get_tree().get_nodes_in_group("stage_boss").size() == 1)
	assert(stage.get_tree().get_nodes_in_group("stage_goal").size() == 1)
	assert(stage.get_tree().get_nodes_in_group("player").size() == 1)
	assert(stage.get_tree().get_nodes_in_group("active_stage").size() == 1)
	assert(stage.get_tree().get_nodes_in_group("boss_hazard").is_empty())
	for mover in stage.get_tree().get_nodes_in_group("moving_platform"):
		var midpoint: Vector2 = mover.origin + mover.travel * 0.5
		assert(mover.position.distance_to(midpoint) <= maxf(4.0, mover.travel.length() * 0.10))
	var enemy_positions: Array[Vector2] = []
	for foe in stage.get_tree().get_nodes_in_group("enemy"):
		assert(absf(foe.global_position.x - foe.origin_x) <= foe.patrol_distance + 4.0)
		for previous in enemy_positions:
			assert(previous.distance_to(foe.global_position) >= 420.0)
		enemy_positions.append(foe.global_position)
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		if String(item.get_meta("debug_label", "")) == "SUPPLY BLOCK":
			assert(item.global_position.y >= 440.0 and item.global_position.y <= 490.0)
	var surfaces: Array[Node] = stage.get_tree().get_nodes_in_group("main_route_surface")
	surfaces.sort_custom(func(a: Node, b: Node) -> bool:
		return float(a.get_meta("surface_start")) < float(b.get_meta("surface_start"))
	)
	assert(surfaces.size() >= 7)
	var maximum_gap := 0.0
	var maximum_rise := 0.0
	for index in range(1, surfaces.size()):
		var previous := surfaces[index - 1]
		var current := surfaces[index]
		var gap := maxf(0.0, float(current.get_meta("surface_start")) - float(previous.get_meta("surface_end")))
		var rise := maxf(0.0, float(previous.get_meta("surface_y")) - float(current.get_meta("surface_y")))
		maximum_gap = maxf(maximum_gap, gap)
		maximum_rise = maxf(maximum_rise, rise)
	assert(maximum_gap <= 190.0)
	assert(maximum_rise <= 190.0)
	print(
		"GEOMETRY AUDIT PASS stage=%d surfaces=%d max_gap=%.1f max_rise=%.1f keys=%d rescues=%d bosses=1 goals=1 enemies=%d" %
		[stage.stage_index, surfaces.size(), maximum_gap, maximum_rise, keys, survivors, enemy_positions.size()]
	)
	stage.get_tree().quit(0)


func run_debug_overlay() -> void:
	await stage.get_tree().physics_frame
	stage.review_registry.audit(stage.player)
	stage.hud.audit_layout()
	stage.notebook.audit_layout()
	print(
		"DEBUG OVERLAY AUDIT PASS stage=%d ids=compact visible_max=%d note_panel=%dx%d hud=legible" %
		[
			stage.stage_index,
			ReviewRegistry.MAX_VISIBLE_BADGES,
			roundi(stage.notebook.panel.size.x),
			roundi(stage.notebook.panel.size.y)
		]
	)
	stage.get_tree().quit(0)


func target_objective() -> Node2D:
	var target: Node2D
	var target_x := INF
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		if (
			item is Node2D
			and String(item.get_meta("kind", "")) in ["key", "survivor"]
			and item.global_position.x < target_x
		):
			target = item
			target_x = item.global_position.x
	return target


func jump_needed(direction: float) -> bool:
	var player := stage.player
	var space := stage.get_world_2d().direct_space_state
	var sign_x := 1.0 if direction >= 0.0 else -1.0
	var forward := PhysicsRayQueryParameters2D.create(
		player.global_position + Vector2(sign_x * 28.0, -42.0),
		player.global_position + Vector2(sign_x * 112.0, -42.0)
	)
	forward.exclude = [player.get_rid()]
	var ahead_ground := PhysicsRayQueryParameters2D.create(
		player.global_position + Vector2(sign_x * 118.0, -105.0),
		player.global_position + Vector2(sign_x * 118.0, 82.0)
	)
	ahead_ground.exclude = [player.get_rid()]
	return not space.intersect_ray(forward).is_empty() or space.intersect_ray(ahead_ground).is_empty()


func release_inputs() -> void:
	for action in ["move_left", "move_right", "sprint", "fire", "jump", "dash", "crouch"]:
		Input.action_release(action)
