#!/usr/bin/env python3
"""Fast structural and asset validation for the production Godot game."""

from pathlib import Path
import os
import shutil
import subprocess
from PIL import Image

ROOT = Path(__file__).resolve().parent

REQUIRED = [
    "project.godot",
    "export_presets.cfg",
    "icon.svg",
    "scenes/main.tscn",
    "scenes/player.tscn",
    "scripts/main.gd",
    "scripts/game_session.gd",
    "scripts/feedback_service.gd",
    "scripts/android_bridge.gd",
    "scripts/world.gd",
    "scripts/player.gd",
    "scripts/player_presentation.gd",
    "scripts/runner_camera.gd",
    "scripts/runner_effects.gd",
    "scripts/enemy.gd",
    "scripts/projectile.gd",
    "scripts/touch_controls.gd",
    "scripts/game_hud.gd",
    "scripts/moving_platform.gd",
    "scripts/reactive_ice.gd",
    "scripts/freezable_water.gd",
    "scripts/launch_pad.gd",
    "scripts/supply_block.gd",
    "scripts/trick_ring.gd",
    "scripts/trail_boss.gd",
    "scripts/boss_hazard.gd",
    "scripts/review_notebook.gd",
    "scripts/review_registry.gd",
    "scripts/gameplay_auditor.gd",
    "scripts/mechanics_auditor.gd",
    "scripts/game_over_overlay.gd",
    "scripts/stage_complete_overlay.gd",
    "scripts/android_verification_harness.gd",
    "android/build/src/main/AndroidManifest.xml",
    "android/build/src/main/java/com/jtripppiie/mooserush/YouRushBridge.java",
    "assets/runner_overhaul.png",
    "assets/route_platform_ice.png",
    "assets/route_platform_moving.png",
    "assets/route_platform_snow.png",
    "assets/route_terrain_snow_v2.png",
    "assets/glacial_water_surface.png",
    "assets/boss_laser_emitter.png",
    "assets/laser_ice_impact.png",
    "assets/arctic_launch_pad.png",
    "assets/aurora_supply_block.png",
    "assets/collectibles_atlas.png",
    "assets/trail_objects_atlas.png",
    "assets/background_midnight_sun.png",
    "assets/background_dark_winter.png",
    "assets/boot_splash.png",
    "assets/scenery_tree_summer.png",
    "assets/scenery_tree_winter.png",
    "assets/wildlife_bear_walk.png",
    "assets/wildlife_eagle_fly.png",
    "assets/wildlife_moose_walk.png",
    "assets/wildlife_polar_bear_walk.png",
    "assets/wildlife_salmon_swim.png",
    "assets/wildlife_wolf_run.png",
]
missing = [name for name in REQUIRED if not (ROOT / name).is_file()]
if missing:
    raise SystemExit("Missing production files: " + ", ".join(missing))


def text(relative: str) -> str:
    return (ROOT / relative).read_text()


def require(source: str, markers: tuple[str, ...], label: str) -> None:
    for marker in markers:
        assert marker in source, f"{label} missing {marker!r}"


project = text("project.godot")
preset = text("export_presets.cfg")
main = text("scripts/main.gd")
session = text("scripts/game_session.gd")
world = text("scripts/world.gd")
player = text("scripts/player.gd")
player_presentation = text("scripts/player_presentation.gd")
player_scene = text("scenes/player.tscn")
touch = text("scripts/touch_controls.gd")
hud = text("scripts/game_hud.gd")
boss = text("scripts/trail_boss.gd")
enemy = text("scripts/enemy.gd")
notebook = text("scripts/review_notebook.gd")
registry = text("scripts/review_registry.gd")
auditor = text("scripts/gameplay_auditor.gd")
mechanics = text("scripts/mechanics_auditor.gd")
harness = text("scripts/android_verification_harness.gd")
bridge = text("scripts/android_bridge.gd")
bridge_java = text("android/build/src/main/java/com/jtripppiie/mooserush/YouRushBridge.java")

require(
    project,
    (
        'run/main_scene="res://scenes/main.tscn"',
        'image="res://assets/boot_splash.png"',
        'window/stretch/aspect="expand"',
        'renderer/rendering_method.mobile="gl_compatibility"',
        "limits/opengl/max_lights_per_object=2",
        "limits/opengl/max_renderable_lights=8",
    ),
    "project",
)
require(
    preset,
    (
        'version/code=541',
        'version/name="5.4.1"',
        "you-rush-alaska-5.4.1-debug.apk",
        "architectures/arm64-v8a=true",
        "architectures/x86_64=false",
        'package/unique_name="com.jtripppiie.mooserush"',
    ),
    "export preset",
)
assert 'name="Android Emulator Debug"' not in preset
assert "architectures/x86_64=true" not in preset
assert "x86_64-debug.apk" not in preset
require(
    main,
    (
        "show_launch_splash",
        "SPLASH_TOTAL_SECONDS := 4.0",
        "restart_stage",
        "dispose_world",
        "transition_locked",
        "world.restart_requested.connect",
        "world.advance_requested.connect",
        "AndroidVerificationHarness",
        "--mechanics-audit",
        "--debug-overlay-audit=",
        "NOTIFICATION_APPLICATION_PAUSED",
        "pause_for_background",
        "GameSession.APP_VERSION",
        "menu_panel_style",
        'frame.name = "MainMenuFrame"',
        "set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)",
    ),
    "main controller",
)
assert "remove_child(world)" in main
assert "ui.remove_child(child)" in main
assert world.count("GameSession.complete_stage") == 0
assert "GameSession.complete_stage(stage, score, elapsed_seconds, damage_taken)" in main

