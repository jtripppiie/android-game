extends SceneTree
const Game = preload("res://scripts/game.gd")
const Sound = preload("res://scripts/game_audio.gd")
const Builder = preload("res://scripts/track_builder.gd")

func _initialize() -> void:
    call_deferred("_run")

func _run() -> void:
    for kind in ["engine","wind","click","count","go","jump","land","crash","hot","cool","finish"]:
        var stream := Sound.synth(kind)
        assert(stream.mix_rate == 22050 and stream.data.size() > 2000)
        var peak := 0
        for i in range(0,stream.data.size(),2):
            peak=maxi(peak,absi(stream.data.decode_s16(i)))
        assert(peak > 1000 and peak < 32767, "Audio must be audible and unclipped")
        if kind in ["engine","wind"]:
            assert(stream.loop_end == stream.data.size()/2)
    var game = Game.new()
    root.add_child(game)
    game.set_physics_process(false)
    var original := ConfigFile.new()
    var had_save := original.load(Game.SAVE_FILE)==OK
    game.unlocked=23
    game.best_times[23]=81.25
    game.best_medals[23]="GOLD"
    game.sound.set_enabled(false)
    for slot in range(Builder.SLOT_COUNT):
        var course: Array=[]
        for i in range(Builder.KINDS.size()):
            course=Builder.edit(course,Builder.KINDS[i],320+i*440,(i+slot)%4)
        course=Builder.edit(course,"tabletop",3760,2)
        game.custom_courses[slot]=course
        game.custom_times[slot]=24.0+slot
    game._save_progress()
    var loaded = Game.new()
    root.add_child(loaded)
    loaded.set_physics_process(false)
    assert(loaded.unlocked==23 and loaded.best_times[23]==81.25 and loaded.best_medals[23]=="GOLD")
    assert(not loaded.sound.enabled)
    for slot in range(Builder.SLOT_COUNT):
        assert(loaded.custom_courses[slot]==game.custom_courses[slot])
        assert(loaded.custom_times[slot]==24.0+slot)
    loaded.custom_slot=5
    loaded._builder_test()
    for frame in range(60*120):
        loaded._physics_process(1.0/60.0)
        if loaded.mode=="finish": break
    assert(loaded.mode=="finish" and loaded.custom_race)
    assert(loaded.custom_times[5] <= 29.0, "A slower test ride must preserve the existing best")
    loaded._primary_action()
    assert(loaded.mode=="editor")
    if had_save: original.save(Game.SAVE_FILE)
    else: DirAccess.remove_absolute(ProjectSettings.globalize_path(Game.SAVE_FILE))
    game.free()
    loaded.free()
    print("Audio PCM, 24-course progress, six-slot persistence and custom player ride: PASS")
    quit()
