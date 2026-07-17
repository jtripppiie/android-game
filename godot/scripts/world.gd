class_name AlaskaStage
extends Node2D

const PLAYER_SCENE := preload("res://scenes/player.tscn")

signal stage_completed(stage: int, score: int, elapsed_seconds: float, damage_taken: int)
signal exit_requested
signal restart_requested(stage: int)
signal advance_requested(stage: int)

var player: AlaskaRunner
var hud: AlaskaGameHud
var pause_panel: PanelContainer
var key_collected := false
var finished := false
var survivors_found := 0
var boss_defeated := false
var boss_node: TrailBoss
var best_score := 0
var notebook: ReviewNotebook
var enemy_spawn_positions: Array[Vector2] = []
var stage_index := 0
var review_registry: ReviewRegistry
var auditor: GameplayAuditor
var game_over_overlay: GameOverOverlay
var stage_complete_overlay: StageCompleteOverlay
var goal_beacon: Sprite2D
var goal_status_label: Label
var run_elapsed := 0.0
var damage_taken := 0

func _ready() -> void:
	# Main handles pause/back input in ALWAYS mode. Gameplay must remain
	# explicitly pausable or every child inherits processing while paused.
	process_mode = Node.PROCESS_MODE_PAUSABLE
	add_to_group("active_stage")
	stage_index = GameSession.selected_stage
	review_registry = ReviewRegistry.new()
	review_registry.configure(self, stage_index, GameSession.review_mode)
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
	notebook.nearest_id_provider = nearest_debug_id
	note_layer.add_child(notebook)
	auditor = GameplayAuditor.new()
	add_child(auditor)
	auditor.configure(self)

func spawn_player() -> void:
	player = PLAYER_SCENE.instantiate() as AlaskaRunner
	player.position = Vector2(190, 470)
	player.fired.connect(spawn_snowball)
	player.checkpoint_reached.connect(func(_point): announce("CHECKPOINT SAVED", 2))
	player.action_feedback.connect(func(message): announce(message, feedback_priority(message)))
	player.defeated.connect(show_game_over)
	player.damaged.connect(func(_health_remaining): damage_taken += 1)
	add_child(player)

func build_background() -> void:
	var skies := [Color("#7b4f80"), Color("#247a86"), Color("#163d62"), Color("#071326"), Color("#29485c")]
	var sky: Color = skies[stage_index]
	if GameSession.high_contrast: sky = Color("#00152b")
	var winter := stage_index >= 3
	var backdrop_texture: Texture2D = load("res://assets/%s" % ("background_dark_winter.png" if winter else "background_midnight_sun.png"))
	var far_parallax := Parallax2D.new()
	far_parallax.name = "FarMountainParallax"
	far_parallax.scroll_scale = Vector2(0.16, 0.08)
	far_parallax.z_index = -20
	add_child(far_parallax)
	for index in range(5):
		var backdrop := Sprite2D.new()
		backdrop.texture = backdrop_texture
		backdrop.position = Vector2(640.0 + index * 1280.0, 360.0)
		backdrop.scale = Vector2(1280.0 / backdrop_texture.get_width(), 720.0 / backdrop_texture.get_height())
		if GameSession.high_contrast:
			backdrop.modulate = Color(0.58, 0.72, 0.84) if winter else Color(0.72, 0.80, 0.84)
		else:
			backdrop.modulate = Color(0.70, 0.82, 0.92) if stage_index == 3 else Color(0.86, 0.92, 1.0) if stage_index == 4 else Color.WHITE
		far_parallax.add_child(backdrop)
	var sky_node := Polygon2D.new()
	sky_node.polygon = PackedVector2Array([Vector2(-500,-500),Vector2(6500,-500),Vector2(6500,720),Vector2(-500,720)])
	sky_node.color = sky
	sky_node.z_index = -30
	add_child(sky_node)
	var tree_texture: Texture2D = load("res://assets/%s" % ("scenery_tree_winter.png" if stage_index >= 2 else "scenery_tree_summer.png"))
	var tree_parallax := Parallax2D.new()
	tree_parallax.name = "TreeLineParallax"
	tree_parallax.scroll_scale = Vector2(0.56, 0.32)
	tree_parallax.z_index = -6
	add_child(tree_parallax)
	for index in range(10):
		var tree := Sprite2D.new()
		tree.texture = tree_texture
		tree.position = Vector2(420.0 + index * 590.0, 430.0 + (index % 3) * 18.0)
		tree.scale = Vector2.ONE * (0.12 + (index % 2) * 0.018)
		tree.modulate = Color(0.62, 0.74, 0.80, 0.78)
		tree_parallax.add_child(tree)

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
	platform(Rect2(2880, 430, 600, 290), Color("#f6e0a4"))
	platform(Rect2(3600, 520, 720, 200), Color("#f0d58d"))
	platform(Rect2(4440, 500, 1270, 220), Color("#f6e0a4"))
	moving_platform(Vector2(930, 390), Vector2(0, -100), 3.0)
	launch_pad(Vector2(2320, 510)); trick_ring_line(Vector2(2400, 420))
	supply_block(Vector2(3780, 468))
	collectible(Vector2(1180, 440), "key"); collectible(Vector2(1900, 390), "survivor"); collectible(Vector2(4020, 460), "survivor")
	enemy(Vector2(1400, 450), 90, "wolf"); enemy(Vector2(3300, 370), 130, "wolf")
	checkpoint(Vector2(2980, 370)); finalize_stage()

