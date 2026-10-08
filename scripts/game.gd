extends Node2D
## Moto Thrash: a small, original, landscape, pixel-art motocross game.
## Single 480 x 270 canvas + procedural graphics: no downloaded art or IP.

const Sound = preload("res://scripts/game_audio.gd")
const Stunt = preload("res://scripts/stunt_show.gd")
const Terrain = preload("res://scripts/terrain.gd")
const Pixel = preload("res://scripts/pixel_font.gd")
const Rules = preload("res://scripts/race_rules.gd")
const Builder = preload("res://scripts/track_builder.gd")
const Scenery = preload("res://scripts/retro_scenery.gd")
const SIZE := Vector2(480.0, 270.0)
const BIKE_X := 124.0
const STICK_CENTER := Vector2(55.0, 224.0)
const BOOST_CENTER := Vector2(431.0, 224.0)
const CONTROL_VISUAL_SCALE := 0.95
const SAVE_FILE := "user://dirt_rush.cfg"

const SKY := [
    Color("#81d9e0"), Color("#f5a96c"), Color("#8fc4d3"),
    Color("#e7b87c"), Color("#42456f"), Color("#99c4de")
]
const HILLS := [
    Color("#64b0a6"), Color("#b77867"), Color("#648c8a"),
    Color("#bb8f6a"), Color("#343e66"), Color("#7392b8")
]
const DIRT := Color("#7c4e35")
const DIRT_LIGHT := Color("#ae7448")
const DIRT_DARK := Color("#492f2e")
const CREAM := Color("#fff0c6")
const INK := Color("#172539")
const YELLOW := Color("#f7cb5e")
const RED := Color("#ed6a5c")
const CYAN := Color("#82e5d5")

var mode := "menu" # menu, race, pause, finish, editor
var computer_mode := false
var stunt_race := false
var stunt_bus_count := 5
var stunt_ramp_setting := 0
var stunt_speed_setting := 0
var stunt_cleared := 0
var stunt_flew := false
var stunt_takeoff := 0.0
var stunt_jump_distance := 0.0
var stunt_success := false
var stunt_score := 0
var stunt_best_score := 0
var stunt_best_buses := 0
var computer_cooling := false
var computer_lane := 2
var stage := 0
var unlocked := 0
var best_times: Array[float] = []
var best_medals: Array[String] = []
var sound
var countdown_cue := 0
var features: Array[Dictionary] = []
var terrain_cache: Array[PackedFloat32Array] = []
var custom_courses: Array = []
var custom_times: Array[float] = []
var custom_slot := 0
var custom_race := false
var edit_page := 0
var edit_tool := "ramp"
var edit_palette := 0
var undo_stack: Array = []
var clear_confirm := false
var distance := 0.0
var speed := 110.0
var elapsed := 0.0
var countdown := 2.3
var lane_position := 2.0
var lane_velocity := 0.0
var airborne := false
var boost_pad_timer := 0.0
var altitude := 0.0
var jump_velocity := 0.0
var bike_tilt := 0.0
var stunt_angle := 0.0
var stunt_strain := 0.0
var stunt_active := false
var heat := 0.0
var overheated := false
var slow_timer := 0.0
var oil_timer := 0.0
var crash_timer := 0.0
var crash_count := 0
var boost_active := false
var new_record := false
var medal := ""
var message := ""
var message_timer := 0.0
var dust_timer := 0.0
var visual_time := 0.0
var sparks: Array[Dictionary] = []
var rivals: Array[Dictionary] = []
var race_place := 1

# Same interaction model as Captain Quack: floating thumbstick displacement,
# normalized to a circular radius, paired with an independent held action.
var touch_vector := Vector2.ZERO
var stick_visual := Vector2.ZERO
var move_touch_id := -1
var boost_touch_id := -1

func _ready() -> void:
    get_tree().quit_on_go_back = false
    texture_repeat = CanvasItem.TEXTURE_REPEAT_ENABLED
    texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR
    best_times.resize(Rules.TRACK_COUNT)
    best_medals.resize(Rules.TRACK_COUNT)
    custom_times.resize(Builder.SLOT_COUNT)
    for slot in range(Builder.SLOT_COUNT): custom_courses.append([])
    sound=Sound.new()
    add_child(sound)
    _load_progress()
    _prepare_preview()
    set_process(true)
    set_physics_process(true)

func _prepare_preview() -> void:
    features = Stunt.build(stunt_bus_count,stunt_ramp_setting) if stunt_race else (Builder.for_race(custom_courses[custom_slot]) if custom_race else Rules.generate_features(stage))
    terrain_cache = Terrain.build_cache(features,_course_length())
    distance = 0.0
    speed = 110.0
    lane_position = 2.0
    altitude = 0.0
    airborne = false
    boost_pad_timer = 0.0
    bike_tilt = 0.0
    sparks.clear()
    _clear_controls()
    queue_redraw()

func _clear_controls() -> void:
    move_touch_id = -1
    boost_touch_id = -1
    touch_vector = Vector2.ZERO
    stick_visual = Vector2.ZERO
    boost_active = false
    stunt_angle = 0.0
    stunt_strain = 0.0
    stunt_active = false

func _start_race() -> void:
    _prepare_preview()
    elapsed = 0.0
    computer_cooling = false
    computer_lane = 2
    lane_velocity = 0.0
    heat = 0.0
    overheated = false
    countdown = 3.0
    countdown_cue=3
    sound.cue("count")
    jump_velocity = 0.0
    crash_timer = 0.0
    crash_count = 0
    slow_timer = 0.0
    oil_timer = 0.0
    dust_timer = 0.0
    message = ""
    message_timer = 0.0
    medal = ""
    new_record = false
    stunt_cleared=0
    stunt_flew=false
    stunt_success=false
    stunt_jump_distance=0.0
    stunt_score=0
    _reset_rivals()
    mode = "race"
    queue_redraw()

func _physics_process(delta: float) -> void:
    visual_time += delta
    stick_visual = stick_visual.move_toward(touch_vector, delta * 9.0)
    if mode == "race":
        if countdown > 0.0:
            countdown = maxf(0.0, countdown - delta)
            var beat := ceili(countdown)
            if beat != countdown_cue:
                countdown_cue=beat
                sound.cue("go" if beat==0 else "count")
        else:
            _race_step(delta)
    sound.update_motor(mode=="race" and countdown<=0,speed,boost_active,airborne,crash_timer>0)
    queue_redraw()

