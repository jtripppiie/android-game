class_name ReviewRegistry
extends RefCounted

const MAX_VISIBLE_BADGES := 4
const BADGE_RADIUS := 660.0

var stage: Node2D
var stage_index := 0
var counters := {}
var ids_visible := false


func configure(owner: Node2D, index: int, initially_visible: bool) -> void:
	stage = owner
	stage_index = index
	ids_visible = initially_visible


func register(item: Node, prefix: String, description: String) -> void:
	counters[prefix] = int(counters.get(prefix, 0)) + 1
	item.add_to_group("debug_item")
	var identifier := "S%d-%s%02d" % [stage_index + 1, prefix, counters[prefix]]
	item.set_meta("debug_id", identifier)
	item.set_meta("debug_label", description.to_upper())
	if not item is Node2D:
		return
	var badge := Label.new()
	badge.name = "DebugId"
	badge.text = "%s · %s" % [identifier, description.to_upper()]
	badge.position = Vector2(-120, -62)
	badge.size = Vector2(240, 30)
	badge.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	badge.add_theme_font_size_override("font_size", 16)
	badge.add_theme_color_override("font_color", Color("#ffda79"))
	badge.add_theme_constant_override("outline_size", 5)
	badge.add_theme_color_override("font_outline_color", Color(0.02, 0.06, 0.10, 0.95))
	badge.visible = ids_visible
	item.add_child(badge)


func toggle() -> bool:
	ids_visible = not ids_visible
	if not ids_visible:
		hide_all()
	return ids_visible


func hide_all() -> void:
	if not is_instance_valid(stage):
		return
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		var badge := item.get_node_or_null("DebugId")
		if badge:
			badge.visible = false


func update(player: Node2D) -> void:
	if not is_instance_valid(stage) or not is_instance_valid(player):
		return
	var candidates: Array[Dictionary] = []
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		if not item is Node2D:
			continue
		var badge := item.get_node_or_null("DebugId") as Label
		if badge:
			badge.visible = false
			badge.modulate = Color.WHITE
			badge.scale = Vector2.ONE
			position_surface_badge(item, badge, player)
		var distance := distance_to_player(item, player)
		if distance <= BADGE_RADIUS:
			candidates.append({"item": item, "distance": distance})
	if not ids_visible:
		return
	candidates.sort_custom(func(a: Dictionary, b: Dictionary) -> bool:
		return float(a.distance) < float(b.distance)
	)
	for index in range(mini(MAX_VISIBLE_BADGES, candidates.size())):
		var candidate_item := candidates[index].item as Node2D
		var candidate_badge := candidate_item.get_node_or_null("DebugId") as Label
		if candidate_badge:
			candidate_badge.visible = true
			if index == 0:
				candidate_badge.modulate = Color("#4ddbb8")
				candidate_badge.scale = Vector2.ONE * 1.08


func context(player: AlaskaRunner, key_collected: bool, rescues: int) -> String:
	if not is_instance_valid(stage) or not is_instance_valid(player):
		return "stage unavailable"
	var visible: Array[String] = []
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		if item is Node2D and distance_to_player(item, player) <= BADGE_RADIUS:
			visible.append(String(item.get_meta("debug_id", "UNSET")))
	visible.sort()
	return "stage=%s | x=%d | y=%d | score=%d | combo=%d | key=%s | rescues=%d/2 | nearby=%s" % [
		GameSession.STAGES[stage_index].name,
		roundi(player.global_position.x),
		roundi(player.global_position.y),
		player.score,
		player.combo,
		key_collected,
		rescues,
		", ".join(visible)
	]


func nearest(player: Node2D) -> String:
	if not is_instance_valid(stage) or not is_instance_valid(player):
		return "NO TARGET"
	var closest_item: Node2D
	var closest_distance := INF
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		if item is Node2D:
			var distance := distance_to_player(item, player)
			if distance < closest_distance:
				closest_distance = distance
				closest_item = item
	if not is_instance_valid(closest_item):
		return "NO ITEM NEARBY"
	return "%s · %s · %dm" % [
		String(closest_item.get_meta("debug_id", "UNSET")),
		String(closest_item.get_meta("debug_label", "ITEM")),
		roundi(closest_distance / 100.0)
	]


func audit(player: Node2D) -> void:
	ids_visible = true
	update(player)
	var visible_badges := 0
	var followed_surface := false
	for item in stage.get_tree().get_nodes_in_group("debug_item"):
		if not item is Node2D:
			continue
		var badge := item.get_node_or_null("DebugId") as Label
		if badge == null:
			continue
		assert(badge.get_theme_font_size("font_size") >= 16)
		assert(String(item.get_meta("debug_id", "")).begins_with("S%d-" % (stage_index + 1)))
		if badge.visible:
			visible_badges += 1
		if item.has_meta("surface_start"):
			var start := float(item.get_meta("surface_start"))
			var finish := float(item.get_meta("surface_end"))
			if player.global_position.x >= start and player.global_position.x <= finish:
				var badge_world_center: float = item.global_position.x + badge.position.x + badge.size.x * 0.5
				assert(absf(badge_world_center - player.global_position.x) <= 1.0)
				followed_surface = true
	assert(visible_badges > 0 and visible_badges <= MAX_VISIBLE_BADGES)
	assert(followed_surface)
	ids_visible = false
	update(player)


func position_surface_badge(item: Node2D, badge: Label, player: Node2D) -> void:
	if not item.has_meta("surface_start"):
		return
	var nearest_world_x := clampf(
		player.global_position.x,
		float(item.get_meta("surface_start")),
		float(item.get_meta("surface_end"))
	)
	badge.position = Vector2(
		nearest_world_x - item.global_position.x - badge.size.x * 0.5,
		float(item.get_meta("surface_y")) - item.global_position.y - 62.0
	)


func distance_to_player(item: Node2D, player: Node2D) -> float:
	if item.has_meta("surface_start"):
		var nearest_x := clampf(
			player.global_position.x,
			float(item.get_meta("surface_start")),
			float(item.get_meta("surface_end"))
		)
		return player.global_position.distance_to(Vector2(nearest_x, float(item.get_meta("surface_y"))))
	return item.global_position.distance_to(player.global_position)
