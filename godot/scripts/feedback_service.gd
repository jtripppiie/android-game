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
	var profile := profile_for(message)
	var frequency := profile.x
	var seconds := profile.y
	var vibration_ms := roundi(profile.z)
	if not GameSession.muted and frequency > 0.0 and seconds > 0.0:
		push_tone(frequency, seconds)
	if GameSession.haptics and vibration_ms > 0:
		Input.vibrate_handheld(vibration_ms)

func profile_for(message: String) -> Vector3:
	var upper := message.to_upper()
	if "HIT" in upper or "ROUTE LOST" in upper or "CURRENT" in upper:
		return Vector3(240.0, 0.085, 55.0)
	if (
		"LEVEL CLEAR" in upper
		or "DEFEATED" in upper
		or "KEY FOUND" in upper
		or "RESCUE" in upper
		or "WEAK" in upper
	):
		return Vector3(760.0, 0.070, 34.0)
	if (
		"CHECKPOINT" in upper
		or "SECRET CACHE" in upper
		or "AURORA LAUNCH" in upper
		or "PERFECT LAND" in upper
	):
		return Vector3(620.0, 0.045, 18.0)
	if (
		"BOSS" in upper
		or "ARMORED" in upper
		or "SUN FLARE" in upper
		or "SALMON SPLASH" in upper
		or "SHOCKWAVE" in upper
		or "FEATHER" in upper
		or "SNOW BARRAGE" in upper
	):
		return Vector3(440.0, 0.040, 0.0)
	# Ordinary LAND, AIR JUMP, DASH, ring, and combo updates already have
	# animation and on-screen feedback. Keeping them silent prevents a constant
	# buzz/beep loop during normal traversal.
	return Vector3.ZERO

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