func _race_step(dt: float) -> void:
    elapsed += dt
    boost_pad_timer = maxf(0.0, boost_pad_timer - dt)
    slow_timer = maxf(0.0, slow_timer - dt)
    oil_timer = maxf(0.0, oil_timer - dt)
    crash_timer = maxf(0.0, crash_timer - dt)
    message_timer = maxf(0.0, message_timer - dt)
    var joystick := Input.get_vector("move_left", "move_right", "move_up", "move_down")
    if touch_vector.length() > joystick.length():
        joystick = touch_vector
    var boost_pressed: bool = Input.is_action_pressed("boost") or boost_touch_id != -1
    if computer_mode:
        joystick = _computer_steering()
        if heat >= 85.0:
            computer_cooling = true
        elif heat <= 30.0:
            computer_cooling = false
        boost_pressed = not computer_cooling
    boost_active = boost_pressed and not overheated and crash_timer <= 0.0
    heat = Rules.heat_step(heat, boost_active, dt)
    if heat >= Rules.HEAT_MAX:
        heat = Rules.HEAT_MAX
        overheated = true
        boost_active = false
        sound.cue("hot")
        message = "ENGINE HOT!"
        message_timer = 1.3
    elif overheated and heat <= Rules.HEAT_UNLOCK:
        overheated = false
        sound.cue("cool")
        message = "BOOST READY"
        message_timer = 0.8

    _update_ground_stunt(joystick,dt)
    if stunt_active or stunt_race: lane_velocity=0.0
    var target_lane_speed := joystick.y * 2.3 if crash_timer <= 0 and not stunt_active and not stunt_race else 0.0
    lane_velocity = move_toward(lane_velocity, target_lane_speed, (1.8 if oil_timer>0 else 10.0) * dt)
    lane_position = clampf(lane_position + lane_velocity * dt, 0.0, 3.0)

    if crash_timer > 0.0:
        speed = move_toward(speed, 25.0, 240.0 * dt)
    else:
        var goal := Rules.target_speed(0.0 if stunt_active else joystick.x, boost_active, overheated, slow_timer > 0.0)
        if stunt_race: goal=Stunt.SPEEDS[stunt_speed_setting]+(80.0 if boost_active else 0.0)
        if boost_pad_timer > 0:
            goal += 42.0
        speed = move_toward(speed, goal, (560.0 if stunt_race else 115.0) * dt)

    var last_distance := distance
    distance += speed * dt
    var ground := Terrain.height_at(features,distance,lane_position)
    var ground_tilt := -atan(Terrain.slope_at(features,distance,lane_position))
    if airborne:
        bike_tilt = clampf(bike_tilt + joystick.x * dt * 2.6, -1.1, 1.1)
        altitude += jump_velocity * dt
        jump_velocity -= 430.0 * dt
        if altitude <= ground and jump_velocity <= 0.0:
            altitude = ground
            jump_velocity = 0.0
            airborne = false
            if absf(bike_tilt-ground_tilt) > 0.62:
                _crash("ROUGH LANDING")
            else:
                sound.cue("land")
                message = "CLEAN LANDING"
                message_timer = 0.7
            bike_tilt = ground_tilt
    else:
        altitude = ground
        bike_tilt = stunt_angle if stunt_active else ground_tilt
        var launch := Terrain.launch_at(features,last_distance,distance,lane_position)
        if launch > 0 and crash_timer <= 0:
            altitude=maxf(altitude,Terrain.height_at(features,last_distance,lane_position))
            jump_velocity = launch + speed * 0.08
            airborne = true
            if stunt_race:
                stunt_flew=true
                stunt_takeoff=distance
            sound.cue("jump")
            message = "AIR TIME"
            message_timer = 0.65
    _step_rivals(dt)
    if crash_timer <= 0.0:
        _check_features(last_distance, distance)

    dust_timer += dt
    var interval := 0.055 if boost_active else 0.12
    if speed > 55.0 and not airborne and dust_timer >= interval:
        dust_timer = 0.0
        _spawn_dust(BIKE_X - 16.0, Rules.lane_y(lane_position) - altitude + 2.0, Color("#efd19a") if boost_active else Color("#c9a271"))
    _update_particles(dt)
    if stunt_race:
        if crash_count>0:
            _finish_stunt(false)
        elif stunt_flew and not airborne:
            _finish_stunt(stunt_cleared==stunt_bus_count)
        elif distance>=_course_length():
            _finish_stunt(false)
        return
    if distance >= _course_length():
        elapsed -= maxf(0.0,distance-_course_length())/maxf(speed,1.0)
        _finish_race()

func _update_ground_stunt(joystick: Vector2, dt: float) -> void:
    var flat := absf(Terrain.slope_at(features,distance,lane_position))<0.04
    if stunt_race or computer_mode or airborne or crash_timer>0 or speed<65 or not flat:
        stunt_angle=0.0
        stunt_strain=0.0
        stunt_active=false
        return
    var requested := joystick.y>0.28 and absf(joystick.x)>0.20
    var target := 0.0
    if requested:
        var angle := atan2(absf(joystick.x),joystick.y)
        target=signf(joystick.x)*clampf(angle/(PI/4)*0.62,0,1.08)*minf(joystick.length(),1.0)
    stunt_angle=move_toward(stunt_angle,target,dt*2.4)
    stunt_active=absf(stunt_angle)>0.03
    if absf(stunt_angle)>0.78:
        stunt_strain=minf(1.0,stunt_strain+dt*(absf(stunt_angle)-0.70)*4.0)
    else:
        stunt_strain=maxf(0.0,stunt_strain-dt*1.2)
    if stunt_strain>=1.0:
        _crash("LOST BALANCE")

# Look ahead through the same course and use the same controls and physics as a player.
func _computer_steering() -> Vector2:
    var best_score := INF
    var chosen := computer_lane
    var lookahead := maxf(180.0, speed * 1.3)
    for lane in range(Rules.LANE_COUNT):
        var score := absf(float(lane) - lane_position) * 0.8
        if lane != computer_lane:
            score += 0.35
        var terrain_gap := absf(Terrain.height_at(features,distance,lane)-Terrain.height_at(features,distance,lane_position))
        score += terrain_gap * 0.5
        for feature in features:
            var ahead := float(feature["x"]) - distance
            if bool(feature["used"]) or ahead < -12.0 or ahead > lookahead:
                continue
            var urgency := 1.0 - maxf(ahead, 0.0) / lookahead
            var feature_lane := float(feature["lane"])
            var kind := str(feature["kind"])
            if int(feature_lane) == lane:
                if kind in ["rock","mud","oil","whoops"] or kind in Rules.JUMP_HAZARDS:
                    score += (12.0 if kind == "rock" else 5.0) * (0.3 + urgency)
                elif ahead > 35.0:
                    score -= 1.8 * urgency
            # Avoid crossing a dangerous lane just as its obstacle reaches the bike.
            if kind == "rock" and ahead < speed * (absf(feature_lane - lane_position) / 2.3 + 0.2):
                if feature_lane >= minf(lane_position, float(lane)) and feature_lane <= maxf(lane_position, float(lane)):
                    score += 8.0
        if score < best_score:
            best_score = score
            chosen = lane
    computer_lane = chosen
    var vertical := clampf((float(chosen) - lane_position) * 4.0 - lane_velocity * 0.5, -1.0, 1.0)
    var horizontal := 1.0
    if airborne:
        var landing_tilt := -atan(Terrain.slope_at(features,distance+speed*0.12,lane_position))
        horizontal = clampf((landing_tilt-bike_tilt) * 5.0, -1.0, 1.0)
    return Vector2(horizontal, vertical)

func _start_computer_race() -> void:
    stunt_race=false
    computer_mode = true
    custom_race = false
    _start_race()

func _next_stage() -> void:
    if computer_mode:
        stage = (stage + 1) % Rules.TRACK_COUNT
    else:
        stage = mini(unlocked, stage + 1)

func _check_features(old_x: float, new_x: float) -> void:
    for i in range(features.size()):
        var feature: Dictionary = features[i]
        if bool(feature["used"]):
            continue
        var x: float = float(feature["x"])
        var kind := str(feature["kind"])
        if kind in Rules.JUMP_HAZARDS:
            var half_width := Rules.hazard_width(kind)/2
            var overlaps := new_x>=x-half_width and old_x<=x+half_width
            if overlaps and absf(lane_position-float(feature["lane"]))<0.45:
                if altitude-Terrain.height_at(features,new_x,lane_position)<Rules.hazard_clearance(kind):
                    feature["used"]=true
                    _crash("MISSED "+kind.to_upper()+" JUMP")
                elif new_x>x+half_width:
                    if stunt_race:
                        stunt_cleared+=1
                    else:
                        message="JUMP CLEARED!"
                        message_timer=0.8
            if new_x>x+half_width: feature["used"]=true
            features[i]=feature
            continue
        if x > new_x + 8.0 or x < old_x - 11.0:
            continue
        var lane_gap := absf(lane_position - float(feature["lane"]))
        if lane_gap > 0.38:
            continue
        feature["used"] = true
        features[i] = feature
        var clearance := altitude-Terrain.height_at(features,distance,lane_position)
        match str(feature["kind"]):
            "rock":
                if clearance < 12.0:
                    _crash("HIT BARRIER")
            "mud":
                if clearance < 6.0:
                    slow_timer = 1.1
                    message = "MUD / SLOW"
                    message_timer = 0.65
            "oil":
                if clearance < 6.0:
                    oil_timer = 0.8
                    speed *= 0.88
                    message = "OIL / LOW GRIP"
                    message_timer = 0.8
            "boost":
                if clearance < 6.0:
                    sound.cue("cool")
                    boost_pad_timer = 1.0
                    heat = maxf(0,heat-25)
                    message = "COOL + BOOST"
                    message_timer = 0.65

func _crash(reason: String) -> void:
    if crash_timer > 0.0:
        return
    sound.cue("crash")
    crash_count += 1
    crash_timer = 1.0
    speed = 40.0
    altitude = Terrain.height_at(features,distance,lane_position)
    airborne = false
    jump_velocity = 0.0
    bike_tilt = 0.0
    stunt_angle=0.0
    stunt_strain=0.0
    stunt_active=false
    message = reason
    message_timer = 1.0
    for i in range(10):
        _spawn_dust(BIKE_X + randf_range(-8, 10), Rules.lane_y(lane_position) - 4.0, RED if i % 3 == 0 else DIRT_LIGHT)

