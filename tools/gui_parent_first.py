#!/usr/bin/env python3
"""Error-driven port of CreativeCore 1.21 -> 26.x GUI constructors, which now take the parent (IGuiParent) first.
Constructor list below is taken from javac's own 'required:' / candidate output in the build log, not guessed.
On exactly the lines javac flagged, `new GuiX(args)` -> `new GuiX(<parent>, args)`.
<parent> = receiver of an enclosing `recv.add/addLeft/addRight(new GuiX(...))` when it is a plain identifier chain, else `this`.
UNVERIFIED ASSUMPTION: the parent argument is the container the control is added to (or `this`); the compiler cannot check that.
NOT handled (reported): GuiLayer (26.x takes a leading boolean, meaning unknown), super(...)/this(...) in GuiParent subclasses
(needs a parent parameter threaded through), non-GUI constructors.
Usage: python3 tools/gui_parent_first.py build.log src/port/java   (idempotent: skips calls that already start with a parent)"""
import re, sys, pathlib, collections
PARENT_FIRST = {"GuiControl","GuiLabel","GuiButton","GuiButtonContext","GuiButtonIcon","GuiCheckBox","GuiColorPicker","GuiColumn",
 "GuiComboBox","GuiConfigSubControlNested","GuiCounter","GuiCounterDecimal","GuiInventoryGrid","GuiLeftRightBox","GuiPanel","GuiParent",
 "GuiProgressbar","GuiRow","GuiScrollY","GuiShowItem","GuiSlider","GuiSlotViewer","GuiStateButton","GuiSteppedSlider","GuiTabs","GuiTable",
 "GuiTextfield","GuiTimeline","GuiTree","GuiTreeItem",
 # round 5: first-argument mismatch ("String/Player/SimpleContainer cannot be converted to IGuiParent") in build_319a43.log, or required: IGuiParent,...
 "GuiLabeledControl","GuiPlayerInventoryGrid","GuiStackSelector","GuiSlot","GuiTabButton","GuiListBoxBase","GuiArraySlider"}
log, root = sys.argv[1], pathlib.Path(sys.argv[2])
rx = re.compile(r"^(?:.*?src/port/java/)(.*\.java):(\d+): error: (constructor (\w+) in class \w+ cannot be applied|no suitable constructor found for (\w+)\(|incompatible types: ([\w.]+|<anonymous [^>]*>) cannot be converted to IGuiParent|constructor (\w+) in class \w+<.*?> cannot be applied)")
flag = collections.defaultdict(set); wrong = []
for l in open(log, encoding="utf-8", errors="replace"):
    m = rx.match(l.rstrip("\n"))
    if m:
        flag[m.group(1)].add(int(m.group(2)))
        mt = re.search(r"incompatible types: (<anonymous [^>]*>|[\w.]+) cannot be converted to IGuiParent", l)
        if mt and mt.group(1) not in ("String","Player","SimpleContainer","MutableComponent"): wrong.append((m.group(1), int(m.group(2)), mt.group(1)))
NEW = re.compile(r"\bnew\s+(%s)\s*(<[^()]*?>)?\s*\(" % "|".join(sorted(PARENT_FIRST)))
ADDCALL = re.compile(r"([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\.(?:add|addLeft|addRight|addCenter)$")
def enclosing_add_receiver(s, pos):
    """Walk back from pos over unmatched '(' (skipping string literals); return receiver of the nearest enclosing recv.add*( call."""
    depth = 0; i = pos - 1
    while i >= 0:
        c = s[i]
        if c == '"':                      # skip string literal backwards
            i -= 1
            while i >= 0 and not (s[i] == '"' and s[i-1] != "\\"): i -= 1
        elif c == ")": depth += 1
        elif c == "(":
            if depth == 0:
                m = ADDCALL.search(s[:i])
                if m: return m.group(1)
            else: depth -= 1
        i -= 1
    return None
def fix(s):
    out, pos, n = [], 0, 0
    for m in NEW.finditer(s):
        rest = s[m.end():].lstrip()
        par = enclosing_add_receiver(s, m.start()) or "this"
        if re.match(r"(this|parent|%s)\s*[,)]" % re.escape(par), rest): continue   # already ported (idempotent)
        out.append(s[pos:m.end()]); pos = m.end()
        out.append(par + ("" if rest.startswith(")") else ", ")); n += 1
    out.append(s[pos:]); return "".join(out), n
done = skipped = files = 0; manual = collections.Counter()
for rel, lines in flag.items():
    f = root / rel
    if not f.exists(): continue
    src = f.read_text(encoding="utf-8").split("\n"); dirty = False
    for ln in sorted(lines):
        new, n = fix(src[ln-1])
        if n: src[ln-1] = new; done += n; dirty = True
        else:
            t = src[ln-1].strip()
            manual["super/this call" if re.match(r"(super|this)\(", t) else "GuiLayer" if "GuiLayer" in t else "other"] += 1
    if dirty: f.write_text("\n".join(src), encoding="utf-8"); files += 1
if wrong:
    print("\n`this` is not an IGuiParent here (anonymous class / non-GUI class): pick the enclosing container by hand:")
    for w in sorted(set(wrong)): print(f"  {w[0].split('/')[-1]}:{w[1]}  ({w[2]})")
print(f"constructor calls ported: {done} in {files} files; flagged lines left for manual work: {dict(manual)}")
