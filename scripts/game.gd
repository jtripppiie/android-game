extends Node2D
## Dirt Rush: a small, original, landscape, pixel-art motocross game.
## Single 480 x 270 canvas + procedural graphics: no downloaded art or IP.

const Rules = preload("res://scripts/race_rules.gd")
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

var mode := "menu" # menu, race, pause, finish
var stage := 0
var unlocked := 0
var best_times: Array[float] = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
var features: Array[Dictionary] = []
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
    features = Rules.generate_features(stage)
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
    if crash_timer <= 0.0:
        _check_features(last_distance, distance)

    dust_timer += dt
    var interval := 0.055 if boost_active else 0.12
    if speed > 55.0 and altitude < 7.0 and dust_timer >= interval:
        dust_timer = 0.0
        _spawn_dust(BIKE_X - 16.0, Rules.lane_y(lane_position) - 1.0, CYAN if boost_active else DIRT_LIGHT)
    _update_particles(dt)
    if distance >= Rules.TRACK_LENGTHS[stage]:
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
            "ramp":
                if altitude < 8.0:
                    jump_velocity = 172.0 + speed * 0.11
                    altitude = maxf(altitude, 1.0)
                    bike_tilt = -0.17
                    message = "AIR TIME!"
                    message_timer = 0.7
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
    distance = Rules.TRACK_LENGTHS[stage]
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

func _save_progress() -> void:
    var cfg := ConfigFile.new()
    cfg.set_value("progress", "unlocked", unlocked)
    for i in range(Rules.TRACK_COUNT):
        cfg.set_value("records", str(i), best_times[i])
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
                _prepare_preview()
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
        var point := event.position
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
        if Rect2(165, 177, 150, 30).has_point(point):
            _start_race()
        elif Rect2(118, 213, 44, 28).has_point(point):
            stage = maxi(0, stage - 1)
            _prepare_preview()
        elif Rect2(318, 213, 44, 28).has_point(point):
            stage = mini(unlocked, stage + 1)
            _prepare_preview()
    elif mode == "pause":
        if Rect2(159, 151, 162, 28).has_point(point):
            mode = "race"
        elif Rect2(159, 190, 162, 28).has_point(point):
            mode = "menu"
            _prepare_preview()
    elif mode == "finish":
        if Rect2(156, 174, 168, 28).has_point(point):
            stage = mini(unlocked, stage + 1)
            _start_race()
        elif Rect2(156, 211, 168, 28).has_point(point):
            _start_race()

func _primary_action() -> void:
    match mode:
        "menu": _start_race()
        "pause": mode = "race"
        "finish":
            stage = mini(unlocked, stage + 1)
            _start_race()

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
    _draw_bike()
    if mode == "race" or mode == "pause":
        _draw_hud()
        _draw_touch_controls()
    match mode:
        "menu": _draw_menu()
        "pause": _draw_pause()
        "finish": _draw_finish()
    if mode == "race":
        if countdown > 0.0:
            _text("%d" % maxi(1, int(ceilf(countdown))), Vector2(220, 94), 42, YELLOW)
            _text("GET READY", Vector2(198, 113), 13, CREAM)
        elif crash_timer > 0.0 or message_timer > 0.0:
            var txt_color := RED if crash_timer > 0.0 else YELLOW
            _panel(Rect2(162, 51, 156, 25), INK, txt_color)
            _center_text(message, 64, 13, txt_color)

