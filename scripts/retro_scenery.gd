extends RefCounted
## Original pixel art. Terrain, sprites and lettering share a fixed pixel grid.
const Terrain = preload("res://scripts/terrain.gd")
const Pixel = preload("res://scripts/pixel_font.gd")
const Rules = preload("res://scripts/race_rules.gd")
const SURFACE_SHEET = preload("res://assets/sprites/track-surfaces-v1.png")
const HAZARD_SHEET = preload("res://assets/sprites/jump-hazards-v1.png")
const FAN_SHEET = preload("res://assets/sprites/stadium-fans-v1.png")
const EXTRA_FAN_SHEET = preload("res://assets/sprites/stadium-fans-v2.png")
const HUMAN_FAN_SHEET = preload("res://assets/sprites/stadium-people-v1.png")
const FAN_COUNT := 32
const EXTRA_FAN_X := [0,374,724,1086,1448]
const EXTRA_FAN_Y := [0,376,704,1086]
const FAN_REGIONS := [Rect2i(0,0,458,444),Rect2i(458,0,429,444),Rect2i(887,0,443,444),Rect2i(1330,0,444,444),Rect2i(0,444,443,443),Rect2i(443,444,462,443),Rect2i(905,444,425,443),Rect2i(1330,444,444,443)]
const TREE_SHEET = preload("res://assets/sprites/trackside-trees-v1.png")
const TREE_THEME_VARIANTS := [[3,7,5],[4,0,7],[0,1,6],[3,5,7],[1,6,5],[2,7,0]]
const RIDER_SHEET = preload("res://assets/sprites/moto-rider-sheet-v1.png")
# Explicit atlas regions preserve the generated recovery poses across uneven gutters.
const RIDER_REGIONS := [Rect2i(0,0,444,444),Rect2i(444,0,443,444),Rect2i(887,0,443,444),Rect2i(1330,0,444,444),Rect2i(0,444,444,443),Rect2i(444,444,477,443),Rect2i(921,444,409,443),Rect2i(1330,444,444,443)]
const RIDER_BASELINES := [411.0,411.0,411.0,411.0,393.0,389.0,385.0,397.0]
const GRASS := ["#9bcb43","#75ac51","#47906c","#c1bd53","#384d8e","#669791"]
const ROAD := ["#cf943c","#d99252","#b9a260","#ca8860","#8d98ae","#d2a965"]
const WALL := ["#75602a","#78503b","#5a633e","#87553c","#465275","#65644b"]
static var bike_sprites: Dictionary = {}
static var road_textures: Dictionary = {}
static var crowds: Dictionary = {}
static var fan_images: Dictionary = {}
static var fan_lineups: Dictionary = {}
static var landscape_textures: Dictionary = {}
static var tree_sprites: Dictionary = {}
static var tree_sources: Dictionary = {}
static var tree_layouts: Dictionary = {}
static var surface_sprites: Dictionary = {}
static var grass_textures: Dictionary = {}
static var hazard_sprites: Dictionary = {}

static func poly(c: CanvasItem, points: Array, color: Color) -> void:
    c.draw_colored_polygon(PackedVector2Array(points),color)

static func oval(c: CanvasItem, center: Vector2, radius: Vector2, color: Color) -> void:
    c.draw_rect(Rect2(roundf(center.x-radius.x),roundf(center.y-1),roundf(radius.x*2),2),color)
    c.draw_rect(Rect2(roundf(center.x-radius.x+2),roundf(center.y-2),roundf(radius.x*2-4),4),color)

static func _stick_fan(pose: int) -> Image:
    var image := Image.create(28,22,false,Image.FORMAT_RGBA8)
    image.fill(Color.TRANSPARENT)
    var ink: Color=[Color("#cfbd91"),Color("#a2bec3"),Color("#b6c291"),Color("#d5a18c")][pose]
    for y in range(-4,5):
        for x in range(-4,5):
            if x*x+y*y<=18: image.set_pixel(14+x,5+y,ink)
    var face := Color("#172539")
    image.set_pixel(12,4,face)
    image.set_pixel(16,4,face)
    for point in [Vector2i(12,6),Vector2i(13,7),Vector2i(14,7),Vector2i(15,7),Vector2i(16,6)]: image.set_pixelv(point,face)
    var hip := Vector2i(13,15)
    _fan_line(image,Vector2i(14,9),hip,ink)
    _fan_line(image,Vector2i(14,10),Vector2i(9,12),ink)
    _fan_line(image,Vector2i(9,12),Vector2i(7,6) if pose in [0,3] else Vector2i(7,15),ink)
    _fan_line(image,Vector2i(14,10),Vector2i(19,12),ink)
    _fan_line(image,Vector2i(19,12),Vector2i(22,5) if pose in [0,1] else Vector2i(23,10),ink)
    _fan_line(image,hip,Vector2i(10,18),ink)
    _fan_line(image,Vector2i(10,18),Vector2i(7,18) if pose==2 else Vector2i(8,21),ink)
    _fan_line(image,hip,Vector2i(17,18),ink)
    _fan_line(image,Vector2i(17,18),Vector2i(22,16) if pose==3 else Vector2i(19,21),ink)
    return image

