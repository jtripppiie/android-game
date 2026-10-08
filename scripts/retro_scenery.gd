extends RefCounted
## Original procedural 480x270 scenery. Designed for nearest-neighbor scaling.
## No image downloads, engine-time randomness, or copied course artwork.

const SKIES := [
    [Color("#48a5e7"), Color("#9edbf8")],
    [Color("#5a9bd6"), Color("#c9e9f6")],
    [Color("#60b8ee"), Color("#c8e7ff")],
    [Color("#ed946c"), Color("#f9cb89")],
    [Color("#132659"), Color("#4663a1")],
    [Color("#5f9acb"), Color("#bbd9e6")]
]
const FAR_MOUNTAINS := [
    Color("#a17fb9"), Color("#8a9bc1"), Color("#7d9ba9"),
    Color("#c07d99"), Color("#50538f"), Color("#8c9cbb")
]
const CLOSE_MOUNTAINS := [
    Color("#dd9b7c"), Color("#638c83"), Color("#6c9691"),
    Color("#bd7166"), Color("#303867"), Color("#60837c")
]

static func paint(c: CanvasItem, stage: int, scroll: float, tick: float) -> void:
    var theme := clampi(stage, 0, 5)
    var night := theme == 4
    for stripe in range(13):
        var k := float(stripe) / 13.0
        c.draw_rect(Rect2(0, stripe * 8, 480, 9), SKIES[theme][0].lerp(SKIES[theme][1], k))
    if night:
        c.draw_circle(Vector2(384.0 - fposmod(scroll * 0.012, 100.0), 27), 13, Color("#f9ecc7"))
        c.draw_circle(Vector2(380.0 - fposmod(scroll * 0.012, 100.0), 24), 3, Color("#e0d5ba"))
        for i in range(34):
            var x := float((i * 71 + 13) % 478)
            var y := float((i * 31 + 6) % 66)
            c.draw_rect(Rect2(x, y, 1 + (i % 3 == 0), 1), Color("#f2eef0"))
    else:
        var sun_x := 415.0 - fposmod(scroll * 0.025, 190.0)
        c.draw_rect(Rect2(sun_x, 18, 15, 15), Color("#f6db98"))
        c.draw_rect(Rect2(sun_x + 3, 21, 9, 9), Color("#fff3c2"))
    for i in range(-1, 7):
        var cx := float(i * 104 + (i * 23) % 29) - fposmod(scroll * 0.025, 104.0)
        var cy := 13.0 + float((i * 17) % 23)
        _cloud(c, Vector2(cx, cy), night)
    # Two independently scrolling mountain silhouettes for an NES-era parallax feel.
    for i in range(-2, 9):
        var x := float(i * 92) - fposmod(scroll * 0.049, 92.0)
        var y := 61.0 + float((i * 11 + 7) % 13)
        _mountain(c, x, y, FAR_MOUNTAINS[theme], night, 1.0)
    for i in range(-2, 9):
        var x := float(i * 89 + 27) - fposmod(scroll * 0.095, 89.0)
        var y := 83.0 + float((i * 7 + 3) % 8)
        _mountain(c, x, y, CLOSE_MOUNTAINS[theme], night, 0.63)
    # Blue water and its dithered highlights stay behind the racing lanes.
    var water_color := Color("#346398") if night else Color("#63bada")
    c.draw_rect(Rect2(0, 87, 480, 17), water_color)
    c.draw_rect(Rect2(0, 88, 480, 2), water_color.lightened(0.23))
    for i in range(44):
        var wave_x := fposmod(float(i * 23) - scroll * 0.12, 495.0) - 8.0
        c.draw_rect(Rect2(wave_x, 91 + i % 4 * 3, 7 + i % 3 * 3, 1), Color("#b2e8e9") if not night else Color("#94add5"))
    c.draw_rect(Rect2(0, 102, 480, 3), Color("#528b69") if theme in [1, 2, 5] else Color("#a98756"))
    for i in range(-1, 24):
        var x := float(i * 24 + (i * 17) % 11) - fposmod(scroll * 0.20, 24.0)
        if theme in [1, 2, 5]:
            _pine(c, Vector2(x, 103), (i * 5) % 9 + 9, night)
        else:
            if i % 4 == 0:
                _shrub(c, Vector2(x, 102), night)
            elif i % 6 == 0:
                _cactus(c, Vector2(x, 102), night, 1.0)
    for i in range(-2, 11):
        var wx := float(i * 73 + 18) - fposmod(scroll * 0.29, 73.0)
        if theme in [1, 2, 5]:
            if i % 2 == 0:
                _pine(c, Vector2(wx, 109), 22 + i % 3 * 5, night)
            else:
                _tree(c, Vector2(wx, 107), night)
        else:
            if i % 4 == 2:
                _cactus(c, Vector2(wx, 110), night, 1.15)
            elif i % 2 == 0:
                _tree(c, Vector2(wx, 108), night)
    if theme in [0, 3]:
        var wind_x := 354.0 - fposmod(scroll * 0.13, 690.0)
        _windmill(c, wind_x, 102.0)
        _house(c, wind_x + 65.0, 102.0, night)
    if theme == 4:
        var tower_x := 116.0 - fposmod(scroll * 0.13, 650.0)
        _tower(c, tower_x, 102.0)
        _house(c, tower_x + 225.0, 102.0, true)

