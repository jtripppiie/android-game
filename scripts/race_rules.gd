extends RefCounted
## Pure, deterministic race calculations. Kept apart from input and drawing for tests.

const TRACK_NAMES := [
    "DESERT DASH", "SUNSET RUN", "FOREST RUN", "CANYON KICK", "MOON RIDGE", "COAST SPRINT",
    "DUNE RIDER", "EMBER CIRCUIT", "PINE SLALOM", "MESA CROSS", "NIGHT SHIFT", "TIDAL RUN",
    "SANDSTORM", "COPPER RIDGE", "ROOT RALLY", "RIDGE RUNNER", "LUNAR LEAP", "BREAKWATER",
    "DUNE GAUNTLET", "INFERNO GP", "FOREST ELITE", "CANYON CROWN", "MIDNIGHT GP", "DIRT LEGEND"
]
const TRACK_LENGTHS := [4200.0,4600.0,5000.0,5400.0,5800.0,6400.0,
    6800.0,7200.0,7600.0,8000.0,8400.0,9000.0,
    9400.0,9800.0,10200.0,10600.0,11000.0,11600.0,
    12000.0,12400.0,12800.0,13200.0,13800.0,14600.0]
const TRACK_COUNT := 24
const CUP_NAMES := ["ROOKIE CUP","CLUB CUP","PRO CUP","LEGEND CUP"]
const HEAT_MAX := 100.0
const HEAT_UNLOCK := 34.0
const LANE_COUNT := 4
const JUMP_HAZARDS := ["water","river","crocs","lava","car","bus"]

static func hazard_width(kind: String) -> float:
    match kind:
        "water", "car": return 48.0
        "river": return 76.0
        "crocs", "lava": return 60.0
        "bus": return 72.0
    return 0.0

static func hazard_clearance(kind: String) -> float:
    match kind:
        "car": return 18.0
        "bus": return 24.0
        "crocs": return 12.0
    return 8.0

static func lane_y(index: float) -> float:
    return 126.0 + 26.0 * clampf(index, 0.0, 3.0)

static func target_speed(horizontal_input: float, boost: bool, heat_locked: bool, slow: bool) -> float:
    var goal := 125.0 + 30.0 * clampf(horizontal_input, -1.0, 1.0)
    if slow:
        goal *= 0.63
    if boost and not heat_locked:
        goal += 93.0
    return goal

static func heat_step(heat: float, is_boosting: bool, dt: float) -> float:
    return clampf(heat + (38.0 if is_boosting else -18.0) * maxf(dt, 0.0), 0.0, HEAT_MAX)

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
    var selected := clampi(stage,0,TRACK_COUNT-1)
    var tier := selected/6
    var rng := RandomNumberGenerator.new()
    rng.seed=4102+selected*7919
    var result: Array[Dictionary] = []
    var x := 340.0
    var section := 0
    while x < TRACK_LENGTHS[selected]-280:
        # Occasional jump set pieces from track 3 onward; one lane stays open.
        if selected>=2 and section%4==2:
            var safe_lane := (selected+section)%4
            var hazard: String = ["car","lava","crocs","bus","water","river"][selected%6]
            for lane in range(4):
                if lane==safe_lane: continue
                result.append({"x":x,"lane":lane,"kind":"jump_ramp","used":false})
                result.append({"x":x+80,"lane":lane,"kind":hazard,"used":false})
            x+=380.0
            section+=1
            continue
        var pattern := (section+selected+rng.randi_range(0,2)) % (4+tier)
        for lane in range(4):
            var kind := "big_ramp" if pattern==0 else "ramp"
            var shift := 0.0
            match pattern:
                1: shift=float(lane/2)*84
                2:
                    if lane==(section+selected)%4: kind="mud"
                    elif lane==(section+selected+1)%4: kind="boost"
                    else: shift=28
                3: kind="whoops" if lane%2==0 else "ramp"
                4:
                    kind="tabletop" if lane%2==0 else "big_ramp"
                    shift=lane*18
                5:
                    kind="mud" if lane%2==section%2 else "boost"
                6:
                    kind="tabletop" if lane<2 else "whoops"
                    shift=48 if lane>=2 else 0
            result.append({"x":x+shift,"lane":lane,"kind":kind,"used":false})
            if kind=="whoops":
                for gap in [44,88]: result.append({"x":x+shift+gap,"lane":lane,"kind":kind,"used":false})
        # Later cups have shorter recovery straights and alternating route choices.
        x += float(rng.randi_range(310-tier*15,370-tier*12))
        section += 1
    result.sort_custom(func(a: Dictionary,b: Dictionary)->bool:return float(a["x"])<float(b["x"]))
    return result