static func _fan_line(image: Image, a: Vector2i, b: Vector2i, color: Color) -> void:
    var steps := maxi(absi(b.x-a.x),absi(b.y-a.y))
    for step in range(steps+1):
        image.set_pixelv(Vector2i(Vector2(a).lerp(Vector2(b),float(step)/maxi(1,steps)).round()),color)

static func _fan_image(variant: int) -> Image:
    if fan_images.has(variant): return fan_images[variant]
    var source: Image
    if variant<8:
        source=FAN_SHEET.get_image().get_region(FAN_REGIONS[variant])
    elif variant<20:
        var col := (variant-8)%4
        var row := (variant-8)/4
        var region := Rect2i(EXTRA_FAN_X[col],EXTRA_FAN_Y[row],EXTRA_FAN_X[col+1]-EXTRA_FAN_X[col],EXTRA_FAN_Y[row+1]-EXTRA_FAN_Y[row])
        source=EXTRA_FAN_SHEET.get_image().get_region(region)
    elif variant<28:
        var sheet := HUMAN_FAN_SHEET.get_image()
        var col := (variant-20)%4
        var row := (variant-20)/4
        var left := roundi(col*sheet.get_width()/4.0)
        var right := roundi((col+1)*sheet.get_width()/4.0)
        var top := roundi(row*sheet.get_height()/2.0)
        var bottom := roundi((row+1)*sheet.get_height()/2.0)
        source=sheet.get_region(Rect2i(left,top,right-left,bottom-top))
    else:
        source=_stick_fan(variant-28)
    for y in range(source.get_height()):
        for x in range(source.get_width()):
            if source.get_pixel(x,y).a<0.08: source.set_pixel(x,y,Color.TRANSPARENT)
    var image := source.get_region(source.get_used_rect())
    var scale := minf(28.0/image.get_width(),22.0/image.get_height())
    if variant>=20 and variant<28:
        # Keep faces and clothing, but use a chunky arcade pixel grid.
        image.resize(maxi(1,roundi(image.get_width()*scale/2)),maxi(1,roundi(image.get_height()*scale/2)),Image.INTERPOLATE_NEAREST)
        for y in range(image.get_height()):
            for x in range(image.get_width()):
                var pixel := image.get_pixel(x,y)
                pixel.a=1.0 if pixel.a>=0.5 else 0.0
                image.set_pixel(x,y,pixel)
        image.resize(image.get_width()*2,image.get_height()*2,Image.INTERPOLATE_NEAREST)
    else:
        image.resize(maxi(1,roundi(image.get_width()*scale)),maxi(1,roundi(image.get_height()*scale)),Image.INTERPOLATE_BILINEAR)
    # A restrained stadium palette: slightly darker, less candy-colored fans.
    for y in range(image.get_height()):
        for x in range(image.get_width()):
            var color := image.get_pixel(x,y)
            var gray := color.r*0.299+color.g*0.587+color.b*0.114
            var muted := color.lerp(Color(gray,gray,gray,color.a),0.18).darkened(0.16)
            image.set_pixel(x,y,muted)
    fan_images[variant]=image
    return image

static func _fan_variant(index: int, course_seed: int) -> int:
    # Deal from a shuffled roster so every design appears before cycling again.
    var cycle := floori(float(index)/FAN_COUNT)
    var key := "%d/%d" % [course_seed,cycle]
    if not fan_lineups.has(key):
        if fan_lineups.size()>=128: fan_lineups.clear()
        var lineup: Array[int]=[]
        for variant in range(FAN_COUNT): lineup.append(variant)
        var rng := RandomNumberGenerator.new()
        rng.seed=absi(hash("lineup/"+key))
        for i in range(FAN_COUNT-1,0,-1):
            var j := rng.randi_range(0,i)
            var swap := lineup[i]
            lineup[i]=lineup[j]
            lineup[j]=swap
        fan_lineups[key]=lineup
    return fan_lineups[key][posmod(index,FAN_COUNT)]

