extends SceneTree
const Game=preload("res://scripts/game.gd")

func _initialize() -> void:
    call_deferred("_run")

func _flat(game) -> void:
    game._start_race()
    game.countdown=0
    game.features.clear()
    game.speed=125

func _run() -> void:
    var game=Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    for direction in [-1,1]:
        _flat(game)
        game._on_pointer_down(Game.STICK_CENTER+Vector2(20*direction,20),17)
        for i in range(90): game._physics_process(1.0/60)
        assert(game.stunt_active and game.stunt_angle*direction>0.4)
        assert(game.lane_position==2 and game.crash_count==0, "Balanced diagonals hold the lane")
        var large: float=absf(game.stunt_angle)
        game._set_stick(Game.STICK_CENTER+Vector2(11*direction,25))
        for i in range(30): game._physics_process(1.0/60)
        assert(absf(game.stunt_angle)<large, "Pad angle controls bike angle")
        game._on_pointer_up(17)
        for i in range(30): game._physics_process(1.0/60)
        assert(not game.stunt_active and absf(game.bike_tilt)<0.01)
    _flat(game)
    game._on_pointer_down(Game.STICK_CENTER+Vector2(-29,10),17)
    for i in range(150):
        game._physics_process(1.0/60)
        if game.crash_count>0: break
    assert(game.crash_count==1 and game.message=="LOST BALANCE")
    _flat(game)
    game._on_pointer_down(Game.STICK_CENTER+Vector2(20,20),17)
    for i in range(30): game._physics_process(1.0/60)
    game._notification(Node.NOTIFICATION_APPLICATION_PAUSED)
    assert(not game.stunt_active and game.stunt_angle==0)
    _flat(game)
    game.airborne=true
    game.altitude=60
    game._on_pointer_down(Game.STICK_CENTER+Vector2(-20,20),17)
    game._physics_process(1.0/60)
    assert(not game.stunt_active and game.bike_tilt<0, "Air steering remains normal lean")
    _flat(game)
    game.features.append({"x":320.0,"lane":2,"kind":"jump_ramp","used":false})
    game.distance=280
    game._on_pointer_down(Game.STICK_CENTER+Vector2(-20,20),17)
    game._physics_process(1.0/60)
    assert(not game.stunt_active, "Ramp riding takes priority over flat-ground tricks")
    _flat(game)
    game._on_pointer_down(Game.STICK_CENTER+Vector2(0,25),17)
    game._physics_process(0.1)
    assert(not game.stunt_active and game.lane_position>2, "Straight down still steers lanes")
    game.free()
    print("Wheelies: both diagonals, analog angle, lane hold, release, overbalance, pause, ramps and air control PASS")
    quit()