func _spawn_dust(px: float, py: float, tint: Color) -> void:
    if sparks.size() >= 65:
        sparks.pop_front()
    sparks.append({
        "p": Vector2(px, py), "v": Vector2(randf_range(-64.0, -16.0), randf_range(-25.0, 7.0)),
        "life": randf_range(0.22, 0.49), "color": tint
    })

func _update_particles(dt: float) -> void:
    for i in range(sparks.size() - 1, -1, -1):
        var part: Dictionary = sparks[i]
        part["life"] = float(part["life"]) - dt
        if float(part["life"]) <= 0.0:
            sparks.remove_at(i)
        else:
            part["p"] = Vector2(part["p"]) + Vector2(part["v"]) * dt
            sparks[i] = part

func _finish_race() -> void:
    mode = "finish"
    sound.cue("finish")
    distance = _course_length()
    if computer_mode:
        medal = "COMPUTER FINISH"
        new_record = false
        _clear_controls()
        return
    if custom_race:
        medal = "CUSTOM FINISH"
        var old_best: float = custom_times[custom_slot]
        new_record = old_best <= 0.0 or elapsed < old_best
        if new_record:
            custom_times[custom_slot] = elapsed
    else:
        medal = Rules.medal_for(elapsed, stage, crash_count)
        var ranks := ["","FINISH","BRONZE","SILVER","GOLD"]
        if ranks.find(medal)>ranks.find(best_medals[stage]): best_medals[stage]=medal
        var previous: float = best_times[stage]
        new_record = previous <= 0.0 or elapsed < previous
        if new_record:
            best_times[stage] = elapsed
        if stage + 1 > unlocked:
            unlocked = mini(stage + 1, Rules.TRACK_COUNT - 1)
    _save_progress()
    _clear_controls()

func _finish_stunt(success: bool) -> void:
    stunt_success=success
    stunt_jump_distance=maxf(0.0,distance-stunt_takeoff) if stunt_flew else 0.0
    stunt_score=stunt_cleared*100+roundi(stunt_jump_distance/10) if success else 0
    new_record=success and stunt_score>stunt_best_score
    if success:
        stunt_best_score=maxi(stunt_best_score,stunt_score)
        stunt_best_buses=maxi(stunt_best_buses,stunt_cleared)
        sound.cue("finish")
    mode="finish"
    _clear_controls()
    _save_progress()

func _load_progress() -> void:
    var cfg := ConfigFile.new()
    if cfg.load(SAVE_FILE) != OK:
        # Desktop project renaming changes user://; retain the previous game's saves.
        var imported := false
        for old_title in ["Cinderspoke", "Dirt Rush"]:
            var legacy := OS.get_user_data_dir().get_base_dir().path_join(old_title + "/dirt_rush.cfg")
            if cfg.load(legacy) == OK:
                imported = true
                break
        if not imported:
            return
        cfg.save(SAVE_FILE)
    sound.set_enabled(bool(cfg.get_value("settings","sound",true)))
    stunt_best_score=maxi(0,int(cfg.get_value("stunt","best_score",0)))
    stunt_best_buses=clampi(int(cfg.get_value("stunt","best_buses",0)),0,20)
    stunt_bus_count=clampi(int(cfg.get_value("stunt","buses",5)),1,20)
    stunt_ramp_setting=clampi(int(cfg.get_value("stunt","ramp",0)),0,2)
    stunt_speed_setting=clampi(int(cfg.get_value("stunt","speed",0)),0,2)
    unlocked = clampi(int(cfg.get_value("progress", "unlocked", 0)), 0, Rules.TRACK_COUNT - 1)
    for i in range(Rules.TRACK_COUNT):
        best_times[i] = maxf(0.0, float(cfg.get_value("records_v3" if i>=2 else "records_v2", str(i), 0.0)))
        var saved_medal := str(cfg.get_value("medals_v3" if i>=2 else "medals_v2",str(i),""))
        best_medals[i]=saved_medal if saved_medal in ["","FINISH","BRONZE","SILVER","GOLD"] else ""
    for i in range(Builder.SLOT_COUNT):
        var saved: Variant = cfg.get_value("builder", "slot_%d" % i, [])
        custom_courses[i] = Builder.sanitize(saved)
        custom_times[i] = maxf(0.0, float(cfg.get_value("builder", "best_v2_%d" % i, 0.0)))

func _save_progress() -> void:
    var cfg := ConfigFile.new()
    cfg.load(SAVE_FILE) # Preserve legacy records while new course lengths use a separate table.
    cfg.set_value("settings","sound",sound.enabled)
    cfg.set_value("stunt","best_score",stunt_best_score)
    cfg.set_value("stunt","best_buses",stunt_best_buses)
    cfg.set_value("stunt","buses",stunt_bus_count)
    cfg.set_value("stunt","ramp",stunt_ramp_setting)
    cfg.set_value("stunt","speed",stunt_speed_setting)
    cfg.set_value("progress", "unlocked", unlocked)
    for i in range(Rules.TRACK_COUNT):
        cfg.set_value("records_v3" if i>=2 else "records_v2", str(i), best_times[i])
        cfg.set_value("medals_v3" if i>=2 else "medals_v2",str(i),best_medals[i])
    for i in range(Builder.SLOT_COUNT):
        cfg.set_value("builder", "slot_%d" % i, Builder.sanitize(custom_courses[i]))
        cfg.set_value("builder", "best_v2_%d" % i, custom_times[i])
    if cfg.save(SAVE_FILE) != OK:
        push_warning("Could not save Moto Thrash progress")

func _active_dialog() -> AcceptDialog:
    for child in get_children():
        if child is AcceptDialog and child.visible and not child.is_queued_for_deletion():
            return child
    return null

func _input(event: InputEvent) -> void:
    if _active_dialog() != null:
        if event is InputEventKey and event.pressed and event.keycode == KEY_ESCAPE:
            _back_action()
            get_viewport().set_input_as_handled()
        return
    if event is InputEventKey and event.pressed and not event.echo:
        if event.keycode==KEY_M:
            _toggle_sound()
            return
        if event.keycode == KEY_ESCAPE or event.keycode == KEY_P:
            _back_action()
            queue_redraw()
            return
        if mode == "editor":
            if event.keycode == KEY_LEFT:
                _set_edit_page(-1)
            elif event.keycode == KEY_RIGHT:
                _set_edit_page(1)
            elif event.keycode == KEY_TAB:
                _set_editor_slot(1)
            elif event.keycode == KEY_Z:
                _builder_undo()
            elif event.keycode>=KEY_1 and event.keycode<=KEY_8:
                edit_tool=Builder.TOOLS[edit_palette*8+event.keycode-KEY_1]
            elif event.keycode==KEY_9:
                edit_palette=1-edit_palette
            elif event.keycode==KEY_C:
                _builder_test(true)
            elif event.keycode == KEY_ENTER or event.keycode == KEY_KP_ENTER:
                _builder_test()
            queue_redraw()
            return
        if event.keycode == KEY_ENTER or event.keycode == KEY_KP_ENTER:
            _primary_action()
            return
        if event.keycode == KEY_R and mode == "race":
            _start_race()
            return
        if mode == "menu" and event.keycode == KEY_C:
            _start_computer_race()
            return
        if mode == "menu" and event.keycode == KEY_TAB:
            stage = (stage + 1) % (unlocked + 1)
            _prepare_preview()
    if event is InputEventScreenTouch:
        var point: Vector2 = event.position
        if event.pressed:
            _on_pointer_down(point, event.index)
        else:
            _on_pointer_up(event.index)
    elif event is InputEventScreenDrag:
        if event.index == move_touch_id:
            _set_stick(event.position)
    elif event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
        if event.pressed:
            _on_pointer_down(event.position, -20)
        else:
            _on_pointer_up(-20)
    elif event is InputEventMouseMotion and move_touch_id == -20:
        _set_stick(event.position)

func _toggle_sound() -> void:
    sound.set_enabled(not sound.enabled)
    sound.cue("click")
    _save_progress()
    queue_redraw()

