extends RefCounted
## Standalone, configurable long-jump event. Never changes campaign progress.
const TAKEOFF := 900.0
const BUS_SPACING := 76.0
const SPEEDS := [450.0,600.0,780.0]
const HEIGHTS := [60.0,90.0,120.0]
const RAMPS := ["stunt_ramp_low","stunt_ramp_mid","stunt_ramp_high"]

static func build(bus_count: int, ramp: int) -> Array[Dictionary]:
    var result: Array[Dictionary]=[{"x":TAKEOFF,"lane":2,"kind":RAMPS[clampi(ramp,0,2)],"used":false}]
    for i in range(clampi(bus_count,1,20)):
        result.append({"x":TAKEOFF+80+i*BUS_SPACING,"lane":2,"kind":"bus","used":false})
    return result

static func end_of_buses(bus_count: int) -> float:
    return TAKEOFF+80+(clampi(bus_count,1,20)-1)*BUS_SPACING+36

static func length(bus_count: int) -> float:
    return end_of_buses(bus_count)+1000