func build_salmon_rush() -> void:
	platform(Rect2(0, 540, 760, 180), Color("#b8e2ce"))
	platform(Rect2(900, 500, 500, 220), Color("#c9ead8"))
	platform(Rect2(1580, 540, 520, 180), Color("#b8e2ce"))
	platform(Rect2(2210, 470, 650, 250), Color("#c9ead8"))
	platform(Rect2(3040, 540, 520, 180), Color("#b8e2ce"))
	platform(Rect2(3740, 460, 540, 260), Color("#c9ead8"))
	platform(Rect2(4460, 520, 1250, 200), Color("#b8e2ce"))
	for x in [820.0, 1480.0, 2180.0, 2940.0, 3640.0]:
		var water := FreezableWater.new(); water.position = Vector2(x, 550); water.size = Vector2(120, 48); register_debug_item(water, "WT", "river"); add_child(water)
	collectible(Vector2(1240, 440), "key"); collectible(Vector2(2500, 410), "survivor"); collectible(Vector2(3980, 400), "survivor")
	enemy(Vector2(1800, 480), 100, "salmon"); enemy(Vector2(3400, 480), 100, "salmon")
	checkpoint(Vector2(3140, 480)); finalize_stage()

func build_moose_pass() -> void:
	platform(Rect2(0, 540, 900, 180), Color("#e8f4f5"))
	slope(PackedVector2Array([Vector2(900,540),Vector2(1260,420),Vector2(1260,720),Vector2(900,720)]), Color("#d9ecef"))
	platform(Rect2(1260, 420, 460, 300), Color("#e8f4f5"))
	platform(Rect2(1810, 505, 290, 215), Color("#cce4e8"))
	moving_platform(Vector2(1770, 390), Vector2(230, -80), 3.2)
	platform(Rect2(2140, 430, 590, 290), Color("#e8f4f5"))
	slope(PackedVector2Array([Vector2(2730,430),Vector2(3230,560),Vector2(3230,720),Vector2(2730,720)]), Color("#d9ecef"))
	platform(Rect2(3230, 560, 520, 160), Color("#e8f4f5"))
	platform(Rect2(3860, 460, 300, 260), Color("#cce4e8"))
	moving_platform(Vector2(3760, 330), Vector2(280, 0), 2.6)
	platform(Rect2(4260, 520, 1450, 200), Color("#e8f4f5"))
	for data in [[740,465],[1440,345],[1980,430],[2470,355],[3420,485],[3990,385]]:
		collectible(Vector2(data[0],data[1]), "coin")
	collectible(Vector2(2590, 350), "key")
	collectible(Vector2(1990, 370), "survivor")
	collectible(Vector2(4050, 390), "survivor")
	checkpoint(Vector2(3350, 500))
	enemy(Vector2(1520, 360), 120, "wolf")
	enemy(Vector2(2240, 370), 80, "bear")
	enemy(Vector2(3630, 500), 80, "wolf")
	boss(Vector2(5160, 448))
	goal(Vector2(5480, 450))
	build_route_branches()
	# This vertical slice is deliberately authored. The old directed layer
	# stacked extra geometry and enemies over the same spaces.
	announce("MOOSE PASS · FIND KEY · RESCUE 2 · REACH BOSS", 2, 3.2)