func _sound_button_rect() -> Rect2:
    return Rect2(405,70 if mode in ["race","pause"] else 3,72,18)

func _show_privacy() -> void:
    var dialog := AcceptDialog.new()
    dialog.title = "Privacy"
    dialog.min_size = Vector2i(440,230)
    var text := RichTextLabel.new()
    text.custom_minimum_size = Vector2(410,175)
    text.add_theme_font_size_override("normal_font_size",12)
    text.text = "MOTO THRASH: RETRO RACING\n\nThe game works offline. It has no accounts, ads, analytics or in-app purchases. The game does not collect or transmit personal data.\n\nProgress, settings, custom tracks and personal bests are saved on this device. Clearing app storage or uninstalling removes these saves. There is no cloud save or online track sharing.\n\nGoogle Play processes your purchase under Google's own privacy policy. If you contact support using the store listing, your email service sends the information you choose to include."
    dialog.add_child(text)
    add_child(dialog)
    dialog.confirmed.connect(dialog.queue_free)
    dialog.canceled.connect(dialog.queue_free)
    dialog.popup_centered(Vector2i(440,230))

func _show_credits() -> void:
    var dialog := AcceptDialog.new()
    dialog.title = "Credits and licenses"
    dialog.min_size = Vector2i(440,230)
    var text := RichTextLabel.new()
    text.custom_minimum_size = Vector2(410,175)
    text.add_theme_font_size_override("normal_font_size",12)
    text.text = "MOTO THRASH - TripperDeeLabs\nOriginal game artwork and synthesized audio.\n\nGodot Engine: https://godotengine.org/license/\n\n" + Engine.get_license_text()
    for component in Engine.get_copyright_info():
        text.text += "\n\n" + str(component.get("name",""))
        for part in component.get("parts",[]):
            text.text += "\n" + str(part.get("copyright",[])) + "\nLicense: " + str(part.get("license",""))
    var licenses := Engine.get_license_info()
    for name in licenses:
        text.text += "\n\n" + str(name) + "\n" + str(licenses[name])
    dialog.add_child(text)
    add_child(dialog)
    dialog.confirmed.connect(dialog.queue_free)
    dialog.canceled.connect(dialog.queue_free)
    dialog.popup_centered(Vector2i(440,230))

func _on_pointer_down(point: Vector2, pointer_id: int) -> void:
    if _active_dialog() != null:
        return
    if mode=="menu" and Rect2(199,3,72,18).has_point(point):
        _show_privacy()
        return
    if mode=="menu" and Rect2(83,3,108,18).has_point(point):
        _open_stunt_setup()
        return
    if mode=="menu" and Rect2(3,3,72,18).has_point(point):
        _show_credits()
        return
    if mode!="editor" and _sound_button_rect().has_point(point):
        _toggle_sound()
        return
    sound.cue("click")
    if mode=="stunt_setup":
        _click_stunt_setup(point)
        return
    if mode == "editor":
        _click_editor(point)
        return
    if mode != "race":
        _click_menu(point)
        return
    if point.distance_to(Vector2(460, 15)) < 18.0:
        mode = "pause"
        _clear_controls()
    elif not computer_mode and move_touch_id == -1 and point.y > 32.0 and point.distance_to(STICK_CENTER) < 48.0:
        move_touch_id = pointer_id
        _set_stick(point)
    elif not computer_mode and boost_touch_id == -1 and point.y > 32.0 and point.distance_to(BOOST_CENTER) < 48.0:
        boost_touch_id = pointer_id
    queue_redraw()

func _on_pointer_up(pointer_id: int) -> void:
    if move_touch_id == pointer_id:
        move_touch_id = -1
        touch_vector = Vector2.ZERO
    if boost_touch_id == pointer_id:
        boost_touch_id = -1

func _set_stick(point: Vector2) -> void:
    touch_vector = ((point - STICK_CENTER) / (32.0 * CONTROL_VISUAL_SCALE)).limit_length(1.0)
    if touch_vector.length() < 0.12:
        touch_vector = Vector2.ZERO

func _click_menu(point: Vector2) -> void:
    if mode == "menu":
        if Rect2(24, 176, 137, 31).has_point(point):
            stunt_race=false
            computer_mode = false
            stage = mini(stage, unlocked)
            custom_race = false
            _start_race()
        elif Rect2(171, 176, 137, 31).has_point(point):
            _start_computer_race()
        elif Rect2(318, 176, 137, 31).has_point(point):
            _open_editor()
        elif Rect2(118, 223, 44, 26).has_point(point):
            stage = maxi(0, stage - 1)
            _prepare_preview()
        elif Rect2(318, 223, 44, 26).has_point(point):
            stage = mini(unlocked, stage + 1)
            _prepare_preview()
    elif mode == "pause":
        if Rect2(159, 151, 162, 28).has_point(point):
            mode = "race"
        elif Rect2(159, 190, 162, 28).has_point(point):
            if stunt_race:
                _open_stunt_setup()
            elif custom_race:
                _open_editor()
            else:
                mode = "menu"
                _prepare_preview()
    elif mode == "finish":
        if Rect2(103, 39, 66, 23).has_point(point):
            _back_action()
        elif Rect2(156, 174, 168, 28).has_point(point):
            if stunt_race:
                _open_stunt_setup()
            elif custom_race:
                _open_editor()
            else:
                _next_stage()
                _start_race()
        elif Rect2(156, 211, 168, 28).has_point(point):
            _start_race()

func _primary_action() -> void:
    match mode:
        "menu":
            stunt_race=false
            computer_mode = false
            custom_race = false
            stage = mini(stage, unlocked)
            _start_race()
        "pause": mode = "race"
        "finish":
            if stunt_race:
                _open_stunt_setup()
            elif custom_race:
                _open_editor()
            else:
                _next_stage()
                _start_race()
        "editor": _builder_test()
        "stunt_setup": _start_stunt()

func _back_action() -> void:
    var dialog := _active_dialog()
    if dialog != null:
        dialog.hide()
        dialog.queue_free()
        return
    _clear_controls()
    match mode:
        "race": mode = "pause"
        "pause": mode = "race"
        "finish":
            if stunt_race:
                _open_stunt_setup()
            elif custom_race:
                _open_editor()
            else:
                mode = "menu"
                _prepare_preview()
        "editor": _builder_back()
        "stunt_setup":
            stunt_race=false
            mode="menu"
            _prepare_preview()
        "menu":
            if OS.get_name()=="Android": get_tree().quit()
    queue_redraw()

func _notification(what: int) -> void:
    if what == NOTIFICATION_WM_GO_BACK_REQUEST:
        _back_action()
    if what == NOTIFICATION_APPLICATION_PAUSED or what == NOTIFICATION_WM_WINDOW_FOCUS_OUT:
        if mode == "race":
            mode = "pause"
            _clear_controls()

func _track_transform() -> Transform2D:
    if mode in ["race", "pause", "finish"]:
        return Transform2D(Vector2(1,0),Vector2(0,1.2),Vector2(0,-23))
    return Transform2D.IDENTITY

func _draw() -> void:
    _draw_background()
    if stunt_race:
        _draw_stunt_course()
    else:
        # Fill the former bottom dashboard area while keeping the far track edge fixed.
        draw_set_transform_matrix(_track_transform())
        _draw_tracks()
        for lane in range(Rules.LANE_COUNT):
            Scenery.track_lane(self,terrain_cache[lane],distance,lane,_theme_index())
            _draw_features(lane)
            var goal_x := _course_length()-distance+BIKE_X
            if goal_x > -12 and goal_x < 480:
                _draw_checkers(Rect2(goal_x,Rules.lane_y(lane)-13,12,26),4)
            if mode != "editor" and mode != "menu":
                _draw_rivals(lane)
                if roundi(lane_position) == lane:
                    _draw_particles()
                    _draw_bike()
        Scenery.front(self, distance, _theme_index(), custom_slot+100 if custom_race or mode=="editor" else stage)
        draw_set_transform(Vector2.ZERO)
    if mode == "race" or mode == "pause":
        _draw_hud()
        _draw_touch_controls()
    match mode:
        "menu": _draw_menu()
        "pause": _draw_pause()
        "finish": _draw_finish()
        "editor": _draw_editor()
        "stunt_setup": _draw_stunt_setup()
    if mode=="menu":
        _button(Rect2(3,3,72,18),"CREDITS",CYAN)
        _button(Rect2(83,3,108,18),"STUNT SHOW",YELLOW)
        _button(Rect2(199,3,72,18),"PRIVACY",CYAN)
    if mode!="editor":
        draw_rect(_sound_button_rect(),Color("#192940"))
        Pixel.text(self,"SFX ON" if sound.enabled else "SFX OFF",_sound_button_rect().position+Vector2(15,6),1,CREAM)
    if mode == "race":
        if countdown > 0.0:
            _text("%d" % maxi(1, int(ceilf(countdown))), Vector2(220, 94), 42, YELLOW)
            _text("GET READY", Vector2(198, 113), 13, CREAM)
        elif crash_timer > 0.0 or message_timer > 0.0:
            var txt_color := RED if crash_timer > 0.0 else YELLOW
            _panel(Rect2(168, 77, 144, 17), INK, txt_color)
            _center_text(message, 89, 10, txt_color)

