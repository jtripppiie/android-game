class_name AlaskaStage
extends Node2D

signal stage_completed(stage: int, score: int)
signal exit_requested

var player: AlaskaRunner
var hud_label: Label
var checkpoint_label: Label
var key_collected := false
var finished := false
var survivors_found := 0
var boss_defeated := false
var best_score := 0
var notebook: ReviewNotebook
var debug_item_counter := 0
var debug_ids_visible := true
var stage_index := 0

func _ready() -> void:
	stage_index = GameSession.selected_stage
	RenderingServer.set_default_clear_color(Color("#102d4b"))
	build_background()
	build_hud()
	build_level()
	spawn_player()
	load_profile()
	var touch_layer := CanvasLayer.new()
	touch_layer.layer = 20
	add_child(touch_layer)
	touch_layer.add_child(TouchControls.new())
	var note_layer := CanvasLayer.new()
	note_layer.layer = 30
	add_child(note_layer)
	notebook = ReviewNotebook.new()
	notebook.context_provider = debug_note_context
	note_layer.add_child(notebook)

func spawn_player() -> void:
	player = AlaskaRunner.new()
	player.position = Vector2(190, 470)
	player.fired.connect(spawn_snowball)
	player.checkpoint_reached.connect(func(_point): checkpoint_label.text = "CHECKPOINT SAVED")
	player.action_feedback.connect(func(message): checkpoint_label.text = message; FeedbackService.cue(message))
	add_child(player)

func build_background() -> void:
	var skies := [Color("#7b4f80"), Color("#247a86"), Color("#163d62"), Color("#071326"), Color("#29485c")]
	var sky: Color = skies[stage_index]
	if GameSession.high_contrast: sky = Color("#00152b")
	var sky_node := Polygon2D.new()
	sky_node.polygon = PackedVector2Array([Vector2(-500,-500),Vector2(6500,-500),Vector2(6500,720),Vector2(-500,720)])
	sky_node.color = sky
	sky_node.z_index = -10
	add_child(sky_node)
	for i in range(12):
		var mountain := Polygon2D.new()
		var x := float(i * 560 - 300)
		mountain.polygon = PackedVector2Array([Vector2(x,560),Vector2(x+280,170+(i%3)*55),Vector2(x+600,560)])
		mountain.color = sky.lightened(0.10 if i % 2 == 0 else 0.18)
		mountain.z_index = -8
		add_child(mountain)

func build_level() -> void:
	if stage_index == 0: build_midnight_sun()
	elif stage_index == 1: build_salmon_rush()
	elif stage_index == 2: build_moose_pass()
	elif stage_index == 3: build_dark_winter()
	else: build_bear_country()

func build_midnight_sun() -> void:
	platform(Rect2(0, 540, 920, 180), Color("#f6e0a4"))
	platform(Rect2(1020, 500, 520, 220), Color("#f0d58d"))
	platform(Rect2(1660, 450, 420, 270), Color("#f6e0a4"))
	platform(Rect2(2200, 520, 610, 200), Color("#f0d58d"))
	platform(Rect2(2940, 430, 540, 290), Color("#f6e0a4"))
	platform(Rect2(3600, 520, 720, 200), Color("#f0d58d"))
	platform(Rect2(4440, 500, 1270, 220), Color("#f6e0a4"))
	moving_platform(Vector2(930, 390), Vector2(0, -100), 3.0)
	launch_pad(Vector2(2320, 510)); trick_ring_line(Vector2(2400, 420))
	collectible(Vector2(1280, 440), "key"); collectible(Vector2(1900, 390), "survivor"); collectible(Vector2(4020, 460), "survivor")
	enemy(Vector2(1460, 450), 130, "wolf"); enemy(Vector2(3300, 370), 150, "wolf")
	checkpoint(Vector2(2980, 370)); finalize_stage()

func build_salmon_rush() -> void:
	platform(Rect2(0, 540, 760, 180), Color("#b8e2ce"))
	platform(Rect2(900, 500, 500, 220), Color("#c9ead8"))
	platform(Rect2(1580, 540, 520, 180), Color("#b8e2ce"))
	platform(Rect2(2280, 470, 580, 250), Color("#c9ead8"))
	platform(Rect2(3040, 540, 520, 180), Color("#b8e2ce"))
	platform(Rect2(3740, 460, 540, 260), Color("#c9ead8"))
	platform(Rect2(4460, 520, 1250, 200), Color("#b8e2ce"))
	for x in [820.0, 1480.0, 2180.0, 2940.0, 3640.0]:
		var water := FreezableWater.new(); water.position = Vector2(x, 550); water.size = Vector2(120, 48); register_debug_item(water, "WT", "river"); add_child(water)
	collectible(Vector2(1240, 440), "key"); collectible(Vector2(2500, 410), "survivor"); collectible(Vector2(3980, 400), "survivor")
	enemy(Vector2(1800, 480), 110, "salmon"); enemy(Vector2(3280, 480), 110, "salmon")
	checkpoint(Vector2(3100, 480)); finalize_stage()

