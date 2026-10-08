extends SceneTree
const Scenery=preload("res://scripts/retro_scenery.gd")
func _initialize() -> void:
    var distinct: Array=[]
    for variant in range(8):
        for height in [14,15,16,17,23,24,25,26,27,28]:
            var texture:=Scenery._tree_sprite(variant,height)
            assert(texture.get_height()==height*2)
            assert(texture.get_width()>0 and texture.get_width()<80)
            assert(texture==Scenery._tree_sprite(variant,height), "Trees must be cached")
            var image:=texture.get_image()
            var count:=0
            for y in range(height*2):
                for x in range(image.get_width()):
                    var alpha:=image.get_pixel(x,y).a
                    assert(alpha>=0 and alpha<=1)
                    if alpha>0: count+=1
            assert(count>20)
        var pixels:=Scenery._tree_sprite(variant,28).get_image().get_data()
        assert(not pixels in distinct, "Each tree needs a different silhouette")
        distinct.append(pixels)
    var a:=Scenery._tree_layout(0,false,0,10)
    assert(a==Scenery._tree_layout(0,false,0,10), "Scrolling must not reshuffle trees")
    assert(a!=Scenery._tree_layout(0,false,1,10), "Adjacent chunks cannot repeat the same tree row")
    assert(a!=Scenery._tree_layout(0,false,0,11), "Courses need distinct scenery seeds")
    var gaps: Dictionary={}
    for i in range(1,a.size()): gaps[int(a[i]["x"]-a[i-1]["x"])]=true
    assert(gaps.size()>1, "Tree spacing must vary")
    assert(Scenery._crowd(0).get_image().get_data()!=Scenery._crowd(1).get_image().get_data())
    print("Eight tree sprites, cached sizes, alpha and crowd animation: PASS")
    quit()
