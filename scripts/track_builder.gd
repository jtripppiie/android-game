extends RefCounted
## Offline track-builder data model with six saved course slots.
const COURSE_LENGTH := 4000.0
const SLOT_COUNT := 6
const PAGE_STEP := 400.0
const PAGE_COUNT := 10
const CELL := 40.0
const MAX_FEATURES := 200
const Rules = preload("res://scripts/race_rules.gd")
const KINDS := ["ramp", "big_ramp", "rock", "mud", "whoops", "tabletop", "boost", "oil", "jump_ramp", "water", "river", "crocs", "lava", "car", "bus"]
const TOOLS := ["ramp", "big_ramp", "tabletop", "whoops", "mud", "boost", "rock", "oil", "jump_ramp", "water", "river", "crocs", "lava", "car", "bus", "erase"]
const LABELS := ["RAMP", "HIGH", "TABLE", "BUMPS", "MUD", "BOOST", "BLOCK", "OIL", "JUMP", "WATER", "RIVER", "CROCS", "LAVA", "CAR", "BUS", "ERASE"]

static func snap_x(world_x: float) -> float:
    return clampf(roundf(world_x / CELL) * CELL, 120.0, COURSE_LENGTH - 80.0)

static func valid_kind(kind: String) -> bool:
    return kind in KINDS

static func sanitize(raw: Variant) -> Array[Dictionary]:
    var clean: Array[Dictionary] = []
    if not (raw is Array):
        return clean
    for item in raw:
        if not (item is Dictionary):
            continue
        var kind := str(item.get("kind", ""))
        if not valid_kind(kind):
            continue
        var lane := int(item.get("lane", -1))
        var x := float(item.get("x", -1.0))
        if lane < 0 or lane >= 4 or not is_finite(x):
            continue
        if x < 120.0 or x > COURSE_LENGTH - 80.0:
            continue
        var snapped := snap_x(x)
        var already_present := false
        for existing in clean:
            if float(existing["x"]) == snapped and int(existing["lane"]) == lane:
                already_present = true
                break
        if already_present:
            continue
        clean.append({"x": snapped, "lane": lane, "kind": kind})
        if clean.size() >= MAX_FEATURES:
            break
    clean.sort_custom(func(a: Dictionary, b: Dictionary) -> bool:
        return float(a["x"]) < float(b["x"]))
    return clean

static func edit(items: Array, kind: String, world_x: float, lane: int) -> Array[Dictionary]:
    var clean := sanitize(items)
    if lane < 0 or lane >= 4 or not is_finite(world_x):
        return clean
    if world_x < 120.0 or world_x > COURSE_LENGTH - 80.0:
        return clean
    if kind != "erase" and not valid_kind(kind):
        return clean
    var x := snap_x(world_x)
    for i in range(clean.size() - 1, -1, -1):
        if float(clean[i]["x"]) == x and int(clean[i]["lane"]) == lane:
            clean.remove_at(i)
    if kind != "erase" and clean.size() < MAX_FEATURES:
        clean.append({"x": x, "lane": lane, "kind": kind})
    return sanitize(clean)

static func for_race(raw: Variant) -> Array[Dictionary]:
    var result := sanitize(raw)
    var approaches: Array[Dictionary]=[]
    for feature in result:
        if str(feature["kind"]) in Rules.JUMP_HAZARDS:
            var lip := float(feature["x"])-80.0
            var supplied := false
            for other in result:
                if int(other["lane"])==int(feature["lane"]) and absf(float(other["x"])-lip)<1 and str(other["kind"])=="jump_ramp": supplied=true
            if not supplied: approaches.append({"x":lip,"lane":feature["lane"],"kind":"jump_ramp"})
    result.append_array(approaches)
    result.sort_custom(func(a: Dictionary,b: Dictionary)->bool:return float(a["x"])<float(b["x"]))
    for i in range(result.size()):
        result[i]["used"] = false
    return result
