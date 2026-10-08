extends SceneTree
const Scenery=preload("res://scripts/retro_scenery.gd")
func _initialize() -> void:
    for body in [Color("#ee754c"),Color("#347de3"),Color("#4db96c"),Color("#e9bc3d")]:
        var previous: PackedByteArray=[]
        for frame in range(8):
            var image:=Scenery._rider_sprite(body,frame).get_image()
            assert(image.get_width()>=100 and image.get_width()<=120)
            assert(image.get_height()==111)
            var opaque := 0
            var transparent := 0
            for y in range(image.get_height()):
                for x in range(image.get_width()):
                    var pixel:=image.get_pixel(x,y)
                    assert(pixel.a>=0 and pixel.a<=1)
                    if pixel.a>0: opaque+=1
                    else: transparent+=1
            assert(opaque>300 and transparent>300, "Each pose must contain isolated artwork")
            assert(image.get_data()!=previous, "Adjacent poses must be different artwork")
            previous=image.get_data()
    var player:=Scenery._rider_sprite(Color("#ee754c"),0).get_image().get_data()
    var rival:=Scenery._rider_sprite(Color("#347de3"),0).get_image().get_data()
    assert(player!=rival, "Rivals must retain distinct paint colors")
    print("Rider PNG atlas: all 32 pose/color variants, alpha and distinct poses PASS")
    quit()
