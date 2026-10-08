extends SceneTree
const Game=preload("res://scripts/game.gd")
const Scenery=preload("res://scripts/retro_scenery.gd")
const Builder=preload("res://scripts/track_builder.gd")
func _initialize() -> void:
    call_deferred("_run")
func _run() -> void:
    var game=Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    game._primary_action()
    game.distance=320
    game.lane_position=2
    game.altitude=0
    for kind in ["oil","mud","boost"]:
        var texture:=Scenery._surface_sprite(kind)
        assert(texture.get_width()==96 and texture.get_height()==36)
        assert(texture==Scenery._surface_sprite(kind))
        var features:=Builder.for_race(Builder.edit([],kind,320,2))
        assert(features.size()==1)
        game.features=features
        game.heat=70
        game._check_features(319,321)
        if kind=="oil": assert(game.oil_timer>0)
        if kind=="mud": assert(game.slow_timer>0)
        if kind=="boost": assert(game.boost_pad_timer>0 and game.heat==45)
    game.oil_timer=0
    game.altitude=20
    game.features=Builder.for_race(Builder.edit([],"oil",320,2))
    game._check_features(319,321)
    assert(game.oil_timer==0, "Airborne riders can clear oil")
    game.oil_timer=0.8
    game._start_race()
    assert(game.oil_timer==0, "Restart clears grip penalty")
    game.free()
    print("Surface sprites, builder export, oil/mud/boost effects and airborne clearance: PASS")
    quit()
