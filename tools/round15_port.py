#!/usr/bin/env python3
"""Round 15: verified-signature ports (mc-api.txt, mc-find.txt). Idempotent. Run from project root."""
import re, pathlib
R = pathlib.Path('src/port/java')
n = 0
def edit(p, fn):
    global n
    t = p.read_text(); u = fn(t)
    if u != t: p.write_text(u); n += 1; print('edited', p.name)

# 1. RenderType moved package (mc-find.txt)
for p in R.rglob('*.java'):
    edit(p, lambda t: t.replace('import net.minecraft.client.renderer.RenderType;', 'import net.minecraft.client.renderer.rendertype.RenderType;'))

# 2. Item.canAttackBlock -> Item.canDestroyBlock(ItemStack, BlockState, Level, BlockPos, LivingEntity)
pat = re.compile(r'public boolean canAttackBlock\(BlockState (\w+), Level (\w+), BlockPos (\w+), Player (\w+)\)')
for p in R.rglob('*.java'):
    edit(p, lambda t: pat.sub(r'public boolean canDestroyBlock(net.minecraft.world.item.ItemStack __stack, BlockState \1, Level \2, BlockPos \3, net.minecraft.world.entity.LivingEntity \4)', t))

# 3. Item.appendHoverText(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)
#    Keep the old List-based body as appendLegacyTooltip and bridge to it.
ah = re.compile(r'(?P<ind>[ \t]*)@Override\s*\n(?P=ind)public void appendHoverText\(ItemStack (?P<s>\w+), TooltipContext (?P<c>\w+), List<Component> (?P<l>\w+), TooltipFlag (?P<f>\w+)\) \{')
def hover(t):
    def rep(m):
        i = m['ind']
        return (f"{i}@Override\n{i}public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> out, TooltipFlag flag) {{\n"
                f"{i}    List<Component> list = new java.util.ArrayList<>();\n{i}    appendLegacyTooltip(stack, context, list, flag);\n{i}    list.forEach(out);\n{i}}}\n\n"
                f"{i}protected void appendLegacyTooltip(ItemStack {m['s']}, TooltipContext {m['c']}, List<Component> {m['l']}, TooltipFlag {m['f']}) {{")
    return ah.sub(rep, t)
for p in (R/'team/creative/littletiles/common/item').glob('*.java'):
    edit(p, hover)

# 4. LittleAnimationLevel
p = R/'team/creative/littletiles/common/entity/animation/LittleAnimationLevel.java'
def lvl(t):
    # sound / event first param Player -> Entity (Level.playSound/playSeededSound/levelEvent now take Entity)
    t = re.sub(r'(public void (?:playSound|playSeededSound)\()(?:@Nullable )?Player (\w+)', r'\1@Nullable Entity \2', t)
    t = re.sub(r'(public void levelEvent\()Player (\w+)', r'\1Entity \2', t)
    # addParticle(opts, boolean, double...) -> (opts, boolean, boolean, double...)
    t = t.replace('public void addParticle(ParticleOptions p_195590_1_, boolean p_195590_2_, double x,',
                  'public void addParticle(ParticleOptions p_195590_1_, boolean p_195590_2_, boolean __alwaysShow, double x,')
    t = t.replace('getRealLevel().addParticle(p_195590_1_, p_195590_2_, vec.x,', 'getRealLevel().addParticle(p_195590_1_, p_195590_2_, __alwaysShow, vec.x,')
    # removed from Level in 26.3 (ServerLevel-only or gone): drop overrides, see PORT_NOTES
    def drop(t, name):
        return re.sub(r'\n[ \t]*@Override\s*\n[ \t]*public [\w<>]+ ' + name + r'\([^)]*\) \{.*?\n    \}\n', '\n', t, flags=re.S)
    for nm in ['setMapData','getFreeMapId','getShade','potionBrewing','setDayTimeFraction','getDayTimeFraction','getDayTimePerTick','setDayTimePerTick']:
        t = drop(t, nm)
    t = t.replace('''    public RecipeManager getRecipeManager() {
        return getRealLevel().getRecipeManager();
    }''', '''    public net.minecraft.world.item.crafting.RecipeAccess recipeAccess() {
        return getRealLevel().recipeAccess();
    }''')
    t = t.replace('''    public int getMinBuildHeight() {
        return parentLevel.getMinBuildHeight();
    }''', '''    public int getMinY() {
        return parentLevel.getMinY();
    }''')
    if 'clockManager()' not in t:
        add = '''
    @Override
    public net.minecraft.world.clock.ClockManager clockManager() {
        return parentLevel.clockManager();
    }
    
    @Override
    public net.minecraft.world.attribute.EnvironmentAttributeSystem environmentAttributes() {
        return parentLevel.environmentAttributes();
    }
    
    @Override
    public void setRespawnData(net.minecraft.world.level.storage.LevelData.RespawnData data) {
        parentLevel.setRespawnData(data);
    }
    
    @Override
    public net.minecraft.world.level.storage.LevelData.RespawnData getRespawnData() {
        return parentLevel.getRespawnData();
    }
    
}'''
        t = t.rstrip()
        assert t.endswith('}')
        t = t[:-1].rstrip() + '\n' + add + '\n'
    return t
edit(p, lvl)

# 5. LittlePacketTypes: remaining bare buffer
p = R/'team/creative/littletiles/common/packet/LittlePacketTypes.java'
edit(p, lambda t: re.sub(r'(PlacementPosition\.class,\s*)buffer, null\)\)', r'\1LittleNetBuf.of(buffer), null))', t))
print('files edited', n)