static func texture_track(c: CanvasItem, scroll: float, theme: int) -> void:
    # Original multi-band dirt, embedded ruts, grit, and puddle hints.
    var step := floori(scroll / 12.0)
    for lane in range(4):
        var floor_y := 109.0 + float(lane) * 33.0
        for i in range(44):
            var seed := i + step + lane * 123
            var x := float(i * 12) - fposmod(scroll, 12.0)
            var dy := float((seed * 19 + lane * 7) % 18)
            var col := Color("#b57c4e") if seed % 3 == 0 else Color("#53352f")
            c.draw_rect(Rect2(x, floor_y - 9 + dy, 2 + seed % 3, 1), col)
            if seed % 13 == 0:
                c.draw_rect(Rect2(x + 3, floor_y + 12, 7, 2), Color("#382e2e"))
        c.draw_rect(Rect2(0, floor_y + 15, 480, 2), Color("#49312b"))
        c.draw_rect(Rect2(0, floor_y - 12, 480, 2), Color("#c18a55"))
    # Pixel dust patches: low opacity, do not disguise gameplay obstacles.
    for i in range(13):
        var px := fposmod(float(i * 73) - scroll * 0.96, 540.0) - 20.0
        var py := 117.0 + float(i % 4) * 33.0
        c.draw_rect(Rect2(px, py, 19, 2), Color("#bf8651"))
        c.draw_rect(Rect2(px + 5, py + 2, 9, 1), Color("#51352d"))

static func front(c: CanvasItem, scroll: float, stage: int) -> void:
    c.draw_rect(Rect2(0, 255, 480, 15), Color("#8b5b35") if stage != 4 else Color("#433f59"))
    for i in range(-2, 16):
        var x := float(i * 38) - fposmod(scroll * 0.65, 38.0)
        if i % 3 == 0:
            _shrub(c, Vector2(x, 266), stage == 4)
        if i % 5 == 0:
            c.draw_rect(Rect2(x + 18, 258, 9, 6), Color("#858080"))
            c.draw_rect(Rect2(x + 19, 258, 5, 2), Color("#c3b69d"))
    # Foreground fence, lower than the rider and behind the thumb controls.
    for i in range(-2, 14):
        var fx := float(i * 44) - fposmod(scroll * 0.70, 44.0)
        c.draw_rect(Rect2(fx, 243, 4, 27), Color("#4c3430"))
        c.draw_rect(Rect2(fx + 1, 242, 3, 22), Color("#916142"))
        c.draw_rect(Rect2(fx - 5, 250, 49, 3), Color("#ad7950"))
        c.draw_rect(Rect2(fx - 5, 257, 49, 3), Color("#674332"))

static func _cloud(c: CanvasItem, p: Vector2, night: bool) -> void:
    var shade := Color("#777cb8") if night else Color("#dbf1f1")
    var white := Color("#999bd5") if night else Color("#f8ffff")
    c.draw_rect(Rect2(p.x, p.y + 7, 33, 5), shade)
    c.draw_rect(Rect2(p.x + 5, p.y + 4, 23, 8), shade)
    c.draw_rect(Rect2(p.x + 12, p.y, 10, 12), white)
    c.draw_rect(Rect2(p.x + 2, p.y + 10, 38, 3), shade)

static func _mountain(c: CanvasItem, x: float, y: float, color: Color, night: bool, scale: float) -> void:
    var h := 30.0 * scale
    var w := 64.0 * scale
    c.draw_colored_polygon(PackedVector2Array([
        Vector2(x - w / 2, y + 32), Vector2(x + 8 * scale, y - h),
        Vector2(x + w, y + 32)
    ]), color)
    c.draw_colored_polygon(PackedVector2Array([
        Vector2(x + 8 * scale, y - h), Vector2(x + 17 * scale, y - h + 12 * scale),
        Vector2(x + 28 * scale, y + 32), Vector2(x + 4 * scale, y + 32)
    ]), color.darkened(0.15))
    c.draw_colored_polygon(PackedVector2Array([
        Vector2(x + 8 * scale, y - h), Vector2(x, y - h + 12 * scale),
        Vector2(x + 7 * scale, y - h + 9 * scale),
        Vector2(x + 14 * scale, y - h + 13 * scale)
    ]), Color("#b0bde3") if night else Color("#e9ccbf"))

