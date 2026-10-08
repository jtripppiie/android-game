extends SceneTree
const Game = preload("res://scripts/game.gd")
func _initialize() -> void:
    call_deferred("_run")

func touch(game: Node, id: int, point: Vector2, pressed: bool) -> void:
    var event := InputEventScreenTouch.new()
    event.index = id
    event.position = point
    event.pressed = pressed
    game._input(event)

func _run() -> void:
    var game = Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    game._primary_action()
    game.countdown = 0
    touch(game,11,Game.STICK_CENTER,true)
    touch(game,12,Game.BOOST_CENTER,true)
    var drag := InputEventScreenDrag.new()
    drag.index = 11
    drag.position = Game.STICK_CENTER+Vector2(15,-25)
    game._input(drag)
    for frame in range(15):
        game._physics_process(1.0/60.0)
    assert(game.lane_position < 2.0, "Pad must steer while boost is held")
    assert(game.boost_active and game.heat > 0, "Second touch must boost simultaneously")
    touch(game,11,Game.STICK_CENTER,false)
    assert(game.touch_vector==Vector2.ZERO and game.boost_touch_id==12)
    touch(game,12,Game.BOOST_CENTER,false)
    assert(game.boost_touch_id == -1)
    touch(game,13,Vector2(420,15),true)
    assert(game.boost_touch_id == -1, "HUD taps must not trigger boost")
    touch(game,14,Game.STICK_CENTER,true)
    touch(game,15,Game.BOOST_CENTER,true)
    touch(game,16,Vector2(460,15),true)
    assert(game.mode=="pause" and game.move_touch_id == -1 and game.boost_touch_id == -1)
    game.free()
    print("Touch controls: PASS (simultaneous steering/boost, independent release, HUD, pause)")
    quit()