static func _crowd(frame: int, section: int = 0, course_seed: int = 0) -> ImageTexture:
    var key := "%d/%d/%d" % [course_seed,section,posmod(frame,4)]
    if crowds.has(key): return crowds[key]
    # Keep a long session's scrolling scenery cache bounded.
    if crowds.size()>=128: crowds.clear()
    var image := Image.create(384,48,false,Image.FORMAT_RGBA8)
    image.fill(Color("#263b50"))
    var rng := RandomNumberGenerator.new()
    rng.seed=absi(hash("fans/%d/%d" % [course_seed,section]))
    for row in range(2):
        image.fill_rect(Rect2i(0,row*24,384,24),Color("#345066") if row==0 else Color("#294257"))
        for col in range(10):
            var variant := _fan_variant(section*20+row*10+col,course_seed)
            var fan: Image=_fan_image(variant).duplicate()
            if rng.randf()<0.5: fan.flip_x()
            var phase := posmod(frame+rng.randi_range(0,3),4)
            var bob := 1 if phase in [1,2] else 0
            var x := roundi(col*38.4+(38.4-fan.get_width())/2)
            var y := row*24+23-fan.get_height()-bob
            image.blend_rect(fan,Rect2i(Vector2i.ZERO,fan.get_size()),Vector2i(x,y))
        image.fill_rect(Rect2i(0,row*24+23,384,1),Color("#93b6b7"))
    var texture := ImageTexture.create_from_image(image)
    crowds[key]=texture
    return texture

static func _tree_sprite(variant: int, height: int) -> ImageTexture:
    var key := variant*100+height
    if tree_sprites.has(key): return tree_sprites[key]
    if not tree_sources.has(variant):
        var sheet := TREE_SHEET.get_image()
        var col := variant%4
        var row := variant/4
        var left := roundi(col*sheet.get_width()/4.0)
        var right := roundi((col+1)*sheet.get_width()/4.0)
        var top := roundi(row*sheet.get_height()/2.0)
        var bottom := roundi((row+1)*sheet.get_height()/2.0)
        var source := sheet.get_region(Rect2i(left,top,right-left,bottom-top))
        for y in range(source.get_height()):
            for x in range(source.get_width()):
                var pixel := source.get_pixel(x,y)
                pixel.a=0.0 if pixel.a<0.05 else pixel.a
                source.set_pixel(x,y,pixel)
        tree_sources[variant]=source.get_region(source.get_used_rect())
    var image: Image=tree_sources[variant].duplicate()
    var width := maxi(1,roundi(image.get_width()*float(height)/image.get_height()))
    image.resize(width*2,height*2,Image.INTERPOLATE_BILINEAR)
    var texture := ImageTexture.create_from_image(image)
    tree_sprites[key]=texture
    return texture

static func _tree_layout(theme: int, foreground: bool, chunk: int, course_seed: int) -> Array:
    var key := "%d/%s/%d/%d" % [theme,str(foreground),chunk,course_seed]
    if tree_layouts.has(key): return tree_layouts[key]
    var rng := RandomNumberGenerator.new()
    rng.seed=absi(key.hash())
    var variants: Array=TREE_THEME_VARIANTS[clampi(theme,0,5)]
    var trees: Array=[]
    var x := rng.randf_range(20,55)
    while x<290:
        trees.append({"x":x,"variant":variants[rng.randi_range(0,variants.size()-1)],"height":rng.randi_range(24,30) if foreground else rng.randi_range(23,28),"flip":rng.randf()<0.5})
        x+=rng.randf_range(48,94) if foreground else rng.randf_range(48,100)
    tree_layouts[key]=trees
    return trees

