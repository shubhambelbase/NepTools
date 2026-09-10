import os
import stat
import shutil
from collections import defaultdict

ROOT = "\\\\?\\C:\\"
LARGE_THRESHOLD = 100 * 1024 * 1024
TOP_LARGE = 40


def clean(p):
    return p.replace("\\\\?\\", "").lower()


CATS = [
    ("Recycle Bin", lambda p: p.startswith("c:\\$recycle.bin"), "safe"),
    ("Windows.old", lambda p: p.startswith("c:\\windows.old"), "safe"),
    ("User Temp folders", lambda p: "\\appdata\\local\\temp\\" in p, "safe"),
    ("Windows Temp", lambda p: p.startswith("c:\\windows\\temp"), "safe"),
    ("Windows Update download cache", lambda p: p.startswith("c:\\windows\\softwaredistribution\\download"), "safe"),
    ("Delivery Optimization cache", lambda p: "\\deliveryoptimization\\" in p, "safe"),
    ("Prefetch", lambda p: p.startswith("c:\\windows\\prefetch"), "safe"),
    ("Crash dumps / error reports", lambda p: ("\\wer\\" in p or "\\minidump\\" in p or "\\crashdumps\\" in p or "\\livekernelreports\\" in p or p.endswith(".dmp")), "safe"),
    ("Browser caches", lambda p: ("\\user data\\" in p or "\\firefox\\profiles\\" in p) and ("cache" in p or "service worker" in p or "cachestorage" in p or "gpucache" in p), "safe"),
    ("Gradle caches", lambda p: "\\.gradle\\" in p and ("caches" in p or "wrapper" in p or "daemon" in p), "safe"),
    ("Maven cache", lambda p: "\\.m2\\repository" in p, "safe"),
    ("npm/yarn/pnpm cache", lambda p: ("\\npm-cache" in p or "\\.npm" in p or "\\pnpm\\store" in p or "\\yarn\\cache" in p or "\\.cache\\yarn" in p), "safe"),
    ("pip cache", lambda p: "\\pip\\cache" in p or "\\.cache\\pip" in p, "safe"),
    ("JetBrains/Android Studio caches", lambda p: ("\\androidstudio" in p or "\\jetbrains\\" in p or "\\.android\\" in p) and ("cache" in p or "caches" in p or "index" in p), "safe"),
    ("VS Code caches", lambda p: "\\appdata\\roaming\\code\\" in p and ("cache" in p or "cachestorage" in p or "service worker" in p), "safe"),
    ("AI/model caches (HF, Ollama...)", lambda p: ("\\.cache\\" in p or "\\ollama" in p or "\\lm.studio" in p), "review"),
    ("Android SDK", lambda p: "\\android\\sdk" in p, "review"),
    ("WSL/Docker disk images", lambda p: p.endswith(".vhdx") or p.endswith(".vhd"), "review"),
    ("Downloads installers/archives", lambda p: "\\downloads\\" in p and p.endswith((".zip", ".7z", ".rar", ".iso", ".exe", ".msi", ".tar.gz")), "review"),
    ("Log/trace files", lambda p: p.endswith(".log") or p.endswith(".etl"), "review"),
    ("Windows Installer cache", lambda p: p.startswith("c:\\windows\\installer"), "system-do-not-delete"),
    ("WinSxS component store", lambda p: p.startswith("c:\\windows\\winsxs"), "system-do-not-delete"),
    ("System files", lambda p: p in ("c:\\pagefile.sys", "c:\\swapfile.sys", "c:\\hiberfil.sys", "c:\\memory.dmp"), "system-do-not-delete"),
]


def categorize(p):
    for name, fn, risk in CATS:
        try:
            if fn(p):
                return name, risk
        except Exception:
            pass
    return "General (apps, documents...)", "general"


def human(n):
    n = float(n)
    for unit in ("B", "KB", "MB", "GB", "TB"):
        if n < 1024 or unit == "TB":
            return f"{n:.1f} {unit}" if unit != "B" else f"{int(n)} B"
        n /= 1024.0


def main():
    du = shutil.disk_usage("C:\\")
    print("==== C: DRIVE SPACE ANALYSIS ====")
    print(f"Total: {human(du.total)}   Used: {human(du.used)}   Free: {human(du.free)}")
    print()
    top_sizes = defaultdict(int)
    user_sizes = defaultdict(int)
    usersub_sizes = defaultdict(int)
    cat_size = defaultdict(int)
    cat_count = defaultdict(int)
    large = []
    skipped = 0
    files = 0
    stack = [ROOT]
    while stack:
        d = stack.pop()
        try:
            entries = os.scandir(d)
        except OSError:
            skipped += 1
            continue
        with entries:
            for e in entries:
                try:
                    if e.is_symlink():
                        continue
                    st = e.stat(follow_symlinks=False)
                    if stat.S_ISDIR(st.st_mode):
                        if getattr(st, "st_file_attributes", 0) & stat.FILE_ATTRIBUTE_REPARSE_POINT:
                            continue
                        stack.append(e.path)
                        continue
                    sz = st.st_size
                    files += 1
                    p = clean(e.path)
                    parts = p.split("\\")
                    if len(parts) > 1 and parts[1]:
                        top_sizes[parts[1]] += sz
                        if parts[1] == "users" and len(parts) > 2:
                            user_sizes[parts[2]] += sz
                            if len(parts) > 3:
                                usersub_sizes[(parts[2], parts[3])] += sz
                    cat, risk = categorize(p)
                    cat_size[(risk, cat)] += sz
                    cat_count[(risk, cat)] += 1
                    if sz >= LARGE_THRESHOLD:
                        large.append((sz, p, cat, risk))
                except OSError:
                    skipped += 1
    print(f"Scanned {files} files ({human(sum(top_sizes.values()))} reachable), skipped {skipped} entries without access")
    print("(run from an admin terminal for a fuller scan of Windows system folders)")
    print()
    print("-- Top-level folders --")
    for k, v in sorted(top_sizes.items(), key=lambda x: -x[1])[:12]:
        print(f"  C:\\{k:<28} {human(v):>12}")
    print()
    print("-- Per-user usage --")
    for k, v in sorted(user_sizes.items(), key=lambda x: -x[1])[:6]:
        print(f"  C:\\Users\\{k:<24} {human(v):>12}")
        subs = [(sk[1], sv) for sk, sv in usersub_sizes.items() if sk[0] == k]
        for sk, sv in sorted(subs, key=lambda x: -x[1])[:6]:
            print(f"      \\{sk:<26} {human(sv):>12}")
    print()
    for risk, label in (("safe", "SAFE TO DELETE"), ("review", "REVIEW BEFORE DELETING"), ("system-do-not-delete", "SYSTEM - DO NOT DELETE MANUALLY"), ("general", "GENERAL FILES (unclassified)")):
        items = [(k[1], v, cat_count[k]) for k, v in cat_size.items() if k[0] == risk]
        items.sort(key=lambda x: -x[1])
        total = sum(v for _, v, _ in items)
        print(f"-- {label} --  TOTAL {human(total)}")
        for name, v, c in items[:12]:
            print(f"  {name:<36} {human(v):>12}  ({c} files)")
        print()
    large.sort(reverse=True)
    print(f"-- {TOP_LARGE} largest files --")
    for sz, p, cat, risk in large[:TOP_LARGE]:
        print(f"  {human(sz):>12}  [{risk:<21}] {p}")


if __name__ == "__main__":
    main()
