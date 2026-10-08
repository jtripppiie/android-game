extends Node2D
## Dirt Rush: a small, original, landscape, pixel-art motocross game.
## Single 480 x 270 canvas + procedural graphics: no downloaded art or IP.

const Rules = preload("res://scripts/race_rules.gd")
const Builder = preload("res://scripts/track_builder.gd")
const Scenery = preload("res://scripts/retro_scenery.gd")
const SIZE := Vector2(480.0, 270.0)
const BIKE_X := 124.0
const STICK_CENTER := Vector2(65.0, 226.0)
const BOOST_CENTER := Vector2(421.0, 225.0)
const SAVE_FILE := "user://dirt_rush.cfg"
var font: Font = null

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
var stage := 0
var unlocked := 0
var best_times: Array[float] = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
var features: Array[Dictionary] = []
var custom_courses: Array = [[], [], []]
var custom_times: Array[float] = [0.0, 0.0, 0.0]
var custom_slot := 0
var custom_race := false
var edit_page := 0
var edit_tool := "ramp"
var undo_stack: Array = []
var clear_confirm := false
var distance := 0.0
var speed := 110.0
var elapsed := 0.0
var countdown := 2.3
var lane_position := 1.5
var lane_velocity := 0.0
var altitude := 0.0
var jump_velocity := 0.0
var bike_tilt := 0.0
var heat := 0.0
var overheated := false
var slow_timer := 0.0
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
    font = ThemeDB.fallback_font
    _load_progress()
    _prepare_preview()
    set_process(true)
    set_physics_process(true)

func _prepare_preview() -> void:
    features = Builder.for_race(custom_courses[custom_slot]) if custom_race else Rules.generate_features(stage)
    distance = 0.0
    speed = 110.0
    lane_position = 1.5
    altitude = 0.0
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

func _start_race() -> void:
    _prepare_preview()
    elapsed = 0.0
    heat = 0.0
    overheated = false
    countdown = 2.2
    jump_velocity = 0.0
    crash_timer = 0.0
    crash_count = 0
    slow_timer = 0.0
    dust_timer = 0.0
    message = ""
    message_timer = 0.0
    medal = ""
    new_record = false
    _reset_rivals()
    mode = "race"
    queue_redraw()

func _physics_process(delta: float) -> void:
    visual_time += delta
    stick_visual = stick_visual.move_toward(touch_vector, delta * 9.0)
    if mode == "race":
        if countdown > 0.0:
            countdown = maxf(0.0, countdown - delta)
        else:
            _race_step(delta)
    queue_redraw()

func _race_step(dt: float) -> void:
    elapsed += dt
    slow_timer = maxf(0.0, slow_timer - dt)
    crash_timer = maxf(0.0, crash_timer - dt)
    message_timer = maxf(0.0, message_timer - dt)
    var joystick := Input.get_vector("move_left", "move_right", "move_up", "move_down")
    if touch_vector.length() > joystick.length():
        joystick = touch_vector
    var boost_pressed: bool = Input.is_action_pressed("boost") or boost_touch_id != -1
    boost_active = boost_pressed and not overheated and crash_timer <= 0.0
    heat = Rules.heat_step(heat, boost_active, dt)
    if heat >= Rules.HEAT_MAX:
        heat = Rules.HEAT_MAX
        overheated = true
        boost_active = false
        message = "ENGINE HOT!"
        message_timer = 1.3
    elif overheated and heat <= Rules.HEAT_UNLOCK:
        overheated = false
        message = "BOOST READY"
        message_timer = 0.8

    var target_lane_speed := joystick.y * 2.3
    lane_velocity = move_toward(lane_velocity, target_lane_speed, 10.0 * dt)
    lane_position = clampf(lane_position + lane_velocity * dt, 0.0, 3.0)

    if crash_timer > 0.0:
        speed = move_toward(speed, 25.0, 240.0 * dt)
    else:
        var goal := Rules.target_speed(joystick.x, boost_active, overheated, slow_timer > 0.0)
        speed = move_toward(speed, goal, 115.0 * dt)

    if altitude > 0.0 or jump_velocity > 0.0:
        if absf(joystick.x) > 0.08:
            bike_tilt = clampf(bike_tilt + joystick.x * dt * 2.6, -1.1, 1.1)
        else:
            bike_tilt = move_toward(bike_tilt, 0.0, dt * 0.5)
        altitude += jump_velocity * dt
        jump_velocity -= 430.0 * dt
        if altitude <= 0.0:
            altitude = 0.0
            jump_velocity = 0.0
            if absf(bike_tilt) > 0.52:
                _crash("ROUGH LANDING")
            else:
                message = "NICE LANDING!"
                message_timer = 0.5
            bike_tilt = 0.0
    else:
        bike_tilt = move_toward(bike_tilt, 0.0, dt * 3.5)

    var last_distance := distance
    distance += speed * dt
    _step_rivals(dt)
    if crash_timer <= 0.0:
        _check_features(last_distance, distance)

    dust_timer += dt
    var interval := 0.055 if boost_active else 0.12
    if speed > 55.0 and altitude < 7.0 and dust_timer >= interval:
        dust_timer = 0.0
        _spawn_dust(BIKE_X - 16.0, Rules.lane_y(lane_position) - 1.0, CYAN if boost_active else DIRT_LIGHT)
    _update_particles(dt)
    if distance >= _course_length():
        _finish_race()