require(
    world,
    (
        "var review_registry: ReviewRegistry",
        "var auditor: GameplayAuditor",
        "var game_over_overlay: GameOverOverlay",
        "var stage_complete_overlay: StageCompleteOverlay",
        "show_game_over",
        "restart_requested.emit",
        "advance_requested.emit",
        "show_stage_complete",
        "build_snow_terrain",
        "build_snow_slope",
        "REFUSED STACKED WILDLIFE",
        "distance_to(at) < 420.0",
        "hud.update_snapshot",
        "review_registry.register",
        "auditor.complete",
        "PROCESS_MODE_PAUSABLE",
        "release_gameplay_inputs",
        "update_goal_presentation",
        "LOCKED · COMPLETE OBJECTIVES",
        "shape.size = Vector2(96, 1100)",
        "player.set_checkpoint(at + Vector2(0, 50))",
    ),
    "stage",
)
for obsolete in (
    "func run_autoplay_audit",
    "func run_geometry_audit",
    "var debug_category_counters",
    "var visual_capture_x",
):
    assert obsolete not in world, f"obsolete stage responsibility remains: {obsolete}"
assert 'enemy(Vector2(3820, 610), 115, "bear")' not in world
assert 'enemy(Vector2(1150, 410), 120, "eagle")' not in world

require(
    player,
    (
        "signal defeated",
        "JUMP_SPEED := 900.0",
        "SPRINT_SPEED := 540.0",
        "SHORT_JUMP_CUT := 0.52",
        "AIR_FRICTION := 360.0",
        "APEX_GRAVITY_SCALE",
        "same-frame",
        "air_jumps_left := 1",
        "queue_stomp",
        "controls_enabled",
        "RUN ENDED",
        "respawn(apply_score_penalty",
        "facing = 1.0",
    ),
    "player",
)
for state in ("idle", "run", "sprint", "crouch", "jump", "fall", "dash", "stomp"):
    assert f'"{state}"' in player
require(
    player_scene,
    (
        'type="CapsuleShape2D"',
        'parent="Presentation"',
        'name="GroundShadow"',
        'name="Camera2D"',
    ),
    "player scene",
)
require(
    player_presentation,
    (
        "FRAME_GROUND_Y",
        "player_photo_texture",
        "ground_shadow.visible = grounded",
    ),
    "player presentation",
)
assert "CollisionShape2D.new()" not in player
assert "Sprite2D.new()" not in player
assert "Camera2D.new()" not in player

