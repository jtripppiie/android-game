#!/usr/bin/env python3
from pathlib import Path
import shutil
import subprocess
from PIL import Image

root = Path(__file__).resolve().parent
required = [
    "project.godot", "export_presets.cfg", "icon.svg", "scenes/main.tscn",
    "scripts/main.gd", "scripts/game_session.gd", "scripts/feedback_service.gd",
    "scripts/world.gd", "scripts/player.gd", "scripts/enemy.gd",
    "scripts/projectile.gd", "scripts/touch_controls.gd",
    "scripts/game_hud.gd",
    "scripts/moving_platform.gd",
    "scripts/reactive_ice.gd", "scripts/freezable_water.gd",
    "scripts/launch_pad.gd",
    "scripts/supply_block.gd",
    "scripts/trick_ring.gd",
    "scripts/trail_boss.gd", "scripts/boss_hazard.gd", "scripts/review_notebook.gd",
    "assets/runner_overhaul.png",
    "assets/route_platform_ice.png", "assets/route_platform_moving.png",
    "assets/route_platform_snow.png", "assets/route_terrain_snow_v2.png",
    "assets/glacial_water_surface.png",
    "assets/boss_laser_emitter.png", "assets/laser_ice_impact.png",
    "assets/arctic_launch_pad.png",
    "assets/aurora_supply_block.png",
    "assets/collectibles_atlas.png",
    "assets/trail_objects_atlas.png",
    "assets/background_midnight_sun.png", "assets/background_dark_winter.png",
    "assets/boot_splash.png",
    "assets/scenery_tree_summer.png", "assets/scenery_tree_winter.png",
    "assets/wildlife_bear_walk.png", "assets/wildlife_eagle_fly.png",
    "assets/wildlife_moose_walk.png", "assets/wildlife_polar_bear_walk.png",
    "assets/wildlife_salmon_swim.png", "assets/wildlife_wolf_run.png",
    "tools/build_composition_audit.py",
    "tools/build_boot_splash.py",
]
missing = [name for name in required if not (root / name).is_file()]
if missing:
    raise SystemExit("Missing: " + ", ".join(missing))

project = (root / "project.godot").read_text()
export_preset = (root / "export_presets.cfg").read_text()
world = (root / "scripts/world.gd").read_text()
player = (root / "scripts/player.gd").read_text()
touch = (root / "scripts/touch_controls.gd").read_text()
boss = (root / "scripts/trail_boss.gd").read_text()
hud = (root / "scripts/game_hud.gd").read_text()
notebook = (root / "scripts/review_notebook.gd").read_text()
assert 'run/main_scene="res://scenes/main.tscn"' in project
for marker in ('image="res://assets/boot_splash.png"', "fullsize=true", "use_filter=true"):
    assert marker in project, marker
for marker in ('version/code=532', 'version/name="5.3.2"', 'you-rush-alaska-5.3.2-debug.apk'):
    assert marker in export_preset, marker
for marker in ("build_level", "checkpoint", "goal", "collectible", "enemy", "moving_platform", "survivor"):
    assert marker in world, marker
main_source = (root / "scripts/main.gd").read_text()
for marker in ("dispose_world", "transition_locked", "remove_child(world)", "ui.remove_child(child)", "if transition_locked or is_instance_valid(world): return", "run_lifecycle_audit"):
    assert marker in main_source, marker
for marker in ("show_launch_splash", "SPLASH_MINIMUM_SECONDS := 1.25", "SPLASH_TOTAL_SECONDS := 4.0", "TAP TO BEGIN", "dismiss_launch_splash"):
    assert marker in main_source, marker
for marker in ("current_screen", "_unhandled_input", "NOTIFICATION_APPLICATION_PAUSED", "pause_for_background", "run_system_audit", "SYSTEM AUDIT PASS", "run_pause_audit", "PAUSE AUDIT PASS"):
    assert marker in main_source, marker
assert world.count("GameSession.complete_stage") == 0
assert "func _on_stage_completed(stage: int, score: int) -> void:\n\tGameSession.complete_stage(stage, score)" in main_source
for marker in ('Vector2(0, 82)', 'add_theme_constant_override("separation", 20)', 'add_theme_constant_override("separation", 12)'):
    assert marker in main_source, marker
for marker in ("font_disabled_color", 'add_theme_stylebox_override("disabled"', "font_pressed_color"):
    assert marker in main_source, marker
