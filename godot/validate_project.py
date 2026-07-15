#!/usr/bin/env python3
from pathlib import Path
import shutil
import subprocess
from PIL import Image

root = Path(__file__).resolve().parent
required = [
    "project.godot", "export_presets.cfg", "scenes/main.tscn",
    "scripts/world.gd", "scripts/player.gd", "scripts/enemy.gd",
    "scripts/projectile.gd", "scripts/touch_controls.gd",
    "scripts/moving_platform.gd",
    "scripts/encounter_card.gd", "scripts/encounter_director.gd",
    "scripts/reactive_ice.gd", "scripts/freezable_water.gd",
    "assets/runner_overhaul.png",
    "assets/route_platform_ice.png", "assets/route_platform_moving.png",
    "assets/route_platform_snow.png", "assets/glacial_water_surface.png",
    "assets/boss_laser_emitter.png", "assets/laser_ice_impact.png",
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
for state in ("idle", "run", "sprint", "crouch", "jump", "fall", "dash", "stomp"):
    assert f'"{state}"' in player, state
for action in ("move_left", "move_right", "crouch", "jump", "fire", "sprint", "dash"):
    assert f'"{action}"' in touch, action

image = Image.open(root / "assets/runner_overhaul.png")
assert image.mode == "RGBA", image.mode
assert image.width % 6 == 0, image.size
alpha = image.getchannel("A")
transparent = sum(1 for value in alpha.get_flattened_data() if value == 0)
assert transparent > image.width * image.height * 0.60

for asset_name in (
    "route_platform_ice.png", "route_platform_moving.png", "route_platform_snow.png",
    "glacial_water_surface.png", "boss_laser_emitter.png", "laser_ice_impact.png",
):
    asset = Image.open(root / "assets" / asset_name)
    assert asset.mode == "RGBA", (asset_name, asset.mode)
    assert asset.getbbox() is not None, asset_name

for marker in ("build_directed_encounters", "encounter_sequence", "ReactiveIce", "card.hazards"):
    assert marker in world, marker
projectile = (root / "scripts/projectile.gd").read_text()
assert 'has_method("snowball_hit")' in projectile
assert "area_entered.connect" in projectile
freezable_water = (root / "scripts/freezable_water.gd").read_text()
for marker in ("bridge.shattered.connect", "_on_bridge_shattered", 'set_deferred("disabled", false)'):
    assert marker in freezable_water, marker

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
