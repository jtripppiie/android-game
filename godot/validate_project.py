#!/usr/bin/env python3
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parent
required = [
    "project.godot", "export_presets.cfg", "scenes/main.tscn",
    "scripts/world.gd", "scripts/player.gd", "scripts/enemy.gd",
    "scripts/projectile.gd", "scripts/touch_controls.gd",
    "scripts/moving_platform.gd",
    "assets/runner_overhaul.png",
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
print(f"Godot overhaul validation passed: {image.width}x{image.height}, six frames, {transparent} transparent pixels")
