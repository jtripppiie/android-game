extends Node

const PROFILE_PATH := "user://profile.cfg"
const PROFILE_BACKUP_PATH := "user://profile-backup.cfg"
const PROFILE_SCHEMA := 3
const APP_VERSION := "5.4.1"
const MAX_PHOTO_BYTES := 20 * 1024 * 1024
const PHOTO_TEXTURE_SIZE := 96

const STAGES := [
	{"name":"MIDNIGHT SUN RUN", "line":"Vault driftwood before the eclipse.", "boss":"MIDNIGHT SUN", "par_score":300, "par_time":45.0},
	{"name":"SALMON RUSH", "line":"Cross rivers and rescue the salmon route.", "boss":"SALMON BOSS", "par_score":340, "par_time":35.0},
	{"name":"MOOSE PASS", "line":"Climb antler ridges through Chugach.", "boss":"MOOSE BOSS", "par_score":650, "par_time":30.0},
	{"name":"DARK WINTER", "line":"Navigate readable ice routes in low light.", "boss":"EAGLE BOSS", "par_score":250, "par_time":60.0},
	{"name":"BEAR COUNTRY", "line":"Master the final snowfield expedition.", "boss":"POLAR BEAR", "par_score":550, "par_time":50.0}
]

var selected_stage := 0
var unlocked_stage := 0
var best_scores := [0, 0, 0, 0, 0]
var completed_runs := [0, 0, 0, 0, 0]
var best_times := [0.0, 0.0, 0.0, 0.0, 0.0]
var best_stars := [0, 0, 0, 0, 0]
var total_score := 0
var muted := false
var haptics := true
var large_text := false
var reduced_motion := false
var high_contrast := false
var review_mode := false
var touch_scale := 1.0
var photo_path := ""
var legacy_imported := false
var cached_photo_path := ""
var cached_photo_texture: Texture2D

func _ready() -> void:
	load_profile()

func complete_stage(stage: int, score: int, elapsed_seconds := 0.0, damage_taken := 0) -> void:
	if stage < 0 or stage >= STAGES.size():
		push_error("Refused invalid stage completion index: %d" % stage)
		return
	best_scores[stage] = maxi(best_scores[stage], score)
	if elapsed_seconds > 0.0:
		var previous_time: float = best_times[stage]
		best_times[stage] = elapsed_seconds if previous_time <= 0.0 else minf(previous_time, elapsed_seconds)
	best_stars[stage] = maxi(best_stars[stage], star_rating(stage, score, elapsed_seconds, damage_taken))
	completed_runs[stage] += 1
	total_score += maxi(0, score)
	unlocked_stage = maxi(unlocked_stage, mini(STAGES.size() - 1, stage + 1))
	save_profile()

func load_profile() -> void:
	var config := ConfigFile.new()
	var load_error := config.load(PROFILE_PATH)
	if load_error != OK:
		load_error = config.load(PROFILE_BACKUP_PATH)
	if load_error != OK:
		return
	unlocked_stage = clampi(int(config.get_value("campaign", "unlocked_stage", 0)), 0, STAGES.size() - 1)
	selected_stage = clampi(int(config.get_value("campaign", "selected_stage", 0)), 0, unlocked_stage)
	total_score = maxi(0, int(config.get_value("campaign", "total_score", 0)))
	muted = bool(config.get_value("accessibility", "muted", false))
	haptics = bool(config.get_value("accessibility", "haptics", true))
	large_text = bool(config.get_value("accessibility", "large_text", false))
	reduced_motion = bool(config.get_value("accessibility", "reduced_motion", false))
	high_contrast = bool(config.get_value("accessibility", "high_contrast", false))
	touch_scale = clampf(float(config.get_value("accessibility", "touch_scale", 1.0)), 0.85, 1.15)
	review_mode = bool(config.get_value("review", "enabled", false))
	photo_path = String(config.get_value("customization", "photo_path", ""))
	invalidate_photo_cache()
	legacy_imported = bool(config.get_value("migration", "java_profile_imported", false))
	for index in range(STAGES.size()):
		best_scores[index] = maxi(0, int(config.get_value("scores", str(index), 0)))
		completed_runs[index] = maxi(0, int(config.get_value("completions", str(index), 0)))
		best_times[index] = maxf(0.0, float(config.get_value("best_times", str(index), 0.0)))
		best_stars[index] = clampi(int(config.get_value("stars", str(index), 0)), 0, 3)

