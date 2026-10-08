extends SceneTree
const Game=preload("res://scripts/game.gd")

func _initialize() -> void:
    call_deferred("_run")

func _run() -> void:
    var game=Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    game._show_privacy()
    assert(game._active_dialog() != null)
    var enter := InputEventKey.new()
    enter.keycode = KEY_ENTER
    enter.pressed = true
    game._input(enter)
    assert(game.mode == "menu", "Dialog keys must not start a race behind it")
    game._on_pointer_down(Vector2(90,190),1)
    assert(game.mode == "menu", "Dialog touches must not activate the menu behind it")
    game._notification(Node.NOTIFICATION_WM_GO_BACK_REQUEST)
    assert(game._active_dialog() == null and game.mode == "menu", "Android Back closes the dialog first")
    game._primary_action()
    assert(game._boost_state()=="WAIT")
    game.countdown=0
    game.features.clear()
    assert(game._boost_state()=="READY")
    game._on_pointer_down(Game.BOOST_CENTER,7)
    for i in range(170): game._physics_process(1.0/60)
    assert(game.overheated and not game.boost_active)
    assert(game._boost_state()=="COOL")
    game._on_pointer_up(7)
    for i in range(180): game._physics_process(1.0/60)
    assert(game.overheated, "Overheat recovery now takes longer than three seconds")
    for i in range(60): game._physics_process(1.0/60)
    assert(not game.overheated and game._boost_state()=="READY")
    game._on_pointer_down(Game.BOOST_CENTER,8)
    game._physics_process(1.0/60)
    assert(game._boost_state()=="ACTIVE")
    game._notification(Node.NOTIFICATION_APPLICATION_PAUSED)
    assert(game.mode=="pause" and game.boost_touch_id==-1)
    var stopped: float=game.distance
    game._physics_process(1)
    assert(game.distance==stopped)
    game._notification(Node.NOTIFICATION_WM_GO_BACK_REQUEST)
    assert(game.mode=="race" and not game.boost_active)
    # Cross the finish part way through a tick; record only time spent racing.
    game.elapsed=30
    game.distance=game._course_length()-1
    game.speed=125
    game._physics_process(1.0/60)
    assert(game.mode=="finish" and absf(game.elapsed-30.008)<0.0001)
    game._click_menu(Vector2(135,50))
    assert(game.mode=="menu", "Finish screen needs a touch exit")
    game._open_editor()
    game.custom_courses[game.custom_slot]=[]
    game.custom_times[game.custom_slot]=20
    game._builder_place(Vector2(200,126))
    assert(game.custom_times[game.custom_slot]==0, "Changed courses need fresh records")
    game.custom_times[game.custom_slot]=21
    game._builder_undo()
    assert(game.custom_times[game.custom_slot]==0)
    game._builder_test()
    game.mode="finish"
    game._notification(Node.NOTIFICATION_WM_GO_BACK_REQUEST)
    assert(game.mode=="editor", "Custom races should return to the builder")
    game.free()
    print("Release gameplay: boost lifecycle, suspend/back, precise finish, touch exit, custom records PASS")
    quit()
