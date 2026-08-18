package com.avaricious.effects;

public abstract class AbstractIdleEffect {

    protected float time;
    protected boolean enabled;
    protected boolean allowed;

    protected float amplitude;
    protected float speed;

    public AbstractIdleEffect(float amplitude, float speed) {
        this.amplitude = amplitude;
        this.speed = speed;

        time = 0f;
        enabled = true;
        allowed = true;
    }

    protected abstract float calcValue();

    protected abstract float defaultValue();

    public float getValue() {
        return enabled && allowed ? calcValue() : defaultValue();
    }

    public void update(float delta) {
        if (!enabled || !allowed) return;
        time += delta;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public void setStrength(float amplitude, float speed) {
        this.amplitude = amplitude;
        this.speed = speed;
    }

    public void reset() {
        time = 0f;
    }
}
