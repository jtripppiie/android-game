package com.jtripppiie.mooserush;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Android-only services that must survive the Java-to-Godot migration. */
public final class YouRushBridge extends GodotPlugin {
    private static final int VOICE_NOTE_REQUEST = 7401;
    private static final String VOICE_RESULT = "voice_note_result";
    private static final String VOICE_ERROR = "voice_note_error";

    public YouRushBridge(Godot godot) { super(godot); }

    @Override public String getPluginName() { return "YouRushBridge"; }

    @Override public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();
        signals.add(new SignalInfo(VOICE_RESULT, String.class));
        signals.add(new SignalInfo(VOICE_ERROR, String.class));
        return signals;
    }

    @UsedByGodot
    public boolean isVoiceNoteAvailable() {
        Activity activity = getActivity();
        if (activity == null) return false;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        return intent.resolveActivity(activity.getPackageManager()) != null;
    }

    @UsedByGodot
    public void startVoiceNote() {
        runOnUiThread(() -> {
            Activity activity = getActivity();
            if (activity == null) { emitSignal(VOICE_ERROR, "Android activity unavailable"); return; }
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe what should change");
            try { activity.startActivityForResult(intent, VOICE_NOTE_REQUEST); }
            catch (Exception error) { emitSignal(VOICE_ERROR, "Speech recognition is not installed"); }
        });
    }

    @Override public void onMainActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != VOICE_NOTE_REQUEST) return;
        if (resultCode != Activity.RESULT_OK || data == null) {
            emitSignal(VOICE_ERROR, "Voice note canceled");
            return;
        }
        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) emitSignal(VOICE_ERROR, "No speech was recognized");
        else emitSignal(VOICE_RESULT, results.get(0));
    }

    /** Returns every old SharedPreferences value so no player progress is silently discarded. */
    @UsedByGodot
    public String readLegacyProfile() {
        Activity activity = getActivity();
        if (activity == null) return "{}";
        SharedPreferences preferences = activity.getSharedPreferences("moose_rush", Activity.MODE_PRIVATE);
        JSONObject result = new JSONObject();
        try {
            for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) result.put(entry.getKey(), entry.getValue());
        } catch (Exception ignored) { return "{}"; }
        return result.toString();
    }
}
