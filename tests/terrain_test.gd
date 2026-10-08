extends SceneTree
const Terrain = preload("res://scripts/terrain.gd")
const Game = preload("res://scripts/game.gd")

func _initialize() -> void:
    call_deferred("_run")

func _run() -> void:
    var features: Array[Dictionary] = [{"x":340.0,"lane":2,"kind":"big_ramp","used":false}]
    assert(is_zero_approx(Terrain.height_at(features,260,2)))
    assert(is_equal_approx(Terrain.height_at(features,300,2),19))
    assert(is_equal_approx(Terrain.height_at(features,340,2),38))
    assert(is_equal_approx(Terrain.height_at(features,364,2),38))
    assert(is_zero_approx(Terrain.height_at(features,436,2)))
    assert(is_zero_approx(Terrain.height_at(features,340,1)))
    assert(is_equal_approx(Terrain.height_at(features,340,1.5),19))
    assert(Terrain.launch_at(features,339,341,2)>0)
    assert(Terrain.launch_at(features,341,343,2)==0)
    var cache := Terrain.build_cache(features,2100)
    for x in range(250,450):
        assert(is_equal_approx(Terrain.cached_height(cache[2],x),Terrain.height_at(features,x,2)),"Cached rendering must match physics")
    var game = Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    game._start_race()
    game.features = features
    game.countdown = 0
    game.distance = 280.0
    game.speed = 155.0
    game._race_step(1.0/60.0)
    assert(not game.airborne)
    assert(is_equal_approx(game.altitude,Terrain.height_at(features,game.distance,2)))
    assert(game.bike_tilt<0,"Bike must lean up with the approach slope")
    var jumped := false
    var landed := false
    for frame in range(240):
        game._race_step(1.0/60.0)
        if game.airborne: jumped=true
        if jumped and not game.airborne:
            landed=true
            break
    assert(jumped and landed,"A terrain crest must launch and land the rider")
    assert(game.crash_count==0,"A balanced landing must succeed")
    assert(is_equal_approx(game.altitude,Terrain.height_at(features,game.distance,2)))
    game.distance=700
    game.altitude=1
    game.airborne=true
    game.jump_velocity=-100
    game.bike_tilt=1.1
    game._race_step(1.0/60.0)
    assert(game.crash_count==1 and game.crash_timer>0,"Bad pitch must visibly crash")
    game.distance=game._course_length()+1
    game.elapsed=30
    for rival in game.rivals:
        rival["x"]=game._course_length()
        rival["finish_time"]=25.0
    game._step_rivals(1.0/60.0)
    assert(game.race_place==4,"Earlier finishers must stay ahead in the final standings")
    game.free()
    print("Terrain: PASS (slope contact, shared heights, launch, landing, crash)")
    quit()
