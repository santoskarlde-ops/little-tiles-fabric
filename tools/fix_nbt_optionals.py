#!/usr/bin/env python3
"""Error-driven fix for 26.x NBT getters returning Optional. Reads javac errors from a build log and, on exactly
the flagged lines, rewrites tag.getInt("k") -> tag.getInt("k").orElse(0) etc. (Optional API only, so no guessed names.)
Also rewrites CompoundTag.getList(k, type) -> getListOrEmpty(k) (name verified in CreativeCore 26.x) where javac flagged it.
Usage: python3 tools/fix_nbt_optionals.py build.log src/port/java   (rerun after each compile; idempotent)"""
import re, sys, pathlib, collections
log, root = sys.argv[1], pathlib.Path(sys.argv[2])
ACC = {  # Optional<T> -> (getter, default)
    "Integer": ("getInt", "0"), "Boolean": ("getBoolean", "false"), "String": ("getString", '""'),
    "Float": ("getFloat", "0f"), "Double": ("getDouble", "0.0"), "Long": ("getLong", "0L"),
    "int[]": ("getIntArray", "new int[0]"), "long[]": ("getLongArray", "new long[0]"),
    "byte[]": ("getByteArray", "new byte[0]"), "CompoundTag": ("getCompound", "new CompoundTag()"),
}
rx = re.compile(r"^(.*\.java):(\d+): error: (.*)$")
todo = collections.defaultdict(set)   # (file, line) -> {('opt', T) | ('list',)}
for l in open(log, encoding="utf-8", errors="replace"):
    m = rx.match(l.rstrip("\n"))
    if not m: continue
    p, ln, msg = m.groups()
    p = p.split("src/port/java/", 1)[-1]
    mo = re.match(r"incompatible types: Optional<(.+?)> cannot be converted to ", msg) or re.match(r"bad operand type Optional<(.+?)> for unary", msg)
    if mo and mo.group(1) in ACC: todo[(p, int(ln))].add(("opt", mo.group(1)))
    elif msg.startswith("method getList in class CompoundTag cannot be applied"): todo[(p, int(ln))].add(("list",))

def close_paren(s, i):  # s[i] == '(' ; return index of matching ')'
    d = 0; q = None; j = i
    while j < len(s):
        c = s[j]
        if q:
            if c == "\\": j += 1
            elif c == q: q = None
        elif c in "\"'": q = c
        elif c == "(": d += 1
        elif c == ")":
            d -= 1
            if d == 0: return j
        j += 1
    return -1

def fix_line(s, kinds):
    n = 0
    for k in kinds:
        if k[0] == "opt":
            g, dflt = ACC[k[1]]
            pos = 0
            while True:
                m = re.compile(r"\.%s\(" % g).search(s, pos)
                if not m: break
                e = close_paren(s, m.end() - 1)
                if e < 0: break
                if s[e+1:e+9] == ".orElse(" or s[e+1:e+12] == ".orElseGet(": pos = e + 1; continue
                s = s[:e+1] + ".orElse(%s)" % dflt + s[e+1:]; n += 1; pos = e + 1
        else:
            m = re.search(r"\.getList\(", s)
            if m:
                e = close_paren(s, m.end() - 1)
                inner = s[m.end():e]; depth = 0; cut = -1
                for i, c in enumerate(inner):  # last top-level comma
                    if c in "([{": depth += 1
                    elif c in ")]}": depth -= 1
                    elif c == "," and depth == 0: cut = i
                if cut > 0: s = s[:m.start()] + ".getListOrEmpty(" + inner[:cut] + ")" + s[e+1:]; n += 1
    return s, n

changed = fixes = 0; byfile = collections.defaultdict(dict)
for (p, ln), kinds in todo.items(): byfile[p][ln] = kinds
for p, lines in byfile.items():
    f = root / p
    if not f.exists(): continue
    src = f.read_text(encoding="utf-8").split("\n"); dirty = False
    for ln, kinds in lines.items():
        new, n = fix_line(src[ln-1], kinds)
        if n: src[ln-1] = new; fixes += n; dirty = True
    if dirty: f.write_text("\n".join(src), encoding="utf-8"); changed += 1
print(f"flagged lines: {sum(len(v) for v in byfile.values())}  rewrites: {fixes}  files changed: {changed}")
