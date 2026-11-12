package team.creative.littletiles.common.structure.animation;

import net.minecraft.nbt.CompoundTag;

public class AnimationTransition {
    
    public final String name;
    public final int start;
    public final int end;
    public final AnimationTimeline timeline;
    
    public AnimationTransition(String name, int start, int end, AnimationTimeline timeline) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.timeline = timeline;
    }
    
    public AnimationTransition(CompoundTag nbt) {
        this.name = nbt.getString("n").orElse("");
        this.start = nbt.getInt("f").orElse(0);
        this.end = nbt.getInt("t").orElse(0);
        this.timeline = new AnimationTimeline(nbt.getCompound("a").orElse(new CompoundTag()));
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("n", name);
        nbt.putInt("f", start);
        nbt.putInt("t", end);
        nbt.put("a", timeline.save());
        return nbt;
    }
    
}