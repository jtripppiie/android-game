extends "res://scripts/game.gd"
# Test-only driver. Never exported with the game.
var computer_lane := 2
var computer_cooling := false
func _start_race() -> void:
    computer_lane = 2
    computer_cooling = false
    super._start_race()
func _race_step(dt: float) -> void:
    touch_vector = _computer_steering()
    if heat >= 85.0: computer_cooling = true
    elif heat <= 30.0: computer_cooling = false
    boost_touch_id = -1 if computer_cooling else 99
    super._race_step(dt)
func _update_ground_stunt(_joystick: Vector2, _dt: float) -> void:
    pass
func _save_progress() -> void:
    pass
func _computer_steering() -> Vector2:
    var best_score := INF
    var chosen := computer_lane
    var lookahead := maxf(180.0, speed * 1.3)
    for lane in range(Rules.LANE_COUNT):
        var score := absf(float(lane) - lane_position) * 0.8
        if lane != computer_lane:
            score += 0.35
        var terrain_gap := absf(Terrain.height_at(features,distance,lane)-Terrain.height_at(features,distance,lane_position))
        score += terrain_gap * 0.5
        for feature in features:
            var ahead := float(feature["x"]) - distance
            if bool(feature["used"]) or ahead < -12.0 or ahead > lookahead:
                continue
            var urgency := 1.0 - maxf(ahead, 0.0) / lookahead
            var feature_lane := float(feature["lane"])
            var kind := str(feature["kind"])
            if int(feature_lane) == lane:
                if kind in ["rock","mud","oil","whoops"] or kind in Rules.JUMP_HAZARDS:
                    score += (12.0 if kind == "rock" else 5.0) * (0.3 + urgency)
                elif ahead > 35.0:
                    score -= 1.8 * urgency
            # Avoid crossing a dangerous lane just as its obstacle reaches the bike.
            if kind == "rock" and ahead < speed * (absf(feature_lane - lane_position) / 2.3 + 0.2):
                if feature_lane >= minf(lane_position, float(lane)) and feature_lane <= maxf(lane_position, float(lane)):
                    score += 8.0
        if score < best_score:
            best_score = score
            chosen = lane
    computer_lane = chosen
    var vertical := clampf((float(chosen) - lane_position) * 4.0 - lane_velocity * 0.5, -1.0, 1.0)
    var horizontal := 1.0
    if airborne:
        var landing_tilt := -atan(Terrain.slope_at(features,distance+speed*0.12,lane_position))
        horizontal = clampf((landing_tilt-bike_tilt) * 5.0, -1.0, 1.0)
    return Vector2(horizontal, vertical)
