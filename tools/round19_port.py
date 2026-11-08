#!/usr/bin/env python3
"""Round 19: packet-listener handlers for 26.3 (interfaces from mc-api3.txt). Idempotent.
Usage: python3 tools/round19_port.py path/to/mc-api3.txt
New abstract handlers are forwarded to the vanilla listener, exactly like the existing handlers in these classes."""
import re, sys, pathlib
api = open(sys.argv[1]).read()
R = pathlib.Path('src/port/java/team/creative/littletiles')
def abstracts(*names):
    out = []
    for s in re.split(r'(?m)^Compiled from ', api)[1:]:
        if s.split('"')[1] in names:
            for l in s.split('\n'):
                m = re.search(r'abstract void (\w+)\(([\w.]+)\);', l)
                if m: out.append((m[1], m[2]))
    return out

def patch(path, ifaces, vanilla, drop, rename_stale):
    p = R/path; t = p.read_text()
    for name, typ in drop:   # stale handlers whose packet type no longer exists
        t = re.sub(r'\n    @Override\s*\n    public void ' + name + r'\(' + typ + r' \w+\) \{.*?\n    \}\n', '\n', t, flags=re.S)
    for nm in rename_stale:  # API removed from the base type: keep body, drop @Override
        t = re.sub(r'@Override(\s*\n\s*public [\w<>?.@ ]+ ' + nm + r'\()', r'\1', t)
    have = set(re.findall(r'public \w+ (\w+)\(', t)) 
    add = []
    for name, typ in abstracts(*ifaces):
        simple = typ.split('.')[-1]
        if re.search(r'public void ' + name + r'\(\s*(?:[\w.]+\.)?' + simple + r' ', t): continue
        add.append(f'''    @Override
    public void {name}({typ} packet) {{
        {vanilla}.{name}(packet);
    }}
    ''')
    if add:
        t = t.rstrip(); assert t.endswith('}')
        t = t[:-1].rstrip() + '\n    \n' + '\n'.join(add) + '\n}\n'
    p.write_text(t); print(path, 'added', len(add))

patch('server/player/LittleServerPlayerHandler.java', ['ServerGamePacketListener.java', 'ServerCommonPacketListener.java'], 'getVanilla()',
      [('handlePickItem', 'ServerboundPickItemPacket'), ('handleAnimate', 'ServerboundSwingPacket'), ('handleDebugSampleSubscription', 'ServerboundDebugSampleSubscriptionPacket')],
      ['disconnect', 'send', 'getConnection', 'getMainThreadEventLoop', 'getConnectionType'])
patch('client/player/LittleClientPlayerHandler.java', ['ClientGamePacketListener.java', 'ClientCommonPacketListener.java'], 'vanilla()',
      [('handleAddExperienceOrb', 'ClientboundAddExperienceOrbPacket'), ('handleSetCarriedItem', 'ClientboundSetCarriedItemPacket'), ('handleHorseScreenOpen', 'ClientboundHorseScreenOpenPacket'),
       ('handleAddOrRemoveRecipes', 'ClientboundRecipePacket')],
      ['send', 'getConnection', 'getConnectionType'])

# PacketListener.flow() is abstract
for path, flow in [('server/player/LittleServerPlayerHandler.java', 'SERVERBOUND'), ('client/player/LittleClientPlayerHandler.java', 'CLIENTBOUND')]:
    p = R/path; t = p.read_text()
    if ' flow()' not in t:
        t = t.rstrip(); t = t[:-1].rstrip() + f'\n    \n    @Override\n    public net.minecraft.network.protocol.PacketFlow flow() {{\n        return net.minecraft.network.protocol.PacketFlow.{flow};\n    }}\n}}\n'
        p.write_text(t)

# CollisionGetter.getWorldBorder() is abstract
p = R/'common/entity/animation/LittleAnimationLevel.java'; t = p.read_text()
if 'getWorldBorder()' not in t:
    t = t.rstrip(); t = t[:-1].rstrip() + '\n    \n    @Override\n    public net.minecraft.world.level.border.WorldBorder getWorldBorder() {\n        return parentLevel.getWorldBorder();\n    }\n}\n'
    p.write_text(t)