require(
    touch,
    (
        "DPAD_SIZE := 196.0",
        "InputEventScreenTouch",
        "touch_roles",
        "dpad_touch_id",
        "ACTION_DRIFT_MARGIN",
        "DPAD_VERTICAL_THRESHOLD",
        "deliberate_vertical",
        "apply_touch_action_changes",
        'return "SNOW"',
        "GameSession.high_contrast",
        "GameSession.large_text",
        "GameSession.touch_scale",
    ),
    "touch controls",
)
require(
    hud,
    (
        "layout_for_viewport",
        "last_snapshot",
        "last_progress_pixels",
        "objective_text",
        "message_queue",
        "audit_layout",
        "RunnerStatus",
        "ObjectiveStatus",
        'pause_button.text = "PAUSE"',
    ),
    "HUD",
)
require(
    session,
    (
        "PROFILE_SCHEMA := 3",
        'APP_VERSION := "5.4.1"',
        "PROFILE_BACKUP_PATH",
        "backup_existing_profile",
        "completed_runs",
        "best_times",
        "best_stars",
        "star_rating",
        "touch_scale",
        "legacy_imported",
        "import_legacy_profile",
        "Could not save profile.cfg",
    ),
    "profile",
)
require(
    registry,
    (
        "MAX_VISIBLE_BADGES := 4",
        'identifier := "S%d-%s%02d"',
        "distance_to_player",
        "position_surface_badge",
        "func audit",
    ),
    "review registry",
)
require(
    auditor,
    (
        "AUTOPLAY PASS",
        "AUTOPLAY FAIL",
        "VISUAL AUDIT CAPTURE",
        "GEOMETRY AUDIT PASS",
        "DEBUG OVERLAY AUDIT PASS",
        "complete=true",
        "failure_reason=none",
        "visual_capture_pending",
    ),
    "gameplay auditor",
)
require(
    mechanics,
    (
        "MECHANICS AUDIT PASS",
        "short_jump",
        "full_jump",
        "reverse_dash",
        "air_speed",
        "third_jump=blocked",
        "clean_restart=true",
    ),
    "mechanics auditor",
)
require(
    harness,
    (
        "if not OS.is_debug_build()",
        "SUPPORTED_SCENARIOS",
        "VERIFICATION READY",
        "boss-attack",
        "stage-complete",
        "background-resume",
        "world.collect",
        "world.finish_level",
    ),
    "Android verification harness",
)
require(
    bridge,
    (
        "not OS.is_debug_build()",
        "getVerificationScenario",
        "getVerificationStage",
        "readLegacyProfile",
    ),
    "Godot Android bridge",
)
require(
    bridge_java,
    (
        "ApplicationInfo.FLAG_DEBUGGABLE",
        "verification_scenario",
        "verification_stage",
        "getVerificationScenario",
        "getVerificationStage",
        "readLegacyProfile",
    ),
    "Java Android bridge",
)
require(
    notebook,
    (
        "user://debug-review-notes.txt",
        "FIX FIRST",
        "QUICK_NOTES",
        "undo_last_note",
        "paused_before_open",
        "panel.size = Vector2(480, 292)",
    ),
    "review notebook",
)
require(
    boss,
    (
        "TELL_SECONDS",
        "RECOVER_SECONDS",
        "activated := false",
        "BOSS AHEAD",
        "ARMORED",
        "WEAK · FIRE",
        "TELL_BY_VARIANT",
        "RECOVER_BY_VARIANT",
        "HIT_COOLDOWN",
        "WATCH THE MARKER",
        "target_x > rest_position.x",
        "hitbox_sizes",
    ),
    "boss",
)
require(
    enemy,
    (
        "ledge_ray",
        "force_raycast_update",
        "is_stomp_contact",
        "ContactSensor",
        "get_overlapping_bodies",
        "runner.velocity.y >= -180.0",
        "inside_patrol",
    ),
    "enemy",
)

runner = Image.open(ROOT / "assets/runner_overhaul.png")
assert runner.mode == "RGBA"
assert runner.width % 6 == 0
alpha = runner.getchannel("A")
transparent = sum(1 for value in alpha.get_flattened_data() if value == 0)
assert transparent > runner.width * runner.height * 0.60
frame_width = runner.width // 6
frame_ground_y = (-85.0, -89.8, -86.7, -81.6, -90.1, -89.1)
foot_errors = []
for frame_index, offset_y in enumerate(frame_ground_y):
    frame_alpha = alpha.crop(
        (frame_index * frame_width, 0, (frame_index + 1) * frame_width, runner.height)
    )
    bounds = frame_alpha.getbbox()
    assert bounds is not None, f"empty runner frame {frame_index}"
    last_opaque_pixel_center = bounds[3] - 0.5
    local_foot_y = offset_y + (last_opaque_pixel_center - runner.height / 2.0) * 0.34
    foot_errors.append(local_foot_y)
    assert abs(local_foot_y) < 0.5, (
        f"runner frame {frame_index} foot misses collision line by {local_foot_y:.2f}px"
    )

for asset_name in (
    "route_platform_ice.png",
    "route_platform_moving.png",
    "route_platform_snow.png",
    "route_terrain_snow_v2.png",
    "glacial_water_surface.png",
    "boss_laser_emitter.png",
    "laser_ice_impact.png",
    "arctic_launch_pad.png",
    "aurora_supply_block.png",
    "collectibles_atlas.png",
    "trail_objects_atlas.png",
):
    asset = Image.open(ROOT / "assets" / asset_name)
    assert asset.mode == "RGBA", (asset_name, asset.mode)
    assert asset.getbbox() is not None, asset_name

godot = os.environ.get("GODOT_BIN") or shutil.which("godot4") or shutil.which("godot")
if godot:
    parse_result = subprocess.run(
        [godot, "--headless", "--path", str(ROOT), "--editor", "--quit"],
        check=True,
        timeout=60,
        env={**os.environ, "HOME": os.environ.get("HOME", "/tmp")},
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    fatal_log_markers = (
        "SCRIPT ERROR:",
        "Failed to load script",
        "Parse Error:",
        "Compile Error:",
    )
    parse_failures = [
        line
        for line in parse_result.stdout.splitlines()
        if any(marker in line for marker in fatal_log_markers)
    ]
    if parse_failures:
        raise SystemExit(
            "Headless Godot reported script failures despite exit code 0:\n"
            + "\n".join(parse_failures)
        )
    runtime_status = "headless Godot parse passed"
else:
    runtime_status = "structural validation passed; set GODOT_BIN for parse validation"

print(
    "Godot production validation passed: "
    f"{len(REQUIRED)} required files, runner {runner.width}x{runner.height}, "
    f"{transparent} transparent pixels, max foot error "
    f"{max(abs(error) for error in foot_errors):.2f}px; {runtime_status}"
)
