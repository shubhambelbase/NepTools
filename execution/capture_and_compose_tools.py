import os
import time
import subprocess
from PIL import Image, ImageDraw, ImageFont

ADB = r"D:\Android\Sdk\platform-tools\adb.exe"
DEVICE = "192.168.1.72:39671"
PACKAGE = "com.neptools.app"
MAIN_ACTIVITY = "com.neptools.app/.MainActivity"
OUT_DIR = os.path.join(".tmp", "screenshots")
MOSAIC_FILE = os.path.join(".tmp", "all_tools_mosaic.png")

os.makedirs(OUT_DIR, exist_ok=True)

TOOLS = [
    ("01_tools_hub", "tools", "Tools Hub Grid"),
    ("02_calendar", "calendar", "Calendar Patro"),
    ("03_day_detail", "day/2083/5/15", "Day Detail & Panchang"),
    ("04_date_converter", "converter", "BS-AD Date Converter"),
    ("05_weather", "weather", "Weather & Solar Engine"),
    ("06_forex_currency", "currency", "Forex Currency Rates"),
    ("07_age_calc", "age", "Age & Milestones Calc"),
    ("08_rashifal", "rashifal", "Daily Rashifal Horoscope"),
    ("09_emergency", "emergency", "Emergency Directory"),
    ("10_fuel_prices", "fuel", "Live Fuel Prices"),
    ("11_kalimati", "kalimati", "Kalimati Market Rates"),
    ("12_radio_fm", "radio", "Live FM Radio Streamer"),
    ("13_electricity_bill", "bill_calc", "Electricity & Water Bill"),
    ("14_loan_emi", "loan_emi", "Loan EMI Calculator"),
    ("15_bill_splitter", "bill_splitter", "Bill Splitter"),
    ("16_land_converter", "land_converter", "Land Area Converter"),
    ("17_postal_codes", "postal", "Postal Codes Directory"),
    ("18_driving_license", "driving_license", "Driving License Prep"),
    ("19_doc_templates", "templates", "Legal Document Templates"),
    ("20_qr_scanner", "qr", "QR Scanner & Generator"),
    ("21_compass", "compass", "Magnetic Compass"),
    ("22_vastu_compass", "vastu_compass", "Vastu Shastra Compass"),
    ("23_bubble_level", "bubble_level", "Bubble Spirit Level"),
    ("24_sound_meter", "sound_meter", "Decibel Sound Meter"),
    ("25_spy_camera", "spy_camera", "Spy Camera Detector"),
    ("26_pet_whistle", "pet_whistle", "Pet Ultrasonic Whistle"),
    ("27_password_vault", "password_vault", "Encrypted Password Vault"),
    ("28_file_converter", "file_converter", "Document & PDF Engine"),
    ("29_image_compressor", "image_compressor", "Image Compressor"),
    ("30_lan_drop", "lan_drop", "LAN Drop Local Sharing"),
    ("31_speed_test", "speed_test", "Network Speed Test"),
    ("32_decision_maker", "decision_maker", "Decision Wheel"),
    ("33_muhurat", "muhurat", "Auspicious Muhurat"),
    ("34_guna_milan", "guna_milan", "36-Point Guna Milan"),
    ("35_ekadashi", "ekadashi_list", "Ekadashi Vrata Dates"),
    ("36_habit_tracker", "habit_tracker", "52-Week Habit Tracker"),
    ("37_subscription_tracker", "subscription_tracker", "Subscription Manager"),
    ("38_astrology_hub", "astrology", "Astrology Hub"),
    ("39_kundali", "astrology/kundali", "Janma Kundali Chart"),
    ("40_gochar_wheel", "astrology/gochar", "Gochar Transit Wheel"),
    ("41_dasha_periods", "astrology/dasha", "Vimshottari Dasha"),
    ("42_voice_notes", "voice", "Voice Notes & Audio"),
    ("43_recent_updates", "recent_updates", "Recent Updates Freshness"),
    ("44_settings", "settings", "Settings & Diagnostics")
]

def run_adb(args):
    cmd = [ADB, "-s", DEVICE] + args
    res = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    return res.stdout.strip()