func build_moose_pass() -> void:
	platform(Rect2(0, 540, 900, 180), Color("#e8f4f5"))
	slope(PackedVector2Array([Vector2(900,540),Vector2(1260,420),Vector2(1260,720),Vector2(900,720)]), Color("#d9ecef"))
	platform(Rect2(1260, 420, 460, 300), Color("#e8f4f5"))
	platform(Rect2(1810, 505, 290, 215), Color("#cce4e8"))
	moving_platform(Vector2(1770, 390), Vector2(230, -80), 3.2)
	platform(Rect2(2190, 430, 540, 290), Color("#e8f4f5"))
	slope(PackedVector2Array([Vector2(2730,430),Vector2(3230,560),Vector2(3230,720),Vector2(2730,720)]), Color("#d9ecef"))
	platform(Rect2(3230, 560, 520, 160), Color("#e8f4f5"))
	platform(Rect2(3860, 460, 300, 260), Color("#cce4e8"))
	moving_platform(Vector2(3760, 330), Vector2(280, 0), 2.6)
	platform(Rect2(4260, 520, 1450, 200), Color("#e8f4f5"))
	for data in [[740,465],[1440,345],[1980,430],[2470,355],[3420,485],[3990,385]]:
		collectible(Vector2(data[0],data[1]), "coin")
	collectible(Vector2(2590, 350), "key")
	collectible(Vector2(2050, 370), "survivor")
	collectible(Vector2(4050, 390), "survivor")
	checkpoint(Vector2(3300, 500))
	enemy(Vector2(1520, 360), 120, "wolf")
	enemy(Vector2(2350, 370), 155, "bear")
	enemy(Vector2(3520, 500), 120, "wolf")
	enemy(Vector2(4520, 460), 180, "bear")
	boss(Vector2(5160, 448))
	goal(Vector2(5480, 450))
	build_route_branches()
	# This vertical slice is deliberately authored. The old directed layer
	# stacked extra geometry and enemies over the same spaces.
	var title := Label.new()
	title.text = "CHUGACH RUN · FIND THE KEY · REACH THE RESCUE BEACON"
	title.position = Vector2(120, 92)
	title.add_theme_font_size_override("font_size", 24)
	title.add_theme_color_override("font_color", Color("#fff1b8"))
	add_child(title)

func build_dark_winter() -> void:
	platform(Rect2(0, 550, 780, 170), Color("#8eb9ca"))
	platform(Rect2(920, 470, 360, 250), Color("#a4cbd8"))
	platform(Rect2(1420, 380, 420, 340), Color("#8eb9ca"))
	platform(Rect2(1990, 520, 440, 200), Color("#a4cbd8"))
	platform(Rect2(2600, 420, 430, 300), Color("#8eb9ca"))
	platform(Rect2(3200, 540, 450, 180), Color("#a4cbd8"))
	platform(Rect2(3820, 400, 430, 320), Color("#8eb9ca"))
	platform(Rect2(4420, 510, 1290, 210), Color("#a4cbd8"))
	moving_platform(Vector2(800, 360), Vector2(180, 0), 2.8); moving_platform(Vector2(3070, 330), Vector2(0, 150), 2.5)
	collectible(Vector2(1600, 320), "key"); collectible(Vector2(2190, 460), "survivor"); collectible(Vector2(4000, 340), "survivor")
	enemy(Vector2(1150, 410), 120, "eagle"); enemy(Vector2(3420, 480), 130, "wolf")
	checkpoint(Vector2(2650, 350)); finalize_stage()