static func _tree_row(c: CanvasItem, scroll: float, theme: int, foreground: bool, course_seed: int) -> void:
    var travel := scroll if foreground else scroll*0.65
    var first := floori(travel/320.0)-1
    for chunk in range(first,first+4):
        for tree in _tree_layout(theme,foreground,chunk,course_seed):
            var texture := _tree_sprite(int(tree["variant"]),int(tree["height"]))
            var size := Vector2(texture.get_size())/2.0
            var x := chunk*320.0+float(tree["x"])-travel
            var base := 242.0 if foreground else 110.0
            var tint := Color("#8e9cb9") if theme==4 else Color.WHITE
            var rect := Rect2(Vector2(x-size.x/2,base-size.y),size)
            if bool(tree["flip"]):
                rect.position.x+=rect.size.x
                rect.size.x= -rect.size.x
            c.draw_texture_rect(texture,rect,false,tint)

static func _landscape(theme: int) -> ImageTexture:
    if landscape_textures.has(theme): return landscape_textures[theme]
    var image := Image.create(256,40,false,Image.FORMAT_RGBA8)
    image.fill(Color.TRANSPARENT)
    var grass := Color(GRASS[theme])
    var far := grass.lerp(Color("#55758a"),0.35)
    var near := grass.darkened(0.23)
    for x in range(256):
        var ridge := 16+int(5*sin(x*TAU/128.0)+3*sin(x*TAU/64.0))
        image.fill_rect(Rect2i(x,ridge,1,40-ridge),far)
        var lower := 28+int(3*sin(x*TAU/128.0+1.2))
        image.fill_rect(Rect2i(x,lower,1,40-lower),grass.darkened(0.08))
    var texture := ImageTexture.create_from_image(image)
    landscape_textures[theme]=texture
    return texture

static func dust(c: CanvasItem, at: Vector2, travel: float, tint: Color, strength: float) -> void:
    for i in range(5):
        var phase := fposmod(travel*0.09+i*0.21,1.0)
        var size := 1+int(phase*3)
        var p := (at+Vector2(-12-phase*24,-2-phase*7+sin(i*2.1)*2)).round()
        c.draw_rect(Rect2(p,Vector2(size*2,size)),Color(tint, (1-phase)*0.28*strength))

static func paint(c: CanvasItem, stage: int, scroll: float, tick: float, race_layout: bool = false, course_seed: int = 0) -> void:
    var theme := clampi(stage,0,5)
    c.draw_rect(Rect2(0,0,480,270),Color(GRASS[theme]))
    var crowd_scroll := scroll*0.15
    var first_crowd := floori(crowd_scroll/192.0)
    var crowd_y := 32.0 if race_layout else 0.0
    var crowd_height := 24.0 if race_layout else 32.0
    for section in range(first_crowd,first_crowd+4):
        c.draw_texture_rect(_crowd(int(tick*5),section,course_seed),Rect2(floorf(section*192.0-crowd_scroll),crowd_y,192,crowd_height),false)
    var banner_y := 56.0 if race_layout else 35.0
    c.draw_rect(Rect2(0,banner_y,480,13 if race_layout else 16),Color("#315a91") if theme!=4 else Color("#30345b"))
    c.draw_rect(Rect2(0,banner_y-1,480,1),Color("#fff1d5"))
    Pixel.text(c,"MOTO THRASH / MOTO CLUB",Vector2(9,banner_y+4 if race_layout else 42),1,Color("#fff5d9"))
    # Cached silhouettes move slower than the track to give the stadium depth.
    for i in range(-1,3):
        c.draw_texture(_landscape(theme),Vector2(i*256-floorf(fposmod(scroll*0.22,256)),70))
    # Match the foreground turf, moving with the upper tree row.
    var grass_scroll := scroll*0.65
    var first_grass_chunk := floori(grass_scroll/320.0)
    for chunk in range(first_grass_chunk,first_grass_chunk+3):
        var variant := posmod(hash("upper/%d/%d" % [course_seed,chunk]),8)
        c.draw_texture_rect_region(_grass_texture(theme,variant),Rect2(floorf(chunk*320.0-grass_scroll),96,320,16),Rect2(0,4,320,16))
    c.draw_rect(Rect2(0,109,480,3),Color(GRASS[theme]).darkened(0.24))
    _tree_row(c,scroll,theme,false,course_seed)

static func texture_track(c: CanvasItem, _scroll: float, theme: int) -> void:
    c.draw_rect(Rect2(0,113,480,112),Color(WALL[clampi(theme,0,5)]))

