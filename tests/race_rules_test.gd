extends SceneTree

const Rules = preload("res://scripts/race_rules.gd")

func _initialize() -> void:
    var failures := 0
    for stage in range(Rules.TRACK_COUNT):
        var a := Rules.generate_features(stage)
        var b := Rules.generate_features(stage)
        if a != b:
            push_error("Track generation differs between runs: %d" % stage)
            failures += 1
        if a.is_empty():
            push_error("Track has no obstacles: %d" % stage)
            failures += 1
        for feature in a:
            if feature["lane"] < 0 or feature["lane"] > 3:
                push_error("Invalid lane")
                failures += 1
    if not is_equal_approx(Rules.heat_step(0, true, 1.0), 38.0):
        push_error("Turbo heating failed")
        failures += 1
    if not is_equal_approx(Rules.heat_step(20, false, 1.0), 0.0):
        push_error("Engine cooling failed")
        failures += 1
    if Rules.target_speed(0, true, false, false) <= Rules.target_speed(0, false, false, false):
        push_error("Boost speed failed")
        failures += 1
    if Rules.medal_for(1000, 0, 20) != "FINISH":
        push_error("Medal thresholds failed")
        failures += 1
    print("Race rules: ", "PASS" if failures == 0 else "FAIL (%d)" % failures)
    quit(0 if failures == 0 else 1)
