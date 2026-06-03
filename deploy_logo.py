"""
Deploy Suhanova logo to all Android resource folders.
Uses the exact uploaded logo - no modifications to the design.
"""
from PIL import Image
import os

SRC = r"C:\Users\abdul\.gemini\antigravity\brain\5daed2e5-85c2-4d85-9ad2-a44a041a1c9a\media__1780430755296.jpg"
RES = r"c:\suhana\SuhanovaApp\app\src\main\res"
DRAWABLE = r"c:\suhana\SuhanovaApp\app\src\main\res\drawable"

img = Image.open(SRC).convert("RGBA")
print("Loaded logo: %dx%d" % img.size)

# Android mipmap icon sizes
mipmap_sizes = {
    "mipmap-mdpi":    48,
    "mipmap-hdpi":    72,
    "mipmap-xhdpi":   96,
    "mipmap-xxhdpi":  144,
    "mipmap-xxxhdpi": 192,
}

for folder, size in mipmap_sizes.items():
    out_dir = os.path.join(RES, folder)
    os.makedirs(out_dir, exist_ok=True)
    resized = img.resize((size, size), Image.LANCZOS)
    resized.save(os.path.join(out_dir, "ic_launcher.png"), "PNG")
    resized.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG")
    print("  OK %s: %dx%d" % (folder, size, size))

# Drawable assets for use in Compose
os.makedirs(DRAWABLE, exist_ok=True)

sizes_drawable = {
    "logo_suhanova.png":        512,
    "logo_suhanova_large.png":  512,
    "logo_suhanova_medium.png": 256,
    "logo_suhanova_small.png":  128,
    "logo_suhanova_64.png":      64,
    "ic_notification.png":       96,
    "logo_512.png":             512,
}

for fname, size in sizes_drawable.items():
    resized = img.resize((size, size), Image.LANCZOS)
    resized.save(os.path.join(DRAWABLE, fname), "PNG")
    print("  OK drawable/%s: %dx%d" % (fname, size, size))

print("\nDone! All logo assets deployed.")
