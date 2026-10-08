extends RefCounted
## Pure, deterministic race calculations. Kept apart from input and drawing for tests.

const TRACK_NAMES := [
    "DESERT DASH", "SUNSET RUN", "FOREST RUN",
    "CANYON KICK", "MOON RIDGE", "FINAL LAP"
]
const TRACK_LENGTHS := [2100.0, 2450.0, 2800.0, 3200.0, 3550.0, 4000.0]
const TRACK_SEEDS := [4102, 9811, 5062, 7733, 6629, 8471]
const TRACK_COUNT := 6
const HEAT_MAX := 100.0
const HEAT_UNLOCK := 34.0
const LANE_COUNT := 4

static func lane_y(index: float) -> float:
    return 110.0 + 33.0 * clampf(index, 0.0, 3.0)

static func target_speed(horizontal_input: float, boost: bool, heat_locked: bool, slow: bool) -> float:
    var goal := 125.0 + 30.0 * clampf(horizontal_input, -1.0, 1.0)
    if slow:
        goal *= 0.63
    if boost and not heat_locked:
        goal += 93.0
    return goal

static func heat_step(heat: float, is_boosting: bool, dt: float) -> float:
    return clampf(heat + (38.0 if is_boosting else -24.0) * maxf(dt, 0.0), 0.0, HEAT_MAX)

static func medal_for(time: float, stage: int, crashes: int) -> String:
    var target: float = TRACK_LENGTHS[clampi(stage, 0, TRACK_COUNT - 1)] / 172.0
    if time <= target and crashes == 0:
        return "GOLD"
    if time <= target * 1.22:
        return "SILVER"
    if time <= target * 1.5:
        return "BRONZE"
    return "FINISH"

static func generate_features(stage: int) -> Array[Dictionary]:
    var selected := clampi(stage, 0, TRACK_COUNT - 1)
    var rng := RandomNumberGenerator.new()
    rng.seed = TRACK_SEEDS[selected]
    var result: Array[Dictionary] = []
    var x := 340.0
    var count := 0
    while x < TRACK_LENGTHS[selected] - 110.0:
        var lane := rng.randi_range(0, LANE_COUNT - 1)
        var type_id := rng.randi_range(0, 9)
        var kind := "ramp" if type_id < 4 else ("rock" if type_id < 8 else "mud")
        result.append({"x": x, "lane": lane, "kind": kind, "used": false})
        # Later tracks get denser, but never fill all four lanes at the same X.
        x += float(rng.randi_range(95, 150)) - float(selected * 4)
        count += 1
        if count % 5 == 0 and selected >= 2:
            # A second feature in a different lane gives a little slalom challenge.
            var second_lane := (lane + rng.randi_range(1, 3)) % LANE_COUNT
            result.append({"x": x - 44.0, "lane": second_lane, "kind": "rock", "used": false})
    return result