func build_dark_winter() -> void:
	platform(Rect2(0, 550, 780, 170), Color("#8eb9ca"))
	platform(Rect2(860, 470, 420, 250), Color("#a4cbd8"))
	platform(Rect2(1420, 380, 420, 340), Color("#8eb9ca"))
	platform(Rect2(1990, 520, 440, 200), Color("#a4cbd8"))
	platform(Rect2(2600, 420, 430, 300), Color("#8eb9ca"))
	# The former 170 px convergence gap punished a checkpoint respawn that
	# arrived while the vertical mover was out of phase. Keep the mover as the
	# fast line, but make the 140 px ground transfer independently reliable.
	platform(Rect2(3170, 540, 480, 180), Color("#a4cbd8"))
	platform(Rect2(3820, 400, 430, 320), Color("#8eb9ca"))
	platform(Rect2(4420, 510, 1290, 210), Color("#a4cbd8"))
	moving_platform(Vector2(800, 360), Vector2(180, 0), 2.8); moving_platform(Vector2(3070, 330), Vector2(0, 150), 2.5)
	collectible(Vector2(1600, 320), "key"); collectible(Vector2(2190, 460), "survivor"); collectible(Vector2(4000, 340), "survivor")
	enemy(Vector2(3420, 480), 130, "wolf")
	checkpoint(Vector2(2720, 350)); finalize_stage()

func build_bear_country() -> void:
	platform(Rect2(0, 540, 860, 180), Color("#eef6f7"))
	slope(PackedVector2Array([Vector2(860,540),Vector2(1320,360),Vector2(1320,720),Vector2(860,720)]), Color("#dcebed"))
	platform(Rect2(1320, 360, 560, 360), Color("#eef6f7"))
	platform(Rect2(2040, 500, 520, 220), Color("#dcebed"))
	platform(Rect2(2660, 400, 690, 320), Color("#eef6f7"))
	platform(Rect2(3520, 520, 580, 200), Color("#dcebed"))
	platform(Rect2(4280, 470, 1430, 250), Color("#eef6f7"))
	launch_pad(Vector2(2160, 490)); trick_ring_line(Vector2(2250, 400)); moving_platform(Vector2(3380, 350), Vector2(220, 0), 2.4)
	supply_block(Vector2(3680, 468))
	collectible(Vector2(1450, 300), "key"); collectible(Vector2(2320, 440), "survivor"); collectible(Vector2(3800, 460), "survivor")
	enemy(Vector2(1740, 300), 90, "bear"); enemy(Vector2(3100, 340), 140, "bear")
	checkpoint(Vector2(2760, 330)); finalize_stage()

func finalize_stage() -> void:
	boss(Vector2(5160, 448))
	goal(Vector2(5480, 450))
	announce("%s · FIND KEY · RESCUE 2 · REACH BOSS" % GameSession.STAGES[stage_index].name, 2, 3.2)

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
	ice.shattered.connect(func(_at): announce("ICE ROUTE SHATTERED", 2))
	register_debug_item(ice, "IC", "breakable ice")
	add_child(ice)
	collectible(Vector2(2890, 340), "coin")
	var water := FreezableWater.new()
	water.position = Vector2(2145, 535)
	water.size = Vector2(105, 42)
	register_debug_item(water, "WT", "freezable water")
	add_child(water)
	route_sign(Vector2(2070, 485), "FREEZE · JUMP · FALL")

	# LOW: safer cave line. Stage wildlife already guards the convergence; do
	# not add another bear here or two large silhouettes stack in one view.
	platform(Rect2(3320, 650, 620, 70), Color("#8cb9c4"))
	platform(Rect2(3450, 595, 145, 24), Color("#b8dce3"))
	collectible(Vector2(3500, 550), "coin")

	# Reunion: routes converge through moving footing before the final approach.
	moving_platform(Vector2(4300, 360), Vector2(0, 150), 2.1)
	platform(Rect2(4480, 300, 190, 24), Color("#dff8fb"))
	collectible(Vector2(4550, 255), "coin")
	route_sign(Vector2(980, 330), "HIGH · SPEED")
	route_sign(Vector2(2750, 440), "PRECISION · BREAKABLE")
	route_sign(Vector2(3300, 625), "LOW · WILDLIFE")