func _draw_background() -> void:
    var sky: Color = SKY[stage]
    var hills: Color = HILLS[stage]
    draw_rect(Rect2(0, 0, SIZE.x, 115), sky)
    var sun_x := 379.0 - fposmod(distance * 0.06, 400.0)
    draw_rect(Rect2(sun_x, 48, 28, 28), YELLOW if stage != 4 else Color("#dee6fd"))
    if stage == 4:
        for i in range(15):
            var sx := float((i * 73 + 13) % 477)
            var sy := float((i * 29 + 16) % 88)
            draw_rect(Rect2(sx, sy, 2, 2), CREAM)
    for i in range(-2, 9):
        var x := float(i) * 80.0 - fposmod(distance * 0.09, 80.0)
        draw_colored_polygon(PackedVector2Array([
            Vector2(x - 40, 104), Vector2(x + 2, 67),
            Vector2(x + 32, 91), Vector2(x + 60, 104)
        ]), hills)
        draw_rect(Rect2(x + 1, 68, 5, 7), Color("#e4ddc7") if stage in [2, 5] else hills.lightened(0.2))
    draw_rect(Rect2(0, 100, 480, 14), hills.darkened(0.14))
    for i in range(-2, 17):
        var shrub_x := float(i) * 37.0 - fposmod(distance * 0.2, 37.0)
        draw_rect(Rect2(shrub_x, 94, 6, 6), Color("#3b8b77") if stage != 4 else Color("#677394"))
        draw_rect(Rect2(shrub_x + 2, 90, 2, 8), hills.darkened(0.24))

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
    var goal_x := Rules.TRACK_LENGTHS[stage] - distance + BIKE_X
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
                    Vector2(sx - 19, fy + 5), Vector2(sx + 13, fy - 11),
                    Vector2(sx + 21, fy - 11), Vector2(sx + 21, fy + 5)
                ]), YELLOW)
                draw_line(Vector2(sx - 19, fy + 5), Vector2(sx + 13, fy - 11), INK, 2.0)
                draw_rect(Rect2(sx + 13, fy - 11, 8, 3), CREAM)
            "rock":
                draw_rect(Rect2(sx - 8, fy - 5, 14, 10), Color("#3b4557"))
                draw_rect(Rect2(sx - 5, fy - 9, 9, 5), Color("#64717b"))
                draw_rect(Rect2(sx - 2, fy - 8, 4, 3), Color("#9ba0a2"))
            "mud":
                draw_rect(Rect2(sx - 13, fy + 2, 25, 5), Color("#302a38"))
                draw_rect(Rect2(sx - 10, fy + 1, 9, 2), Color("#c2905c"))
                draw_rect(Rect2(sx + 5, fy, 5, 2), Color("#c2905c"))

func _draw_bike() -> void:
    var ground_y := Rules.lane_y(lane_position)
    # Small low-resolution elliptical contact shadow.
    draw_rect(Rect2(BIKE_X - 22, ground_y + 4, 43, 4), Color(0.14, 0.12, 0.13, 0.45))
    draw_set_transform(Vector2(BIKE_X, ground_y - altitude), bike_tilt, Vector2.ONE)
    # Exhaust + square pixel wheels.
    draw_rect(Rect2(-26, -12, 9, 4), INK)
    draw_circle(Vector2(-14, 0), 8, INK)
    draw_circle(Vector2(15, 0), 8, INK)
    draw_circle(Vector2(-14, 0), 4, Color("#8a9ba6"))
    draw_circle(Vector2(15, 0), 4, Color("#8a9ba6"))
    draw_rect(Rect2(-18, -3, 5, 6), INK)
    draw_rect(Rect2(13, -3, 5, 6), INK)
    # Original orange bike: tank, fork, seat, and blocky fenders.
    draw_colored_polygon(PackedVector2Array([
        Vector2(-17, -8), Vector2(-9, -17), Vector2(9, -14),
        Vector2(17, -7), Vector2(7, -4), Vector2(-10, -4)
    ]), RED)
    draw_rect(Rect2(-16, -17, 18, 3), INK)
    draw_rect(Rect2(5, -12, 13, 3), YELLOW)
    draw_line(Vector2(7, -12), Vector2(15, -1), INK, 2.0)
    # Rider: boot, pants, torso, helmet, visor, hands.
    draw_rect(Rect2(-8, -13, 10, 6), Color("#233451"))
    draw_rect(Rect2(-4, -20, 11, 10), Color("#f4c04e"))
    draw_rect(Rect2(2, -18, 10, 3), Color("#f3d0a2"))
    draw_rect(Rect2(-6, -31, 13, 12), Color("#e8f1ee"))
    draw_rect(Rect2(-4, -33, 11, 3), CYAN)
    draw_rect(Rect2(3, -27, 7, 4), INK)
    draw_rect(Rect2(-10, -11, 8, 4), INK)
    draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)

func _draw_particles() -> void:
    for particle in sparks:
        var pos: Vector2 = particle["p"]
        var alpha: float = clampf(float(particle["life"]) * 2.0, 0.0, 1.0)
        var color: Color = particle["color"]
        color.a = alpha
        draw_rect(Rect2(pos.x, pos.y, 3, 3), color)

