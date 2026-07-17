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

        # Reconstruct the current circular thumb pad, ported from the most
        # responsive Java build: compact, draggable, diagonal, and forgiving.
        center = (124, 600)
        draw.ellipse((31, 515, 217, 701), fill=(0, 0, 0, 70))
        draw.ellipse((34, 510, 214, 690), fill=(9, 23, 33, 205), outline="#84d5e8", width=3)
        draw.ellipse((64, 517, 184, 637), fill=(255, 255, 255, 18))
        arrows = [
            ((72, 600), [(60, 600), (80, 592), (80, 608)]),
            ((176, 600), [(188, 600), (168, 592), (168, 608)]),
            ((124, 548), [(124, 536), (116, 556), (132, 556)]),
            ((124, 652), [(124, 664), (116, 644), (132, 644)]),
        ]
        for arrow_center, points in arrows:
            x, y = arrow_center
            draw.ellipse((x - 23, y - 23, x + 23, y + 23), fill=(255, 255, 255, 158))
            draw.polygon(points, fill="#18202a")
        draw.ellipse((110, 586, 138, 614), fill=(255, 218, 121, 235))
        draw.ellipse((118, 594, 130, 606), fill="#18202a")

        draw.ellipse((1122, 556, 1246, 680), fill=(199, 110, 15, 225), outline="#fff0a8", width=4)
        draw.text((1184, 618), "JUMP", anchor="mm", font=ImageFont.load_default(size=19), fill="#071326")
        draw.ellipse((1020, 456, 1114, 550), fill=(5, 97, 133, 225), outline="#84eaff", width=4)
        draw.text((1067, 503), "SNOW", anchor="mm", font=ImageFont.load_default(size=17), fill="white")
        draw.rounded_rectangle((1008, 584, 1106, 660), radius=22, fill=(87, 43, 133, 225), outline="#d7adff", width=4)
        draw.text((1057, 622), "DASH", anchor="mm", font=ImageFont.load_default(size=17), fill="white")

        output = OUT / f"stage-{stage_index + 1}-{name.lower().replace(' ', '-')}.png"
        canvas.convert("RGB").save(output, quality=95)
        outputs.append(output)
    print("\n".join(str(path) for path in outputs))


if __name__ == "__main__":
    build()