static func _road_texture(theme: int, grade: int) -> ImageTexture:
    var key := theme*3+grade+1
    if road_textures.has(key): return road_textures[key]
    var base := Color(ROAD[clampi(theme,0,5)])
    if grade > 0:
        base = Color("#e9bd8e") if theme!=4 else Color("#abbcd3")
    elif grade < 0:
        base = base.darkened(0.17)
    var image := Image.create(128,26,false,Image.FORMAT_RGBA8)
    image.fill(base)
    var rng := RandomNumberGenerator.new()
    rng.seed = 6351+theme
    # Small contrasting grains and paired tire ruts, kept below the obstacle contrast.
    for i in range(135):
        var x := rng.randi_range(0,126)
        var y := rng.randi_range(2,23)
        var shade := base.darkened(0.10) if i%3 else base.lightened(0.14)
        image.set_pixel(x,y,shade)
        if i%5==0: image.set_pixel(x+1,y,shade)
    for y in [7,17]:
        for x in range(0,128,16):
            image.fill_rect(Rect2i(x,y,9,1),base.darkened(0.075))
            image.fill_rect(Rect2i(x+2,y+1,6,1),base.lightened(0.06))
    # Small sunlit stones and alternating shade pixels add depth without blur.
    for i in range(18):
        var px := rng.randi_range(1,124)
        var py := rng.randi_range(3,21)
        image.fill_rect(Rect2i(px,py,2,1),base.lightened(0.23))
        image.set_pixel(px+1,py+1,base.darkened(0.24))
    for x in range(0,128,2):
        image.set_pixel(x,1,base.lightened(0.12))
        image.set_pixel(x+1,24,base.darkened(0.16))
    image.fill_rect(Rect2i(0,0,128,1),base.lightened(0.2))
    image.fill_rect(Rect2i(0,25,128,1),base.darkened(0.1))
    var texture := ImageTexture.create_from_image(image)
    road_textures[key] = texture
    return texture

static func track_lane(c: CanvasItem, samples: PackedFloat32Array, distance: float, lane: int, theme: int) -> void:
    var base := Rules.lane_y(lane)
    var earth := Color(ROAD[clampi(theme,0,5)])
    var wall := Color(WALL[clampi(theme,0,5)])
    var offset := floorf(distance)-124.0
    var previous := Terrain.cached_height(samples,offset-8)
    for x in range(-8,488,8):
        var next := Terrain.cached_height(samples,offset+x+8)
        var current := Terrain.cached_height(samples,offset+x) if x == -8 else previous
        if current>0 or next>0:
            poly(c,[Vector2(x,base+13-current),Vector2(x+8,base+13-next),Vector2(x+8,base+14),Vector2(x,base+14)],wall)
            # Exposed compacted-earth strata on the near face of each bank.
            for depth in [5.0,12.0,21.0,31.0]:
                if minf(current,next)>depth:
                    c.draw_line(Vector2(x,base+13-current+depth),Vector2(x+8,base+13-next+depth),wall.lightened(0.10 if int(depth)%2 else 0.04),1)
            if minf(current,next)>10 and posmod(int(offset+x),24)<8:
                var stone := Vector2(x+3,base+19-minf(current,next)).round()
                c.draw_rect(Rect2(stone,Vector2(3,1)),wall.lightened(0.24))
                c.draw_rect(Rect2(stone+Vector2(1,1),Vector2(3,1)),wall.darkened(0.15))
            c.draw_line(Vector2(x,base+13-current),Vector2(x+8,base+13-next),earth.lightened(0.28),1)
        var grade := 1 if next-current>0.5 else (-1 if next-current< -0.5 else 0)
        var points := PackedVector2Array([Vector2(x,base-13-current),Vector2(x+8,base-13-next),Vector2(x+8,base+13-next),Vector2(x,base+13-current)])
        var uv0 := (offset+x)/128.0
        var uv1 := (offset+x+8)/128.0
        c.draw_polygon(points,PackedColorArray([Color.WHITE]),PackedVector2Array([Vector2(uv0,0),Vector2(uv1,0),Vector2(uv1,1),Vector2(uv0,1)]),_road_texture(theme,grade))
        c.draw_line(Vector2(x,base-13-current),Vector2(x+8,base-13-next),earth.lightened(0.30),1)
        previous = next
    # Dashed lane center lines follow the elevation, instead of lying beneath ramps.
    for i in range(-1,34):
        var x := i*16-floorf(fposmod(distance,16.0))
        var world_x := x+distance-124
        var h0 := Terrain.cached_height(samples,world_x)
        var h1 := Terrain.cached_height(samples,world_x+7)
        c.draw_line(Vector2(x,base+11-h0),Vector2(x+7,base+11-h1),wall,1)
    var start_x := 144.0-distance
    if start_x > -8 and start_x <480:
        c.draw_rect(Rect2(start_x,base-13,3,26),Color("#fff2cd"))
        c.draw_line(Vector2(start_x-37,base+8),Vector2(start_x,base+8),Color("#f3d69a"),1)