assert 'enemy(Vector2(3820, 610), 115, "bear")' not in world
assert 'enemy(Vector2(1150, 410), 120, "eagle")' not in world
assert "var mountain := Polygon2D.new()" not in world
assert 'var winter := stage_index >= 3' in world
for marker in ("enemy_spawn_positions", "REFUSED STACKED WILDLIFE", "distance_to(at) < 420.0"):
    assert marker in world, marker
for state in ("idle", "run", "sprint", "crouch", "jump", "fall", "dash", "stomp"):
    assert f'"{state}"' in player, state
for marker in ("ring_chain = 0", "ring_chain_timer = 0.0", "ring_rush_timer = 0.0", "was_on_floor = false"):
    assert marker in player, marker
for action in ("move_left", "move_right", "crouch", "jump", "fire", "sprint", "dash"):
    assert f'"{action}"' in touch, action
for marker in ("layout_controls", "SAFE_MARGIN", "InputEventScreenTouch", "InputEventMouseButton", "dpad_up", "dpad_down", "input_action_for", "control_color", "control_text_color", "release_all_touches", "dpad_touch_contains", "dpad_input_vector", "draw_dpad_arrow", "apply_touch_action_changes", "touch_pressed_actions", "DPAD_DRIFT_MARGIN", 'return "SNOW"'):
    assert marker in touch, marker
for marker in ("build_snow_terrain", "build_snow_slope", "route_terrain_snow_v2.png", "AtlasTexture", "STRETCH_SCALE", "mapped_uv", "crest_shadow"):
    assert marker in world, marker
for marker in ('set_deferred("monitoring", false)', 'set_deferred("monitorable", false)'):
    assert marker in boss, marker
for marker in ("update_snapshot", "objective_text", "post_message", "message_queue", "audit_layout", "ui_font", "StageProgressFill"):
    assert marker in hud, marker
for marker in ("QUICK_NOTES", "apply_quick_note", "undo_last_note", "recent_note_summary", "audit_layout", "note_font"):
    assert marker in notebook, marker
assert "var hud: AlaskaGameHud" in world
assert "hud.update_snapshot(" in world
assert "func announce(message: String" in world

image = Image.open(root / "assets/runner_overhaul.png")
assert image.mode == "RGBA", image.mode
assert image.width % 6 == 0, image.size
alpha = image.getchannel("A")
transparent = sum(1 for value in alpha.get_flattened_data() if value == 0)
assert transparent > image.width * image.height * 0.60

for asset_name in (
    "route_platform_ice.png", "route_platform_moving.png", "route_platform_snow.png",
    "glacial_water_surface.png", "boss_laser_emitter.png", "laser_ice_impact.png",
    "arctic_launch_pad.png",
    "aurora_supply_block.png",
):
    asset = Image.open(root / "assets" / asset_name)
    assert asset.mode == "RGBA", (asset_name, asset.mode)
    assert asset.getbbox() is not None, asset_name

for marker in ("ReactiveIce", "launch_pad", "supply_block", "trick_ring_line"):
    assert marker in world, marker
assert "build_directed_encounters()" not in world
for marker in ("run_autoplay_audit", "audit_target_objective", "audit_jump_needed", "AUTOPLAY PASS", "capture_visual_audit", "run_geometry_audit", "GEOMETRY AUDIT PASS", "main_route_surface"):
    assert marker in world, marker
assert "save_profile()\n\tif autoplay_audit" not in world
for marker in ("TrailBoss", "ReviewNotebook", "debug_note_context", "boss_defeated", "update_debug_labels", "debug_category_counters"):
    assert marker in world, marker
for marker in ('register_debug_item(zone, "CP"', 'register_debug_item(zone, "GO"', 'register_debug_item(ice, "IC"', "debug_distance_to_player"):
    assert marker in world, marker
for marker in ("build_pause_panel", "toggle_pause_panel", "EXIT TO MAP", "exit_run_to_map", "PROCESS_MODE_PAUSABLE", "release_gameplay_inputs"):
    assert marker in world, marker
assert 'pause_button.text = "PAUSE"' in hud
assert 'is_action_just_pressed("ui_cancel")' not in world
assert 'pause_button.text = "MAP"' not in hud
for marker in ("BAR_HEIGHT := 82.0", "RunnerStatus", "ObjectiveStatus", "AUR %d", "SCORE %d"):
    assert marker in hud, marker
for marker in ("Vector2(14, 10)", "Vector2(446, 10)", "Vector2(1066, 10)"):
    assert marker in hud, marker
for marker in ("combo_timer", "chain_action", "score"):
    assert marker in player, marker
