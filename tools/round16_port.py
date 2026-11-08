#!/usr/bin/env python3
"""Round 16: ports verified against mc-find2.txt / mc-api2.txt / CreativeNetwork (fabric). Idempotent."""
import re, pathlib
R = pathlib.Path('src/port/java'); n = 0
def edit(p, fn):
    global n
    t = p.read_text(); u = fn(t)
    if u != t: p.write_text(u); n += 1

IMPORTS = {
 'import net.minecraft.world.entity.animal.WaterAnimal;': 'import net.minecraft.world.entity.animal.fish.WaterAnimal;',
 'import net.minecraft.client.renderer.block.model.BakedQuad;': 'import net.minecraft.client.resources.model.geometry.BakedQuad;',
 'import com.mojang.blaze3d.vertex.VertexFormat;': 'import com.mojang.renderpearl.api.vertex.VertexFormat;',
}
def disp(t):
    out = []; i = 0; key = '.displayClientMessage('
    while True:
        j = t.find(key, i)
        if j < 0: out.append(t[i:]); break
        k = j + len(key); depth = 1; q = False; m = k; split = -1
        while m < len(t) and depth:
            c = t[m]
            if q:
                if c == '\\': m += 1
                elif c == '"': q = False
            elif c == '"': q = True
            elif c in '([{': depth += 1
            elif c in ')]}': depth -= 1
            elif c == ',' and depth == 1: split = m
            m += 1
        arg2 = t[split+1:m-1].strip() if split > 0 else ''
        if arg2 in ('true', 'false'):
            out.append(t[i:j] + ('.sendOverlayMessage(' if arg2 == 'true' else '.sendSystemMessage(') + t[k:split] + ')')
        else:
            out.append(t[i:m])
        i = m
    return ''.join(out)

for p in R.rglob('*.java'):
    def f(t):
        for a, b in IMPORTS.items(): t = t.replace(a, b)
        t = t.replace('.registryOrThrow(', '.lookupOrThrow(')
        t = re.sub(r'\.absMoveTo\(', '.snapTo(', t)
        t = disp(t)
        return t
    edit(p, f)

# CreativeNetwork (Fabric) renamed getType -> getPacketType
edit(R/'team/creative/littletiles/common/packet/LittlePacketTypes.java',
     lambda t: t.replace('LittleTiles.NETWORK.getType(', 'LittleTiles.NETWORK.getPacketType('))

# LittleAnimationLevel: missing abstract Level methods
p = R/'team/creative/littletiles/common/entity/animation/LittleAnimationLevel.java'
def lvl(t):
    if 'dragonParts()' in t: return t
    add = '''
    @Override
    public java.util.Collection<net.minecraft.world.entity.boss.enderdragon.EnderDragonPart> dragonParts() {
        return java.util.List.of();
    }
    
    @Override
    public void explode(Entity source, net.minecraft.world.damagesource.DamageSource damageSource, net.minecraft.world.level.ExplosionDamageCalculator calculator, double x, double y, double z,
            float radius, boolean fire, Level.ExplosionInteraction interaction, net.minecraft.core.particles.ParticleOptions small, net.minecraft.core.particles.ParticleOptions large,
            net.minecraft.util.random.WeightedList<net.minecraft.core.particles.ExplosionParticleInfo> particles, Holder<SoundEvent> sound) {
        if (getOrigin() == null)
            return;
        Vec3 vec = getOrigin().pose().transform(new Vec3(x, y, z));
        getRealLevel().explode(source, damageSource, calculator, vec.x, vec.y, vec.z, radius, fire, interaction, small, large, particles, sound);
    }
    
}'''
    t = t.rstrip(); assert t.endswith('}')
    return t[:-1].rstrip() + '\n' + add + '\n'
edit(p, lvl)
print('files edited', n)
