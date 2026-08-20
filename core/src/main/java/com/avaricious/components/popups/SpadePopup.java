package com.avaricious.components.popups;

import com.avaricious.effects.PulseEffect;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** A claimed casino-progression spade flying into the top meter. */
public class SpadePopup implements IPopup {

    private static final float SIZE = 0.62f;
    private static final float TARGET_Y = 8.81f;
    private static final float FLIGHT_DELAY = 0.10f;
    private static final float FLIGHT_DURATION = 0.68f;
    private static final float LIFETIME = 0.85f;

    private final TextureRegion spade = Assets.I().get(AssetKey.SPADE);
    private final PulseEffect pulseEffect = new PulseEffect();
    private final Color color;
    private final float startX;
    private final float startY;
    private final Runnable onArrival;

    private float age = 0f;
    private boolean arrived = false;

    public SpadePopup(
        Color symbolColor,
        float startX,
        float startY,
        Runnable onArrival
    ) {
        this.color = new Color(symbolColor);
        this.startX = startX;
        this.startY = startY;
        this.onArrival = onArrival;

        pulseEffect.setStrength(1.25f);
        pulseEffect.setSpeed(0.065f);
        pulseEffect.pulse();
    }

    @Override
    public void update(float delta) {
        age += delta;
        pulseEffect.update(delta);

        if (!arrived && age >= FLIGHT_DELAY + FLIGHT_DURATION) {
            arrived = true;
            onArrival.run();
        }
    }

    @Override
    public void draw(float delta) {
        if (isFinished()) return;

        float flight = flightProgress();
        float directionTowardCenter = startX < 8f ? 1f : -1f;
        float arc = MathUtils.sin(MathUtils.PI * flight)
            * 0.34f
            * directionTowardCenter;
        float flutter = MathUtils.sin(age * 24f)
            * 0.045f
            * (1f - flight);

        float arrival = Math.min(1f, age / 0.18f);
        float softPop = MathUtils.sin(MathUtils.PI * arrival) * 0.09f;

        float centerX = startX + arc + flutter;
        float centerY = MathUtils.lerp(startY, TARGET_Y, flight) + softPop;

        float entrance = MathUtils.clamp(age / 0.09f, 0f, 1f);
        float destinationScale = MathUtils.lerp(1f, 0.42f, flight * flight);
        float scale = pulseEffect.getScale() * entrance * destinationScale;
        float alpha = 1f - MathUtils.clamp((age - 0.70f) / 0.15f, 0f, 1f);
        float rotation = pulseEffect.getRotation()
            + MathUtils.sin(age * 11f) * 9f * (1f - flight);

        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            centerX - SIZE / 2f,
            centerY - SIZE / 2f,
            SIZE,
            SIZE,
            scale * 1.18f,
            rotation,
            ZIndex.POPUP_DEFAULT,
            new Color(0f, 0f, 0f, alpha)
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            centerX - SIZE / 2f,
            centerY - SIZE / 2f,
            SIZE,
            SIZE,
            scale,
            rotation,
            ZIndex.POPUP_DEFAULT,
            new Color(color.r, color.g, color.b, alpha)
        ));
    }

    @Override
    public boolean isFinished() {
        return age >= LIFETIME;
    }

    private float flightProgress() {
        float value = MathUtils.clamp(
            (age - FLIGHT_DELAY) / FLIGHT_DURATION,
            0f,
            1f
        );
        return value * value * (3f - 2f * value);
    }
}
