#!/usr/bin/env python3
"""Caret-driven port of CreativeCore 26.x GUI member changes (verified against CreativeCore sources):
  GuiParent.flow = X             -> setFlow(X)                 (GuiParent.setFlow(GuiFlow) exists)
  GuiControl.visible/enabled = b -> setVisible(b)/setEnabled(b) (GuiControl.setVisible/setEnabled exist)
  GuiCheckBox.value (read)       -> get()                      (returns false on the server: dist() == null there)
  GuiCheckBox.value = v;         -> GuiCompat.setSilent(cb, v) (set() notifies listeners; the old field write did not)
Only the exact token javac pointed at (caret column) is changed. Reads of `enabled` and all `rect` uses are listed, not changed.
Usage: python3 tools/gui_members.py build.log src/port/java   (idempotent)"""
import re, sys, pathlib, collections
log = open(sys.argv[1], encoding="utf-8", errors="replace").read().split("\n"); root = pathlib.Path(sys.argv[2])
rx = re.compile(r"^.*?src/port/java/(.*\.java):(\d+): error: cannot find symbol$")
IMPORT = "import team.creative.littletiles.common.gui.GuiCompat;"
IMPORT2 = "import team.creative.littletiles.client.gui.GuiClientCompat;"

hits = {}
for i, l in enumerate(log):
    m = rx.match(l)
    if not m: continue
    sym = None; caret = None
    for k in range(i + 1, min(i + 8, len(log))):
        x = log[k]
        if k > i + 1 and ": error:" in x: break           # do not read the next error's symbol/caret
        if x.strip().startswith("symbol:"): sym = x.strip()[7:].strip()
        if re.match(r"^\s*\^\s*$", x) and caret is None: caret = x.index("^")
    mm = re.match(r"variable (flow|value|enabled|visible|rect)$", sym or "")
    if mm and caret is not None: hits[(m.group(1), int(m.group(2)), mm.group(1), caret)] = log[i + 1]
byfile = collections.defaultdict(lambda: collections.defaultdict(list))
for (f, n, name, caret), srcline in hits.items(): byfile[f][n].append((name, caret, srcline))
done = collections.Counter(); manual = []; applied = set(); checked = set()
for f, lines in byfile.items():
    p = root / f
    if not p.exists(): continue
    src = p.read_text(encoding="utf-8").split("\n"); dirty = False
    for n, v in lines.items():
        for name, caret, srcline in sorted(v, key=lambda t: -t[1]):
            s = src[n - 1]
            if (f, n) not in checked:                     # compare with the log once per line; later hits on the same line see our own edits
                if s.strip() != srcline.strip():
                    if (f, n, name) not in applied: manual.append((f, n, name, "line changed"))
                    continue
                checked.add((f, n))
            pos = s.find(name, max(0, caret - 1))
            if pos < 0: manual.append((f, n, name, "token not found")); continue
            head, after = s[:pos], s[pos + len(name):]
            if head.endswith(("GuiClientCompat.", "GuiCompat.")) or (name in ("rect", "enabled", "visible", "value") and after.startswith("(")):
                applied.add((f, n, name)); continue          # duplicate log entry for a token we already rewrote
            assign = re.match(r"\s*=(?!=)\s*(.+);\s*$", after)
            if name == "flow" and assign:
                recv = head[:-1] if head.endswith(".") else head
                s = recv + (".setFlow(" if head.endswith(".") else "setFlow(") + assign.group(1) + ");"; done["flow"] += 1
            elif name in ("visible", "enabled") and assign and head.endswith("."):
                s = head[:-1] + (".setVisible(" if name == "visible" else ".setEnabled(") + assign.group(1) + ");"; done[name] += 1
            elif name == "value" and assign and head.endswith("."):
                ind = re.match(r"\s*", s).group(0)
                s = ind + "GuiCompat.setSilent(" + head[:-1].strip() + ", " + assign.group(1) + ");"; done["value="] += 1
            elif name in ("rect", "enabled", "visible") and not assign:
                fn = {"rect": "rect", "enabled": "isEnabled", "visible": "isVisible"}[name]
                if head.endswith("."):
                    j = len(head) - 2; depth = 0
                    while j >= 0:
                        c = head[j]
                        if c == ")": depth += 1
                        elif c == "(":
                            if depth == 0: break
                            depth -= 1
                        elif depth == 0 and not (c.isalnum() or c in "_."): break
                        j -= 1
                    s = head[:j + 1] + "GuiClientCompat." + fn + "(" + head[j + 1:-1] + ")" + after
                else:
                    s = head + "GuiClientCompat." + fn + "(this)" + after
                done[name + " read"] += 1
            elif name == "value" and not assign and head.endswith("."):
                s = head[:-1] + ".get()" + after; done["value read"] += 1
            else: manual.append((f, n, name, s.strip()[:110])); continue
            src[n - 1] = s; dirty = True; applied.add((f, n, name))
    if dirty:
        text = "\n".join(src)
        if "GuiCompat.setSilent" in text and IMPORT not in text:
            text = re.sub(r"(\nimport )", "\n" + IMPORT + r"\1", text, count=1)
        if "GuiClientCompat." in text and IMPORT2 not in text:
            text = re.sub(r"(\nimport )", "\n" + IMPORT2 + r"\1", text, count=1)
        p.write_text(text, encoding="utf-8")
manual = [m for m in manual if (m[0], m[1], m[2]) not in applied]
print("ported:", dict(done)); print("left manual:", dict(collections.Counter(m[2] for m in manual)))
for m in manual[:6]: print("  ", m[0].split("/")[-1], m[1], m[2], "|", m[3])
