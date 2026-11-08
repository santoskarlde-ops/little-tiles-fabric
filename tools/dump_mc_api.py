#!/usr/bin/env python3
"""Dump the public API of the Minecraft 26.3 classes the port keeps tripping over, using javap on the jar
Loom already downloaded. Shell-agnostic (works from fish). Needs a JDK on PATH or JAVA_HOME.
Usage: python3 tools/dump_mc_api.py [output file, default ~/Downloads/mc-api.txt]"""
import os, sys, shutil, subprocess, zipfile, pathlib

NAMES = ["Level", "ClientLevel", "ServerLevel", "LevelAccessor", "LevelReader", "BlockGetter", "LevelData", "WritableLevelData",
         "ServerLevelData", "Item", "Block", "BlockBehaviour", "BlockEntity", "Entity", "ServerChunkCache", "ChunkSource",
         "DistanceManager", "TicketStorage", "TicketType", "Ticket", "ServerPlayer", "ServerGamePacketListenerImpl",
         "ServerCommonPacketListenerImpl", "ClientPacketListener", "EntityRenderer", "ItemStack", "Ingredient",
         "RecipeManager", "PoseStack", "RenderType", "GuiGraphicsExtractor", "KeyMapping", "BlockEntityRenderer"]

def find_jar():
    for root in (pathlib.Path.home() / ".gradle", pathlib.Path(__file__).resolve().parent.parent / ".gradle"):
        if not root.exists(): continue
        for p in root.rglob("*.jar"):
            if "26.3" not in str(p): continue
            try:
                with zipfile.ZipFile(p) as z:
                    if "net/minecraft/world/level/Level.class" in z.namelist(): return p
            except Exception: pass
    return None

def resolve(jar):
    with zipfile.ZipFile(jar) as z: entries = [n for n in z.namelist() if n.startswith("net/minecraft/") and n.endswith(".class") and "$" not in n]
    out, missing = [], []
    for n in NAMES:
        hits = [e[:-6].replace("/", ".") for e in entries if e.rsplit("/", 1)[-1] == n + ".class"]
        if hits: out += hits[:3]
        else: missing.append(n)
    return out, missing

def javap():
    j = shutil.which("javap")
    if j: return j
    jh = os.environ.get("JAVA_HOME")
    if jh and os.path.exists(os.path.join(jh, "bin", "javap")): return os.path.join(jh, "bin", "javap")
    sys.exit("javap not found: put a JDK on PATH or set JAVA_HOME")

if __name__ == "__main__":
    jar = find_jar()
    if not jar: sys.exit("could not find the Minecraft 26.3 jar under ~/.gradle (run ./gradlew compileJava once first)")
    classes, missing = resolve(jar)
    outp = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path.home() / "Downloads" / "mc-api.txt")
    with open(outp, "w", encoding="utf-8") as f:
        f.write(f"# jar: {jar}\n# not found: {missing}\n")
        for c in classes:
            r = subprocess.run([javap(), "-public", "-cp", str(jar), c], capture_output=True, text=True)
            f.write(f"\n=== {c}\n{r.stdout}{r.stderr if r.returncode else ''}")
    print(f"wrote {outp}  ({len(classes)} classes; not found: {missing})")
