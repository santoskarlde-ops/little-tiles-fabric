#!/usr/bin/env python3
"""Round 17: Level constructor lost the profiler supplier; getSeaLevel is abstract in LevelReader. Idempotent."""
import pathlib
R = pathlib.Path('src/port/java')
p = R/'team/creative/littletiles/common/entity/animation/LittleAnimationLevel.java'
t = p.read_text()
t = t.replace('level.getProfilerSupplier(), level.isClientSide()', 'level.isClientSide()')
if 'getSeaLevel()' not in t:
    t = t.rstrip(); assert t.endswith('}')
    t = t[:-1].rstrip() + '''
    
    @Override
    public int getSeaLevel() {
        return parentLevel.getSeaLevel();
    }
    
}
'''
p.write_text(t)
