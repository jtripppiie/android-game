class_name AndroidVerificationHarness
extends Node

const SUPPORTED_SCENARIOS := [
	"launch",
	"main-menu",
	"stage-map",
	"stage-start",
	"running",
	"short-jump",
	"full-jump",
	"air-jump",
	"dash",
	"stomp",
	"snowball",
	"key-collected",
	"first-rescue",
	"second-rescue",
	"checkpoint",
	"enemy-encounter",
	"player-damaged",
	"boss-tell",
	"boss-attack",
	"boss-defeated",
	"stage-goal",
	"stage-complete",
	"next-stage-unlocked",
	"pause-menu",
	"return-to-map",
	"game-over",
	"restart",
	"review-mode",
	"large-text",
	"high-contrast",
	"reduced-motion",
	"touch-controls",
	"simultaneous-input",
	"background-resume"
]

var controller: Node
var world: AlaskaStage
var scenario := ""
var stage_index := 0


func run(main_controller: Node, requested_scenario: String, requested_stage: int) -> void:
	if not OS.is_debug_build():
		push_error("Verification harness refused a non-debug build")
		return
	controller = main_controller
	scenario = requested_scenario.to_lower().replace("_", "-")
	stage_index = clampi(requested_stage, 0, GameSession.STAGES.size() - 1)
	if scenario not in SUPPORTED_SCENARIOS:
		push_error("Unknown verification scenario: %s" % scenario)
		print("VERIFICATION FAIL scenario=%s reason=unsupported" % scenario)
		return
	if scenario == "launch":
		controller.show_launch_splash()
		await controller.get_tree().process_frame
		report_ready()
		return
	if scenario == "main-menu":
		controller.show_menu()
		await controller.get_tree().process_frame
		report_ready()
		return
	if scenario == "stage-map":
		controller.show_map()
		await controller.get_tree().process_frame
		report_ready()
		return
	prepare_accessibility()
	GameSession.selected_stage = stage_index
	controller.start_stage(stage_index)
	await controller.get_tree().process_frame
	await controller.get_tree().physics_frame
	world = controller.world as AlaskaStage
	if not is_instance_valid(world):
		print("VERIFICATION FAIL scenario=%s stage=%d reason=stage-not-created" % [scenario, stage_index])
		return
	await apply_scenario()
	report_ready()


func prepare_accessibility() -> void:
	if scenario == "review-mode":
		GameSession.review_mode = true
	elif scenario == "large-text":
		GameSession.large_text = true
	elif scenario == "high-contrast":
		GameSession.high_contrast = true
	elif scenario == "reduced-motion":
		GameSession.reduced_motion = true


func apply_scenario() -> void:
	match scenario:
		"stage-start", "touch-controls", "reduced-motion", "background-resume":
			await wait_physics(2)
		"running":
			await press_for("move_right", 24, ["sprint"])
		"short-jump":
			Input.action_press("jump")
			await wait_physics(3)
			Input.action_release("jump")
			await wait_physics(8)
		"full-jump":
			Input.action_press("jump")
			await wait_physics(14)
			Input.action_release("jump")
			await wait_physics(2)
		"air-jump":
			await tap_action("jump", 2)
			await wait_physics(8)
			await tap_action("jump", 2)
			await wait_physics(5)
		"dash":
			await tap_action("dash", 3)
			await wait_physics(2)
		"stomp":
			await tap_action("jump", 2)
			await wait_physics(8)
			world.player.queue_stomp()
			await wait_physics(2)
		"snowball":
			Input.action_press("fire")
			await wait_physics(2)
			Input.action_release("fire")
			await wait_physics(2)
		"simultaneous-input":
			Input.action_press("move_right")
			Input.action_press("sprint")
			await tap_action("jump", 3)
			await wait_physics(8)
			Input.action_release("move_right")
			Input.action_release("sprint")
		"key-collected":
			collect_kind("key", 1)
			await wait_physics(2)
		"first-rescue":
			collect_kind("survivor", 1)
			await wait_physics(2)
		"second-rescue":
			collect_kind("survivor", 2)
			await wait_physics(2)
		"checkpoint":
			var checkpoint_zone := debug_item_with_label("CHECKPOINT")
			if checkpoint_zone:
				world.player.set_checkpoint(checkpoint_zone.global_position + Vector2(0, -30))
			await wait_physics(2)
		"enemy-encounter":
			var foe := world.get_tree().get_first_node_in_group("enemy") as Node2D
			if foe:
				world.player.global_position = foe.global_position + Vector2(-190, 0)
			await wait_physics(2)
		"player-damaged":
			world.player.invulnerability = 0.0
			world.player.take_hit(world.player.global_position.x + 100.0)
			await wait_physics(2)
		"boss-tell":
			activate_boss()
			await wait_physics(2)
		"boss-attack":
			activate_boss()
			world.boss_node.state = TrailBoss.State.TELL
			world.boss_node.state_timer = world.boss_node.tell_seconds() - 0.03
			await wait_physics(4)
		"boss-defeated":
			defeat_boss()
			await wait_physics(2)
		"stage-goal":
			var goal := world.get_tree().get_first_node_in_group("stage_goal") as Node2D
			if goal:
				world.player.global_position = goal.global_position + Vector2(-120, 0)
			world.finish_level(world.player)
			await wait_physics(2)
		"stage-complete":
			complete_stage()
			await controller.get_tree().process_frame
		"next-stage-unlocked":
			complete_stage()
			await controller.get_tree().process_frame
			if is_instance_valid(world.stage_complete_overlay):
				world.stage_complete_overlay.map_requested.emit()
			await controller.get_tree().process_frame
		"pause-menu":
			world.toggle_pause_panel()
			await controller.get_tree().process_frame
		"return-to-map":
			world.exit_run_to_map()
			await controller.get_tree().process_frame
		"game-over":
			world.player.health = 1
			world.player.invulnerability = 0.0
			world.player.take_hit(world.player.global_position.x + 100.0)
			await controller.get_tree().process_frame
		"restart":
			controller.restart_stage(stage_index)
			await controller.get_tree().process_frame
			await controller.get_tree().physics_frame
			world = controller.world as AlaskaStage
		"review-mode":
			world.review_registry.ids_visible = true
			world.review_registry.update(world.player)
			world.notebook.open()
			await controller.get_tree().process_frame
		"large-text", "high-contrast":
			await wait_physics(2)


