extends Node
## Original procedural arcade audio. No recordings, downloads or licensed samples.
var enabled := true
var engine: AudioStreamPlayer
var wind: AudioStreamPlayer
var effects: Array[AudioStreamPlayer] = []
var bank: Dictionary = {}
var next_voice := 0

static func synth(kind: String) -> AudioStreamWAV:
    var durations := {"engine":0.5,"wind":0.5,"click":0.055,"count":0.10,"go":0.30,"jump":0.20,"land":0.15,"crash":0.48,"hot":0.35,"cool":0.28,"finish":1.1}
    var duration: float = durations.get(kind,0.1)
    var rate := 22050
    var count := int(duration*rate)
    var bytes := PackedByteArray()
    bytes.resize(count*2)
    var rng := RandomNumberGenerator.new()
    rng.seed = 21847
    var phase := 0.0
    var old_noise := 0.0
    for i in range(count):
        var t := float(i)/rate
        var progress := t/duration
        var noise := rng.randf_range(-1,1)
        var sample := 0.0
        var frequency := 440.0
        match kind:
            "engine":
                sample = sin(t*TAU*88)*0.40+sin(t*TAU*176)*0.18+sin(t*TAU*264)*0.09
                sample += (1.0 if sin(t*TAU*44)>0.6 else -0.2)*0.08
            "wind": sample=(noise-old_noise)*0.12
            "click": frequency=650-350*progress
            "count": frequency=520
            "go": frequency=1040
            "jump": frequency=160+progress*500
            "land":
                frequency=110-80*progress
                sample=noise*0.2
            "crash":
                frequency=140-100*progress
                sample=noise*0.48
            "hot": frequency=210+sin(t*TAU*24)*60
            "cool": frequency=450+progress*700
            "finish":
                var notes := [523.25,659.25,783.99,1046.5,783.99,1046.5]
                frequency=notes[mini(5,int(progress*6))]
        if kind not in ["engine","wind"]:
            phase += frequency/rate
            var tone := sin(phase*TAU)*0.32+sin(phase*TAU*2)*0.1
            sample += tone
            var envelope := minf(t/0.007,1)*pow(1-progress,1.4)
            if kind=="finish": envelope=minf(t/0.01,1)*minf((duration-t)/0.08,1)*0.75
            sample *= envelope
        old_noise=noise
        var value := int(clampf(sample,-0.95,0.95)*32767)
        bytes.encode_s16(i*2,value)
    var stream := AudioStreamWAV.new()
    stream.format=AudioStreamWAV.FORMAT_16_BITS
    stream.mix_rate=rate
    stream.stereo=false
    stream.data=bytes
    if kind in ["engine","wind"]:
        stream.loop_mode=AudioStreamWAV.LOOP_FORWARD
        stream.loop_begin=0
        stream.loop_end=count
    return stream

func _ready() -> void:
    if DisplayServer.get_name()=="headless": return
    for kind in ["engine","wind","click","count","go","jump","land","crash","hot","cool","finish"]:
        bank[kind]=synth(kind)
    engine=AudioStreamPlayer.new()
    add_child(engine)
    engine.stream=bank["engine"]
    engine.volume_db=-20
    wind=AudioStreamPlayer.new()
    add_child(wind)
    wind.stream=bank["wind"]
    wind.volume_db=-28
    for i in range(5):
        var voice := AudioStreamPlayer.new()
        voice.volume_db=-12
        add_child(voice)
        effects.append(voice)

func cue(kind: String) -> void:
    if not enabled or effects.is_empty() or not bank.has(kind): return
    var voice := effects[next_voice]
    next_voice=(next_voice+1)%effects.size()
    voice.stream=bank[kind]
    voice.play()

func update_motor(racing: bool, speed: float, boosting: bool, in_air: bool, crashed: bool) -> void:
    if engine==null: return
    if not enabled or not racing:
        engine.stop()
        wind.stop()
        return
    if not engine.playing: engine.play()
    engine.pitch_scale=lerpf(engine.pitch_scale,0.55+speed/180.0+(0.20 if boosting else 0),0.12)
    engine.volume_db=-25 if crashed else (-17 if boosting else -21)
    if in_air or speed>170:
        if not wind.playing: wind.play()
        wind.volume_db=-23 if in_air else -30
    else:
        wind.stop()

func set_enabled(value: bool) -> void:
    enabled=value
    if not value:
        if engine!=null: engine.stop()
        if wind!=null: wind.stop()
        for voice in effects: voice.stop()
