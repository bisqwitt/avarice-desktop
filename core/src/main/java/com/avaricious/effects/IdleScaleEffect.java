package com.avaricious.effects;

import com.badlogic.gdx.math.MathUtils;

public class IdleScaleEffect extends AbstractIdleEffect {

    private final float phase = MathUtils.random(0f, MathUtils.PI2);

    public IdleScaleEffect() {
        this(0.07f, 0.5f);
    }

    public IdleScaleEffect(float amplitude, float speed) {
        super(amplitude, speed);
    }

    @Override
    public float calcValue() {
        float wave = MathUtils.sin(time * speed + phase);
        return 1f + wave * amplitude;
    }

    @Override
    public float defaultValue() {
        return 1;
    }
}
