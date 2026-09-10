import os
import re
import shutil
import sys

TARGETS = [
    ("Gradle caches", r"C:\Users\Shubham\.gradle\caches", "dir"),
    ("npm cache (Local)", r"C:\Users\Shubham\AppData\Local\npm-cache", "dir"),
    ("npm cache (Roaming)", r"C:\Users\Shubham\AppData\Roaming\npm-cache", "dir"),
    ("Claude old versions", r"C:\Users\Shubham\.local\share\claude\versions", "claude-versions"),
    ("Claude old binary", r"C:\Users\Shubham\.local\bin\claude.exe.old.1787331536781", "file"),
    ("Google Updater crx_cache", r"C:\Program Files (x86)\Google\GoogleUpdater\crx_cache", "children"),
    ("Driver Booster downloads", r"C:\ProgramData\IObit\Driver Booster\download", "children"),
    ("Broken torch ~orch", r"C:\Users\Shubham\AppData\Roaming\Python\Python314\site-packages\~orch", "dir"),
    ("HF whisper model", r"C:\Users\Shubham\.cache\huggingface\hub\models--systran--faster-whisper-small", "dir"),
]

PP = "\\\\?\\"


def dir_size(p):
    total = 0
    stack = [PP + p if not p.startswith(PP) else p]
    while stack:
        d = stack.pop()
        try:
            with os.scandir(d) as it:
                for e in it:
                    try:
                        if e.is_dir(follow_symlinks=False):
                            stack.append(e.path)
                        else:
                            total += e.stat(follow_symlinks=False).st_size
                    except OSError:
                        pass
        except OSError:
            pass
    return total


def human(n):
    n = float(n)
    for unit in ("B", "KB", "MB", "GB"):
        if n < 1024 or unit == "GB":
            return f"{n:.1f} {unit}"
        n /= 1024.0


def rm_tree(path):
    errors = 0

    def onerr(fn, p, exc):
        nonlocal errors
        errors += 1

    shutil.rmtree(path, onerror=onerr)
    return errors


def main():
    try:
        import ctypes
        admin = ctypes.windll.shell32.IsUserAnAdmin()
    except Exception:
        admin = False
    print(f"Running as admin: {admin}")
    if not admin:
        print("NOTE: without admin, Program Files / ProgramData targets may fail.\n")

    before = shutil.disk_usage("C:\\").free
    grand = 0

    for label, path, mode in TARGETS:
        if not os.path.exists(path):
            print(f"  {label:<28} -- not found, skipped")
            continue

        if mode == "claude-versions":
            freed = 0
            fails = 0
            versions = []
            with os.scandir(path) as it:
                for e in it:
                    if e.is_dir():
                        nums = tuple(int(x) for x in re.findall(r"\d+", e.name))
                        versions.append((nums, e.name, e.path))
            if versions:
                keep = max(versions)[1]
                for nums, name, vpath in versions:
                    if name != keep:
                        sz = dir_size(vpath)
                        f = rm_tree(PP + vpath)
                        freed += sz if f == 0 else 0
                        fails += f
                        print(f"  {label:<28} removed {name}  ({human(sz)})")
                print(f"  {label:<28} kept {keep}")
            grand += freed
            continue

        if mode == "file":
            sz = os.path.getsize(path)
            try:
                os.remove(PP + path)
                print(f"  {label:<28} deleted  ({human(sz)})")
                grand += sz
            except OSError as ex:
                print(f"  {label:<28} FAILED: {ex}")
            continue

        sz = dir_size(path)
        if sz == 0 and mode == "dir":
            print(f"  {label:<28} empty/locked, skipped")
            continue

        if mode == "dir":
            fails = rm_tree(PP + path)
        else:
            fails = 0
            with os.scandir(PP + path) as it:
                children = [e.path for e in it]
            for c in children:
                try:
                    if os.path.isdir(c):
                        fails += rm_tree(c)
                    else:
                        os.remove(c)
                except OSError:
                    fails += 1

        if fails == 0:
            print(f"  {label:<28} deleted  ({human(sz)})")
            grand += sz
        else:
            print(f"  {label:<28} partial: {fails} locked/failed items, freed ~{human(sz) if fails == 0 else 'part'}")
            grand += sz

    after = shutil.disk_usage("C:\\").free
    print()
    print(f"Total reclaimed (measured targets): {human(grand)}")
    print(f"Free space: {human(before)} -> {human(after)}")


if __name__ == "__main__":
    sys.exit(main())