static func _grass_texture(theme: int, variant: int) -> ImageTexture:
    var key := theme*8+variant
    if grass_textures.has(key): return grass_textures[key]
    var image := Image.create(320,48,false,Image.FORMAT_RGBA8)
    var base := Color(GRASS[theme]).darkened(0.08)
    image.fill(base)
    var rng := RandomNumberGenerator.new()
    rng.seed=7219+key*137
    # Broken turf patches, rather than a flat green strip or evenly spaced dots.
    for i in range(170):
        var x := rng.randi_range(0,309)
        var y := rng.randi_range(2,43)
        var width := rng.randi_range(4,11)
        var shade := base.lightened(0.065) if i%3==0 else base.darkened(0.07)
        image.fill_rect(Rect2i(x+2,y,width-2,1),shade)
        image.fill_rect(Rect2i(x,y+1,width,2),shade)
        image.fill_rect(Rect2i(x+1,y+3,width-2,1),shade)
    # Short angular blades with darker roots and a sunlit tip.
    for i in range(310):
        var x := rng.randi_range(2,316)
        var y := rng.randi_range(4,46)
        var height := rng.randi_range(1,3)
        image.fill_rect(Rect2i(x-1,y,4,1),base.darkened(0.20))
        image.fill_rect(Rect2i(x,y-height,1,height),base.lightened(0.18))
        image.set_pixel(x-1,y-1,base.lightened(0.09))
        image.set_pixel(x+2,y-2,base.darkened(0.13))
        if i%4==0: image.set_pixel(x+1,y-height,base.lightened(0.26))
    # Ragged soil-to-grass transition along the track shoulder.
    for x in range(320):
        var depth := rng.randi_range(1,3)
        image.fill_rect(Rect2i(x,0,1,depth),base.darkened(0.26))
        if x%3==0: image.set_pixel(x,depth,base.lightened(0.20))
    var texture := ImageTexture.create_from_image(image)
    grass_textures[key]=texture
    return texture

static func front(c: CanvasItem, scroll: float, stage: int, course_seed: int = 0) -> void:
    var theme := clampi(stage,0,5)
    var first := floori(scroll/320.0)
    for chunk in range(first,first+3):
        var variant := posmod(hash("%d/%d" % [course_seed,chunk]),8)
        c.draw_texture(_grass_texture(theme,variant),Vector2(floorf(chunk*320.0-scroll),225))
    _tree_row(c,scroll,stage,true,course_seed)

static func bike(c: CanvasItem, center: Vector2, body: Color, _suit: Color, pitch: float, size: float, _spin: float, _tick: float, _number: int = 7, view_transform: Transform2D = Transform2D.IDENTITY, pose: int = 0) -> void:
    var frame := clampi(pose,0,7)
    var key := body.to_html()+str(frame)
    if not bike_sprites.has(key):
        bike_sprites[key] = _rider_sprite(body,frame)
    var texture: ImageTexture = bike_sprites[key]
    # Move with the track, but never stretch the illustrated rider vertically.
    var direction := Vector2(cos(pitch),sin(pitch))
    var projected := view_transform.x*direction.x+view_transform.y*direction.y
    c.draw_set_transform_matrix(Transform2D(snappedf(projected.angle(),PI/24),Vector2(size,size),0,(view_transform*center).round()))
    var anchor := Vector2(texture.get_width()/4.0,roundf(RIDER_BASELINES[frame]/8.0)-5)
    c.draw_texture_rect(texture,Rect2(-anchor,Vector2(texture.get_size())/2.0),false)
    c.draw_set_transform_matrix(view_transform)