func build_bear_country() -> void:
	platform(Rect2(0, 540, 860, 180), Color("#eef6f7"))
	slope(PackedVector2Array([Vector2(860,540),Vector2(1320,360),Vector2(1320,720),Vector2(860,720)]), Color("#dcebed"))
	platform(Rect2(1320, 360, 560, 360), Color("#eef6f7"))
	platform(Rect2(2040, 500, 520, 220), Color("#dcebed"))
	platform(Rect2(2730, 400, 620, 320), Color("#eef6f7"))
	platform(Rect2(3520, 520, 580, 200), Color("#dcebed"))
	platform(Rect2(4280, 470, 1430, 250), Color("#eef6f7"))
	launch_pad(Vector2(2160, 490)); trick_ring_line(Vector2(2250, 400)); moving_platform(Vector2(3380, 350), Vector2(220, 0), 2.4)
	collectible(Vector2(1520, 300), "key"); collectible(Vector2(2320, 440), "survivor"); collectible(Vector2(3800, 460), "survivor")
	enemy(Vector2(1780, 300), 170, "bear"); enemy(Vector2(3050, 340), 180, "bear"); enemy(Vector2(4560, 410), 150, "wolf")
	checkpoint(Vector2(2780, 330)); finalize_stage()

func finalize_stage() -> void:
	boss(Vector2(5160, 448))
	goal(Vector2(5480, 450))
	var title := Label.new()
	title.text = "%s · KEY · 2 RESCUES · %s" % [GameSession.STAGES[stage_index].name, GameSession.STAGES[stage_index].boss]
	title.position = Vector2(120, 92)
	title.add_theme_font_size_override("font_size", 24)
	title.add_theme_color_override("font_color", Color("#fff1b8"))
	add_child(title)

func build_route_branches() -> void:
	# HIGH: moving-platform chain with the best reward line and aerial exposure.
	platform(Rect2(1080, 295, 150, 24), Color("#dff8fb"))
	moving_platform(Vector2(1370, 255), Vector2(190, -70), 2.4)
	platform(Rect2(1650, 235, 170, 24), Color("#dff8fb"))
	for point in [Vector2(1140,250), Vector2(1430,205), Vector2(1700,190)]:
		collectible(point, "coin")

	# PRECISION: brittle ice is a usable bridge or a player-created drop route.
	var ice := ReactiveIce.new()
	ice.position = Vector2(2890, 390)
	ice.size = Vector2(220, 26)
	ice.shattered.connect(func(_at): checkpoint_label.text = "ICE ROUTE SHATTERED")
	add_child(ice)
	collectible(Vector2(2890, 340), "coin")
	var water := FreezableWater.new()
	water.position = Vector2(2145, 535)
	water.size = Vector2(105, 42)
	add_child(water)
	route_sign(Vector2(2070, 485), "FREEZE · JUMP · FALL")

	# LOW: safer cave line, but a bear owns the exit and must be outplayed.
	platform(Rect2(3320, 650, 620, 70), Color("#8cb9c4"))
	platform(Rect2(3450, 595, 145, 24), Color("#b8dce3"))
	enemy(Vector2(3820, 610), 115, "bear")
	collectible(Vector2(3500, 550), "coin")

	# Reunion: routes converge through moving footing before the final approach.
	moving_platform(Vector2(4300, 360), Vector2(0, 150), 2.1)
	platform(Rect2(4480, 300, 190, 24), Color("#dff8fb"))
	collectible(Vector2(4550, 255), "coin")
	route_sign(Vector2(980, 330), "HIGH · SPEED")
	route_sign(Vector2(2750, 440), "PRECISION · BREAKABLE")
	route_sign(Vector2(3300, 625), "LOW · WILDLIFE")

func route_sign(at: Vector2, message: String) -> void:
	var label := Label.new()
	label.position = at
	label.text = message
	label.add_theme_font_size_override("font_size", 17)
	label.add_theme_color_override("font_color", Color("#ffda79"))
	add_child(label)

func platform(rect: Rect2, color: Color) -> void:
	var body := StaticBody2D.new()
	body.position = rect.position
	register_debug_item(body, "PF", "platform")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = rect.size
	collision.shape = shape
	collision.position = rect.size * 0.5
	body.add_child(collision)
	if rect.size.y <= 30.0:
		var art := Sprite2D.new()
		art.texture = load("res://assets/route_platform_ice.png")
		art.position = Vector2(rect.size.x * 0.5, 14)
		art.scale = Vector2(rect.size.x / art.texture.get_width(), 0.16)
		body.add_child(art)
	else:
		var art := Polygon2D.new()
		art.polygon = PackedVector2Array([Vector2.ZERO,Vector2(rect.size.x,0),rect.size,Vector2(0,rect.size.y)])
		art.color = color
		body.add_child(art)
	add_child(body)

func slope(points: PackedVector2Array, color: Color) -> void:
	var body := StaticBody2D.new()
	var collision := CollisionPolygon2D.new()
	collision.polygon = points
	body.add_child(collision)
	var art := Polygon2D.new()
	art.polygon = points
	art.color = color
	body.add_child(art)
	add_child(body)

