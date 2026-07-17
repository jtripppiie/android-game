#!/usr/bin/env python3
"""Build the boot splash from the game's exact background and runner assets."""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "assets"
OUT = ASSETS / "boot_splash.png"
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"


def fit_background(image: Image.Image, size: tuple[int, int]) -> Image.Image:
    ratio = max(size[0] / image.width, size[1] / image.height)
    resized = image.resize((round(image.width * ratio), round(image.height * ratio)), Image.Resampling.LANCZOS)
    left = (resized.width - size[0]) // 2
    top = (resized.height - size[1]) // 2
    return resized.crop((left, top, left + size[0], top + size[1]))


def runner_frame() -> Image.Image:
    sheet = Image.open(ASSETS / "runner_overhaul.png").convert("RGBA")
    width = sheet.width // 6
    image = sheet.crop((width * 3, 0, width * 4, sheet.height))
    bounds = image.getchannel("A").getbbox()
    image = image.crop(bounds)
    return image.resize((round(image.width * 0.78), round(image.height * 0.78)), Image.Resampling.LANCZOS)


def main() -> None:
    canvas = fit_background(Image.open(ASSETS / "background_dark_winter.png").convert("RGB"), (1280, 720)).convert("RGBA")
    canvas = Image.alpha_composite(canvas, Image.new("RGBA", canvas.size, (2, 12, 24, 78)))
    draw = ImageDraw.Draw(canvas, "RGBA")
    draw.rounded_rectangle((70, 112, 760, 596), radius=38, fill=(3, 17, 30, 196), outline=(77, 219, 184, 230), width=4)

    title_font = ImageFont.truetype(FONT, 92)
    alaska_font = ImageFont.truetype(FONT, 42)
    small_font = ImageFont.truetype(FONT, 24)
    draw.text((116, 174), "YOU RUSH", font=title_font, fill="#fff4d2", stroke_width=5, stroke_fill="#071326")
    draw.text((120, 292), "ALASKA EXPEDITION", font=alaska_font, fill="#84eaff", stroke_width=3, stroke_fill="#071326")
    draw.line((120, 365, 664, 365), fill="#ffda79", width=7)
    draw.text((120, 405), "RUN · JUMP · RESCUE", font=small_font, fill="white")
    draw.text((120, 458), "FIVE TRAILS. ONE WILD JOURNEY.", font=small_font, fill="#d5e4e8")

    runner = runner_frame()
    shadow = Image.new("RGBA", runner.size, (0, 0, 0, 0))
    shadow.putalpha(runner.getchannel("A").filter(ImageFilter.GaussianBlur(10)))
    canvas.alpha_composite(shadow, (900, 140))
    canvas.alpha_composite(runner, (876, 116))
    canvas.convert("RGB").save(OUT, quality=96)
    print(OUT)


if __name__ == "__main__":
    main()
