extends Node

const STAGES := [
	{"name":"MIDNIGHT SUN RUN", "line":"Vault driftwood before the eclipse.", "boss":"MIDNIGHT SUN"},
	{"name":"SALMON RUSH", "line":"Cross rivers and rescue the salmon route.", "boss":"SALMON BOSS"},
	{"name":"MOOSE PASS", "line":"Climb antler ridges through Chugach.", "boss":"MOOSE BOSS"},
	{"name":"DARK WINTER", "line":"Navigate readable ice routes in low light.", "boss":"EAGLE BOSS"},
	{"name":"BEAR COUNTRY", "line":"Master the final snowfield expedition.", "boss":"POLAR BEAR"}
]

var selected_stage := 0
var unlocked_stage := 0
var best_scores := [0, 0, 0, 0, 0]
var total_score := 0
var muted := false
var haptics := true
var large_text := false
var reduced_motion := false
var high_contrast := false
var review_mode := false
var photo_path := ""
var legacy_imported := false

func _ready() -> void:
	load_profile()

func complete_stage(stage: int, score: int) -> void:
	best_scores[stage] = maxi(best_scores[stage], score)
	total_score += maxi(0, score)
	unlocked_stage = maxi(unlocked_stage, mini(STAGES.size() - 1, stage + 1))
	save_profile()

func load_profile() -> void:
	var config := ConfigFile.new()
	if config.load("user://profile.cfg") != OK: return
	unlocked_stage = clampi(int(config.get_value("campaign", "unlocked_stage", 0)), 0, STAGES.size() - 1)
	total_score = int(config.get_value("campaign", "total_score", 0))
	muted = bool(config.get_value("accessibility", "muted", false))
	haptics = bool(config.get_value("accessibility", "haptics", true))
	large_text = bool(config.get_value("accessibility", "large_text", false))
	reduced_motion = bool(config.get_value("accessibility", "reduced_motion", false))
	high_contrast = bool(config.get_value("accessibility", "high_contrast", false))
	review_mode = bool(config.get_value("review", "enabled", false))
	photo_path = String(config.get_value("customization", "photo_path", ""))
	legacy_imported = bool(config.get_value("migration", "java_profile_imported", false))
	for index in range(STAGES.size()): best_scores[index] = int(config.get_value("scores", str(index), 0))

func save_profile() -> void:
	var config := ConfigFile.new()
	config.set_value("campaign", "unlocked_stage", unlocked_stage)
	config.set_value("campaign", "total_score", total_score)
	config.set_value("accessibility", "muted", muted)
	config.set_value("accessibility", "haptics", haptics)
	config.set_value("accessibility", "large_text", large_text)
	config.set_value("accessibility", "reduced_motion", reduced_motion)
	config.set_value("accessibility", "high_contrast", high_contrast)
	config.set_value("review", "enabled", review_mode)
	config.set_value("customization", "photo_path", photo_path)
	config.set_value("migration", "java_profile_imported", legacy_imported)
	for index in range(STAGES.size()): config.set_value("scores", str(index), best_scores[index])
	config.save("user://profile.cfg")

func import_legacy_profile(json_text: String) -> void:
	if legacy_imported or json_text.is_empty() or json_text == "{}": return
	var parsed = JSON.parse_string(json_text)
	if not parsed is Dictionary: return
	var old: Dictionary = parsed
	unlocked_stage = maxi(unlocked_stage, clampi(int(old.get("unlocked_stage", 0)), 0, STAGES.size() - 1))
	selected_stage = clampi(int(old.get("selected_stage", selected_stage)), 0, unlocked_stage)
	best_scores[0] = maxi(best_scores[0], int(old.get("best_score", 0)))
	total_score = maxi(total_score, int(old.get("xp", 0)))
	muted = bool(old.get("muted", muted))
	legacy_imported = true
	save_profile()
