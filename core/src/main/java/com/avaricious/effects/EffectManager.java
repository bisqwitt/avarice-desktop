package com.avaricious.effects;

import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class EffectManager {

    public static float streak = 0f;

    public static void create(TextureRegion texture, Rectangle bounds, TextureEcho.Type parentType) {
        ParticleManager.I().create(bounds.x, bounds.y, ParticleType.RAINBOW, 0.03f, 50, ZIndex.SYMBOL_HIT_PARTICLES);
//        TextureEcho.create(texture, bounds, new Color(1f, 1f, 1f, 1f), streak);
        TextureEcho.create(texture, bounds, parentType, streak);
//        BorderPulseMesh.I().triggerOnce(BorderPulseMesh.Type.RAINBOW);
    }

    public static void increaseStreak() {
        streak++;
    }

    public static void endStreak() {
        streak = 0f;
    }

}