func route_sign(at: Vector2, message: String) -> void:
	var panel := PanelContainer.new()
	panel.position = at
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.02, 0.08, 0.13, 0.88)
	style.border_color = Color("#4ddbb8")
	style.set_border_width_all(2)
	style.set_corner_radius_all(8)
	style.content_margin_left = 10
	style.content_margin_right = 10
	style.content_margin_top = 5
	style.content_margin_bottom = 5
	panel.add_theme_stylebox_override("panel", style)
	var label := Label.new()
	label.text = message
	label.add_theme_font_size_override("font_size", 17)
	label.add_theme_color_override("font_color", Color("#ffda79"))
	panel.add_child(label)
	add_child(panel)

func platform(rect: Rect2, color: Color) -> void:
	var body := StaticBody2D.new()
	body.position = rect.position
	if rect.size.y >= 100.0:
		body.add_to_group("main_route_surface")
		body.set_meta("surface_start", rect.position.x)
		body.set_meta("surface_end", rect.end.x)
		body.set_meta("surface_y", rect.position.y)
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
		build_snow_terrain(body, rect.size, color)
	add_child(body)

func build_snow_terrain(body: StaticBody2D, size: Vector2, stage_tint: Color) -> void:
	# The former terrain was a flat color rectangle with a 49 px snow strip
	# pasted across it. Use a complete snow/ice/rock cross-section so the whole
	# visible platform communicates natural arctic terrain.
	var source: Texture2D = load("res://assets/route_terrain_snow_v2.png")
	var cropped := AtlasTexture.new()
	cropped.atlas = source
	# Remove transparent generation padding while retaining the uneven snowline.
	cropped.region = Rect2(0, 72, 2176, 596)
	var terrain := TextureRect.new()
	terrain.texture = cropped
	terrain.position = Vector2.ZERO
	terrain.size = size
	terrain.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
	terrain.stretch_mode = TextureRect.STRETCH_SCALE
	terrain.mouse_filter = Control.MOUSE_FILTER_IGNORE
	terrain.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
	# Keep stages visually related without repainting the terrain as a flat slab.
	terrain.modulate = Color.WHITE.lerp(stage_tint, 0.08)
	body.add_child(terrain)
	add_platform_crest(body, size.x)


func add_platform_crest(body: StaticBody2D, width: float) -> void:
	# Extend a few pixels beyond each independent terrain rectangle. The soft
	# shadow and cap bridge the abrupt UV boundary where authored surfaces meet
	# without weakening the collision edge or hiding a real gap.
	var shadow := Line2D.new()
	shadow.name = "SnowCrestShadow"
	shadow.points = PackedVector2Array([Vector2(-6, 7), Vector2(width + 6, 7)])
	shadow.width = 15.0
	shadow.default_color = Color(0.28, 0.57, 0.70, 0.40)
	shadow.antialiased = true
	body.add_child(shadow)
	var highlight := Line2D.new()
	highlight.name = "SnowCrestHighlight"
	highlight.points = PackedVector2Array([Vector2(-6, 2), Vector2(width + 6, 2)])
	highlight.width = 7.0
	highlight.default_color = Color(0.96, 0.995, 1.0, 0.72)
	highlight.antialiased = true
	body.add_child(highlight)