func _theme_index() -> int:
    if stunt_race: return 0
    return custom_slot if custom_race or mode == "editor" else stage%6

func _draw_background() -> void:
    Scenery.paint(self, _theme_index(), distance, visual_time, mode in ["race","pause","finish"], custom_slot+100 if custom_race or mode=="editor" else stage)

func _draw_tracks() -> void:
    Scenery.texture_track(self,distance,_theme_index())

func _draw_features(lane_filter: int = -1) -> void:
    for feature in features:
        if lane_filter >= 0 and int(feature["lane"]) != lane_filter:
            continue
        var sx := float(feature["x"]) - distance + BIKE_X
        if sx < -45 or sx > 525:
            continue
        var ground := Terrain.height_at(features,float(feature["x"]),float(feature["lane"]))
        Scenery.obstacle(self,str(feature["kind"]),sx,Rules.lane_y(float(feature["lane"]))-ground,int(float(feature["x"])/40))

func _reset_rivals() -> void:
    rivals.clear()
    if stunt_race:
        race_place=1
        return
    for i in range(3):
        rivals.append({
            "x": 0.0,
            "lane": float([0, 1, 3][i]),
            "speed": 110.0,
            "heat": 0.0,
            "cooling": false,
            "air": false,
            "finish_time": -1.0,
            "tilt": 0.0,
            "h": 0.0,
            "jump_v": 0.0,
            "next_shift": 2.0 + float(i * 2),
            "last_ramp": -1
        })
    race_place = 1

func _step_rivals(dt: float) -> void:
    race_place = 1
    for i in range(rivals.size()):
        var r: Dictionary = rivals[i]
        var old_x := float(r["x"])
        var lane := float(r["lane"])
        # Rivals also budget boost heat instead of cruising above the player's
        # sustainable speed forever in later cups.
        if float(r["heat"])>=82.0+i*3: r["cooling"]=true
        elif float(r["heat"])<=26.0+i*3: r["cooling"]=false
        var boosting := not bool(r["cooling"])
        r["heat"]=Rules.heat_step(float(r["heat"]),boosting,dt)
        var pace := 141.0+float(stage/6)*2+i*3+sin(elapsed*1.7+i*2)*7.0
        if boosting: pace+=93.0
        r["speed"] = move_toward(float(r["speed"]),pace,115*dt)
        var next_x := old_x+float(r["speed"])*dt
        if float(r["finish_time"]) < 0 and next_x >= _course_length():
            r["finish_time"] = elapsed-dt+(_course_length()-old_x)/float(r["speed"])
        r["x"] = minf(_course_length(),next_x)
        var ground := Terrain.height_at(features,float(r["x"]),lane)
        var tilt := -atan(Terrain.slope_at(features,float(r["x"]),lane))
        if bool(r["air"]):
            r["h"] = float(r["h"])+float(r["jump_v"])*dt
            r["jump_v"] = float(r["jump_v"])-430*dt
            r["tilt"] = move_toward(float(r["tilt"]),tilt,2*dt)
            if float(r["h"]) <= ground and float(r["jump_v"]) <= 0:
                r["h"] = ground
                r["air"] = false
        else:
            r["h"] = ground
            r["tilt"] = tilt
            var launch := Terrain.launch_at(features,old_x,float(r["x"]),lane)
            if launch > 0:
                r["h"]=maxf(float(r["h"]),Terrain.height_at(features,old_x,lane))
                r["air"] = true
                r["jump_v"] = launch+float(r["speed"])*0.08
            for feature in features:
                if int(feature["lane"]) == int(lane) and old_x < float(feature["x"]) and float(r["x"]) >= float(feature["x"]):
                    if str(feature["kind"]) == "mud": r["speed"] = 105.0
                    if str(feature["kind"]) == "oil": r["speed"] = 125.0
                    if str(feature["kind"]) == "boost": r["speed"] = 230.0
        # Apply the same jump clearance to opponents, including mid-pit landings.
        for feature in features:
            var kind := str(feature["kind"])
            if kind not in Rules.JUMP_HAZARDS or int(feature["lane"])!=int(lane): continue
            var half_width := Rules.hazard_width(kind)/2
            if float(r["x"])>=float(feature["x"])-half_width and old_x<=float(feature["x"])+half_width and float(r["h"])-ground<Rules.hazard_clearance(kind):
                r["speed"]=40.0
        if float(r["finish_time"]) >= 0:
            var player_finish := elapsed-maxf(0,distance-_course_length())/maxf(speed,1)
            if distance < _course_length() or float(r["finish_time"]) < player_finish:
                race_place += 1
        elif float(r["x"])>distance:
            race_place += 1
        rivals[i] = r

func _draw_rivals(lane_filter: int = -1) -> void:
    for i in range(rivals.size()):
        var r: Dictionary = rivals[i]
        if lane_filter >= 0 and roundi(float(r["lane"])) != lane_filter:
            continue
        var px: float = BIKE_X + float(r["x"]) - distance
        if px < -34.0 or px > 520.0:
            continue
        var floor_y: float = Rules.lane_y(float(r["lane"]))
        var ground := Terrain.height_at(features,float(r["x"]),float(r["lane"]))
        var color: Color = [Color("#347de3"), Color("#4db96c"), Color("#e9bc3d")][i]
        Scenery.oval(self, Vector2(px + 2, floor_y - ground + 4), Vector2(13, 2), Color(0.12,0.15,0.12,0.24))
        if not bool(r["air"]) and float(r["speed"])>100:
            Scenery.dust(self,Vector2(px,floor_y-ground),float(r["x"])+i*15,Color("#edc593"),0.65)
        _draw_pixel_bike(Vector2(px, floor_y - float(r["h"])), color, color.lightened(0.35), float(r["tilt"]), 1.0, (2 if float(r["jump_v"])>0 else 3) if bool(r["air"]) else (1 if float(r["speed"])>190 else 0))

func _draw_bike() -> void:
    var ground_y := Rules.lane_y(lane_position)
    var ground := Terrain.height_at(features,distance,lane_position)
    var lift := maxf(0,altitude-ground)
    Scenery.oval(self,Vector2(BIKE_X+2,ground_y-ground+3),Vector2(14+minf(lift*0.08,5),2),Color(0.13,0.12,0.10,0.34-minf(lift*0.003,0.20)))
    if not airborne and crash_timer<=0 and speed>100:
        Scenery.dust(self,Vector2(BIKE_X,ground_y-ground),distance,Color("#f4cb96"),1.0 if boost_active else 0.6)
    if crash_timer > 0:
        Scenery.crash(self,Vector2(BIKE_X,ground_y-ground),Color("#ee754c"),crash_timer,_track_transform())
    else:
        var balance_lift := absf(sin(bike_tilt))*12.0 if stunt_active else 0.0
        var visible_tilt := bike_tilt+sin(visual_time*22)*stunt_strain*0.06
        _draw_pixel_bike(Vector2(BIKE_X,ground_y-altitude-balance_lift),Color("#ee754c"),CREAM,visible_tilt,1.0,(2 if jump_velocity>0 else 3) if airborne else (1 if boost_active else 0))

func _draw_pixel_bike(center: Vector2, body: Color, suit: Color, pitch: float, scale_factor: float, pose: int = 0) -> void:
    Scenery.bike(self, center, body, suit, pitch, scale_factor, distance / 8.0, visual_time, 7, _track_transform(), pose)

