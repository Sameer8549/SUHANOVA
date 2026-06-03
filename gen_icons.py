#!/usr/bin/env python3
"""
Generate Suhanova app icons for all Android mipmap densities.
Golden 'S' on dark space background.
"""
from PIL import Image, ImageDraw, ImageFont
import os

# Density -> (folder, size)
DENSITIES = [
    ("mipmap-mdpi",    48),
    ("mipmap-hdpi",    72),
    ("mipmap-xhdpi",   96),
    ("mipmap-xxhdpi",  144),
    ("mipmap-xxxhdpi", 192),
]

BASE = r"C:\suhana\SuhanovaApp\app\src\main\res"

def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

BG    = hex_to_rgb("0A0A0F")   # Space black
GOLD  = hex_to_rgb("FFB800")   # Nova gold
PINK  = hex_to_rgb("FF4FA3")   # Stellar pink

def make_icon(size):
    img  = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Background circle
    margin = int(size * 0.04)
    draw.ellipse([margin, margin, size - margin, size - margin], fill=BG + (255,))

    # Gold gradient ring  
    ring = int(size * 0.05)
    draw.ellipse([margin, margin, size - margin, size - margin],
                 outline=GOLD + (200,), width=ring)

    # Draw "S" letter centered
    font_size = int(size * 0.55)
    try:
        font = ImageFont.truetype("C:/Windows/Fonts/arialbd.ttf", font_size)
    except:
        font = ImageFont.load_default()

    # Get bounding box
    bbox = draw.textbbox((0, 0), "S", font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    x = (size - tw) // 2 - bbox[0]
    y = (size - th) // 2 - bbox[1] - int(size * 0.03)

    # Draw S with gold color
    draw.text((x, y), "S", font=font, fill=GOLD + (255,))

    return img

def make_round_icon(size):
    img  = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Full circle background  
    draw.ellipse([0, 0, size, size], fill=BG + (255,))

    # Pink + gold border
    border = max(2, int(size * 0.04))
    draw.ellipse([border//2, border//2, size - border//2, size - border//2],
                 outline=GOLD + (220,), width=border)

    # Draw "S"
    font_size = int(size * 0.55)
    try:
        font = ImageFont.truetype("C:/Windows/Fonts/arialbd.ttf", font_size)
    except:
        font = ImageFont.load_default()

    bbox = draw.textbbox((0, 0), "S", font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    x = (size - tw) // 2 - bbox[0]
    y = (size - th) // 2 - bbox[1] - int(size * 0.03)
    draw.text((x, y), "S", font=font, fill=GOLD + (255,))

    return img

for folder, size in DENSITIES:
    folder_path = os.path.join(BASE, folder)
    os.makedirs(folder_path, exist_ok=True)

    icon       = make_icon(size)
    icon_round = make_round_icon(size)

    icon.save(os.path.join(folder_path, "ic_launcher.png"))
    icon_round.save(os.path.join(folder_path, "ic_launcher_round.png"))
    print(f"Generated {folder} ({size}x{size})")

print("\nAll icons generated successfully!")