func slope(points: PackedVector2Array, color: Color) -> void:
	var body := StaticBody2D.new()
	body.add_to_group("main_route_surface")
	var minimum := points[0]
	var maximum := points[0]
	for point in points:
		minimum = Vector2(minf(minimum.x, point.x), minf(minimum.y, point.y))
		maximum = Vector2(maxf(maximum.x, point.x), maxf(maximum.y, point.y))
	body.set_meta("surface_start", minimum.x)
	body.set_meta("surface_end", maximum.x)
	body.set_meta("surface_y", minimum.y)
	body.position = minimum
	var local_points := PackedVector2Array()
	for point in points:
		local_points.append(point - minimum)
	register_debug_item(body, "PF", "slope")
	var collision := CollisionPolygon2D.new()
	collision.polygon = local_points
	body.add_child(collision)
	build_snow_slope(body, local_points, maximum - minimum, color)
	add_child(body)

func build_snow_slope(body: StaticBody2D, points: PackedVector2Array, bounds: Vector2, stage_tint: Color) -> void:
	var source: Texture2D = load("res://assets/route_terrain_snow_v2.png")
	var cropped := AtlasTexture.new()
	cropped.atlas = source
	cropped.region = Rect2(0, 72, 2176, 596)
	var art := Polygon2D.new()
	art.polygon = points
	art.texture = cropped
	var safe_bounds := Vector2(maxf(bounds.x, 1.0), maxf(bounds.y, 1.0))
	var texture_size := cropped.get_size()
	var mapped_uv := PackedVector2Array()
	for point in points:
		mapped_uv.append(Vector2(point.x / safe_bounds.x * texture_size.x, point.y / safe_bounds.y * texture_size.y))
	art.uv = mapped_uv
	art.color = Color.WHITE.lerp(stage_tint, 0.08)
	body.add_child(art)
	# The first polygon edge is authored as the walkable incline. A soft dark
	# under-edge plus bright crest keeps its collision line readable without
	# reverting to a debug-looking geometric border.
	var crest_shadow := Line2D.new()
	crest_shadow.points = PackedVector2Array([points[0], points[1]])
	crest_shadow.width = 13.0
	crest_shadow.default_color = Color(0.36, 0.64, 0.78, 0.72)
	crest_shadow.antialiased = true
	body.add_child(crest_shadow)
	var crest := Line2D.new()
	crest.points = PackedVector2Array([points[0], points[1]])
	crest.width = 8.0
	crest.default_color = Color(0.95, 0.99, 1.0, 0.96)
	crest.antialiased = true
	body.add_child(crest)

func enemy(at: Vector2, distance: float, kind: String) -> void:
	for existing in enemy_spawn_positions:
		if existing.distance_to(at) < 420.0:
			push_error("REFUSED STACKED WILDLIFE · %s at %s near %s" % [kind, at, existing])
			return
	enemy_spawn_positions.append(at)
	var foe := TrailEnemy.new()
	foe.position = at
	foe.patrol_distance = distance
	foe.kind = kind
	register_debug_item(foe, "AN", kind)
	add_child(foe)

func boss(at: Vector2) -> void:
	# Keep the checkpoint and ordinary wildlife out of the boss attack envelope.
	# The old -360 position could respawn directly inside the polar bear lunge.
	checkpoint(at + Vector2(-620, 0))
	var encounter := TrailBoss.new()
	encounter.add_to_group("stage_boss")
	boss_node = encounter
	encounter.position = at
	encounter.max_health = [6, 6, 7, 7, 9][stage_index]
	encounter.boss_name = GameSession.STAGES[stage_index].boss
	encounter.boss_variant = stage_index
	register_debug_item(encounter, "BOSS", encounter.boss_name)
	encounter.defeated.connect(_on_boss_defeated)
	encounter.feedback.connect(func(message): announce(message, feedback_priority(message)))
	add_child(encounter)

func _on_boss_defeated() -> void:
	boss_defeated = true
	announce("BOSS DEFEATED · REACH THE BEACON", 4, 3.2)
	player.chain_action(100)
	for hazard in get_tree().get_nodes_in_group("boss_hazard"):
		hazard.queue_free()

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
	announce("SECRET CACHE · REWARD ARC", 2)
	call_deferred("spawn_supply_reward_arc", at)

