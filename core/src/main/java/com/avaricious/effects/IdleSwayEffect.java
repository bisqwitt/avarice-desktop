package com.avaricious.effects;

import com.badlogic.gdx.math.MathUtils;

public class IdleSwayEffect extends AbstractIdleEffect {

    private final float phase = MathUtils.random(0f, MathUtils.PI2);
    private final float noiseAmount = 0.15f;
    private final float noiseSpeed = 0.7f;

    public IdleSwayEffect() {
        this(2.2f, 1f);
    }

    public IdleSwayEffect(float amplitudeDeg, float speed) {
        super(amplitudeDeg, speed);
    }

    @Override
    public float calcValue() {
        float mainWave = MathUtils.sin(time * speed + phase);
        float secondaryWave = MathUtils.sin(time * noiseSpeed + phase * 1.731f);

        return mainWave * amplitude +
            secondaryWave * amplitude * noiseAmount;
    }

    @Override
    public float defaultValue() {
        return 0;
    }
}
