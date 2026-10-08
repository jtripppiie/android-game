extends SceneTree
const Game=preload("res://scripts/game.gd")

func _initialize() -> void:
    call_deferred("_run")

func _attempt(game, count: int, ramp: int, pace: int) -> void:
    game.stunt_bus_count=count
    game.stunt_ramp_setting=ramp
    game.stunt_speed_setting=pace
    game._start_stunt()
    assert(game.rivals.is_empty() and game.features.size()==count+1)
    for i in range(60*20):
        game._physics_process(1.0/60)
        assert(game.lane_position==2, "The bus line cannot be bypassed")
        if game.mode=="finish": break
    assert(game.mode=="finish", "Every attempt must reach a result")

func _run() -> void:
    var game=Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    game._on_pointer_down(Vector2(135,12),1)
    assert(game.mode=="stunt_setup")
    var unlocked: int=game.unlocked
    var records: Array=game.best_times.duplicate()
    _attempt(game,5,0,0)
    assert(game.stunt_success and game.stunt_cleared==5 and game.stunt_score>500)
    _attempt(game,20,2,2)
    assert(game.stunt_success and game.stunt_cleared==20 and game.stunt_score>2000, "High ramp and maximum run-up must clear twenty buses")
    print("20-bus jump: distance %.1f, score %d" % [game.stunt_jump_distance,game.stunt_score])
    var high_score: int=game.stunt_best_score
    _attempt(game,20,0,0)
    assert(not game.stunt_success and game.stunt_cleared<20 and game.stunt_score==0)
    assert(game.stunt_best_score==high_score)
    assert(game.unlocked==unlocked and game.best_times==records, "Stunt scores must not alter campaign results")
    game._click_menu(Vector2(240,186))
    assert(game.mode=="stunt_setup")
    for i in range(25): game._click_stunt_setup(Vector2(370,106))
    assert(game.stunt_bus_count==20)
    game._start_stunt()
    game._notification(Node.NOTIFICATION_APPLICATION_PAUSED)
    assert(game.mode=="pause")
    game._click_menu(Vector2(240,202))
    assert(game.mode=="stunt_setup")
    game._back_action()
    assert(game.mode=="menu" and not game.stunt_race)
    var loaded=Game.new()
    root.add_child(loaded)
    loaded.set_physics_process(false)
    assert(loaded.stunt_best_score==high_score and loaded.stunt_best_buses==20)
    loaded.free()
    game.free()
    print("Stunt Show: setup, five/twenty-bus success, failed jump, scores, persistence, pause and campaign isolation PASS")
    quit()