func _check_features(old_x: float, new_x: float) -> void:
    for i in range(features.size()):
        var feature: Dictionary = features[i]
        if bool(feature["used"]):
            continue
        var x: float = float(feature["x"])
        if x > new_x + 8.0 or x < old_x - 11.0:
            continue
        var lane_gap := absf(lane_position - float(feature["lane"]))
        if lane_gap > 0.38:
            continue
        feature["used"] = true
        features[i] = feature
        match str(feature["kind"]):
            "ramp", "big_ramp":
                if altitude < 8.0:
                    jump_velocity = (235.0 if str(feature["kind"]) == "big_ramp" else 172.0) + speed * 0.11
                    altitude = maxf(altitude, 1.0)
                    bike_tilt = -0.17
                    message = "AIR TIME!"
                    message_timer = 0.7
            "whoops":
                if altitude < 6.0:
                    jump_velocity = 92.0
                    altitude = 1.0
                    message = "WHOOPS!"
                    message_timer = 0.35
            "rock":
                if altitude < 12.0:
                    _crash("WIPEOUT!")
            "mud":
                if altitude < 6.0:
                    slow_timer = 1.3
                    message = "DEEP MUD"
                    message_timer = 0.9

func _crash(reason: String) -> void:
    if crash_timer > 0.0:
        return
    crash_count += 1
    crash_timer = 1.0
    speed = 40.0
    altitude = 0.0
    jump_velocity = 0.0
    bike_tilt = 0.0
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
    distance = _course_length()
    if custom_race:
        medal = "CUSTOM FINISH"
        var old_best: float = custom_times[custom_slot]
        new_record = old_best <= 0.0 or elapsed < old_best
        if new_record:
            custom_times[custom_slot] = elapsed
    else:
        medal = Rules.medal_for(elapsed, stage, crash_count)
        var previous: float = best_times[stage]
        new_record = previous <= 0.0 or elapsed < previous
        if new_record:
            best_times[stage] = elapsed
        if stage + 1 > unlocked:
            unlocked = mini(stage + 1, Rules.TRACK_COUNT - 1)
    _save_progress()
    _clear_controls()

func _load_progress() -> void:
    var cfg := ConfigFile.new()
    if cfg.load(SAVE_FILE) != OK:
        return
    unlocked = clampi(int(cfg.get_value("progress", "unlocked", 0)), 0, Rules.TRACK_COUNT - 1)
    for i in range(Rules.TRACK_COUNT):
        best_times[i] = maxf(0.0, float(cfg.get_value("records", str(i), 0.0)))
    for i in range(Builder.SLOT_COUNT):
        var saved: Variant = cfg.get_value("builder", "slot_%d" % i, [])
        custom_courses[i] = Builder.sanitize(saved)
        custom_times[i] = maxf(0.0, float(cfg.get_value("builder", "best_%d" % i, 0.0)))

func _save_progress() -> void:
    var cfg := ConfigFile.new()
    cfg.set_value("progress", "unlocked", unlocked)
    for i in range(Rules.TRACK_COUNT):
        cfg.set_value("records", str(i), best_times[i])
    for i in range(Builder.SLOT_COUNT):
        cfg.set_value("builder", "slot_%d" % i, Builder.sanitize(custom_courses[i]))
        cfg.set_value("builder", "best_%d" % i, custom_times[i])
    if cfg.save(SAVE_FILE) != OK:
        push_warning("Could not save Dirt Rush progress")

