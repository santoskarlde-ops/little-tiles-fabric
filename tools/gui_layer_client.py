#!/usr/bin/env python3
"""GuiLayer(boolean client, String name[, w, h]) in CreativeCore 26.x (verified from GuiLayer.java): every GuiLayer subclass
constructor gets a leading `boolean client`, passed to super(...)/this(...), and every `new <Layer>(...)` gets a client value:
  - lambda/method with a `Player player` in scope -> player.level().isClientSide()
  - inside a GuiLayer subclass (instance context)  -> isClient()      (IGuiParent.isClient() exists)
  - OverlayGuiLayer                                  -> true           (client-only overlay)
Anything else is listed, not guessed. Usage: python3 tools/gui_layer_client.py src/port/java   (idempotent)"""
import re, sys, pathlib, collections
root = pathlib.Path(sys.argv[1]); files = {f: f.read_text(encoding="utf-8") for f in root.rglob("*.java")}
layers = {"GuiLayer"}; changed = True
while changed:
    changed = False
    for f, t in files.items():
        for m in re.finditer(r"\bclass (\w+)(?:<[^>{]*>)?\s+extends\s+(\w+)", t):
            if m.group(2) in layers and m.group(1) not in layers: layers.add(m.group(1)); changed = True
layers.discard("GuiLayer"); print(len(layers), "GuiLayer classes:", ", ".join(sorted(layers)))
ctor_done = site_done = 0; pending = []
for f, t in files.items():
    orig = t; cls = re.search(r"\bclass (\w+)(?:<[^>{]*>)?\s+extends\s+(\w+)", t)
    is_layer_file = bool(cls and cls.group(1) in layers)
    # 1. constructors of layer classes declared in this file
    for name in layers:
        for m in list(re.finditer(r"\n(\s*)(public|protected)\s+" + name + r"\(([^)]*)\)(\s*)\{(\s*)(super|this)\(", t))[::-1]:
            params = m.group(3)
            if re.match(r"\s*boolean client\b", params): continue
            newp = "boolean client" + (", " + params.strip() if params.strip() else "")
            call_end = m.end()
            call = m.group(6) + "(client" + (", " if not t[call_end:call_end + 1] == ")" else "")
            t = t[:m.start()] + "\n" + m.group(1) + m.group(2) + " " + name + "(" + newp + ")" + m.group(4) + "{" + m.group(5) + call + t[call_end:]
            ctor_done += 1
    # 2. instantiation sites
    lines = t.split("\n")
    for i, l in enumerate(lines):
        for m in list(re.finditer(r"\bnew (" + "|".join(sorted(layers)) + r")\(", l))[::-1]:
            if re.match(r"\s*(client|true|false|player\.level)\b", l[m.end():]) or re.match(r"\s*isClient\(\)", l[m.end():]): continue
            before = l[:m.start()]
            ctx = "\n".join(lines[max(0, i - 2):i]) + "\n" + before
            lam = re.findall(r"(?:\(([^()]*)\)|(\w+))\s*->", ctx)
            params = " ".join(a or b for a, b in lam[-1:]) if lam else ""
            window = "\n".join(lines[max(0, i - 25):i + 1])
            if m.group(1) == "OverlayGuiLayer": expr = "true"
            elif re.search(r"\bplayer\b", params) or (not lam and re.search(r"\bPlayer player\b", window)): expr = "player.level().isClientSide()"
            elif is_layer_file and not re.search(r"\bstatic\b[^;{]*\bnew\b|\bstatic final\b", l): expr = "isClient()"
            else: pending.append((f.relative_to(root), i + 1, l.strip()[:120])); continue
            rest = l[m.end():]
            l = l[:m.end()] + expr + ("" if rest.lstrip().startswith(")") else ", ") + rest; lines[i] = l; site_done += 1
    t = "\n".join(lines)
    if t != orig: f.write_text(t, encoding="utf-8")
print(f"constructors changed: {ctor_done}, instantiation sites changed: {site_done}, pending (no client value derivable): {len(pending)}")
for p in pending: print("  ", p[0].name, p[1], "|", p[2])
