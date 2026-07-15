extends Node

signal voice_note_received(text: String)
signal voice_note_failed(message: String)

var plugin: Object

func _ready() -> void:
	if Engine.has_singleton("YouRushBridge"):
		plugin = Engine.get_singleton("YouRushBridge")
		plugin.connect("voice_note_result", _on_voice_result)
		plugin.connect("voice_note_error", _on_voice_error)
		GameSession.import_legacy_profile(plugin.readLegacyProfile())

func voice_available() -> bool:
	return plugin != null and bool(plugin.isVoiceNoteAvailable())

func start_voice_note() -> void:
	if plugin == null:
		voice_note_failed.emit("Voice notes are available in the Android build")
		return
	plugin.startVoiceNote()

func _on_voice_result(text: String) -> void:
	voice_note_received.emit(text)

func _on_voice_error(message: String) -> void:
	voice_note_failed.emit(message)
