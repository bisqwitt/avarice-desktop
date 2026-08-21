package com.avaricious.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

public class PulseEffect {

    private static final float[] SCALE_VALUES = {
        1.0f,
        1.175f * 1.3f,
        0.825f * 1.25f,
        1.075f * 1.2f,
        0.95f * 1.15f,
        1f * 1.1f,
        1.0f
    };

    private static final float[] ROTATION_VALUES = {
        0f,
        7f,
        -7f,
        4f,
        -2f,
        1f,
        0f
    };

    private float segmentDuration = 0.1f;
    private final Interpolation interpolation = Interpolation.sine;

    private int currentSegment;
    private float timer;

    private boolean active;
    private boolean finished;

    private float currentScale = 1.0f;
    private float currentRotation = 0f;

    private float rotationDirection = 1f;
    private float strength = 1f;

    public void update(float delta) {
        if (!active || finished) {
            return;
        }

        timer += delta;

        while (timer >= segmentDuration && !finished) {
            timer -= segmentDuration;
            currentSegment++;

            if (currentSegment >= SCALE_VALUES.length - 1) {
                currentSegment = SCALE_VALUES.length - 2;
                currentScale = 1.0f;
                currentRotation = 0f;
                finished = true;
                active = false;
                return;
            }
        }

        float progress = timer / segmentDuration;

        float scaleFrom = applyScaleStrength(SCALE_VALUES[currentSegment]);
        float scaleTo = applyScaleStrength(SCALE_VALUES[currentSegment + 1]);

        currentScale = interpolation.apply(scaleFrom, scaleTo, progress);

        float rotationFrom =
            ROTATION_VALUES[currentSegment] * rotationDirection * strength;
        float rotationTo =
            ROTATION_VALUES[currentSegment + 1] * rotationDirection * strength;

        currentRotation = interpolation.apply(rotationFrom, rotationTo, progress);
    }

    private float applyScaleStrength(float scale) {
        return 1f + (scale - 1f) * strength;
    }

    public void pulse() {
        currentSegment = 0;
        timer = 0f;
        active = true;
        finished = false;
        currentScale = 1.0f;
        currentRotation = 0f;

        rotationDirection = MathUtils.randomBoolean() ? 1f : -1f;
    }

    public void pulse(float strength) {
        setStrength(strength);
        pulse();
    }

    public void reset() {
        currentSegment = 0;
        timer = 0f;
        active = false;
        finished = true;
        currentScale = 1f;
        currentRotation = 0f;
        strength = 1f;
    }

    public float getScale() {
        return currentScale;
    }

    public float getRotation() {
        return currentRotation;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public void setSpeed(float segmentDuration) {
        this.segmentDuration = segmentDuration;
    }
}