func _draw_particles() -> void:
    for particle in sparks:
        var pos: Vector2 = particle["p"]
        var alpha: float = clampf(float(particle["life"]) * 2.0, 0.0, 1.0)
        var color: Color = particle["color"]
        color.a = alpha
        draw_rect(Rect2(pos.round(),Vector2(2,2)),color)

func _draw_hud() -> void:
    draw_rect(Rect2(0,0,480,30),Color("#10172b"))
    var ink := Color("#f8ecd2")
    var blue := Color("#789fc4")
    Pixel.text(self,"TIME",Vector2(12,2),1,Color("#eb9764"))
    Pixel.text(self,"%04.1f" % elapsed,Vector2(12,13),2,ink)
    Pixel.text(self,"BUSES" if stunt_race else "PLACE",Vector2(107,2),1,Color("#eb9764"))
    Pixel.text(self,"%02d" % stunt_cleared if stunt_race else "%d/4" % race_place,Vector2(108,13),2,ink)
    Pixel.text(self,"COOLING" if overheated else "ENGINE TEMP",Vector2(204,2),1,RED if overheated else Color("#eb9764"))
    draw_rect(Rect2(201,12,116,15),blue,false,1)
    for i in range(18):
        var tint := Color("#8ccd59") if i<10 else (Color("#f5ca56") if i<15 else Color("#e96242"))
        draw_rect(Rect2(205+i*6,15,4,9),tint if float(i)*100/18 < heat else Color("#273b51"))
    Pixel.text(self,"KM/H",Vector2(341,2),1,Color("#eb9764"))
    Pixel.text(self,"%03d" % int(speed*0.36),Vector2(341,13),2,ink)
    draw_rect(Rect2(447,4,28,22),blue,false,1)
    draw_rect(Rect2(455,9,3,12),ink)
    draw_rect(Rect2(463,9,3,12),ink)
    draw_rect(Rect2(0,30,480*clampf(distance/_course_length(),0,1),2),Color("#f9d375"))
    var course: String = "CUSTOM TRACK" if custom_race else Rules.TRACK_NAMES[stage]
    if stunt_race: course="STUNT SHOW / %d BUSES" % stunt_bus_count
    Pixel.text(self,course,Vector2(470-Pixel.width(course),60),1,ink)

func _boost_state() -> String:
    if mode!="race" or countdown>0 or crash_timer>0: return "WAIT"
    if overheated: return "COOL"
    if boost_active: return "ACTIVE"
    return "READY"

func _draw_touch_controls() -> void:
    if computer_mode:
        Pixel.text(self,"COMPUTER DEMO",Vector2(12,72),1,Color("#283a3e"))
        return
    Pixel.text(self,"LEFT/RIGHT: BALANCE / HOLD: BOOST" if stunt_race else "PAD: STEER / LEAN / DOWN-DIAGONAL: BALANCE",Vector2(12,72),1,Color("#283a3e"))
    if stunt_active:
        var label := "WHEELIE" if stunt_angle<0 else "FRONT WHEELIE"
        var tint := RED if stunt_strain>0.25 else YELLOW
        draw_rect(Rect2(110,241,260,17),INK)
        Pixel.text(self,label,Vector2(116,245),1,tint)
        draw_rect(Rect2(216,246,145,4),Color("#35465a"))
        draw_rect(Rect2(216,246,145*clampf(absf(stunt_angle)/1.08,0,1),4),tint)
        draw_rect(Rect2(216+145*0.78/1.08,244,1,8),CREAM)
    # Compact visuals with generous touch targets for two-thumb play.
    draw_circle(STICK_CENTER, 29 * CONTROL_VISUAL_SCALE, Color(0.05, 0.09, 0.14, 0.48))
    draw_arc(STICK_CENTER, 28 * CONTROL_VISUAL_SCALE, 0, TAU, 40, Color("#dce8e3"), 1.4)
    for dir in [Vector2(0, -1), Vector2(1, 0), Vector2(0, 1), Vector2(-1, 0)]:
        var p: Vector2 = STICK_CENTER + dir * 22.0 * CONTROL_VISUAL_SCALE
        draw_rect(Rect2(p.x - 2, p.y - 2, 4, 4), Color("#d3d7d4"))
    draw_circle(STICK_CENTER + stick_visual * 17.0 * CONTROL_VISUAL_SCALE, 11 * CONTROL_VISUAL_SCALE, Color(0.74, 0.77, 0.79, 0.72))
    draw_circle(STICK_CENTER + stick_visual * 17.0 * CONTROL_VISUAL_SCALE, 7 * CONTROL_VISUAL_SCALE, Color(0.88, 0.87, 0.83, 0.47))
    var state := _boost_state()
    var pulse := (sin(visual_time*4.0)+1.0)*0.5
    var accent := CYAN if state=="READY" else (YELLOW if state=="ACTIVE" else Color("#91aac0"))
    var fill := Color("#335361") if state in ["COOL","WAIT"] else Color("#b94740")
    if state=="ACTIVE": fill=Color("#df623c").lerp(Color("#f5a64c"),pulse*0.4)
    draw_circle(BOOST_CENTER, 31 * CONTROL_VISUAL_SCALE, Color(0.10, 0.09, 0.15, 0.55))
    draw_circle(BOOST_CENTER, 26 * CONTROL_VISUAL_SCALE, fill)
    draw_arc(BOOST_CENTER, 30 * CONTROL_VISUAL_SCALE, 0, TAU, 48, accent.darkened(0.65), 2.0)
    var available := clampf((100.0-heat)/(100.0-Rules.HEAT_UNLOCK),0,1) if state=="COOL" else (100.0-heat)/100.0
    if state!="WAIT" and available>0:
        draw_arc(BOOST_CENTER,30*CONTROL_VISUAL_SCALE,-PI/2,-PI/2+TAU*available,48,accent,2.0)
    if state=="READY":
        draw_arc(BOOST_CENTER,(32+pulse*2)*CONTROL_VISUAL_SCALE,0,TAU,48,Color(accent,0.2+pulse*0.4),1.0)
    elif state=="ACTIVE":
        draw_arc(BOOST_CENTER,33*CONTROL_VISUAL_SCALE,visual_time*6,visual_time*6+PI*0.7,24,YELLOW,2.0)
    _center_at("BOOST", BOOST_CENTER + Vector2(0, -1), 12, CREAM)
    _center_at(state, BOOST_CENTER + Vector2(0, 10), 10, accent)

func _draw_menu() -> void:
    draw_rect(Rect2(0,0,480,270),Color(0.07,0.10,0.16,0.35))
    _panel(Rect2(51,61,378,101),Color("#192940"),Color("#f6d585"))
    _draw_checkers(Rect2(54,64,372,4),4)
    Pixel.text(self,"MOTO THRASH",Vector2(135,84),4,Color("#9d5736"))
    Pixel.text(self,"MOTO THRASH",Vector2(132,81),4,Color("#ffedb0"))
    Pixel.text(self,"24 TRACKS / 4 CUPS",Vector2(183,121),1,Color("#91cfd1"))
    Pixel.text(self,Rules.CUP_NAMES[stage/6],Vector2(190,137),1,Color("#f8ecd2"))
    Pixel.text(self,"MEDAL: "+(best_medals[stage] if best_medals[stage]!="" else "NONE"),Vector2(190,151),1,Color("#f6d585"))
    Scenery.bike(self,Vector2(102,143),Color("#ee754c"),CREAM,-0.16,1.5,visual_time,visual_time)
    _button(Rect2(24,176,137,31),"RACE NOW",YELLOW)
    _button(Rect2(171,176,137,31),"WATCH COMPUTER",CYAN)
    _button(Rect2(318,176,137,31),"TRACK BUILDER",Color("#a7b6ab"))
    _button(Rect2(118,223,44,26),"<",CYAN)
    _button(Rect2(318,223,44,26),">",CYAN)
    _panel(Rect2(162,223,156,26),INK,CYAN)
    _center_text("%02d / %s" % [stage+1,Rules.TRACK_NAMES[stage]],240,10,CREAM)
    Pixel.text(self,"ENTER: RACE   C: COMPUTER   TAB: COURSE",Vector2(129,258),1,CREAM)

