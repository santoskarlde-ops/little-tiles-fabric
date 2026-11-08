#!/usr/bin/env python3
"""Round 18. Idempotent. Needs build.log in the project root for the pose() step.
 1. IVecOrigin.pose()/pose(float) (absent in CreativeCore 26.x) -> VecOriginPose.of(recv[, partial]) using error positions from build.log
 2. FakeLevelInfo / FakeServerLevelInfo against 26.3 LevelData / WritableLevelData / ServerLevelData."""
import re, pathlib, sys, collections
ROOT = pathlib.Path('.'); R = ROOT/'src/port/java'
IMP = 'import team.creative.littletiles.common.compat.creativecore.VecOriginPose;'

def recv_start(s, dot):
    i = dot
    while i > 0:
        c = s[i-1]
        if c in ')]':
            op = '(' if c == ')' else '['; d = 0
            while i > 0:
                c = s[i-1]
                if c in ')]': d += 1
                elif c in '([': d -= 1
                i -= 1
                if d == 0: break
        elif c.isalnum() or c in '_.$': i -= 1
        else: break
    return i

def args_end(s, k):  # k = index after '('
    d = 1; q = False; m = k
    while m < len(s) and d:
        c = s[m]
        if q:
            if c == '\\': m += 1
            elif c == '"': q = False
        elif c == '"': q = True
        elif c in '([{': d += 1
        elif c in ')]}': d -= 1
        m += 1
    return m

def pose_step():
    log = pathlib.Path('build.log')
    if not log.exists(): print('no build.log, skipping pose step'); return
    L = log.read_text(errors='ignore').split('\n')
    sites = collections.defaultdict(set)
    for i, l in enumerate(L):
        m = re.match(r'\s*(/\S+?)/src/port/java/(\S+\.java):(\d+): error: cannot find symbol', l)
        if not m: continue
        blk = L[i:i+6]
        if not any(re.search(r'symbol:\s+method pose\(', b) for b in blk): continue
        if not any('IVecOrigin' in b for b in blk): continue
        caret = next((b for b in blk[1:4] if b.strip() == '^' ), None)
        if caret is None: continue
        sites[m[2]].add((int(m[3]), caret.index('^') - (len(caret) - len(caret.lstrip())) + (len(blk[1]) - len(blk[1].lstrip())) if False else caret.index('^'), blk[1]))
    done = 0
    for rel, items in sites.items():
        p = R/rel
        if not p.exists(): continue
        lines = p.read_text().split('\n'); touched = False
        for ln, col, shown in sorted(items, key=lambda x: (-x[0], -x[1])):
            s = lines[ln-1]
            # the log prints the source line verbatim; find ".pose(" at/near caret
            idx = s.find('.pose(', max(0, col-2))
            if idx < 0 or idx > col + 2: idx = s.rfind('.pose(', 0, col+3)
            if idx < 0: continue
            st = recv_start(s, idx); recv = s[st:idx]
            k = idx + len('.pose('); e = args_end(s, k); arg = s[k:e-1].strip()
            lines[ln-1] = s[:st] + 'VecOriginPose.of(' + recv + (', ' + arg if arg else '') + ')' + s[e:]
            touched = True; done += 1
        if touched:
            t = '\n'.join(lines)
            if IMP not in t:
                t = re.sub(r'(package [^;]+;\n)', r'\1\n' + IMP + '\n', t, count=1)
            p.write_text(t)
    print('pose sites rewritten:', done)

def levelinfo_step():
    p = R/'team/creative/littletiles/common/level/little/FakeLevelInfo.java'
    t = p.read_text()
    if 'RespawnData' not in t:
        for nm in ['getSpawnPos', 'getSpawnAngle', 'getDayTime', 'isThundering', 'isRaining', 'setRaining', 'getGameRules']:
            t = re.sub(r'@Override\s*\n(\s*public [\w.<>]+ ' + nm + r'\()', r'\1', t)
        t = t.replace('import net.neoforged.neoforge.common.CommonHooks;\n', '')
        t = t.replace('        CommonHooks.onDifficultyChange(difficulty, this.difficulty);\n', '')
        t = t.replace('import net.minecraft.world.level.storage.WritableLevelData;', 'import net.minecraft.core.GlobalPos;\nimport net.minecraft.world.level.storage.LevelData.RespawnData;\nimport net.minecraft.world.level.storage.WritableLevelData;')
        t = t.replace('    private BlockPos spawnPos;\n    private float spawnAngle;\n', '    private RespawnData respawn = RespawnData.DEFAULT;\n')
        t = t.replace('''    public BlockPos getSpawnPos() {
        return spawnPos;''', '''    public BlockPos getSpawnPos() {
        return respawn.pos();''')
        t = t.replace('''    public float getSpawnAngle() {
        return spawnAngle;''', '''    public float getSpawnAngle() {
        return respawn.yaw();''')
        t = re.sub(r'    @Override\s*\n    public void setSpawn\(BlockPos pos, float angle\) \{.*?\n    \}\n', '''    @Override
    public RespawnData getRespawnData() {
        return respawn;
    }
    
    @Override
    public void setSpawn(RespawnData data) {
        this.respawn = data;
    }
    
    public void setSpawn(BlockPos pos, float angle) {
        this.respawn = new RespawnData(GlobalPos.of(respawn.dimension(), pos.immutable()), angle, respawn.pitch());
    }
''', t, flags=re.S)
        p.write_text(t)
    p = R/'team/creative/littletiles/server/level/little/FakeServerLevelInfo.java'
    t = p.read_text()
    keep = {'getLevelName', 'getGameType', 'isInitialized', 'setInitialized', 'isAllowCommands', 'setGameType', 'setGameTime', 'setAllowCommands'}
    def strip(m):
        return m.group(0) if m.group(2) in keep else m.group(1) + m.group(3)
    t = re.sub(r'(@Override\s*\n\s*)?(?:)', '', t, count=0) if False else t
    t = re.sub(r'    @Override\n(    public [\w.<>]+ (\w+)\()', lambda m: ('    @Override\n' if m.group(2) in keep else '') + m.group(1), t)
    if 'setAllowCommands' not in t:
        t = t.replace('    @Override\n    public void setGameType(GameType type) {}', '    @Override\n    public void setGameType(GameType type) {}\n    \n    @Override\n    public void setAllowCommands(boolean allow) {}')
    p.write_text(t)
    print('level info patched')
pose_step(); levelinfo_step()