func spawn_supply_reward_arc(at: Vector2) -> void:
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
	var art := Sprite2D.new()
	var atlas := AtlasTexture.new()
	atlas.atlas = load("res://assets/collectibles_atlas.png")
	var atlas_index := 0 if kind == "coin" else 2 if kind == "survivor" else 1
	var cell_width := atlas.atlas.get_width() / 3.0
	atlas.region = Rect2(cell_width * atlas_index, 0, cell_width, atlas.atlas.get_height())
	art.texture = atlas
	art.scale = Vector2.ONE * (0.115 if kind == "survivor" else 0.10)
	item.add_child(art)
	var float_tween := item.create_tween().set_loops()
	float_tween.tween_property(art, "position:y", -7.0, 0.55).set_trans(Tween.TRANS_SINE)
	float_tween.tween_property(art, "position:y", 2.0, 0.55).set_trans(Tween.TRANS_SINE)
	item.body_entered.connect(func(body): collect(body, item))
	add_child(item)

func collect(body: Node, item: Area2D) -> void:
	if body != player or bool(item.get_meta("collected", false)): return
	item.set_meta("collected", true)
	if item.get_meta("kind") == "key":
		key_collected = true
		player.chain_action(40)
		announce("KEY FOUND · +40", 3)
	elif item.get_meta("kind") == "survivor":
		survivors_found += 1
		player.chain_action(60)
		announce("RESCUE SIGNAL %d/2 · +60" % survivors_found, 3)
	else:
		player.coins += 1
		player.chain_action(12)
		announce("AURORA +12 · COMBO x%d" % player.combo, 1, 1.4)
	item.queue_free()

func checkpoint(at: Vector2) -> void:
	var zone := Area2D.new()
	zone.add_to_group("stage_checkpoint")
	zone.position = at
	# A checkpoint is progression insurance, not a precision collectible.
	# Span the playable height so launch-pad arcs and high routes cannot sail
	# over the trigger and silently respawn the player at the stage entrance.
	register_debug_item(zone, "CP", "checkpoint")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(96, 1100)
	collision.shape = shape
	collision.position.y = 360.0 - at.y
	zone.add_child(collision)
	zone.body_entered.connect(func(body):
		# Authored checkpoint markers sit roughly 60–70 px above their support.
		# Respawn just above that support instead of dropping from the old
		# airborne -30 offset, which could carry held input into the next gap.
		if body == player: player.set_checkpoint(at + Vector2(0, 50)))
	add_child(zone)
	var marker := atlas_object_sprite(0, 0.24)
	marker.position = at + Vector2(0, -38)
	add_child(marker)

func goal(at: Vector2) -> void:
	var zone := Area2D.new()
	zone.add_to_group("stage_goal")
	zone.position = at
	register_debug_item(zone, "GO", "finish beacon")
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = Vector2(150, 360)
	collision.shape = shape
	zone.add_child(collision)
	zone.body_entered.connect(func(body): finish_level(body))
	add_child(zone)
	var beacon := atlas_object_sprite(1, 0.30)
	beacon.position = at + Vector2(0, -24)
	add_child(beacon)
	goal_beacon = beacon
	goal_status_label = Label.new()
	goal_status_label.position = at + Vector2(-110, -156)
	goal_status_label.size = Vector2(220, 34)
	goal_status_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	goal_status_label.add_theme_font_size_override("font_size", 19)
	goal_status_label.add_theme_constant_override("outline_size", 5)
	goal_status_label.add_theme_color_override("font_outline_color", Color("#071326"))
	add_child(goal_status_label)
	update_goal_presentation()

func atlas_object_sprite(index: int, display_scale: float) -> Sprite2D:
	var sprite := Sprite2D.new()
	var atlas := AtlasTexture.new()
	atlas.atlas = load("res://assets/trail_objects_atlas.png")
	var cell_width := atlas.atlas.get_width() / 3.0
	atlas.region = Rect2(cell_width * index, 0, cell_width, atlas.atlas.get_height())
	sprite.texture = atlas
	sprite.scale = Vector2.ONE * display_scale
	return sprite

