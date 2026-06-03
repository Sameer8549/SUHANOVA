import os
import math
from PIL import Image, ImageDraw

def create_icons():
    img_path = r"C:\Users\abdul\.gemini\antigravity\brain\5daed2e5-85c2-4d85-9ad2-a44a041a1c9a\media__1780412586914.jpg"
    img = Image.open(img_path).convert("RGB")
    
    # 1. Extract the raw logo from the "SVG (SCALABLE)" section
    # Let's crop a wide area and then trim black borders
    svg_area = img.crop((683, 490, 1024, 768))
    
    # Find bounding box by checking for non-black pixels
    bbox = svg_area.getbbox() # getbbox works best on grayscale or if background is pure black (0,0,0)
    # The background might not be *pure* 0,0,0. Let's do a threshold.
    gray = svg_area.convert("L")
    # Threshold: pixels > 20 are white, else black
    bw = gray.point(lambda x: 255 if x > 20 else 0, mode="1")
    bbox = bw.getbbox()
    
    if bbox:
        raw_logo = svg_area.crop(bbox)
        
        # We have the raw logo. Let's create an Android adaptive icon background and foreground, or just legacy icons.
        # Let's just create a square legacy ic_launcher (with a black background and the logo centered).
        
        def make_icon(size, padding_ratio=0.2):
            icon = Image.new("RGBA", (size, size), (10, 10, 15, 255)) # Dark background matching app theme
            # Calculate logo size
            logo_max_size = int(size * (1 - padding_ratio * 2))
            
            # Resize raw logo to fit within logo_max_size while preserving aspect ratio
            w, h = raw_logo.size
            ratio = min(logo_max_size / w, logo_max_size / h)
            new_w, new_h = int(w * ratio), int(h * ratio)
            
            resized_logo = raw_logo.resize((new_w, new_h), Image.Resampling.LANCZOS)
            
            # Paste into center
            offset = ((size - new_w) // 2, (size - new_h) // 2)
            
            # Convert raw_logo to RGBA to handle transparency (wait, it's from a jpg so no transparency)
            # Since the background of raw_logo is black, we can paste it directly onto a black background or use screen blending.
            # But wait, the app theme background is #0A0A0F (10, 10, 15).
            # The raw_logo has a black background (0,0,0).
            # Let's just make the icon background pure black to match the raw_logo background.
            icon = Image.new("RGB", (size, size), (0, 0, 0))
            icon.paste(resized_logo, offset)
            return icon

        res_dir = r"c:\suhana\SuhanovaApp\app\src\main\res"
        
        sizes = {
            "mipmap-mdpi": 48,
            "mipmap-hdpi": 72,
            "mipmap-xhdpi": 96,
            "mipmap-xxhdpi": 144,
            "mipmap-xxxhdpi": 192,
        }
        
        for folder, size in sizes.items():
            folder_path = os.path.join(res_dir, folder)
            os.makedirs(folder_path, exist_ok=True)
            icon = make_icon(size)
            icon.save(os.path.join(folder_path, "ic_launcher.png"))
            icon.save(os.path.join(folder_path, "ic_launcher_round.png")) # Just duplicate for now

        print("Icons generated successfully!")
    else:
        print("Could not find bounding box in SVG section.")

if __name__ == "__main__":
    create_icons()
