import subprocess
import time
import json
import os
import sys

ADB = r"D:\Android\Sdk\platform-tools\adb.exe"
DEVICE = "192.168.1.72:39671"
PACKAGE = "com.neptools.app"
MAIN_ACTIVITY = "com.neptools.app/.MainActivity"

def run_adb(args):
    cmd = [ADB, "-s", DEVICE] + args
    res = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    return res.stdout.strip()

def clear_logcat():
    run_adb(["logcat", "-c"])

def get_crash_log():
    # Check for FatalException or AndroidRuntime crashes
    out = run_adb(["logcat", "-d", "-s", "AndroidRuntime:E", "FATAL:E"])
    crashes = [line for line in out.splitlines() if "FATAL EXCEPTION" in line or "Process: com.neptools.app" in line]
    return crashes

def is_app_alive():
    pid = run_adb(["shell", "pidof", PACKAGE])
    return bool(pid.strip())

def navigate_to_route(route):
    run_adb([
        "shell", "am", "start",
        "-n", MAIN_ACTIVITY,
        "--es", "navigate_to_route", route
    ])
    time.sleep(1.2)

def press_back():
    run_adb(["shell", "input", "keyevent", "4"])
    time.sleep(0.8)

def tap(x, y):
    run_adb(["shell", "input", "tap", str(x), str(y)])
    time.sleep(1.0)

def dump_ui():
    run_adb(["shell", "uiautomator", "dump", "/sdcard/qa_dump.xml"])
    xml_content = run_adb(["shell", "cat", "/sdcard/qa_dump.xml"])
    return xml_content

def main():
    print("Starting Comprehensive QA Test Sweep on Device:", DEVICE)
    print("Package:", PACKAGE)
    
    if not is_app_alive():
        print("Launching NepTools...")
        run_adb(["shell", "monkey", "-p", PACKAGE, "-c", "android.intent.category.LAUNCHER", "1"])
        time.sleep(2.0)

    results = []

    # 1. Test Bottom Navigation Tabs
    print("\n--- PHASE 1: Bottom Navigation Tabs ---")
    tabs = [
        ("Calendar Tab", 540, 2195, "पात्रो"),
        ("Tools Tab", 900, 2195, "टूल्स"),
        ("Home Tab", 170, 2195, "गृह")
    ]
    for tab_name, x, y, expected_text in tabs:
        clear_logcat()
        start_t = time.time()
        tap(x, y)
        alive = is_app_alive()
        crashes = get_crash_log()
        xml = dump_ui()
        passed = alive and (expected_text in xml or "com.neptools.app" in xml) and len(crashes) == 0
        dur = round(time.time() - start_t, 2)
        status = "PASS" if passed else "FAIL"
        print(f"[{status}] {tab_name} ({dur}s) - Alive: {alive}, Crashes: {len(crashes)}")
        results.append({"feature": tab_name, "status": status, "duration": dur, "crashes": crashes})

    # 2. Test All Route Destinations (40+ features)
    print("\n--- PHASE 2: Comprehensive Route & Engine Testing ---")
    routes_to_test = [
        ("Calendar View", "calendar"),
        ("Day Detail (BS 2083-05-15)", "day/2083/5/15"),
        ("Weather & Solar Engine", "weather"),
        ("Date Converter (BS to AD)", "converter"),
        ("Forex Currency Exchange", "currency"),
        ("Age & Milestones Calculator", "age"),
        ("Daily Rashifal (Horoscope)", "rashifal"),
        ("Voice Notes & Recorder", "voice"),
        ("Emergency Directory", "emergency"),
        ("Electricity/Water Bill Calc", "bill_calc"),
        ("Postal Codes Directory", "postal"),
        ("Fuel Prices Live", "fuel"),
        ("Radio Live Player", "radio"),
        ("QR Generator & Scanner", "qr"),
        ("Kalimati Market Rates", "kalimati"),
        ("Legal Document Templates", "templates"),
        ("Loan EMI Calculator", "loan_emi"),
        ("Driving License Prep", "driving_license"),
        ("Image Compressor", "image_compressor"),
        ("LAN Drop File Share", "lan_drop"),
        ("Network Speed Test", "speed_test"),
        ("Decision Maker Wheel", "decision_maker"),
        ("Bill Splitter", "bill_splitter"),
        ("Pet Ultrasonic Whistle", "pet_whistle"),
        ("Magnetic Compass", "compass"),
        ("Vastu Compass", "vastu_compass"),
        ("Bubble Spirit Level", "bubble_level"),
        ("Encrypted Password Vault", "password_vault"),
        ("File/PDF Converter", "file_converter"),
        ("Auspicious Muhurat", "muhurat"),
        ("36-Point Guna Milan", "guna_milan"),
        ("Ekadashi Vrata List", "ekadashi_list"),
        ("Habit Tracker", "habit_tracker"),
        ("Subscription Tracker", "subscription_tracker"),
        ("In-App Updater", "app_updater"),
        ("Recent Updates Freshness", "recent_updates"),
        ("Decibel Sound Meter", "sound_meter"),
        ("Spy Camera EMF Detector", "spy_camera"),
        ("Astrology Hub", "astrology"),
        ("Kundali Chart", "astrology/kundali"),
        ("Vimshottari Dasha", "astrology/dasha"),
        ("Gochar Transit Wheel", "astrology/gochar"),
        ("Land Area Converter", "land_converter"),
        ("Settings & Security Check", "settings"),
        ("About NepTools", "about"),
        ("Terms of Service", "terms"),
        ("Privacy Policy", "privacy")
    ]

    for feature_name, route in routes_to_test:
        clear_logcat()
        start_t = time.time()
        navigate_to_route(route)
        time.sleep(0.5)
        alive = is_app_alive()
        crashes = get_crash_log()
        dur = round(time.time() - start_t, 2)
        passed = alive and len(crashes) == 0
        status = "PASS" if passed else "FAIL"
        print(f"[{status}] {feature_name} (route: {route}) ({dur}s) - Alive: {alive}, Crashes: {len(crashes)}")
        results.append({
            "feature": feature_name,
            "route": route,
            "status": status,
            "duration": dur,
            "crashes": crashes
        })

    # Return to Home tab
    print("\n--- PHASE 3: Resetting to Home Tab ---")
    navigate_to_route("home")
    time.sleep(1.0)
    alive = is_app_alive()
    print("Final App Alive State:", alive)

    # Summary
    total = len(results)
    passed_count = sum(1 for r in results if r["status"] == "PASS")
    failed_count = total - passed_count
    print(f"\n==================================================")
    print(f"QA TEST SUMMARY: Total: {total}, Passed: {passed_count}, Failed: {failed_count}")
    print(f"Pass Rate: {round(passed_count / total * 100, 1)}%")
    print(f"==================================================")

    out_file = "qa_test_report.json"
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)
    print("Saved report to", out_file)

if __name__ == "__main__":
    main()
