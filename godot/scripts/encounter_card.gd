class_name TrailEncounterCard
extends RefCounted

enum Route { GROUND, PRECISION, HIGH }

var id: StringName
var min_stage: int
var min_gates: int
var route: Route
var threat_budget: int
var star_count: int
var flow_variant: bool
var hazards: Array[StringName]

func _init(card_id: StringName, stage: int, gates: int, route_type: Route,
		budget: int, stars: int, flow_only: bool, hazard_list: Array[StringName]) -> void:
	id = card_id
	min_stage = stage
	min_gates = gates
	route = route_type
	threat_budget = budget
	star_count = stars
	flow_variant = flow_only
	hazards = hazard_list

func supports(stage: int, gates: int, flow_active: bool, budget: int) -> bool:
	return stage >= min_stage and gates >= min_gates and threat_budget <= budget \
		and (not flow_variant or flow_active)