func _input(event: InputEvent) -> void:
    if event is InputEventKey and event.pressed and not event.echo:
        if event.keycode == KEY_ESCAPE or event.keycode == KEY_P:
            if mode == "race":
                mode = "pause"
                _clear_controls()
            elif mode == "pause":
                mode = "race"
            elif mode == "finish":
                mode = "menu"
                custom_race = false
                _prepare_preview()
            elif mode == "editor":
                _builder_back()
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
            elif event.keycode == KEY_1:
                edit_tool = "ramp"
            elif event.keycode == KEY_2:
                edit_tool = "big_ramp"
            elif event.keycode == KEY_3:
                edit_tool = "rock"
            elif event.keycode == KEY_4:
                edit_tool = "mud"
            elif event.keycode == KEY_5:
                edit_tool = "whoops"
            elif event.keycode == KEY_6:
                edit_tool = "erase"
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

func _on_pointer_down(point: Vector2, pointer_id: int) -> void:
    if mode == "editor":
        _click_editor(point)
        return
    if mode != "race":
        _click_menu(point)
        return
    if point.distance_to(Vector2(460, 20)) < 25.0:
        mode = "pause"
        _clear_controls()
    elif move_touch_id == -1 and point.distance_to(STICK_CENTER) < 71.0:
        move_touch_id = pointer_id
        _set_stick(point)
    elif boost_touch_id == -1 and point.distance_to(BOOST_CENTER) < 61.0:
        boost_touch_id = pointer_id
    queue_redraw()

func _on_pointer_up(pointer_id: int) -> void:
    if move_touch_id == pointer_id:
        move_touch_id = -1
        touch_vector = Vector2.ZERO
    if boost_touch_id == pointer_id:
        boost_touch_id = -1

func _set_stick(point: Vector2) -> void:
    touch_vector = ((point - STICK_CENTER) / 42.0).limit_length(1.0)
    if touch_vector.length() < 0.12:
        touch_vector = Vector2.ZERO

func _click_menu(point: Vector2) -> void:
    if mode == "menu":
        if Rect2(165, 153, 150, 29).has_point(point):
            custom_race = false
            _start_race()
        elif Rect2(165, 190, 150, 29).has_point(point):
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
            if custom_race:
                _open_editor()
            else:
                mode = "menu"
                _prepare_preview()
    elif mode == "finish":
        if Rect2(156, 174, 168, 28).has_point(point):
            if custom_race:
                _open_editor()
            else:
                stage = mini(unlocked, stage + 1)
                _start_race()
        elif Rect2(156, 211, 168, 28).has_point(point):
            _start_race()

func _primary_action() -> void:
    match mode:
        "menu": _start_race()
        "pause": mode = "race"
        "finish":
            if custom_race:
                _open_editor()
            else:
                stage = mini(unlocked, stage + 1)
                _start_race()
        "editor": _builder_test()

func _notification(what: int) -> void:
    if what == NOTIFICATION_APPLICATION_PAUSED or what == NOTIFICATION_WM_WINDOW_FOCUS_OUT:
        if mode == "race":
            mode = "pause"
            _clear_controls()

func _draw() -> void:
    _draw_background()
    _draw_tracks()
    _draw_features()
    if mode != "menu":
        _draw_particles()
    if mode != "editor":
        _draw_rivals()
        _draw_bike()
    Scenery.front(self, distance, _theme_index())
    if mode == "race" or mode == "pause":
        _draw_hud()
        _draw_touch_controls()
    match mode:
        "menu": _draw_menu()
        "pause": _draw_pause()
        "finish": _draw_finish()
        "editor": _draw_editor()
    if mode == "race":
        if countdown > 0.0:
            _text("%d" % maxi(1, int(ceilf(countdown))), Vector2(220, 94), 42, YELLOW)
            _text("GET READY", Vector2(198, 113), 13, CREAM)
        elif crash_timer > 0.0 or message_timer > 0.0:
            var txt_color := RED if crash_timer > 0.0 else YELLOW
            _panel(Rect2(162, 51, 156, 25), INK, txt_color)
            _center_text(message, 64, 13, txt_color)

func _theme_index() -> int:
    return custom_slot if custom_race or mode == "editor" else stage

func _draw_background() -> void:
    Scenery.paint(self, _theme_index(), distance, visual_time)