func finish_level(body: Node) -> void:
	if body != player or finished: return
	if not key_collected or survivors_found < 2 or not boss_defeated:
		var missing: Array[String] = []
		if not key_collected:
			missing.append("KEY")
		if survivors_found < 2:
			missing.append("%d RESCUE%s" % [2 - survivors_found, "" if survivors_found == 1 else "S"])
		if not boss_defeated:
			missing.append("BOSS")
		announce("EXIT LOCKED · NEED " + " · ".join(missing), 3)
		return
	finished = true
	announce("LEVEL CLEAR · EXPEDITION COMPLETE", 5, 3.5)
	player.velocity = Vector2.ZERO
	var previous_best := best_score
	best_score = maxi(best_score, player.score)
	if is_instance_valid(auditor) and auditor.autoplay:
		auditor.complete(player.score)
		return
	stage_completed.emit(stage_index, player.score, run_elapsed, damage_taken)
	show_stage_complete(previous_best)

func spawn_snowball(origin: Vector2, direction: float) -> void:
	var shot := SnowballProjectile.new()
	add_child(shot)
	shot.setup(origin, direction)

func build_hud() -> void:
	hud = AlaskaGameHud.new()
	hud.pause_requested.connect(toggle_pause_panel)
	add_child(hud)
	build_pause_panel(hud)

func build_pause_panel(layer: CanvasLayer) -> void:
	pause_panel = PanelContainer.new()
	pause_panel.set_anchors_preset(Control.PRESET_TOP_RIGHT)
	pause_panel.offset_left = -372.0
	pause_panel.offset_top = 88.0
	pause_panel.offset_right = -24.0
	pause_panel.offset_bottom = 250.0
	pause_panel.visible = false
	pause_panel.process_mode = Node.PROCESS_MODE_ALWAYS
	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.02, 0.07, 0.12, 0.97)
	style.border_color = Color("#84d5e8")
	style.set_border_width_all(3)
	style.set_corner_radius_all(16)
	pause_panel.add_theme_stylebox_override("panel", style)
	layer.add_child(pause_panel)
	var rows := VBoxContainer.new()
	rows.add_theme_constant_override("separation", 10)
	pause_panel.add_child(rows)
	var title := Label.new()
	title.text = "RUN PAUSED"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 22)
	title.add_theme_color_override("font_color", Color("#fff0a8"))
	rows.add_child(title)
	var actions := HBoxContainer.new()
	actions.add_theme_constant_override("separation", 10)
	rows.add_child(actions)
	for spec in [["RESUME", resume_run], ["EXIT TO MAP", exit_run_to_map]]:
		var button := Button.new()
		button.text = spec[0]
		button.custom_minimum_size = Vector2(150, 58)
		button.add_theme_font_size_override("font_size", 18)
		button.pressed.connect(spec[1])
		actions.add_child(button)

func toggle_pause_panel() -> void:
	if pause_panel.visible: resume_run()
	else:
		release_gameplay_inputs()
		pause_panel.visible = true
		get_tree().paused = true

func resume_run() -> void:
	pause_panel.visible = false
	get_tree().paused = false

func exit_run_to_map() -> void:
	pause_panel.visible = false
	get_tree().paused = false
	exit_requested.emit()

func pause_for_background() -> void:
	release_gameplay_inputs()
	if is_instance_valid(notebook) and notebook.panel.visible:
		get_tree().paused = true
		return
	pause_panel.visible = true
	get_tree().paused = true


func show_game_over() -> void:
	if is_instance_valid(game_over_overlay):
		return
	release_gameplay_inputs()
	player.set_controls_enabled(false)
	game_over_overlay = GameOverOverlay.new()
	game_over_overlay.restart_requested.connect(func() -> void:
		get_tree().paused = false
		restart_requested.emit(stage_index)
	)
	game_over_overlay.map_requested.connect(func() -> void:
		get_tree().paused = false
		exit_requested.emit()
	)
	hud.add_child(game_over_overlay)
	get_tree().paused = true