static func _rider_sprite(body: Color, frame: int) -> ImageTexture:
    # The PNG is the source of all rider artwork. This only prepares atlas frames.
    var region: Rect2i = RIDER_REGIONS[clampi(frame,0,7)]
    var image := RIDER_SHEET.get_image().get_region(region)
    image.resize(roundi(region.size.x/4.0),roundi(region.size.y/4.0),Image.INTERPOLATE_BILINEAR)
    var rival := body.to_html(false) != "ee754c"
    for y in range(image.get_height()):
        for x in range(image.get_width()):
            var pixel := image.get_pixel(x,y)
            if pixel.a < 0.05:
                image.set_pixel(x,y,Color.TRANSPARENT)
                continue
            # Recolor only the saturated orange paint/jersey, keeping cream and metal.
            if rival and pixel.h<0.105 and pixel.s>0.55 and pixel.r>pixel.b*1.5:
                pixel=Color.from_hsv(body.h,clampf(pixel.s*0.85,0.5,0.95),pixel.v,pixel.a)
            image.set_pixel(x,y,pixel)
    return ImageTexture.create_from_image(image)

static func _surface_sprite(kind: String) -> ImageTexture:
    if surface_sprites.has(kind): return surface_sprites[kind]
    var column := ["oil","mud","boost"].find(kind)
    var source := SURFACE_SHEET.get_image()
    var left := roundi(column*source.get_width()/3.0)
    var right := roundi((column+1)*source.get_width()/3.0)
    var image := source.get_region(Rect2i(left,0,right-left,source.get_height()))
    for y in range(image.get_height()):
        for x in range(image.get_width()):
            var pixel := image.get_pixel(x,y)
            if pixel.a<0.05: image.set_pixel(x,y,Color.TRANSPARENT)
    image=image.get_region(image.get_used_rect())
    image.resize(96,36,Image.INTERPOLATE_BILINEAR)
    var texture := ImageTexture.create_from_image(image)
    surface_sprites[kind]=texture
    return texture

static func _hazard_sprite(kind: String) -> ImageTexture:
    if hazard_sprites.has(kind): return hazard_sprites[kind]
    var index := Rules.JUMP_HAZARDS.find(kind)
    var col := index%3
    var row := index/3
    var edges: Array = [0,640,1435,2172] if row==0 else [0,740,1380,2172]
    var source := HAZARD_SHEET.get_image().get_region(Rect2i(edges[col],row*362,edges[col+1]-edges[col],362))
    for y in range(source.get_height()):
        for x in range(source.get_width()):
            if source.get_pixel(x,y).a<0.08: source.set_pixel(x,y,Color.TRANSPARENT)
    var image := source.get_region(source.get_used_rect())
    var height := Rules.hazard_clearance(kind)+6 if kind in ["car","bus"] else 18.0
    image.resize(int(Rules.hazard_width(kind)*2),int(height*2),Image.INTERPOLATE_BILINEAR)
    var texture := ImageTexture.create_from_image(image)
    hazard_sprites[kind]=texture
    return texture

static func obstacle(c: CanvasItem, kind: String, x: float, y: float, _seed: int) -> void:
    if kind in Rules.JUMP_HAZARDS:
        var texture := _hazard_sprite(kind)
        var size := Vector2(texture.get_size())/2
        c.draw_texture_rect(texture,Rect2(Vector2(x-size.x/2,y+6-size.y),size),false)
    elif kind=="jump_ramp":
        c.draw_rect(Rect2(x-4,y-16,2,16),Color("#172539"))
        c.draw_rect(Rect2(x-2,y-16,12,7),Color("#f7cb5e"))
        c.draw_rect(Rect2(x+2,y-16,3,7),Color("#172539"))
    elif kind in ["oil","mud","boost"]:
        c.draw_texture_rect(_surface_sprite(kind),Rect2(x-24,y-9,48,18),false)
    elif kind=="rock":
        # Builder's legacy rock tool is now an unambiguous striped safety barrier.
        c.draw_rect(Rect2(x-12,y-9,25,8),Color("#f7e5b8"))
        for i in range(4):
            poly(c,[Vector2(x-11+i*6,y-9),Vector2(x-7+i*6,y-9),Vector2(x-11+i*6,y-1),Vector2(x-15+i*6,y-1)],Color("#df7347"))
        c.draw_rect(Rect2(x-10,y-1,3,6),Color("#584427"))
        c.draw_rect(Rect2(x+8,y-1,3,6),Color("#584427"))

static func crash(c: CanvasItem, at: Vector2, body: Color, remaining: float, view_transform: Transform2D = Transform2D.IDENTITY) -> void:
    var pose := 4 if remaining>0.72 else (5 if remaining>0.45 else (6 if remaining>0.18 else 7))
    bike(c,at,body,Color.WHITE,0,1,0,0,7,view_transform,pose)