func _draw_pause() -> void:
    draw_rect(Rect2(0, 0, 480, 270), Color(0.04, 0.08, 0.13, 0.72))
    _panel(Rect2(118, 83, 244, 150), INK, CYAN)
    _center_text("PAUSED", 128, 27, YELLOW)
    _button(Rect2(159, 151, 162, 28), "RESUME", YELLOW)
    _button(Rect2(159, 190, 162, 28), "EDIT STUNT" if stunt_race else ("EDIT TRACK" if custom_race else "TRACK SELECT"), CYAN)

func _draw_finish() -> void:
    if stunt_race:
        _draw_stunt_finish()
        return
    draw_rect(Rect2(0, 0, 480, 270), Color(0.04, 0.08, 0.13, 0.78))
    _panel(Rect2(98, 34, 284, 215), INK, YELLOW)
    _button(Rect2(103,39,66,23),"BACK",CYAN)
    _center_text("FINISH LINE!", 78, 20, YELLOW)
    _center_text(medal, 98, 23, CYAN)
    _center_text("TIME   %.2fs" % elapsed, 120, 17, CREAM)
    _center_text("PLACE %d/4 / CRASHES %d" % [race_place,crash_count], 141, 12, CREAM)
    if computer_mode:
        _center_text("DEMO RUN / NO RECORDS SAVED", 161, 12, CYAN)
    elif new_record:
        _center_text("NEW PERSONAL BEST!", 161, 13, YELLOW)
    else:
        var best: float = custom_times[custom_slot] if custom_race else best_times[stage]
        _center_text("BEST  %.2fs" % best, 161, 13, CREAM)
    _button(Rect2(156, 174, 168, 28), "EDIT TRACK" if custom_race else ("NEXT TRACK" if stage < Rules.TRACK_COUNT - 1 else "RACE AGAIN"), YELLOW)
    _button(Rect2(156, 211, 168, 28), "RETRY", CYAN)

func _open_stunt_setup() -> void:
    stunt_race=true
    custom_race=false
    computer_mode=false
    mode="stunt_setup"
    _prepare_preview()

func _start_stunt() -> void:
    stunt_race=true
    custom_race=false
    computer_mode=false
    _save_progress()
    _start_race()

func _click_stunt_setup(point: Vector2) -> void:
    if Rect2(84,222,100,28).has_point(point):
        _back_action()
    elif Rect2(198,222,198,28).has_point(point):
        _start_stunt()
    else:
        for row in range(3):
            var offset := 0
            if Rect2(268,93+row*34,28,27).has_point(point): offset=-1
            if Rect2(358,93+row*34,28,27).has_point(point): offset=1
            if offset==0: continue
            match row:
                0: stunt_bus_count=clampi(stunt_bus_count+offset,1,20)
                1: stunt_ramp_setting=clampi(stunt_ramp_setting+offset,0,2)
                2: stunt_speed_setting=clampi(stunt_speed_setting+offset,0,2)
            _prepare_preview()
            _save_progress()

func _draw_stunt_setup() -> void:
    draw_rect(Rect2(0,0,480,270),Color(0.04,0.08,0.13,0.65))
    _panel(Rect2(70,39,340,217),INK,YELLOW)
    _center_text("STUNT SHOW",72,30,YELLOW)
    var values := [str(stunt_bus_count),["LOW","MID","HIGH"][stunt_ramp_setting],["FAST","FASTER","MAX"][stunt_speed_setting]]
    for row in range(3):
        _text(["BUS LINE","RAMP SIZE","RUN-UP SPEED"][row],Vector2(92,110+row*34),12,CREAM)
        _button(Rect2(268,93+row*34,28,27),"-",CYAN)
        _center_at(values[row],Vector2(327,111+row*34),10,YELLOW)
        _button(Rect2(358,93+row*34,28,27),"+",CYAN)
    _center_text("BEST: %d POINTS / %d BUSES" % [stunt_best_score,stunt_best_buses],205,10,CYAN)
    _button(Rect2(84,222,100,28),"BACK",CYAN)
    _button(Rect2(198,222,198,28),"BUILD + JUMP",YELLOW)

func _draw_stunt_course() -> void:
    var span := maxf(1000,stunt_bus_count*Stunt.BUS_SPACING+650)
    var camera := clampf(distance-300,0,Stunt.TAKEOFF-250)
    var sx := 456.0/span
    var sy := 0.25
    var floor_y := 190.0
    for grass_y in range(112,190,44):
        var height := minf(44,floor_y-grass_y)
        for tile in range(-1,3):
            draw_texture_rect_region(Scenery._grass_texture(0,posmod(tile,8)),Rect2(tile*320-floorf(fposmod(camera*sx,320)),grass_y,320,height),Rect2(0,4,320,height))
    draw_rect(Rect2(0,floor_y,480,80),Color("#775b34"))
    draw_rect(Rect2(0,floor_y,480,2),CREAM)
    for i in range(-1,32):
        var x := fposmod(i*24-camera*sx,500)-10
        draw_rect(Rect2(x,floor_y+10,12,1),Color("#bc9857"))
    for feature in features:
        var x := 12+(float(feature["x"])-camera)*sx
        var kind := str(feature["kind"])
        if kind=="bus":
            var width := Rules.hazard_width(kind)*sx
            var height := width*30.0/72.0
            draw_texture_rect(Scenery._hazard_sprite(kind),Rect2(x-width/2,floor_y-height,width,height),false)
        else:
            var profile := Terrain.profile(kind)
            Scenery.poly(self,[Vector2(x-profile.x*sx,floor_y),Vector2(x,floor_y-profile.w*sy),Vector2(x,floor_y)],Color("#cb914d"))
            draw_line(Vector2(x-profile.x*sx,floor_y),Vector2(x,floor_y-profile.w*sy),YELLOW,2)
            draw_rect(Rect2(x,floor_y-profile.w*sy-12,1,12),CREAM)
            draw_rect(Rect2(x+1,floor_y-profile.w*sy-12,8,5),YELLOW)
    var landing := 12+(Stunt.end_of_buses(stunt_bus_count)+20-camera)*sx
    draw_rect(Rect2(landing,floor_y-2,maxf(0,480-landing),3),CYAN)
    if mode!="stunt_setup":
        var at := Vector2(12+(distance-camera)*sx,floor_y-altitude*sy)
        Scenery.bike(self,at,Color("#ee754c"),CREAM,bike_tilt,0.55,0,visual_time,7,Transform2D.IDENTITY,(2 if jump_velocity>0 else 3) if airborne else 0)
    if stunt_race and mode=="race" and airborne:
        _center_text("%d BUSES CLEARED" % stunt_cleared,112,12,YELLOW)

func _draw_stunt_finish() -> void:
    draw_rect(Rect2(0,0,480,270),Color(0.04,0.08,0.13,0.72))
    if stunt_success:
        for i in range(35):
            var y := fposmod(visual_time*24+i*37,270)
            draw_rect(Rect2(posmod(i*97,480),y,3,2),YELLOW if i%2 else CYAN)
    _panel(Rect2(98,34,284,215),INK,YELLOW if stunt_success else RED)
    _button(Rect2(103,39,66,23),"BACK",CYAN)
    _center_text("LANDED!" if stunt_success else "TRY AGAIN!",87,24,YELLOW if stunt_success else RED)
    _center_text("%d / %d BUSES CLEARED" % [stunt_cleared,stunt_bus_count],111,12,CREAM)
    _center_text("DISTANCE %d / SCORE %d" % [roundi(stunt_jump_distance),stunt_score],135,10,CREAM)
    _center_text("NEW STUNT RECORD!" if new_record else "BEST SCORE: %d" % stunt_best_score,158,12,CYAN)
    _button(Rect2(156,174,168,28),"EDIT STUNT",YELLOW)
    _button(Rect2(156,211,168,28),"RETRY",CYAN)