func show_stage_complete(previous_best: int) -> void:
	if is_instance_valid(stage_complete_overlay):
		return
	release_gameplay_inputs()
	player.set_controls_enabled(false)
	stage_complete_overlay = StageCompleteOverlay.new()
	hud.add_child(stage_complete_overlay)
	stage_complete_overlay.configure(
		stage_index,
		player.score,
		previous_best,
		run_elapsed,
		damage_taken
	)
	stage_complete_overlay.next_requested.connect(func() -> void:
		get_tree().paused = false
		if stage_index >= GameSession.STAGES.size() - 1:
			exit_requested.emit()
		else:
			advance_requested.emit(stage_index + 1)
	)
	stage_complete_overlay.replay_requested.connect(func() -> void:
		get_tree().paused = false
		restart_requested.emit(stage_index)
	)
	stage_complete_overlay.map_requested.connect(func() -> void:
		get_tree().paused = false
		exit_requested.emit()
	)
	get_tree().paused = true

func _process(delta: float) -> void:
	if not finished and is_instance_valid(player) and player.controls_enabled:
		run_elapsed += delta
	if Input.is_action_just_pressed("debug_note") and notebook and GameSession.review_mode and not pause_panel.visible:
		notebook.toggle()
		if notebook.panel.visible: release_gameplay_inputs()
	if Input.is_action_just_pressed("debug_ids") and GameSession.review_mode:
		review_registry.toggle()
	if GameSession.review_mode:
		review_registry.update(player)
	update_goal_presentation()
	if player and is_instance_valid(hud):
		var route := "HIGH" if player.global_position.y < 350.0 else "LOW" if player.global_position.y > 575.0 else "PRECISION"
		hud.update_snapshot(
			player.health,
			player.score,
			best_score,
			player.coins,
			player.combo,
			player.state,
			route,
			key_collected,
			survivors_found,
			boss_defeated,
			player.global_position.x
		)


func update_goal_presentation() -> void:
	if not is_instance_valid(goal_beacon) or not is_instance_valid(goal_status_label):
		return
	var ready := key_collected and survivors_found >= 2 and boss_defeated
	if ready:
		var pulse := (
			1.0
			if GameSession.reduced_motion
			else 0.94 + sin(Time.get_ticks_msec() * 0.006) * 0.06
		)
		goal_beacon.modulate = Color(1.08, 1.02, 0.72, 1.0) * pulse
		goal_status_label.text = "EXIT READY"
		goal_status_label.add_theme_color_override("font_color", Color("#ffda79"))
	else:
		goal_beacon.modulate = Color(0.48, 0.63, 0.69, 0.72)
		goal_status_label.text = "LOCKED · COMPLETE OBJECTIVES"
		goal_status_label.add_theme_color_override("font_color", Color("#9fb9c2"))

func announce(message: String, priority := 1, seconds := 2.4) -> void:
	if is_instance_valid(hud): hud.post_message(message, priority, seconds)
	FeedbackService.cue(message)

func feedback_priority(message: String) -> int:
	if "DEFEATED" in message or "LEVEL CLEAR" in message: return 5
	if "HIT" in message or "ROUTE LOST" in message: return 4
	if "KEY" in message or "RESCUE" in message or "CHECKPOINT" in message: return 3
	if "BOSS" in message or "WEAK" in message or "ARMORED" in message: return 2
	return 1

func release_gameplay_inputs() -> void:
	for controls in get_tree().get_nodes_in_group("touch_controls"):
		controls.release_all_touches()
	for action in ["move_left", "move_right", "sprint", "jump", "crouch", "fire", "dash", "debug_note", "debug_ids"]:
		Input.action_release(action)

func register_debug_item(item: Node, prefix: String, label: String) -> void:
	review_registry.register(item, prefix, label)


func update_debug_labels() -> void:
	review_registry.update(player)


func audit_debug_overlay() -> void:
	review_registry.audit(player)


func debug_note_context() -> String:
	return review_registry.context(player, key_collected, survivors_found)


func nearest_debug_id() -> String:
	return review_registry.nearest(player)


func load_profile() -> void:
	best_score = GameSession.best_scores[stage_index]