for marker in ("JUMP_SPEED := 900.0", "SPRINT_SPEED := 540.0", "air_jumps_left := 1", '"AIR JUMP"', "capsule.height = 96.0", "Vector2(0, -90)"):
    assert marker in player, marker
assert "func collect_aurora_ring() -> void:\n\tcoins += 1" in player
enemy_source = (root / "scripts/enemy.gd").read_text()
for marker in ("wildlife_bear_walk.png", "wildlife_eagle_fly.png", "wildlife_salmon_swim.png"):
    assert marker in enemy_source, marker
for marker in ("is_stomp_contact", "runner.velocity.y >= -180.0", "not runner.is_on_floor()"):
    assert marker in enemy_source, marker
for marker in ("ledge_ray", "force_raycast_update", "not ledge_ray.is_colliding()"):
    assert marker in enemy_source, marker
assert "inside_patrol" in enemy_source and "global_position.x = origin_x + edge_side * patrol_distance" in enemy_source
boss = (root / "scripts/trail_boss.gd").read_text()
for marker in ("TELL_SECONDS", "RECOVER_SECONDS", "ARMORED", "WEAK · FIRE"):
    assert marker in boss, marker
for marker in ("wildlife_moose_walk.png", "wildlife_polar_bear_walk.png", "boss_laser_emitter.png"):
    assert marker in boss, marker
assert "player.global_position.x > global_position.x" in boss
assert "art.flip_h = true" not in boss
assert "hitbox_sizes" in boss and "RectangleShape2D.new()" in boss
assert "clampf(target_x" in boss and "attack_side" in boss
for marker in ("SUN FLARE", "SALMON SPLASH", "ANTLER SHOCKWAVE", "FEATHER SPREAD", "SNOW BARRAGE"):
    assert marker in boss, marker
assert "0.30, 1.05, 0.62, 0.58, 0.90" in boss
boss_hazard = (root / "scripts/boss_hazard.gd").read_text()
for marker in ("class_name BossHazard", "fall_acceleration", "_on_body_entered", "trail_objects_atlas.png"):
    assert marker in boss_hazard, marker
notebook = (root / "scripts/review_notebook.gd").read_text()
for marker in ("user://debug-review-notes.txt", "FIX FIRST", "context_provider", "nearest_id_provider", "note_count", '"JUMP", "SPACE", "ART", "BUG"'):
    assert marker in notebook, marker
for marker in ("paused_before_open", "get_tree().paused = paused_before_open", "voice_available"):
    assert marker in notebook, marker
session_source = (root / "scripts/game_session.gd").read_text()
for marker in ("Refused invalid stage completion index", "maxi(0", "Could not save profile.cfg"):
    assert marker in session_source, marker
bridge_source = (root / "scripts/android_bridge.gd").read_text()
for marker in ('has_signal("voice_note_result")', 'has_method("readLegacyProfile")', 'has_method("isVoiceNoteAvailable")'):
    assert marker in bridge_source, marker
feedback_source = (root / "scripts/feedback_service.gd").read_text()
assert "if GameSession.haptics:" in feedback_source
assert "GameSession.haptics and not GameSession.reduced_motion" not in feedback_source
projectile = (root / "scripts/projectile.gd").read_text()
assert 'has_method("snowball_hit")' in projectile
assert "area_entered.connect" in projectile
assert "trail_objects_atlas.png" in projectile
freezable_water = (root / "scripts/freezable_water.gd").read_text()
for marker in ("bridge.shattered.connect", "_on_bridge_shattered", 'set_deferred("disabled", false)'):
    assert marker in freezable_water, marker
for marker in ("glacial_water_surface.png", "current_tween", "art.visible = false"):
    assert marker in freezable_water, marker
reactive_ice = (root / "scripts/reactive_ice.gd").read_text()
for marker in ("route_platform_ice.png", "laser_ice_impact.png", "spawn_hit_flash"):
    assert marker in reactive_ice, marker
moving_platform_source = (root / "scripts/moving_platform.gd").read_text()
assert "position = origin + travel * 0.5" in moving_platform_source
assert "(sin(clock * TAU / cycle_seconds) + 1.0) * 0.5" in moving_platform_source

godot = shutil.which("godot4") or shutil.which("godot")
if godot:
    subprocess.run(
        [godot, "--headless", "--path", str(root), "--editor", "--quit"],
        check=True, timeout=45,
    )
    runtime_status = "headless Godot parse passed"
else:
    runtime_status = "structural validation passed; Godot executable unavailable"

print(
    f"Godot overhaul validation passed: {image.width}x{image.height}, six frames, "
    f"{transparent} transparent pixels; {runtime_status}"
)
