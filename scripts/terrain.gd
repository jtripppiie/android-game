extends RefCounted
## Original height field shared by course drawing, riders, shadows and AI.

static func profile(kind: String) -> Vector4:
    # Approach length, tabletop length, descent length, elevation.
    match kind:
        "stunt_ramp_low": return Vector4(240,0,0,60)
        "stunt_ramp_mid": return Vector4(240,0,0,90)
        "stunt_ramp_high": return Vector4(240,0,0,120)
        "jump_ramp": return Vector4(64, 0, 0, 32)
        "big_ramp": return Vector4(80, 24, 72, 38)
        "tabletop": return Vector4(68, 74, 64, 28)
        "ramp": return Vector4(56, 12, 48, 24)
        "whoops": return Vector4(18, 0, 18, 8)
    return Vector4.ZERO

static func feature_height(feature: Dictionary, x: float) -> float:
    var shape := profile(str(feature["kind"]))
    if shape.w == 0:
        return 0.0
    var dx := x - float(feature["x"])
    if dx < -shape.x or dx > shape.y + shape.z:
        return 0.0
    if dx < 0:
        return shape.w * (1.0 + dx / shape.x)
    if dx <= shape.y:
        return shape.w
    return shape.w * (1.0 - (dx - shape.y) / shape.z)

static func height_at(features: Array[Dictionary], x: float, lane: float) -> float:
    var low := clampi(floori(lane), 0, 3)
    var high := mini(low + 1, 3)
    var h0 := 0.0
    var h1 := 0.0
    for feature in features:
        var row := int(feature["lane"])
        if row == low:
            h0 = maxf(h0, feature_height(feature, x))
        elif row == high:
            h1 = maxf(h1, feature_height(feature, x))
    if high == low:
        return h0
    return lerpf(h0, h1, clampf(lane - low, 0, 1))

static func slope_at(features: Array[Dictionary], x: float, lane: float) -> float:
    return (height_at(features, x + 1, lane) - height_at(features, x - 1, lane)) / 2.0

static func launch_at(features: Array[Dictionary], old_x: float, new_x: float, lane: float) -> float:
    for feature in features:
        if absf(float(feature["lane"]) - lane) > 0.45:
            continue
        var shape := profile(str(feature["kind"]))
        var lip := float(feature["x"])
        if shape.w > 0 and old_x < lip and new_x >= lip:
            if str(feature["kind"])=="stunt_ramp_low": return 200.0
            if str(feature["kind"])=="stunt_ramp_mid": return 270.0
            if str(feature["kind"])=="stunt_ramp_high": return 350.0
            if str(feature["kind"])=="jump_ramp": return 185.0
            return 110.0 if str(feature["kind"]) == "whoops" else (142.0 if shape.w > 30 else 125.0)
    return 0.0

static func build_cache(features: Array[Dictionary], length: float) -> Array[PackedFloat32Array]:
    var result: Array[PackedFloat32Array] = []
    for lane in range(4):
        var samples := PackedFloat32Array()
        samples.resize(ceili(length)+512)
        result.append(samples)
    for feature in features:
        var shape := profile(str(feature["kind"]))
        if shape.w == 0: continue
        var samples := result[int(feature["lane"])]
        var start := maxi(0,floori(float(feature["x"])-shape.x))
        var end := mini(samples.size()-1,ceili(float(feature["x"])+shape.y+shape.z))
        for x in range(start,end+1):
            samples[x] = maxf(samples[x],feature_height(feature,x))
        result[int(feature["lane"])] = samples
    return result

static func cached_height(samples: PackedFloat32Array, x: float) -> float:
    if x < 0 or x >= samples.size()-1: return 0.0
    var index := floori(x)
    return lerpf(samples[index],samples[index+1],x-index)
