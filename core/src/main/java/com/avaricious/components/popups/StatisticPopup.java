package com.avaricious.components.popups;

import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StatisticPopup implements IPopup {

    private final TextureRegion texture;

    private final float x;
    private final float y;
    private final float lifetime = 1f;
    private float timeAlive = 0f;

    public StatisticPopup(TextureRegion texture, float x, float y) {
        this.texture = texture;
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(float delta) {
        timeAlive += delta;
        if (timeAlive > lifetime) {
            timeAlive = lifetime;
        }
    }

    @Override
    public void draw(float delta) {
        float pulseCurve = getPulseCurve();

        // Scale
        float baseScale = 1.0f;
        float pulseScale = 0.35f;   // intensity of the pop, tweak to taste
        float scale = baseScale + pulseCurve * pulseScale;

        // Wobble
        float wobbleAngle = 8f;     // degrees, tweak to taste
        float rotation = pulseCurve * wobbleAngle;

        // Draw centered
        float width = 7 / 20f;
        float height = 11 / 20f;
        float originX = width / 2f;
        float originY = height / 2f;

        Pencil.I().addDrawing(new TextureDrawing(
            texture,
            x - originX - 0.5f, y - originY, width, height,
            scale, rotation, ZIndex.POPUP_DEFAULT
        ));
    }

    private float getPulseCurve() {
        float progress = timeAlive / lifetime;  // 0..1

        // Fade out over full lifetime
        float initialAlpha = 1f;
        float alpha = initialAlpha * (1f - progress / 2);

        // ---- SINGLE FAST PULSE + WOBBLE ----
        float pulseDuration = 0.2f; // fraction of lifetime used for the pulse
        float t = progress / pulseDuration;
        if (t > 1f) t = 1f;         // clamp after pulse, keep at end value

        // Parabola: 0 -> 1 -> 0 once
        float pulseCurve = 1f - 4f * (t - 0.5f) * (t - 0.5f);
        if (pulseCurve < 0f) pulseCurve = 0f; // numerical safety
        return pulseCurve;
    }

    @Override
    public boolean isFinished() {
        return timeAlive >= lifetime;
    }

}