def capture_screen(route, out_path):
    run_adb(["shell", "am", "start", "-n", MAIN_ACTIVITY, "--es", "navigate_to_route", route])
    time.sleep(1.4)
    run_adb(["shell", "screencap", "-p", "/sdcard/screen_temp.png"])
    run_adb(["pull", "/sdcard/screen_temp.png", out_path])

def main():
    print(f"Starting screenshot capture for {len(TOOLS)} tool screens on {DEVICE}...")
    saved_images = []

    for idx, (tool_id, route, label) in enumerate(TOOLS, start=1):
        target_path = os.path.join(OUT_DIR, f"{tool_id}.png")
        print(f"[{idx}/{len(TOOLS)}] Capturing: {label} (route: {route})...")
        try:
            capture_screen(route, target_path)
            if os.path.exists(target_path) and os.path.getsize(target_path) > 1000:
                saved_images.append((target_path, label))
            else:
                print(f"Warning: Failed to capture {tool_id}")
        except Exception as e:
            print(f"Error capturing {tool_id}: {e}")

    # Return to home
    run_adb(["shell", "am", "start", "-n", MAIN_ACTIVITY, "--es", "navigate_to_route", "home"])

    print(f"\nCaptured {len(saved_images)} screens. Building composite mosaic...")
    if not saved_images:
        print("No images captured.")
        return

    # Create composite mosaic
    # Layout: 6 columns x 8 rows (for 44 images)
    cols = 6
    rows = (len(saved_images) + cols - 1) // cols

    # Thumb dimensions (scaled down from 1080x2400 to crisp thumbnail size, e.g. 270x600)
    thumb_w = 270
    thumb_h = 600
    label_h = 44
    cell_w = thumb_w + 16
    cell_h = thumb_h + label_h + 16
    header_h = 100

    mosaic_w = cols * cell_w + 32
    mosaic_h = rows * cell_h + header_h + 32

    # Background color: deep slate (#0F172A)
    bg_color = (15, 23, 42)
    card_bg = (30, 41, 59)
    text_color = (241, 245, 249)
    subtext_color = (148, 163, 184)
    accent_color = (249, 115, 22)

    mosaic = Image.new("RGB", (mosaic_w, mosaic_h), bg_color)
    draw = ImageDraw.Draw(mosaic)

    try:
        font_title = ImageFont.truetype("arial.ttf", 36)
        font_sub = ImageFont.truetype("arial.ttf", 20)
        font_label = ImageFont.truetype("arial.ttf", 16)
    except:
        font_title = ImageFont.load_default()
        font_sub = ImageFont.load_default()
        font_label = ImageFont.load_default()

    # Draw Header
    draw.text((32, 24), "NepTools v2.7.3 - Comprehensive UI/UX Review Mosaic", fill=text_color, font=font_title)
    draw.text((32, 68), f"Live Device: Xiaomi Redmi (21061119BI) • Total Screened Features: {len(saved_images)}", fill=subtext_color, font=font_sub)

    for i, (img_path, label) in enumerate(saved_images):
        c = i % cols
        r = i // cols
        x0 = 32 + c * cell_w
        y0 = header_h + 16 + r * cell_h

        # Draw card background
        draw.rounded_rectangle([x0, y0, x0 + cell_w - 8, y0 + cell_h - 8], radius=8, fill=card_bg, outline=(51, 65, 85), width=1)

        # Load and resize screenshot
        try:
            with Image.open(img_path) as im:
                im_thumb = im.resize((thumb_w, thumb_h), Image.Resampling.LANCZOS)
                mosaic.paste(im_thumb, (x0 + 4, y0 + 4))
        except Exception as err:
            print(f"Error loading {img_path}: {err}")

        # Draw label
        label_y = y0 + 4 + thumb_h + 8
        # Truncate if too long
        display_label = label if len(label) <= 24 else label[:22] + ".."
        draw.text((x0 + 8, label_y), f"{i+1}. {display_label}", fill=text_color, font=font_label)

    mosaic.save(MOSAIC_FILE, format="PNG", optimize=True)
    print(f"Composite mosaic created successfully: {MOSAIC_FILE}")
    print(f"Mosaic Dimensions: {mosaic_w}x{mosaic_h}, Size: {os.path.getsize(MOSAIC_FILE)} bytes")

if __name__ == "__main__":
    main()
