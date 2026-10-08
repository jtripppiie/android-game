extends SceneTree
const Game=preload("res://scripts/game.gd")
const Builder=preload("res://scripts/track_builder.gd")
const Scenery=preload("res://scripts/retro_scenery.gd")

func _initialize() -> void:
    call_deferred("_run")

func _run() -> void:
    var game=Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    for kind in Game.Rules.JUMP_HAZARDS:
        game._start_race()
        game.countdown=0
        game.features=Builder.for_race(Builder.edit([],kind,400,2))
        assert(game.features.size()==2 and game.features[0]["kind"]=="jump_ramp")
        game.distance=240
        game.speed=125
        var jumped := false
        while game.distance<450:
            game._physics_process(1.0/60)
            jumped=jumped or game.airborne
        assert(jumped and game.crash_count==0, "Default speed must clear the supplied "+kind+" jump")
        var texture:=Scenery._hazard_sprite(kind)
        assert(texture.get_width()==Game.Rules.hazard_width(kind)*2)
        assert(texture==Scenery._hazard_sprite(kind))
        # Clearing the leading edge must not disable collision inside the hazard.
        game.features.assign([{"x":400.0,"lane":2,"kind":kind,"used":false}])
        game.altitude=100
        game._check_features(370,380)
        assert(not game.features[0]["used"])
        game.altitude=0
        game._check_features(399,401)
        assert(game.crash_count==1, "Landing inside "+kind+" must crash")
        game._start_race()
        game.features.assign([{"x":400.0,"lane":1,"kind":kind,"used":false}])
        game._check_features(399,401)
        assert(game.crash_count==0, "Adjacent lanes remain safe")
        game.features.assign([{"x":400.0,"lane":0,"kind":kind,"used":false}])
        game.rivals[0]["x"]=399.0
        game._step_rivals(1.0/60)
        assert(game.rivals[0]["speed"]==40.0, "Rivals must also hit hazards")
    var encountered: Dictionary={}
    for stage in range(24):
        var features:=Game.Rules.generate_features(stage)
        for feature in features:
            if feature["kind"] in Game.Rules.JUMP_HAZARDS:
                assert(stage>=2)
                encountered[feature["kind"]]=true
                var paired := false
                var lanes: Array=[]
                for other in features:
                    if other["kind"]=="jump_ramp" and other["lane"]==feature["lane"] and other["x"]==feature["x"]-80: paired=true
                    if other["x"]==feature["x"]: lanes.append(other["lane"])
                assert(paired and lanes.size()==3, "Every campaign jump needs a ramp and bypass lane")
    assert(encountered.size()==6)
    game._open_editor()
    game._click_editor(Vector2(420,231))
    assert(game.edit_palette==1)
    game._click_editor(Vector2(330,253))
    assert(game.edit_tool=="car")
    game.custom_courses[game.custom_slot]=[]
    game._builder_place(Vector2(320,178))
    assert(game.features.size()==2)
    game.free()
    print("Six jump hazards: supplied ramps, full-span collisions, safe lanes, rival penalties, campaign placement and builder pages PASS")
    quit()