func _draw_tracks() -> void:
    draw_rect(Rect2(0, 104, 480, 151), DIRT_DARK)
    for lane in range(Rules.LANE_COUNT):
        var y := Rules.lane_y(float(lane))
        draw_rect(Rect2(0, y - 11, 480, 28), DIRT if lane % 2 == 0 else Color("#704632"))
        draw_rect(Rect2(0, y - 12, 480, 2), DIRT_LIGHT)
        draw_rect(Rect2(0, y + 16, 480, 2), Color("#352c30"))
        for j in range(-1, 17):
            var dash_x := float(j) * 34.0 - fposmod(distance, 34.0)
            draw_rect(Rect2(dash_x, y + 8, 12, 2), DIRT_LIGHT)
            draw_rect(Rect2(dash_x + 21, y - 7, 5, 2), DIRT_DARK)
    draw_rect(Rect2(0, 250, 480, 20), DIRT_DARK)
    Scenery.texture_track(self, distance, _theme_index())
    var goal_x: float = _course_length() - distance + BIKE_X
    if goal_x >= -15.0 and goal_x < 500.0:
        for lane in range(4):
            for j in range(2):
                draw_rect(Rect2(goal_x + float(j * 6), Rules.lane_y(float(lane)) - 10, 6, 27), CREAM if (lane + j) % 2 == 0 else INK)

func _draw_features() -> void:
    for feature in features:
        var sx: float = float(feature["x"]) - distance + BIKE_X
        if sx < -34.0 or sx > 500.0:
            continue
        var fy: float = Rules.lane_y(float(feature["lane"]))
        match str(feature["kind"]):
            "ramp":
                draw_colored_polygon(PackedVector2Array([
                    Vector2(sx - 24, fy + 6), Vector2(sx + 7, fy - 10),
                    Vector2(sx + 23, fy - 10), Vector2(sx + 25, fy + 6)
                ]), Color("#442e2b"))
                draw_colored_polygon(PackedVector2Array([
                    Vector2(sx - 23, fy + 5), Vector2(sx + 7, fy - 9),
                    Vector2(sx + 21, fy - 9), Vector2(sx + 23, fy + 5)
                ]), Color("#b77945"))
                draw_line(Vector2(sx - 22, fy + 4), Vector2(sx + 7, fy - 9), Color("#e9b471"), 2.0)
                draw_rect(Rect2(sx + 7, fy - 10, 15, 3), Color("#ebc084"))
                draw_rect(Rect2(sx + 4, fy - 1, 3, 2), Color("#715038"))
            "big_ramp":
                draw_colored_polygon(PackedVector2Array([
                    Vector2(sx - 29, fy + 6), Vector2(sx + 9, fy - 21),
                    Vector2(sx + 27, fy - 21), Vector2(sx + 29, fy + 6)
                ]), Color("#4b302d"))
                draw_colored_polygon(PackedVector2Array([
                    Vector2(sx - 27, fy + 5), Vector2(sx + 9, fy - 20),
                    Vector2(sx + 26, fy - 20), Vector2(sx + 27, fy + 5)
                ]), Color("#b07646"))
                draw_line(Vector2(sx - 27, fy + 4), Vector2(sx + 9, fy - 20), Color("#f0b86e"), 2.0)
                draw_rect(Rect2(sx + 9, fy - 21, 17, 3), Color("#efd1a0"))
                draw_rect(Rect2(sx - 3, fy - 2, 7, 2), Color("#744530"))
            "whoops":
                for step in range(3):
                    var xx: float = sx - 20.0 + float(step) * 13.0
                    draw_colored_polygon(PackedVector2Array([
                        Vector2(xx, fy + 6), Vector2(xx + 5, fy - 4 - float(step % 2) * 3),
                        Vector2(xx + 12, fy + 6)
                    ]), Color("#a46d41"))
                    draw_line(Vector2(xx + 1, fy + 4), Vector2(xx + 5, fy - 3 - float(step % 2) * 3), Color("#e0a96d"), 2.0)
            "rock":
                if int(float(feature["x"]) / 40.0) % 3 == 0:
                    for t in range(2):
                        draw_rect(Rect2(sx - 10 + t * 9, fy - 10, 12, 10), Color("#20252c"))
                        draw_circle(Vector2(sx - 4 + t * 9, fy - 5), 3, Color("#52585b"))
                else:
                    draw_rect(Rect2(sx - 8, fy - 5, 14, 10), Color("#3b4557"))
                    draw_rect(Rect2(sx - 5, fy - 9, 9, 5), Color("#64717b"))
                    draw_rect(Rect2(sx - 2, fy - 8, 4, 3), Color("#9ba0a2"))
            "mud":
                draw_rect(Rect2(sx - 17, fy + 1, 32, 6), Color("#3b2c2a"))
                draw_rect(Rect2(sx - 13, fy + 2, 27, 2), Color("#867079"))
                draw_rect(Rect2(sx - 6, fy + 2, 8, 1), Color("#aac1bc"))
                draw_rect(Rect2(sx + 7, fy + 3, 5, 1), Color("#b2c3c8"))

