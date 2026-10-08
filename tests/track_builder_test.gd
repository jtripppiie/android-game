extends SceneTree
const Builder = preload("res://scripts/track_builder.gd")

func _initialize() -> void:
    var failures := 0
    var items: Array = []
    items = Builder.edit(items, "ramp", 160.0, 1)
    items = Builder.edit(items, "ramp", 320.0, 1)
    items = Builder.edit(items, "big_ramp", 520.0, 1)
    if items.size() != 3:
        push_error("Sequential jumps not retained")
        failures += 1
    var next: Array = Builder.edit(items, "rock", 320.0, 1)
    if next.size() != 3 or str(next[1]["kind"]) != "rock":
        push_error("Replacement failed")
        failures += 1
    next = Builder.edit(next, "erase", 320.0, 1)
    if next.size() != 2:
        push_error("Erase failed")
        failures += 1
    if Builder.edit(next, "mud", -25.0, 1).size() != 2:
        push_error("Start area should be protected")
        failures += 1
    if Builder.edit(next, "mud", 600.0, -1).size() != 2:
        push_error("Invalid lane accepted")
        failures += 1
    var invalid: Array = [
        {"x": 200.0, "lane": 0, "kind": "ramp"},
        {"x": 200.0, "lane": 0, "kind": "rock"},
        {"x": 240.0, "lane": 4, "kind": "mud"},
        {"x": 280.0, "lane": 1, "kind": "unknown"}
    ]
    if Builder.sanitize(invalid).size() != 1:
        push_error("Invalid saved items survived validation")
        failures += 1
    var playable := Builder.for_race(items)
    if playable.size() != 3 or not playable[0].has("used"):
        push_error("Custom race export failed")
        failures += 1
    print("Track builder: ", "PASS" if failures == 0 else "FAIL (%d)" % failures)
    quit(0 if failures == 0 else 1)