func save_profile() -> void:
	backup_existing_profile()
	var config := ConfigFile.new()
	config.set_value("profile", "schema", PROFILE_SCHEMA)
	config.set_value("campaign", "unlocked_stage", unlocked_stage)
	config.set_value("campaign", "selected_stage", clampi(selected_stage, 0, unlocked_stage))
	config.set_value("campaign", "total_score", total_score)
	config.set_value("accessibility", "muted", muted)
	config.set_value("accessibility", "haptics", haptics)
	config.set_value("accessibility", "large_text", large_text)
	config.set_value("accessibility", "reduced_motion", reduced_motion)
	config.set_value("accessibility", "high_contrast", high_contrast)
	config.set_value("accessibility", "touch_scale", touch_scale)
	config.set_value("review", "enabled", review_mode)
	config.set_value("customization", "photo_path", photo_path)
	config.set_value("migration", "java_profile_imported", legacy_imported)
	for index in range(STAGES.size()):
		config.set_value("scores", str(index), best_scores[index])
		config.set_value("completions", str(index), completed_runs[index])
		config.set_value("best_times", str(index), best_times[index])
		config.set_value("stars", str(index), best_stars[index])
	var error := config.save(PROFILE_PATH)
	if error != OK:
		push_error("Could not save profile.cfg: %s" % error_string(error))


func backup_existing_profile() -> void:
	if not FileAccess.file_exists(PROFILE_PATH):
		return
	var source := FileAccess.open(PROFILE_PATH, FileAccess.READ)
	if source == null:
		return
	var backup := FileAccess.open(PROFILE_BACKUP_PATH, FileAccess.WRITE)
	if backup == null:
		return
	backup.store_string(source.get_as_text())
	backup.flush()

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
	photo_path = String(old.get("player_photo_uri", photo_path))
	invalidate_photo_cache()
	legacy_imported = true
	save_profile()


func set_photo_path(path: String) -> bool:
	if not path.is_empty():
		var source := FileAccess.open(path, FileAccess.READ)
		if source == null or source.get_length() > MAX_PHOTO_BYTES:
			return false
	photo_path = path
	invalidate_photo_cache()
	save_profile()
	return true


func player_photo_texture() -> Texture2D:
	if photo_path.is_empty():
		return null
	if cached_photo_path == photo_path and cached_photo_texture != null:
		return cached_photo_texture
	var source := FileAccess.open(photo_path, FileAccess.READ)
	if source == null or source.get_length() > MAX_PHOTO_BYTES:
		return null
	var image := Image.new()
	if image.load(photo_path) != OK or image.is_empty():
		return null
	var crop_size := mini(image.get_width(), image.get_height())
	if crop_size <= 0:
		return null
	var crop_origin := Vector2i(
		(image.get_width() - crop_size) / 2,
		(image.get_height() - crop_size) / 2
	)
	image = image.get_region(Rect2i(crop_origin, Vector2i(crop_size, crop_size)))
	image.resize(PHOTO_TEXTURE_SIZE, PHOTO_TEXTURE_SIZE, Image.INTERPOLATE_LANCZOS)
	cached_photo_texture = ImageTexture.create_from_image(image)
	cached_photo_path = photo_path
	return cached_photo_texture


func invalidate_photo_cache() -> void:
	cached_photo_path = ""
	cached_photo_texture = null


func star_rating(stage: int, score: int, elapsed_seconds: float, damage_taken: int) -> int:
	if stage < 0 or stage >= STAGES.size():
		return 0
	var stars := 0
	if score >= int(STAGES[stage].par_score):
		stars += 1
	if elapsed_seconds > 0.0 and elapsed_seconds <= float(STAGES[stage].par_time):
		stars += 1
	if damage_taken == 0:
		stars += 1
	return stars
