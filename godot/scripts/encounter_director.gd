class_name TrailEncounterDirector
extends RefCounted

var rng := RandomNumberGenerator.new()
var previous_id: StringName = &""
var previous_route := -1
var cards: Array[TrailEncounterCard] = []

func _init(seed: int) -> void:
	rng.seed = seed
	cards = [
		TrailEncounterCard.new(&"clean_launch", 0, 0, TrailEncounterCard.Route.PRECISION, 2, 3, false, [&"stage"]),
		TrailEncounterCard.new(&"ground_wildlife", 0, 2, TrailEncounterCard.Route.GROUND, 3, 2, false, [&"stage"]),
		TrailEncounterCard.new(&"jump_then_hunt", 0, 3, TrailEncounterCard.Route.PRECISION, 4, 3, false, [&"stage", &"wolf"]),
		TrailEncounterCard.new(&"eagle_crossfire", 3, 3, TrailEncounterCard.Route.PRECISION, 5, 3, false, [&"eagle", &"wolf"]),
		TrailEncounterCard.new(&"dark_sky_chain", 3, 6, TrailEncounterCard.Route.HIGH, 7, 5, false, [&"eagle", &"wolf", &"eagle"]),
		TrailEncounterCard.new(&"bear_commitment", 4, 3, TrailEncounterCard.Route.GROUND, 6, 2, false, [&"bear", &"wolf"]),
		TrailEncounterCard.new(&"aurora_overdrive", 1, 3, TrailEncounterCard.Route.HIGH, 8, 6, true, [&"stage", &"wolf", &"stage"])
	]

func next(stage: int, gates: int, flow_active: bool) -> TrailEncounterCard:
	var budget := threat_budget(stage, gates, flow_active)
	var candidates: Array[TrailEncounterCard] = []
	for card in cards:
		if card.supports(stage, gates, flow_active, budget) and card.id != previous_id:
			candidates.append(card)
	if candidates.is_empty():
		candidates.append(cards[0])
	var route_changes: Array[TrailEncounterCard] = candidates.filter(
		func(card: TrailEncounterCard) -> bool: return card.route != previous_route)
	var pool := route_changes if not route_changes.is_empty() else candidates
	var selected: TrailEncounterCard = pool[rng.randi_range(0, pool.size() - 1)]
	previous_id = selected.id
	previous_route = selected.route
	return selected

static func threat_budget(stage: int, gates: int, flow_active: bool) -> int:
	var budget := 3 + mini(2, stage / 2) + mini(2, gates / 4)
	return mini(8, budget + (2 if flow_active else 0))

