extends Node

var player: AudioStreamPlayer
var sample_rate := 22050.0

func _ready() -> void:
	if DisplayServer.get_name() == "headless": return
	player = AudioStreamPlayer.new()
	var generator := AudioStreamGenerator.new()
	generator.mix_rate = sample_rate
	generator.buffer_length = 0.25
	player.stream = generator
	add_child(player)
	player.play()

func cue(message: String) -> void:
	if not GameSession.muted:
		var frequency := 760.0 if "WEAK" in message or "RESCUE" in message else 520.0 if "HIT" in message else 340.0
		push_tone(frequency, 0.055)
	if GameSession.haptics and not GameSession.reduced_motion:
		Input.vibrate_handheld(42 if "HIT" in message else 24)

func push_tone(frequency: float, seconds: float) -> void:
	if not is_instance_valid(player): return
	var playback := player.get_stream_playback() as AudioStreamGeneratorPlayback
	if playback == null: return
	var frames := mini(playback.get_frames_available(), int(sample_rate * seconds))
	for index in range(frames):
		var fade := 1.0 - index / float(maxi(1, frames))
		var sample := sin(TAU * frequency * index / sample_rate) * 0.16 * fade
		playback.push_frame(Vector2(sample, sample))

func _exit_tree() -> void:
	if is_instance_valid(player):
		player.stop()
		player.stream = null
