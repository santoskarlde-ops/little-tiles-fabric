package team.creative.littletiles.common.entity.particle;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import team.creative.creativecore.common.util.mc.LanguageUtils;

public enum LittleParticleTexture {
    
    dust_fade_out(Identifier.withDefaultNamespace("smoke"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    dust(Identifier.withDefaultNamespace("smoke"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dust_grow(Identifier.withDefaultNamespace("smoke"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAgeReverse(particle.sprites);
        }
        
    },
    bubble(Identifier.withDefaultNamespace("bubble"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    diamond(Identifier.withDefaultNamespace("totem_of_undying"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark(Identifier.withDefaultNamespace("firework"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.setSpriteFirst(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark_fade_out(Identifier.withDefaultNamespace("firework"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    spiral(Identifier.withDefaultNamespace("entity_effect"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    note(Identifier.withDefaultNamespace("note"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    flame(Identifier.withDefaultNamespace("flame"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    happy(Identifier.withDefaultNamespace("happy_villager"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    angry(Identifier.withDefaultNamespace("angry_villager"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    damage(Identifier.withDefaultNamespace("damage_indicator"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    letter(Identifier.withDefaultNamespace("enchant"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dripping(Identifier.withDefaultNamespace("dripping_water"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    falling(Identifier.withDefaultNamespace("falling_water"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    heart(Identifier.withDefaultNamespace("heart"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    lava(Identifier.withDefaultNamespace("lava"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    soul(Identifier.withDefaultNamespace("soul"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    soul_flame(Identifier.withDefaultNamespace("soul_fire_flame"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    big_smoke(Identifier.withDefaultNamespace("campfire_cosy_smoke"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    nautilus(Identifier.withDefaultNamespace("nautilus"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    };
    
    public static LittleParticleTexture get(String name) {
        for (int i = 0; i < values().length; i++)
            if (values()[i].name().equals(name))
                return values()[i];
        return dust_fade_out;
    }
    
    public final Identifier particleTexture;
    public final LittleParticleRenderType type;
    
    private LittleParticleTexture(Identifier particleTexture, LittleParticleRenderType type) {
        this.particleTexture = particleTexture;
        this.type = type;
    }
    
    public abstract void init(LittleParticle particle);
    
    public abstract void tick(LittleParticle particle);
    
    public Component title() {
        return Component.literal(LanguageUtils.translateOr("particle.texture." + name(), name()));
    }
    
}
