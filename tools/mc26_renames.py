#!/usr/bin/env python3
"""Apply Minecraft 1.21 -> 26.3 renames that are *observed* in CreativeCore's own port
(same files, 1.21 branch vs 26.x branch). Only 1:1, behaviour-neutral renames live here.
Usage: python3 tools/mc26_renames.py <java source dir>
Also covers OnlyIn->Environment, isClientSide(), Util move.
NOT applied (need manual work): GuiGraphics -> GuiGraphicsExtractor (methods changed),
BakedQuad / VertexFormat package moves (Phase 4, rendering), ResourceLocation.tryParse (no use seen in CC 26.x)."""
import pathlib, re, sys
RENAMES = [
    # Fabric has no OnlyIn; @Environment is the Loader equivalent (stripped the same way)
    (r"import net\.neoforged\.api\.distmarker\.OnlyIn;", "import net.fabricmc.api.EnvType;\nimport net.fabricmc.api.Environment;"),
    (r"@OnlyIn\((?:value\s*=\s*)?Dist\.CLIENT\)", "@Environment(EnvType.CLIENT)"),
    (r"@OnlyIn\((?:value\s*=\s*)?Dist\.DEDICATED_SERVER\)", "@Environment(EnvType.SERVER)"),
    # Level.isClientSide became private; CreativeCore 26.x only calls the method
    (r"\.isClientSide\b(?!\s*\()", ".isClientSide()"),
    (r"\bnet\.minecraft\.Util\b", "net.minecraft.util.Util"),
    (r"\bnet\.minecraft\.resources\.ResourceLocation\b", "net.minecraft.resources.Identifier"),
    (r"\bResourceLocation\b", "Identifier"),
    (r"\bnet\.minecraft\.FileUtil\b", "net.minecraft.util.FileUtil"),
]
root = pathlib.Path(sys.argv[1]); n = 0
for f in root.rglob("*.java"):
    t = o = f.read_text(encoding="utf-8")
    for pat, rep in RENAMES: t = re.sub(pat, rep, t)
    if t != o: f.write_text(t, encoding="utf-8"); n += 1
print("changed", n, "files")

# Upstream already carries @Environment next to @OnlyIn in places; drop the duplicate this codemod creates.
def dedupe(root):
    for f in root.rglob("*.java"):
        s = o = f.read_text(encoding="utf-8")
        s = re.sub(r"(?m)^([ \t]*@Environment\(EnvType\.\w+\)[ \t]*\r?\n)(?:[ \t]*@Environment\(EnvType\.\w+\)[ \t]*\r?\n)+", lambda m: m.group(1), s)
        seen = set(); out = []
        for line in s.split("\n"):
            k = line.strip()
            if k.startswith("import ") and k.endswith(";"):
                if k in seen: continue
                seen.add(k)
            out.append(line)
        s = "\n".join(out)
        if s != o: f.write_text(s, encoding="utf-8")
dedupe(root)