func press_for(action: String, frames: int, companion_actions: Array[String] = []) -> void:
	Input.action_press(action)
	for companion in companion_actions:
		Input.action_press(companion)
	await wait_physics(frames)
	Input.action_release(action)
	for companion in companion_actions:
		Input.action_release(companion)


func tap_action(action: String, frames := 1) -> void:
	Input.action_press(action)
	await wait_physics(frames)
	Input.action_release(action)


func wait_physics(frames: int) -> void:
	for _frame in range(frames):
		await controller.get_tree().physics_frame


func collect_kind(kind: String, count: int) -> void:
	var collected := 0
	var items := world.get_tree().get_nodes_in_group("debug_item")
	items.sort_custom(func(a: Node, b: Node) -> bool:
		if not a is Node2D or not b is Node2D:
			return false
		return a.global_position.x < b.global_position.x
	)
	for item in items:
		if (
			item is Area2D
			and String(item.get_meta("kind", "")) == kind
			and collected < count
		):
			world.collect(world.player, item)
			collected += 1


func debug_item_with_label(label: String) -> Node2D:
	for item in world.get_tree().get_nodes_in_group("debug_item"):
		if item is Node2D and String(item.get_meta("debug_label", "")) == label:
			return item
	return null


func activate_boss() -> void:
	if not is_instance_valid(world.boss_node):
		return
	world.player.global_position = world.boss_node.global_position + Vector2(-520, 0)
	world.boss_node.activated = true
	world.boss_node.state = TrailBoss.State.TELL
	world.boss_node.state_timer = 0.08


func defeat_boss() -> void:
	if not is_instance_valid(world.boss_node):
		return
	world.boss_node.activated = true
	world.boss_node.state = TrailBoss.State.RECOVER
	for _hit in range(world.boss_node.max_health):
		if is_instance_valid(world.boss_node):
			world.boss_node.snowball_hit(null)


func complete_stage() -> void:
	collect_kind("key", 1)
	collect_kind("survivor", 2)
	defeat_boss()
	world.finish_level(world.player)


func report_ready() -> void:
	var facts := {
		"scenario": scenario,
		"stage": stage_index,
		"screen": String(controller.current_screen),
		"players": controller.get_tree().get_nodes_in_group("player").size(),
		"worlds": controller.get_tree().get_nodes_in_group("active_stage").size()
	}
	if is_instance_valid(world) and is_instance_valid(world.player):
		facts.merge({
			"x": roundi(world.player.global_position.x),
			"y": roundi(world.player.global_position.y),
			"health": world.player.health,
			"score": world.player.score,
			"key": world.key_collected,
			"rescues": world.survivors_found,
			"boss_defeated": world.boss_defeated,
			"paused": controller.get_tree().paused
		}, true)
	print("VERIFICATION READY " + JSON.stringify(facts))
