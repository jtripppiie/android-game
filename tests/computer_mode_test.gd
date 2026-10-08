extends SceneTree

const Game = preload("res://scripts/game.gd")

func _initialize() -> void:
    call_deferred("_run")

func _run() -> void:
    var game = Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    game.unlocked = 0
    game.best_times.fill(0.0)
    # Exercise the actual menu button and the full race physics on every course.
    game._click_menu(Vector2(240, 191))
    assert(game.computer_mode and game.mode == "race")
    for stage in range(Game.Rules.TRACK_COUNT):
        assert(game.stage == stage)
        assert(game.distance == 0.0 and game.lane_position == 2.0)
        var occupied := [int(game.lane_position)]
        for rival in game.rivals:
            assert(float(rival["x"]) == game.distance, "All riders must share the starting line")
            assert(not int(rival["lane"]) in occupied, "Each rider needs a separate starting lane")
            occupied.append(int(rival["lane"]))
        game._physics_process(1.0)
        assert(game.distance == 0.0, "Player must wait for the countdown")
        for rival in game.rivals:
            assert(float(rival["x"]) == 0.0, "Rivals must wait for the countdown")
        var frames := 0
        var boosted := false
        while game.mode == "race" and frames < 60 * 180:
            game._physics_process(1.0 / 60.0)
            boosted = boosted or game.boost_active
            assert(not game.overheated, "Computer should manage heat")
            frames += 1
        assert(game.mode == "finish", "Computer must finish every track")
        assert(boosted, "Computer should use boost")
        assert(game.unlocked == 0 and game.best_times[stage] == 0.0)
        assert(not game.new_record)
        print("Computer course %d: %.2fs, place %d/4, %d crashes" % [stage + 1, game.elapsed, game.race_place, game.crash_count])
        game._primary_action()
    assert(game.stage == 0 and game.computer_mode)
    var pause := InputEventKey.new()
    pause.keycode = KEY_P
    pause.pressed = true
    game._input(pause)
    assert(game.mode == "pause")
    var paused_distance: float = game.distance
    game._physics_process(1.0)
    assert(game.distance == paused_distance)
    game._input(pause)
    assert(game.mode == "race")
    game.mode = "menu"
    game._primary_action()
    assert(not game.computer_mode and game.mode == "race")
    game.free()
    print("Computer mode tests passed")
    quit()
