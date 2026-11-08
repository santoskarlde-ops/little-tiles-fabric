#!/usr/bin/env python3
"""Rewrite LittleTiles imports of CreativeCore classes that moved between
CreativeCore 2.13.x (1.21 branch) and 2.14.x (26.x branch).
Usage: python3 tools/relocate_cc_imports.py <java source dir> [--dry-run]
Original mod: CreativeMD, N247S (LGPL-3.0). Mechanical import changes only."""
import pathlib, re, sys

B = "team.creative.creativecore."
MOVES = {
    B + "common.util.math.origin.ChildVecOrigin": B + "common.util.math.matrix.ChildVecOrigin",
    B + "common.util.math.origin.IVecOrigin": B + "common.util.math.matrix.IVecOrigin",
    B + "common.util.math.origin.VecOrigin": B + "common.util.math.matrix.VecOrigin",
    B + "common.gui.GuiControlRect": B + "client.gui.GuiControlRect",
    B + "common.gui.extension.GuiExtensionCreator": B + "client.gui.extension.GuiExtensionCreator",
    B + "common.gui.integration.ScreenEventListener": B + "client.gui.integration.ScreenEventListener",
}

def main():
    root = pathlib.Path(sys.argv[1]); dry = "--dry-run" in sys.argv
    changed = 0
    for f in root.rglob("*.java"):
        t = f.read_text(encoding="utf-8"); o = t
        for old, new in MOVES.items():
            t = re.sub(r"\b" + re.escape(old) + r"\b", new, t)
        if t != o:
            changed += 1
            if not dry: f.write_text(t, encoding="utf-8")
    print(f"{'would change' if dry else 'changed'} {changed} files")

if __name__ == "__main__":
    main()