func _draw_editor() -> void:
    # Track and obstacles are the actual live game rendering.
    draw_rect(Rect2(0, 0, 480, 35), INK)
    draw_rect(Rect2(0, 34, 480, 2), YELLOW)
    _text("TRACK BUILDER", Vector2(8, 21), 18, YELLOW)
    _draw_checkers(Rect2(0, 34, 480, 4), 4.0)
    _button(Rect2(252, 2, 27, 28), "<", CYAN)
    _text("SLOT %d/6" % (custom_slot + 1), Vector2(296, 21), 15, CREAM)
    _button(Rect2(445, 2, 32, 28), ">", CYAN)
    _button(Rect2(4, 40, 56, 25), "BACK", CYAN)
    _button(Rect2(65, 40, 34, 25), "<", CYAN)
    _text("AREA %d/10" % (edit_page + 1), Vector2(107, 57), 12, CREAM)
    _button(Rect2(192, 40, 34, 25), ">", CYAN)
    _button(Rect2(233, 40, 59, 25), "UNDO", CYAN)
    _button(Rect2(298, 40, 74, 25), "CONFIRM" if clear_confirm else "CLEAR", RED)
    _button(Rect2(378, 40, 97, 25), "TEST RIDE", YELLOW)
    _button(Rect2(378, 72, 97, 23), "CPU TEST", CYAN)
    for lane in range(Rules.LANE_COUNT):
        var y := Rules.lane_y(float(lane))
        draw_line(Vector2(8, y + 9), Vector2(472, y + 9), Color(CREAM.r, CREAM.g, CREAM.b, 0.12), 1.0)
    draw_rect(Rect2(0, 224, 480, 46), INK)
    _text("SELECT TOOL / TAP LANE", Vector2(8, 234), 10, CREAM)
    _button(Rect2(374,224,102,15),"TOOLS %d/2" % (edit_palette+1),CYAN)
    var labels := Builder.LABELS
    var tools := Builder.TOOLS
    var tool_width := 60.0
    for i in range(8):
        var index := edit_palette*8+i
        var x := float(i) * tool_width + 2.0
        var rect := Rect2(x, 240, tool_width-4, 27)
        _panel(rect, Color("#35465a"), YELLOW if edit_tool == tools[index] else CYAN.darkened(0.45))
        _center_at(labels[index], rect.position + Vector2(rect.size.x/2, 18), 12, CREAM)

func _draw_checkers(area: Rect2, cell: float) -> void:
    for row in range(int(ceilf(area.size.y / cell))):
        for col in range(int(ceilf(area.size.x / cell))):
            draw_rect(Rect2(area.position + Vector2(float(col) * cell, float(row) * cell), Vector2(cell, cell)), CREAM if (row + col) % 2 == 0 else INK)

func _button(rect: Rect2, caption: String, border: Color) -> void:
    var hover := rect.has_point(get_global_mouse_position())
    var primary := border == YELLOW
    var fill := Color("#edaa65") if primary else Color("#22444b")
    if hover:
        fill = fill.lightened(0.14)
    draw_rect(Rect2(rect.position+Vector2(0,2),rect.size),Color(0.02,0.08,0.10,0.4))
    draw_rect(rect,fill)
    draw_rect(rect,border if primary else Color("#547b7b"),false,0.8)
    draw_line(rect.position+Vector2(1,1),rect.position+Vector2(rect.size.x-1,1),Color(1,0.95,0.77,0.17),0.8,true)
    _center_at(caption,rect.position+Vector2(rect.size.x*0.5,rect.size.y*0.5+3.5),10 if caption.length()>10 else 12,Color("#183940") if primary else CREAM)

func _panel(rect: Rect2, color: Color, edge: Color) -> void:
    draw_rect(Rect2(rect.position+Vector2(0,3),rect.size),Color(0.02,0.07,0.1,0.4))
    draw_rect(rect,color)
    draw_rect(rect,edge,false,0.8)
    draw_line(rect.position,rect.position+Vector2(rect.size.x,0),edge,1.5,true)

func _text(content: String, at: Vector2, size: int, tint: Color) -> void:
    var scale := maxi(1,roundi(float(size)/10.0))
    Pixel.text(self,content,at-Vector2(0,7*scale),scale,tint)

func _center_text(content: String, y: float, size: int, tint: Color) -> void:
    var scale := maxi(1,roundi(float(size)/10.0))
    Pixel.text(self,content,Vector2((480-Pixel.width(content,scale))/2,y-7*scale),scale,tint)

func _center_at(content: String, center: Vector2, size: int, tint: Color) -> void:
    var scale := maxi(1,roundi(float(size)/10.0))
    Pixel.text(self,content,Vector2(center.x-Pixel.width(content,scale)/2,center.y-7*scale),scale,tint)

func _course_length() -> float:
    if stunt_race: return Stunt.length(stunt_bus_count)
    return Builder.COURSE_LENGTH if custom_race or mode == "editor" else float(Rules.TRACK_LENGTHS[stage])

func _open_editor() -> void:
    stunt_race=false
    computer_mode = false
    _clear_controls()
    custom_race = false
    mode = "editor"
    edit_page = 0
    edit_tool = "ramp"
    edit_palette = 0
    undo_stack.clear()
    clear_confirm = false
    _preview_editor()

func _preview_editor() -> void:
    features = Builder.for_race(custom_courses[custom_slot])
    terrain_cache = Terrain.build_cache(features,Builder.COURSE_LENGTH)
    distance = float(edit_page) * Builder.PAGE_STEP + BIKE_X
    queue_redraw()

func _set_editor_slot(offset: int) -> void:
    custom_slot = posmod(custom_slot + offset, Builder.SLOT_COUNT)
    edit_page = 0
    undo_stack.clear()
    clear_confirm = false
    _preview_editor()

func _set_edit_page(offset: int) -> void:
    edit_page = clampi(edit_page + offset, 0, Builder.PAGE_COUNT - 1)
    clear_confirm = false
    _preview_editor()

func _builder_test(watch: bool = false) -> void:
    computer_mode=watch
    custom_race = true
    _start_race()

func _builder_back() -> void:
    custom_race = false
    mode = "menu"
    _prepare_preview()

func _builder_undo() -> void:
    clear_confirm = false
    if undo_stack.is_empty():
        return
    custom_courses[custom_slot] = undo_stack.pop_back()
    custom_times[custom_slot] = 0.0
    _save_progress()
    _preview_editor()

func _builder_place(point: Vector2) -> void:
    clear_confirm = false
    var lane := clampi(roundi((point.y - 126.0) / 26.0), 0, Rules.LANE_COUNT - 1)
    var x := float(edit_page) * Builder.PAGE_STEP + point.x
    var old: Array = custom_courses[custom_slot]
    var updated := Builder.edit(old, edit_tool, x, lane)
    if updated == Builder.sanitize(old):
        return
    undo_stack.append(old.duplicate(true))
    if undo_stack.size() > 20:
        undo_stack.pop_front()
    custom_courses[custom_slot] = updated
    custom_times[custom_slot] = 0.0
    _save_progress()
    _preview_editor()

func _builder_clear() -> void:
    if not clear_confirm:
        clear_confirm = true
        queue_redraw()
        return
    clear_confirm = false
    var old: Array = custom_courses[custom_slot]
    if old.is_empty():
        return
    undo_stack.append(old.duplicate(true))
    if undo_stack.size() > 20:
        undo_stack.pop_front()
    custom_courses[custom_slot] = []
    custom_times[custom_slot] = 0.0
    _save_progress()
    _preview_editor()

func _click_editor(point: Vector2) -> void:
    if Rect2(252, 2, 27, 28).has_point(point):
        _set_editor_slot(-1)
    elif Rect2(445, 2, 32, 28).has_point(point):
        _set_editor_slot(1)
    elif Rect2(4, 40, 56, 25).has_point(point):
        _builder_back()
    elif Rect2(65, 40, 34, 25).has_point(point):
        _set_edit_page(-1)
    elif Rect2(192, 40, 34, 25).has_point(point):
        _set_edit_page(1)
    elif Rect2(233, 40, 59, 25).has_point(point):
        _builder_undo()
    elif Rect2(298, 40, 74, 25).has_point(point):
        _builder_clear()
    elif Rect2(378, 40, 97, 25).has_point(point):
        _builder_test()
    elif Rect2(378,72,97,23).has_point(point):
        _builder_test(true)
    elif Rect2(374,224,102,15).has_point(point):
        edit_palette=1-edit_palette
    elif point.y >= 239.0:
        var selected: int = edit_palette*8+clampi(int(point.x/60.0),0,7)
        edit_tool = Builder.TOOLS[selected]
        clear_confirm = false
    elif point.y >= 93.0 and point.y <= 223.0 and point.x >= 8.0 and point.x <= 472.0:
        _builder_place(point)
    queue_redraw()
