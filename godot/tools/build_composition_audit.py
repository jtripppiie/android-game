#!/usr/bin/env python3
"""Build deterministic visual-review frames from the exact game assets.

These are composition reconstructions, not Godot screenshots. They make sprite
orientation, relative scale, ground alignment, contrast, and HUD/control crowding
reviewable on a headless machine whose dummy renderer cannot capture pixels.
"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "assets"
OUT = ROOT.parent / "verification" / "current"
OUT.mkdir(parents=True, exist_ok=True)

STAGES = [
    ("MIDNIGHT SUN", "background_midnight_sun.png", "boss_laser_emitter.png", 0.30, 1, 500),
    ("SALMON RUSH", "background_midnight_sun.png", "wildlife_salmon_swim.png", 1.05, 6, 520),
    ("MOOSE PASS", "background_midnight_sun.png", "wildlife_moose_walk.png", 0.62, 6, 520),
    ("DARK WINTER", "background_dark_winter.png", "wildlife_eagle_fly.png", 0.58, 6, 510),
    ("BEAR COUNTRY", "background_dark_winter.png", "wildlife_polar_bear_walk.png", 0.90, 6, 470),
]


def frame(path: Path, frames: int, index: int = 0) -> Image.Image:
    image = Image.open(path).convert("RGBA")
    width = image.width // frames
    return image.crop((index * width, 0, (index + 1) * width, image.height))


def alpha_crop(image: Image.Image) -> Image.Image:
    bounds = image.getchannel("A").getbbox()
    return image.crop(bounds) if bounds else image


def scaled(image: Image.Image, factor: float) -> Image.Image:
    return image.resize(
        (max(1, round(image.width * factor)), max(1, round(image.height * factor))),
        Image.Resampling.LANCZOS,
    )


def paste_feet(canvas: Image.Image, image: Image.Image, center_x: int, ground_y: int) -> None:
    canvas.alpha_composite(image, (round(center_x - image.width / 2), ground_y - image.height))


def label(draw: ImageDraw.ImageDraw, xy: tuple[int, int], text: str, fill: str = "white") -> None:
    draw.text(xy, text, font=ImageFont.load_default(size=20), fill=fill, stroke_width=3, stroke_fill="#071326")


def build() -> None:
    runner = scaled(alpha_crop(frame(ASSETS / "runner_overhaul.png", 6, 3)), 0.34)
    outputs = []
    for stage_index, (name, backdrop_name, boss_name, boss_scale, boss_frames, ground) in enumerate(STAGES):
        backdrop = Image.open(ASSETS / backdrop_name).convert("RGB")
        backdrop = backdrop.resize((1280, 720), Image.Resampling.LANCZOS).convert("RGBA")
        veil = Image.new("RGBA", backdrop.size, (3, 12, 22, 52))
        canvas = Image.alpha_composite(backdrop, veil)
        draw = ImageDraw.Draw(canvas, "RGBA")

        draw.rectangle((0, ground, 1280, 720), fill=(26, 54, 67, 255))
        draw.rectangle((0, ground, 1280, ground + 18), fill=(221, 241, 241, 255))
        draw.line((0, ground, 1280, ground), fill=(255, 218, 121, 255), width=3)

        boss = scaled(alpha_crop(frame(ASSETS / boss_name, boss_frames, 0)), boss_scale)
        # Sheets are authored left-facing; normal arena puts the player left.
        paste_feet(canvas, runner, 260, ground)
        paste_feet(canvas, boss, 880, ground)

        label(draw, (24, 18), f"{name} · COMPOSITION AUDIT (NOT ENGINE CAPTURE)", "#fff0a8")
        label(draw, (178, ground - runner.height - 34), f"RUNNER {runner.width}x{runner.height}")
        label(draw, (760, ground - boss.height - 34), f"BOSS {boss.width}x{boss.height} · FACES PLAYER")
        draw.line((260, ground - runner.height, 260, ground), fill=(77, 219, 184, 180), width=2)
        draw.line((880, ground - boss.height, 880, ground), fill=(255, 98, 84, 180), width=2)

        # Reconstruct the intended source-only phone controls for crowding review.
        draw.rounded_rectangle((34, 548, 146, 652), radius=24, fill=(5, 26, 40, 235), outline="#84d5e8", width=3)
        draw.rounded_rectangle((250, 548, 362, 652), radius=24, fill=(5, 26, 40, 235), outline="#84d5e8", width=3)
        draw.rounded_rectangle((142, 444, 254, 548), radius=24, fill=(5, 26, 40, 235), outline="#84d5e8", width=3)
        draw.rounded_rectangle((142, 586, 254, 662), radius=24, fill=(5, 26, 40, 235), outline="#84d5e8", width=3)
        label(draw, (74, 581), "LEFT")
        label(draw, (286, 581), "RIGHT")
        label(draw, (176, 475), "UP")
        label(draw, (166, 607), "DOWN")
        draw.ellipse((1096, 530, 1246, 680), fill=(199, 110, 15, 245), outline="#fff0a8", width=4)
        label(draw, (1138, 592), "JUMP", "#071326")
        draw.ellipse((968, 402, 1084, 518), fill=(5, 97, 133, 245), outline="#84eaff", width=4)
        label(draw, (994, 447), "SNOW")
        draw.rounded_rectangle((960, 554, 1078, 650), radius=24, fill=(87, 43, 133, 245), outline="#d7adff", width=4)
        label(draw, (992, 588), "DASH")

        output = OUT / f"stage-{stage_index + 1}-{name.lower().replace(' ', '-')}.png"
        canvas.convert("RGB").save(output, quality=95)
        outputs.append(output)
    print("\n".join(str(path) for path in outputs))


if __name__ == "__main__":
    build()