func _reset_rivals() -> void:
    rivals.clear()
    for i in range(3):
        rivals.append({
            "x": -42.0 - i * 27.0,
            "lane": float([0, 1, 3][i]),
            "speed": 108.0 + float(i * 11) + float(stage % 3 * 4),
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
        var rx: float = float(r["x"])
        var lane: float = float(r["lane"])
        var pace: float = 114.0 + float(i * 10) + float(stage % 3 * 3)
        pace += sin(elapsed * 0.45 + float(i * 2)) * 13.0
        r["speed"] = move_toward(float(r["speed"]), pace, dt * 18.0)
        r["x"] = rx + float(r["speed"]) * dt
        r["next_shift"] = float(r["next_shift"]) - dt
        if float(r["next_shift"]) <= 0.0:
            # Deliberately keep a recognizable rider on each lane most of the time.
            r["lane"] = float(posmod(i + int(elapsed / 12.0), 4))
            r["next_shift"] = 7.0 + float(i)
        if float(r["h"]) > 0.0 or float(r["jump_v"]) > 0.0:
            r["h"] = maxf(0.0, float(r["h"]) + float(r["jump_v"]) * dt)
            r["jump_v"] = float(r["jump_v"]) - 420.0 * dt
            if float(r["h"]) <= 0.0:
                r["jump_v"] = 0.0
        else:
            for j in range(features.size()):
                var feature: Dictionary = features[j]
                if absf(float(feature["x"]) - float(r["x"])) < 6.0 and int(feature["lane"]) == int(r["lane"]) and int(r["last_ramp"]) != j:
                    if str(feature["kind"]) in ["ramp", "big_ramp", "whoops"]:
                        r["jump_v"] = 215.0 if str(feature["kind"]) == "big_ramp" else 155.0
                        r["h"] = 1.0
                    r["last_ramp"] = j
                    break
        if float(r["x"]) > distance:
            race_place += 1
        rivals[i] = r

func _draw_rivals() -> void:
    for i in range(rivals.size()):
        var r: Dictionary = rivals[i]
        var px: float = BIKE_X + float(r["x"]) - distance
        if px < -34.0 or px > 520.0:
            continue
        var floor_y: float = Rules.lane_y(float(r["lane"]))
        var color: Color = [Color("#347de3"), Color("#4db96c"), Color("#e9bc3d")][i]
        draw_rect(Rect2(px - 18, floor_y + 3, 38, 3), Color(0.09, 0.07, 0.09, 0.28))
        _draw_pixel_bike(Vector2(px, floor_y - float(r["h"])), color, color.lightened(0.35), 0.0, 0.84)

func _draw_bike() -> void:
    var ground_y := Rules.lane_y(lane_position)
    draw_rect(Rect2(BIKE_X - 21, ground_y + 4, 43, 4), Color(0.1, 0.09, 0.1, 0.33))
    _draw_pixel_bike(Vector2(BIKE_X, ground_y - altitude), Color("#e7473c"), CREAM, bike_tilt, 1.0)

func _draw_pixel_bike(center: Vector2, body: Color, suit: Color, pitch: float, scale_factor: float) -> void:
    draw_set_transform(center, pitch, Vector2(scale_factor, scale_factor))
    # Pixelated wheels, tire hubs, fork and angled bodywork. All original shapes.
    for wheel in [-16.0, 17.0]:
        draw_rect(Rect2(wheel - 7, -6, 14, 14), Color("#171c27"))
        draw_rect(Rect2(wheel - 5, -7, 10, 16), Color("#191e25"))
        draw_rect(Rect2(wheel - 4, -4, 8, 9), Color("#a6a3a0"))
        draw_rect(Rect2(wheel - 2, -2, 4, 5), Color("#28323e"))
        draw_rect(Rect2(wheel - 1, -5, 2, 12), Color("#d5ced0"))
    draw_line(Vector2(-16, 0), Vector2(-6, -12), Color("#aabdc9"), 2.0)
    draw_line(Vector2(17, 0), Vector2(10, -15), Color("#bec3c4"), 2.0)
    draw_line(Vector2(-6, -12), Vector2(10, -15), Color("#252a32"), 3.0)
    draw_rect(Rect2(-25, -12, 9, 3), body)
    draw_rect(Rect2(16, -13, 9, 3), body)
    draw_colored_polygon(PackedVector2Array([
        Vector2(-16, -14), Vector2(-7, -20), Vector2(6, -20),
        Vector2(17, -14), Vector2(8, -9), Vector2(-7, -8)
    ]), body)
    draw_rect(Rect2(-9, -18, 8, 4), Color("#ffffff"))
    draw_rect(Rect2(-12, -22, 15, 3), Color("#182b38"))
    draw_rect(Rect2(-1, -21, 4, 7), Color("#2a3444"))
    # Boot, bent leg, gloved grip, vest and recognizable helmet silhouette.
    draw_rect(Rect2(-10, -12, 12, 4), Color("#353945"))
    draw_rect(Rect2(-6, -17, 6, 7), suit.darkened(0.22))
    draw_rect(Rect2(0, -24, 9, 14), suit)
    draw_rect(Rect2(5, -25, 12, 4), Color("#202b3a"))
    draw_rect(Rect2(0, -35, 14, 11), suit)
    draw_rect(Rect2(2, -37, 12, 4), body)
    draw_rect(Rect2(10, -32, 8, 4), Color("#161e31"))
    draw_rect(Rect2(0, -35, 2, 8), body)
    draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)

func _draw_particles() -> void:
    for particle in sparks:
        var pos: Vector2 = particle["p"]
        var alpha: float = clampf(float(particle["life"]) * 2.0, 0.0, 1.0)
        var color: Color = particle["color"]
        color.a = alpha
        draw_rect(Rect2(pos.x, pos.y, 3, 3), color)

func _draw_hud() -> void:
    draw_rect(Rect2(0, 0, 480, 39), Color("#101523"))
    _draw_checkers(Rect2(0, 0, 36, 5), 5.0)
    _draw_checkers(Rect2(450, 34, 30, 5), 5.0)
    draw_rect(Rect2(0, 38, 480, 2), Color("#ee6749"))
    _text("DIRT", Vector2(7, 22), 21, CREAM)
    _text("RUSH", Vector2(50, 22), 21, Color("#f0614a"))
    draw_rect(Rect2(103, 5, 1, 30), Color("#526171"))
    _text("TIME", Vector2(110, 13), 10, Color("#a9bccc"))
    _text("%05.1f" % elapsed, Vector2(108, 30), 17, CREAM)
    draw_rect(Rect2(182, 5, 1, 30), Color("#526171"))
    _text("COURSE", Vector2(191, 13), 10, Color("#a9bccc"))
    _text(("BUILD %d" % (custom_slot + 1)) if custom_race else Rules.TRACK_NAMES[stage], Vector2(190, 29), 12, YELLOW)
    draw_rect(Rect2(305, 5, 1, 30), Color("#526171"))
    _text("HEAT", Vector2(312, 13), 10, Color("#a9bccc"))
    draw_rect(Rect2(312, 18, 68, 9), Color("#3b4151"))
    for i in range(10):
        var active := float(i + 1) * 10.0 <= heat
        var tint := Color("#63db71") if i < 5 else (Color("#ffd15c") if i < 8 else Color("#f2594a"))
        draw_rect(Rect2(314.0 + i * 6.4, 20, 5, 5), tint if active else Color("#1e2938"))
    if overheated:
        _text("!", Vector2(380, 27), 13, RED)
    draw_rect(Rect2(395, 5, 1, 30), Color("#526171"))
    _text("PLACE", Vector2(403, 13), 10, Color("#a9bccc"))
    _text("%d / 4" % race_place, Vector2(405, 29), 16, CREAM)
    # Thin course progress strip across the bottom of the instrument panel.
    draw_rect(Rect2(108, 34, 278, 2), Color("#4a5663"))
    draw_rect(Rect2(108, 34, 278 * clampf(distance / _course_length(), 0, 1), 2), Color("#f0ca5e"))
    draw_rect(Rect2(451, 6, 27, 26), Color("#334256"))
    draw_rect(Rect2(459, 11, 3, 15), CREAM)
    draw_rect(Rect2(467, 11, 3, 15), CREAM)

func _draw_touch_controls() -> void:
    # Original low-contrast analog glass, large enough for two thumbs.
    draw_circle(STICK_CENTER, 39, Color(0.05, 0.09, 0.14, 0.48))
    draw_arc(STICK_CENTER, 38, 0, TAU, 40, Color("#dce8e3"), 1.4)
    for dir in [Vector2(0, -1), Vector2(1, 0), Vector2(0, 1), Vector2(-1, 0)]:
        var p: Vector2 = STICK_CENTER + dir * 30.0
        draw_rect(Rect2(p.x - 2, p.y - 2, 4, 4), Color("#d3d7d4"))
    draw_circle(STICK_CENTER + stick_visual * 23.0, 15, Color(0.74, 0.77, 0.79, 0.72))
    draw_circle(STICK_CENTER + stick_visual * 23.0, 10, Color(0.88, 0.87, 0.83, 0.47))
    var pushed := boost_touch_id != -1 or Input.is_action_pressed("boost")
    draw_circle(BOOST_CENTER, 41, Color(0.10, 0.09, 0.15, 0.55))
    draw_circle(BOOST_CENTER, 35, Color("#e9433d") if pushed and not overheated else Color("#b94740"))
    draw_arc(BOOST_CENTER, 40, 0, TAU, 42, Color("#f0c4b9"), 2.0)
    _center_at("BOOST", BOOST_CENTER + Vector2(0, 5), 17, CREAM)

func _draw_menu() -> void:
    draw_rect(Rect2(0, 0, 480, 270), Color(0.04, 0.08, 0.13, 0.64))
    _panel(Rect2(99, 28, 282, 228), INK, YELLOW)
    _draw_checkers(Rect2(102, 31, 275, 5), 5.0)
    _draw_checkers(Rect2(102, 248, 275, 5), 5.0)
    _center_text("DIRT RUSH", 71, 34, YELLOW)
    _center_text("RETRO MOTO ARCADE", 96, 13, CYAN)
    _center_text("4 LANES / JUMPS / TURBO", 117, 11, CREAM)
    _center_text("BEAT THE CLOCK. BUILD A TRACK.", 137, 11, CREAM)
    _button(Rect2(165, 153, 150, 29), "RACE!", YELLOW)
    _button(Rect2(165, 190, 150, 29), "BUILD TRACK", CYAN)
    _button(Rect2(118, 223, 44, 26), "<", CYAN)
    _button(Rect2(318, 223, 44, 26), ">", CYAN)
    _center_text("%d  %s" % [stage + 1, Rules.TRACK_NAMES[stage]], 243, 11, CREAM)
    _text("LEFT STICK: MOVE / LEAN    RIGHT: BOOST", Vector2(105, 264), 10, CREAM)

func _draw_pause() -> void:
    draw_rect(Rect2(0, 0, 480, 270), Color(0.04, 0.08, 0.13, 0.72))
    _panel(Rect2(118, 83, 244, 150), INK, CYAN)
    _center_text("PAUSED", 128, 27, YELLOW)
    _button(Rect2(159, 151, 162, 28), "RESUME", YELLOW)
    _button(Rect2(159, 190, 162, 28), "EDIT TRACK" if custom_race else "TRACK SELECT", CYAN)

func _draw_finish() -> void:
    draw_rect(Rect2(0, 0, 480, 270), Color(0.04, 0.08, 0.13, 0.78))
    _panel(Rect2(98, 34, 284, 215), INK, YELLOW)
    _center_text("FINISH LINE!", 70, 27, YELLOW)
    _center_text(medal, 98, 23, CYAN)
    _center_text("TIME   %.2fs" % elapsed, 120, 17, CREAM)
    _center_text("CRASHES   %d" % crash_count, 141, 12, CREAM)
    if new_record:
        _center_text("NEW PERSONAL BEST!", 161, 13, YELLOW)
    else:
        var best: float = custom_times[custom_slot] if custom_race else best_times[stage]
        _center_text("BEST  %.2fs" % best, 161, 13, CREAM)
    _button(Rect2(156, 174, 168, 28), "EDIT TRACK" if custom_race else ("NEXT TRACK" if stage < Rules.TRACK_COUNT - 1 else "RACE AGAIN"), YELLOW)
    _button(Rect2(156, 211, 168, 28), "RETRY", CYAN)

func _draw_editor() -> void:
    # Track and obstacles are the actual live game rendering.
    draw_rect(Rect2(0, 0, 480, 35), INK)
    draw_rect(Rect2(0, 34, 480, 2), YELLOW)
    _text("TRACK BUILDER", Vector2(8, 21), 18, YELLOW)
    _draw_checkers(Rect2(0, 34, 480, 4), 4.0)
    _button(Rect2(252, 2, 27, 28), "<", CYAN)
    _text("SLOT %d/3" % (custom_slot + 1), Vector2(296, 21), 15, CREAM)
    _button(Rect2(445, 2, 32, 28), ">", CYAN)
    _button(Rect2(4, 40, 56, 25), "BACK", CYAN)
    _button(Rect2(65, 40, 34, 25), "<", CYAN)
    _text("AREA %d/6" % (edit_page + 1), Vector2(107, 57), 12, CREAM)
    _button(Rect2(192, 40, 34, 25), ">", CYAN)
    _button(Rect2(233, 40, 59, 25), "UNDO", CYAN)
    _button(Rect2(298, 40, 74, 25), "CONFIRM" if clear_confirm else "CLEAR", RED)
    _button(Rect2(378, 40, 97, 25), "TEST RIDE", YELLOW)
    for lane in range(Rules.LANE_COUNT):
        var y := Rules.lane_y(float(lane))
        draw_line(Vector2(8, y + 9), Vector2(472, y + 9), Color(CREAM.r, CREAM.g, CREAM.b, 0.12), 1.0)
    draw_rect(Rect2(0, 224, 480, 46), INK)
    _text("TAP A LANE TO PLACE / REPLACE ANY TILE", Vector2(8, 234), 10, CREAM)
    var labels := ["RAMP", "HIGH", "ROCK", "MUD", "BUMPS", "ERASE"]
    var tools := ["ramp", "big_ramp", "rock", "mud", "whoops", "erase"]
    for i in range(6):
        var x := float(i) * 80.0 + 2.0
        var rect := Rect2(x, 240, 76, 27)
        _panel(rect, Color("#35465a"), YELLOW if edit_tool == tools[i] else CYAN.darkened(0.45))
        _center_at(labels[i], rect.position + Vector2(38, 18), 12, CREAM)

func _draw_checkers(area: Rect2, cell: float) -> void:
    for row in range(int(ceilf(area.size.y / cell))):
        for col in range(int(ceilf(area.size.x / cell))):
            draw_rect(Rect2(area.position + Vector2(float(col) * cell, float(row) * cell), Vector2(cell, cell)), CREAM if (row + col) % 2 == 0 else INK)

func _button(rect: Rect2, caption: String, border: Color) -> void:
    _panel(rect, Color("#334354"), border)
    _center_at(caption, rect.position + Vector2(rect.size.x * 0.5, rect.size.y * 0.5 + 4.0), 14, CREAM)

func _panel(rect: Rect2, color: Color, edge: Color) -> void:
    draw_rect(rect, edge)
    draw_rect(Rect2(rect.position + Vector2(3, 3), rect.size - Vector2(6, 6)), color)

func _text(content: String, at: Vector2, size: int, tint: Color) -> void:
    draw_string(font, at, content, HORIZONTAL_ALIGNMENT_LEFT, -1, size, tint)

func _center_text(content: String, y: float, size: int, tint: Color) -> void:
    draw_string(font, Vector2(50, y), content, HORIZONTAL_ALIGNMENT_CENTER, 380, size, tint)

func _center_at(content: String, center: Vector2, size: int, tint: Color) -> void:
    draw_string(font, Vector2(center.x - 90, center.y), content, HORIZONTAL_ALIGNMENT_CENTER, 180, size, tint)

func _course_length() -> float:
    return Builder.COURSE_LENGTH if custom_race or mode == "editor" else float(Rules.TRACK_LENGTHS[stage])

func _open_editor() -> void:
    _clear_controls()
    custom_race = false
    mode = "editor"
    edit_page = 0
    edit_tool = "ramp"
    undo_stack.clear()
    clear_confirm = false
    _preview_editor()

func _preview_editor() -> void:
    features = Builder.for_race(custom_courses[custom_slot])
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

func _builder_test() -> void:
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
    _save_progress()
    _preview_editor()

func _builder_place(point: Vector2) -> void:
    clear_confirm = false
    var lane := clampi(roundi((point.y - 110.0) / 33.0), 0, Rules.LANE_COUNT - 1)
    var x := float(edit_page) * Builder.PAGE_STEP + point.x
    var old: Array = custom_courses[custom_slot]
    var updated := Builder.edit(old, edit_tool, x, lane)
    if updated == Builder.sanitize(old):
        return
    undo_stack.append(old.duplicate(true))
    if undo_stack.size() > 20:
        undo_stack.pop_front()
    custom_courses[custom_slot] = updated
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
    elif point.y >= 239.0:
        var selected: int = clampi(int(point.x / 80.0), 0, 5)
        edit_tool = ["ramp", "big_ramp", "rock", "mud", "whoops", "erase"][selected]
        clear_confirm = false
    elif point.y >= 93.0 and point.y <= 223.0 and point.x >= 8.0 and point.x <= 472.0:
        _builder_place(point)
    queue_redraw()
