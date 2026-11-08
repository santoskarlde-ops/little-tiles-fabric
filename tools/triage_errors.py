#!/usr/bin/env python3
"""Group javac errors from a Gradle build log so the port can be fixed in bulk.
Usage:  ./gradlew compileJava -PportSource=true 2>&1 | tee build.log ; python3 tools/triage_errors.py build.log"""
import re, sys, collections
text = open(sys.argv[1], encoding="utf-8", errors="replace").read() if len(sys.argv) > 1 else sys.stdin.read()
lines = text.splitlines()
err = re.compile(r"^(?:e: )?(.*\.java):(\d+): error: (.*)$")
by_kind = collections.Counter(); by_pkg = collections.Counter(); files = collections.Counter(); total = 0
for i, l in enumerate(lines):
    m = err.match(l)
    if not m: continue
    total += 1; path, _, msg = m.groups(); files[path] += 1
    mp = re.search(r"littletiles/(\w+/\w+)", path); by_pkg[mp.group(1) if mp else "(other)"] += 1
    if msg.startswith("package ") and "does not exist" in msg:
        by_kind["MISSING PACKAGE " + msg.split()[1]] += 1
    elif msg == "cannot find symbol":
        sym = next((x.strip() for x in lines[i+1:i+6] if x.strip().startswith("symbol:")), "symbol: ?")
        by_kind["MISSING " + sym.replace("symbol:", "").strip()] += 1
    else:
        by_kind[re.sub(r"\b\w+\.java\b", "", msg)[:90]] += 1
tally = [l.strip() for l in lines if re.match(r"^\s*[\d,]+ errors?\s*$", l)]
print(f"javac tally: {tally[0] if tally else '?'}  | error lines parsed: {total} (each error is printed twice in the Gradle log, so use javac's number)  | files with errors: {len(files)}\n")
print("== top causes"); [print(f"{c:6} {k}") for k, c in by_kind.most_common(40)]
print("\n== by LittleTiles package"); [print(f"{c:6} {k}") for k, c in by_pkg.most_common(20)]