func enemy(at: Vector2, distance: float, kind: String) -> void:
	var foe := TrailEnemy.new()
	foe.position = at
	foe.patrol_distance = distance
	foe.kind = kind
	register_debug_item(foe, "AN", kind)
	add_child(foe)

func boss(at: Vector2) -> void:
	var encounter := TrailBoss.new()
	encounter.position = at
	encounter.max_health = [8, 8, 10, 12, 16][stage_index]
	encounter.boss_name = GameSession.STAGES[stage_index].boss
	encounter.boss_variant = stage_index
	register_debug_item(encounter, "BOSS", encounter.boss_name)
	encounter.defeated.connect(_on_boss_defeated)
	encounter.feedback.connect(func(message): checkpoint_label.text = message; FeedbackService.cue(message))
	add_child(encounter)

func _on_boss_defeated() -> void:
	boss_defeated = true
	checkpoint_label.text = "BOSS DEFEATED · REACH THE BEACON"
	player.chain_action(100)

func moving_platform(at: Vector2, travel: Vector2, seconds: float) -> void:
	var mover := MovingTrailPlatform.new()
	mover.position = at
	mover.travel = travel
	mover.cycle_seconds = seconds
	register_debug_item(mover, "PF", "moving platform")
	add_child(mover)

func launch_pad(at: Vector2) -> void:
	var pad := AuroraLaunchPad.new()
	pad.position = at
	register_debug_item(pad, "PD", "launch pad")
	add_child(pad)

func supply_block(at: Vector2) -> void:
	var block := AuroraSupplyBlock.new()
	block.position = at
	register_debug_item(block, "BL", "supply block")
	block.opened.connect(_on_supply_block_opened)
	add_child(block)

func trick_ring_line(at: Vector2) -> void:
	var heights := [0.0, -46.0, -72.0, -42.0]
	for index in range(heights.size()):
		var ring := AuroraTrickRing.new()
		ring.position = at + Vector2(index * 54.0, heights[index])
		register_debug_item(ring, "RG", "aurora ring")
		add_child(ring)

func _on_supply_block_opened(at: Vector2) -> void:
	checkpoint_label.text = "SECRET CACHE · REWARD ARC"
	for offset in [-46.0, 0.0, 46.0]:
		collectible(at + Vector2(offset, -52.0 - absf(offset) * 0.22), "coin")

func collectible(at: Vector2, kind: String) -> void:
	var item := Area2D.new()
	item.position = at
	item.set_meta("kind", kind)
	register_debug_item(item, "PU", kind)
	var collision := CollisionShape2D.new()
	var shape := CircleShape2D.new()
	shape.radius = 18
	collision.shape = shape
	item.add_child(collision)
	var art := Polygon2D.new()
	art.polygon = PackedVector2Array([Vector2(0,-18),Vector2(15,-6),Vector2(10,15),Vector2(-10,15),Vector2(-15,-6)])
	art.color = Color("#ffda79") if kind == "coin" else Color("#ff6254") if kind == "survivor" else Color("#4ddbb8")
	item.add_child(art)
	item.body_entered.connect(func(body): collect(body, item))
	add_child(item)

func collect(body: Node, item: Area2D) -> void:
	if body != player: return
	if item.get_meta("kind") == "key":
		key_collected = true
		player.chain_action(40)
		checkpoint_label.text = "RESCUE KEY FOUND"
	elif item.get_meta("kind") == "survivor":
		survivors_found += 1
		player.chain_action(60)
		checkpoint_label.text = "SURVIVOR %d/2" % survivors_found
	else:
		player.coins += 1
		player.chain_action(12)
		checkpoint_label.text = "TRAIL COMBO x%d" % player.combo
	item.queue_free()

func checkpoint(at: Vector2) -> void:
	var zone := Area2D.new()
	zone.position = at
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(80, 170)
	collision.shape = shape
	zone.add_child(collision)
	zone.body_entered.connect(func(body):
		if body == player: player.set_checkpoint(at + Vector2(0,-30)))
	add_child(zone)
	var marker := Polygon2D.new()
	marker.position = at
	marker.polygon = PackedVector2Array([Vector2(-6,40),Vector2(6,40),Vector2(6,-90),Vector2(50,-65),Vector2(6,-40)])
	marker.color = Color("#4ddbb8")
	add_child(marker)