static func _pine(c: CanvasItem, at: Vector2, height: int, night: bool) -> void:
    var dark := Color("#213b56") if night else Color("#245f4d")
    var light := Color("#305579") if night else Color("#3d976a")
    c.draw_rect(Rect2(at.x - 1, at.y - height, 3, height + 1), Color("#493b38"))
    for level in range(3):
        var yy := at.y - float(height) + level * float(height) / 5.0
        var ww := float(height) / 3.0 + level * 2.3
        c.draw_colored_polygon(PackedVector2Array([
            Vector2(at.x, yy), Vector2(at.x - ww, yy + height * 0.38),
            Vector2(at.x + ww, yy + height * 0.38)
        ]), dark if level % 2 == 0 else light)

static func _tree(c: CanvasItem, at: Vector2, night: bool) -> void:
    var leaf := Color("#31486d") if night else Color("#3a7653")
    var highlight := Color("#51749a") if night else Color("#78a64b")
    c.draw_rect(Rect2(at.x, at.y - 20, 4, 22), Color("#714935"))
    for ox in [-10, -4, 2, 8]:
        c.draw_rect(Rect2(at.x + ox, at.y - 30 + abs(ox) / 3, 11, 14), leaf)
    c.draw_rect(Rect2(at.x - 8, at.y - 29, 8, 7), highlight)
    c.draw_rect(Rect2(at.x + 5, at.y - 25, 10, 5), highlight)

static func _shrub(c: CanvasItem, at: Vector2, night: bool) -> void:
    var green := Color("#35496c") if night else Color("#4b8056")
    var light := Color("#547d88") if night else Color("#7cad57")
    c.draw_rect(Rect2(at.x - 5, at.y - 8, 13, 8), green)
    c.draw_rect(Rect2(at.x - 3, at.y - 10, 7, 5), light)
    c.draw_rect(Rect2(at.x + 5, at.y - 4, 6, 4), green)

static func _cactus(c: CanvasItem, at: Vector2, night: bool, scale: float) -> void:
    var color := Color("#33546b") if night else Color("#398b64")
    c.draw_rect(Rect2(at.x, at.y - 20 * scale, 4 * scale, 21 * scale), color)
    c.draw_rect(Rect2(at.x - 5 * scale, at.y - 15 * scale, 6 * scale, 3 * scale), color)
    c.draw_rect(Rect2(at.x - 5 * scale, at.y - 21 * scale, 3 * scale, 9 * scale), color)
    c.draw_rect(Rect2(at.x + 3 * scale, at.y - 13 * scale, 7 * scale, 3 * scale), color)
    c.draw_rect(Rect2(at.x + 8 * scale, at.y - 20 * scale, 3 * scale, 10 * scale), color)

static func _windmill(c: CanvasItem, x: float, ground: float) -> void:
    var beam := Color("#6d4e3b")
    c.draw_line(Vector2(x, ground), Vector2(x + 6, ground - 39), beam, 2.0)
    c.draw_line(Vector2(x + 16, ground), Vector2(x + 8, ground - 39), beam, 2.0)
    for j in range(3):
        c.draw_rect(Rect2(x + 2, ground - 10 - j * 9, 13, 2), beam)
    var hub := Vector2(x + 7, ground - 42)
    for j in range(8):
        var angle := float(j) * TAU / 8.0
        c.draw_line(hub, hub + Vector2(cos(angle), sin(angle)) * 10.0, Color("#775c53"), 2.0)
    c.draw_circle(hub, 3, Color("#3a3238"))

static func _house(c: CanvasItem, x: float, ground: float, night: bool) -> void:
    c.draw_rect(Rect2(x, ground - 17, 29, 17), Color("#ddc09d") if not night else Color("#756779"))
    c.draw_colored_polygon(PackedVector2Array([
        Vector2(x - 3, ground - 17), Vector2(x + 14, ground - 27),
        Vector2(x + 32, ground - 17)
    ]), Color("#b9614b"))
    c.draw_rect(Rect2(x + 6, ground - 12, 7, 6), Color("#ffda7e") if night else Color("#5c7794"))
    c.draw_rect(Rect2(x + 19, ground - 9, 5, 9), Color("#71534b"))

static func _tower(c: CanvasItem, x: float, ground: float) -> void:
    c.draw_rect(Rect2(x + 5, ground - 33, 20, 11), Color("#453c50"))
    c.draw_rect(Rect2(x + 8, ground - 31, 4, 4), Color("#ffce67"))
    c.draw_rect(Rect2(x + 18, ground - 31, 4, 4), Color("#ffce67"))
    for dx in [7.0, 22.0]:
        c.draw_line(Vector2(x + dx, ground - 22), Vector2(x + dx - 3, ground), Color("#7e6655"), 2.0)
    c.draw_line(Vector2(x + 4, ground - 10), Vector2(x + 24, ground - 21), Color("#765850"), 1.0)
