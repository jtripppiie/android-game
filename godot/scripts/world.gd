extends Node2D

const LEVEL_END := 5600.0
var player: AlaskaRunner
var hud_label: Label
var checkpoint_label: Label
var key_collected := false
var finished := false
var survivors_found := 0

func _ready() -> void:
	RenderingServer.set_default_clear_color(Color("#102d4b"))
	build_background()
	build_level()
	spawn_player()
	build_hud()
	var touch_layer := CanvasLayer.new()
	touch_layer.layer = 20
	add_child(touch_layer)
	touch_layer.add_child(TouchControls.new())

func spawn_player() -> void:
	player = AlaskaRunner.new()
	player.position = Vector2(190, 470)
	player.fired.connect(spawn_snowball)
	player.checkpoint_reached.connect(func(_point): checkpoint_label.text = "CHECKPOINT SAVED")
	player.action_feedback.connect(func(message): checkpoint_label.text = message)
	add_child(player)

func build_background() -> void:
	var sky := Polygon2D.new()
	sky.polygon = PackedVector2Array([Vector2(-500,-500),Vector2(6500,-500),Vector2(6500,720),Vector2(-500,720)])
	sky.color = Color("#163d62")
	sky.z_index = -10
	add_child(sky)
	for i in range(12):
		var mountain := Polygon2D.new()
		var x := float(i * 560 - 300)
		mountain.polygon = PackedVector2Array([Vector2(x,560),Vector2(x+280,170+(i%3)*55),Vector2(x+600,560)])
		mountain.color = Color("#285574") if i % 2 == 0 else Color("#376984")
		mountain.z_index = -8
		add_child(mountain)

func build_level() -> void:
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
	goal(Vector2(5480, 450))
	var title := Label.new()
	title.text = "CHUGACH RUN · FIND THE KEY · REACH THE RESCUE BEACON"
	title.position = Vector2(120, 92)
	title.add_theme_font_size_override("font_size", 24)
	title.add_theme_color_override("font_color", Color("#fff1b8"))
	add_child(title)

func platform(rect: Rect2, color: Color) -> void:
	var body := StaticBody2D.new()
	body.position = rect.position
	var collision := CollisionShape2D.new()
	var shape := RectangleShape2D.new()
	shape.size = rect.size
	collision.shape = shape
	collision.position = rect.size * 0.5
	body.add_child(collision)
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
	add_child(foe)

func moving_platform(at: Vector2, travel: Vector2, seconds: float) -> void:
	var mover := MovingTrailPlatform.new()
	mover.position = at
	mover.travel = travel
	mover.cycle_seconds = seconds
	add_child(mover)

func collectible(at: Vector2, kind: String) -> void:
	var item := Area2D.new()
	item.position = at
	item.set_meta("kind", kind)
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
		checkpoint_label.text = "RESCUE KEY FOUND"
	elif item.get_meta("kind") == "survivor":
		survivors_found += 1
		player.combo += 2
		checkpoint_label.text = "SURVIVOR %d/2" % survivors_found
	else:
		player.coins += 1
		player.combo += 1
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
	if not key_collected or survivors_found < 2:
		checkpoint_label.text = "NEED KEY + %d SURVIVORS" % (2 - survivors_found)
		return
	finished = true
	checkpoint_label.text = "LEVEL CLEAR · CHUGACH RESCUE COMPLETE"
	player.velocity = Vector2.ZERO

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
	hud_label.add_theme_font_size_override("font_size", 22)
	hud_label.add_theme_color_override("font_color", Color.WHITE)
	layer.add_child(hud_label)
	checkpoint_label = Label.new()
	checkpoint_label.position = Vector2(410, 22)
	checkpoint_label.size = Vector2(620, 40)
	checkpoint_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	checkpoint_label.add_theme_font_size_override("font_size", 22)
	checkpoint_label.add_theme_color_override("font_color", Color("#ffda79"))
	layer.add_child(checkpoint_label)

func _process(_delta: float) -> void:
	if player:
		hud_label.text = "HP %d  COINS %d  KEY %s  RESCUE %d/2  COMBO %d  %s" % [player.health, player.coins, "YES" if key_collected else "NO", survivors_found, player.combo, player.state.to_upper()]