func _draw_hud() -> void:
    draw_rect(Rect2(0, 0, 480, 39), INK)
    draw_rect(Rect2(0, 38, 480, 2), YELLOW)
    _text("DIRT RUSH", Vector2(9, 15), 14, YELLOW)
    _text("%d/6  %s" % [stage + 1, Rules.TRACK_NAMES[stage]], Vector2(10, 30), 11, CREAM)
    _text("%04.1fs" % elapsed, Vector2(270, 17), 14, CREAM)
    _text("%d MPH" % int(speed * 0.45), Vector2(341, 30), 11, CREAM)
    draw_rect(Rect2(264, 27, 63, 6), DIRT_DARK)
    draw_rect(Rect2(264, 27, 63 * clampf(distance / Rules.TRACK_LENGTHS[stage], 0.0, 1.0), 6), CYAN)
    draw_rect(Rect2(370, 8, 76, 8), DIRT_DARK)
    draw_rect(Rect2(370, 8, 76 * heat / 100.0, 8), RED if heat > 75.0 else YELLOW)
    _text("HEAT", Vector2(370, 28), 11, CREAM)
    if overheated:
        _text("HOT", Vector2(406, 28), 11, RED)
    draw_rect(Rect2(453, 5, 23, 25), Color("#263a4f"))
    draw_rect(Rect2(460, 10, 3, 13), CREAM)
    draw_rect(Rect2(466, 10, 3, 13), CREAM)
    _text("CRASH %d" % crash_count, Vector2(340, 14), 9, CREAM)

func _draw_touch_controls() -> void:
    # Translucent low-resolution controls remain visible for discoverability.
    draw_circle(STICK_CENTER, 37, Color(0.07, 0.13, 0.20, 0.50))
    draw_arc(STICK_CENTER, 37, 0, TAU, 40, Color("#b4e0df"), 2.0)
    draw_line(STICK_CENTER + Vector2(0, -29), STICK_CENTER + Vector2(0, 29), Color("#608b91"), 1.0)
    draw_line(STICK_CENTER + Vector2(-29, 0), STICK_CENTER + Vector2(29, 0), Color("#608b91"), 1.0)
    draw_circle(STICK_CENTER + stick_visual * 22.0, 14, Color("#a7e7d9"))
    draw_circle(STICK_CENTER + stick_visual * 22.0, 7, Color("#4b8291"))
    var pushed := boost_touch_id != -1 or Input.is_action_pressed("boost")
    draw_circle(BOOST_CENTER, 39, Color(0.25, 0.13, 0.10, 0.78))
    draw_circle(BOOST_CENTER, 34, Color("#b63f3e") if pushed and not overheated else Color("#a26842"))
    draw_arc(BOOST_CENTER, 39, 0, TAU, 40, YELLOW if not overheated else RED, 3.0)
    _center_at("BOOST", BOOST_CENTER + Vector2(0, 4), 14, CREAM)
    _text("STEER", Vector2(44, 269), 9, CREAM)

func _draw_menu() -> void:
    draw_rect(Rect2(0, 0, 480, 270), Color(0.04, 0.08, 0.13, 0.64))
    _panel(Rect2(99, 41, 282, 210), INK, YELLOW)
    _center_text("DIRT RUSH", 84, 34, YELLOW)
    _center_text("PIXEL MOTOCROSS", 106, 13, CYAN)
    _center_text("4 LANES  /  6 TRACKS  /  TURBO", 132, 11, CREAM)
    _center_text("LEAN IN AIR  -  LAND CLEAN", 150, 11, CREAM)
    _button(Rect2(165, 177, 150, 30), "START RACE", YELLOW)
    _button(Rect2(118, 213, 44, 28), "<", CYAN)
    _button(Rect2(318, 213, 44, 28), ">", CYAN)
    _center_text("%d  %s" % [stage + 1, Rules.TRACK_NAMES[stage]], 234, 11, CREAM)
    _text("LEFT STICK: MOVE / LEAN    RIGHT: BOOST", Vector2(105, 264), 10, CREAM)

func _draw_pause() -> void:
    draw_rect(Rect2(0, 0, 480, 270), Color(0.04, 0.08, 0.13, 0.72))
    _panel(Rect2(118, 83, 244, 150), INK, CYAN)
    _center_text("PAUSED", 128, 27, YELLOW)
    _button(Rect2(159, 151, 162, 28), "RESUME", YELLOW)
    _button(Rect2(159, 190, 162, 28), "TRACK SELECT", CYAN)

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
        _center_text("BEST  %.2fs" % best_times[stage], 161, 13, CREAM)
    _button(Rect2(156, 174, 168, 28), "NEXT TRACK" if stage < Rules.TRACK_COUNT - 1 else "RACE AGAIN", YELLOW)
    _button(Rect2(156, 211, 168, 28), "RETRY", CYAN)

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