func goal(at: Vector2) -> void:
	var zone := Area2D.new()
	zone.position = at
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(130, 200)
	collision.shape = shape
	zone.add_child(collision)
	zone.body_entered.connect(func(body): finish_level(body))
	add_child(zone)
	var beacon := Polygon2D.new()
	beacon.position = at
	beacon.polygon = PackedVector2Array([Vector2(-24,90),Vector2(24,90),Vector2(14,-70),Vector2(-14,-70)])
	beacon.color = Color("#ff6254")
	add_child(beacon)

func finish_level(body: Node) -> void:
	if body != player or finished: return
	if not key_collected or survivors_found < 2 or not boss_defeated:
		checkpoint_label.text = "NEED KEY · %d RESCUES · BOSS %s" % [2 - survivors_found, "DONE" if boss_defeated else "ALIVE"]
		return
	finished = true
	checkpoint_label.text = "LEVEL CLEAR · CHUGACH RESCUE COMPLETE"
	player.velocity = Vector2.ZERO
	best_score = maxi(best_score, player.score)
	save_profile()
	stage_completed.emit(stage_index, player.score)

func spawn_snowball(origin: Vector2, direction: float) -> void:
	var shot := SnowballProjectile.new()
	add_child(shot)
	shot.setup(origin, direction)

func build_hud() -> void:
	var layer := CanvasLayer.new()
	layer.layer = 10
	add_child(layer)
	hud_label = Label.new()
	hud_label.position = Vector2(24, 20)
	hud_label.add_theme_font_size_override("font_size", 27 if GameSession.large_text else 22)
	hud_label.add_theme_color_override("font_color", Color.WHITE)
	layer.add_child(hud_label)
	checkpoint_label = Label.new()
	checkpoint_label.position = Vector2(410, 22)
	checkpoint_label.size = Vector2(620, 40)
	checkpoint_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	checkpoint_label.add_theme_font_size_override("font_size", 27 if GameSession.large_text else 22)
	checkpoint_label.add_theme_color_override("font_color", Color("#ffda79"))
	layer.add_child(checkpoint_label)
	var menu_button := Button.new()
	menu_button.text = "MAP"
	menu_button.position = Vector2(1064, 12)
	menu_button.size = Vector2(92, 48)
	menu_button.process_mode = Node.PROCESS_MODE_ALWAYS
	menu_button.pressed.connect(func(): get_tree().paused = false; exit_requested.emit())
	layer.add_child(menu_button)

func _process(_delta: float) -> void:
	if Input.is_action_just_pressed("debug_note") and notebook: notebook.toggle()
	if Input.is_action_just_pressed("debug_ids"):
		debug_ids_visible = not debug_ids_visible
		for item in get_tree().get_nodes_in_group("debug_item"):
			var label := item.get_node_or_null("DebugId")
			if label: label.visible = debug_ids_visible
	if player:
		var route := "HIGH" if player.global_position.y < 350.0 else "LOW" if player.global_position.y > 575.0 else "PRECISION"
		hud_label.text = "HP %d  SCORE %d  BEST %d  KEY %s  RESCUE %d/2  COMBO %d  %s  %s" % [player.health, player.score, best_score, "YES" if key_collected else "NO", survivors_found, player.combo, player.state.to_upper(), route]

func register_debug_item(item: Node, prefix: String, label: String) -> void:
	debug_item_counter += 1
	item.add_to_group("debug_item")
	var short_stage := String(GameSession.STAGES[stage_index].name).replace(" ", "-")
	item.set_meta("debug_id", "%s-%s-%d" % [short_stage, prefix, debug_item_counter])
	item.set_meta("debug_label", label.to_upper())
	if item is Node2D:
		var badge := Label.new()
		badge.name = "DebugId"
		badge.text = String(item.get_meta("debug_id"))
		badge.position = Vector2(-48, -82)
		badge.add_theme_font_size_override("font_size", 12)
		badge.add_theme_color_override("font_color", Color("#ffda79"))
		badge.visible = debug_ids_visible
		item.add_child(badge)

func debug_note_context() -> String:
	var visible: Array[String] = []
	for item in get_tree().get_nodes_in_group("debug_item"):
		if item is Node2D and absf(item.global_position.x - player.global_position.x) <= 700.0:
			visible.append(String(item.get_meta("debug_id", "UNSET")))
	return "stage=%s | x=%d | score=%d | combo=%d | key=%s | rescues=%d/2 | visible=%s" % [GameSession.STAGES[stage_index].name, player.global_position.x, player.score, player.combo, key_collected, survivors_found, ", ".join(visible)]

func load_profile() -> void:
	best_score = GameSession.best_scores[stage_index]

func save_profile() -> void:
	GameSession.complete_stage(stage_index, best_score)
