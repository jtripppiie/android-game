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
    "scripts/moving_platform.gd",
    "scripts/encounter_card.gd", "scripts/encounter_director.gd",
    "scripts/reactive_ice.gd", "scripts/freezable_water.gd",
    "scripts/launch_pad.gd",
    "scripts/supply_block.gd",
    "scripts/trick_ring.gd",
    "scripts/trail_boss.gd", "scripts/boss_hazard.gd", "scripts/review_notebook.gd",
    "assets/runner_overhaul.png",
    "assets/route_platform_ice.png", "assets/route_platform_moving.png",
    "assets/route_platform_snow.png", "assets/glacial_water_surface.png",
    "assets/boss_laser_emitter.png", "assets/laser_ice_impact.png",
    "assets/arctic_launch_pad.png",
    "assets/aurora_supply_block.png",
    "assets/collectibles_atlas.png",
    "assets/trail_objects_atlas.png",
    "assets/background_midnight_sun.png", "assets/background_dark_winter.png",
    "assets/scenery_tree_summer.png", "assets/scenery_tree_winter.png",
    "assets/wildlife_bear_walk.png", "assets/wildlife_eagle_fly.png",
    "assets/wildlife_moose_walk.png", "assets/wildlife_polar_bear_walk.png",
    "assets/wildlife_salmon_swim.png", "assets/wildlife_wolf_run.png",
]
missing = [name for name in required if not (root / name).is_file()]
if missing:
    raise SystemExit("Missing: " + ", ".join(missing))

project = (root / "project.godot").read_text()
world = (root / "scripts/world.gd").read_text()
player = (root / "scripts/player.gd").read_text()
touch = (root / "scripts/touch_controls.gd").read_text()
assert 'run/main_scene="res://scenes/main.tscn"' in project
for marker in ("build_level", "checkpoint", "goal", "collectible", "enemy", "moving_platform", "survivor"):
    assert marker in world, marker
main_source = (root / "scripts/main.gd").read_text()
for marker in ("dispose_world", "transition_locked", "remove_child(world)", "if transition_locked or is_instance_valid(world): return", "run_lifecycle_audit"):
    assert marker in main_source, marker
assert 'enemy(Vector2(3820, 610), 115, "bear")' not in world
for state in ("idle", "run", "sprint", "crouch", "jump", "fall", "dash", "stomp"):
    assert f'"{state}"' in player, state
for action in ("move_left", "move_right", "crouch", "jump", "fire", "sprint", "dash"):
    assert f'"{action}"' in touch, action
for marker in ("layout_controls", "SAFE_MARGIN", "InputEventScreenTouch", "InputEventMouseButton", "dpad_up", "dpad_down", "input_action_for", "control_color", 'return "SNOW"'):
    assert marker in touch, marker

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
for marker in ("run_autoplay_audit", "audit_target_objective", "audit_jump_needed", "AUTOPLAY PASS", "capture_visual_audit"):
    assert marker in world, marker
for marker in ("TrailBoss", "ReviewNotebook", "debug_note_context", "save_profile", "boss_defeated", "update_debug_labels", "debug_category_counters"):
    assert marker in world, marker
for marker in ("combo_timer", "chain_action", "score"):
    assert marker in player, marker
for marker in ("JUMP_SPEED := 900.0", "SPRINT_SPEED := 540.0", "air_jumps_left := 1", '"AIR JUMP"', "capsule.height = 96.0", "Vector2(0, -90)"):
    assert marker in player, marker
enemy_source = (root / "scripts/enemy.gd").read_text()
for marker in ("wildlife_bear_walk.png", "wildlife_eagle_fly.png", "wildlife_salmon_swim.png"):
    assert marker in enemy_source, marker
boss = (root / "scripts/trail_boss.gd").read_text()
for marker in ("TELL_SECONDS", "RECOVER_SECONDS", "ARMORED", "WEAK · FIRE"):
    assert marker in boss, marker
for marker in ("wildlife_moose_walk.png", "wildlife_polar_bear_walk.png", "boss_laser_emitter.png"):
    assert marker in boss, marker
for marker in ("SUN FLARE", "SALMON SPLASH", "ANTLER SHOCKWAVE", "FEATHER SPREAD", "SNOW BARRAGE"):
    assert marker in boss, marker
boss_hazard = (root / "scripts/boss_hazard.gd").read_text()
for marker in ("class_name BossHazard", "fall_acceleration", "_on_body_entered", "trail_objects_atlas.png"):
    assert marker in boss_hazard, marker
notebook = (root / "scripts/review_notebook.gd").read_text()
for marker in ("user://debug-review-notes.txt", "FIX FIRST", "context_provider", "nearest_id_provider", "note_count", '"JUMP", "SPACE", "ART", "BUG"'):
    assert marker in notebook, marker
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
