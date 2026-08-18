package com.avaricious.components.popups;

import com.avaricious.effects.PulseEffect;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public abstract class AbstractTextPopup implements IPopup {

    private enum State {
        ENTERING,
        HOLDING,
        EXITING,
        FINISHED
    }

    private final Rectangle bounds;
    private final ZIndex z;

    private final PulseEffect pulseEffect = new PulseEffect();

    private State state = State.ENTERING;

    private float alpha = 0f;

    private float stateTimer = 0f;
    private final float entryDuration = 0.25f;
    private final float lifetime = 1.0f;
    private final float exitDuration = 0.25f;

    // Negative means start below final position
    private final float startYOffset = -1.25f;

    private final Rectangle drawBounds = new Rectangle();
    private final Color drawColor = new Color(1f, 1f, 1f, 1f);

    public AbstractTextPopup(Rectangle bounds, ZIndex z) {
        this.bounds = bounds;
        this.z = z;
        pulseEffect.pulse();
    }

    @Override
    public void update(float delta) {
        pulseEffect.update(delta);
        stateTimer += delta;

        switch (state) {
            case ENTERING:
                updateEntering();
                break;

            case HOLDING:
                updateHolding();
                break;

            case EXITING:
                updateExiting();
                break;

            case FINISHED:
                break;
        }
    }

    private void updateEntering() {
        float progress = MathUtils.clamp(stateTimer / entryDuration, 0f, 1f);
        alpha = Interpolation.fade.apply(progress);

        if (stateTimer >= entryDuration) {
            state = State.HOLDING;
            stateTimer = 0f;
            alpha = 1f;
        }
    }

    private void updateHolding() {
        alpha = 1f;

        if (stateTimer >= lifetime) {
            state = State.EXITING;
            stateTimer = 0f;
        }
    }

    private void updateExiting() {
        float progress = MathUtils.clamp(stateTimer / exitDuration, 0f, 1f);

        // Reverse of entry: alpha goes from 1 -> 0
        alpha = Interpolation.fade.apply(1f - progress);

        if (stateTimer >= exitDuration) {
            state = State.FINISHED;
            stateTimer = 0f;
            alpha = 0f;
        }
    }

    @Override
    public void draw(float delta) {
        if (state == State.FINISHED) return;

        float yOffset = 0f;

        switch (state) {
            case ENTERING: {
                float progress = MathUtils.clamp(stateTimer / entryDuration, 0f, 1f);
                yOffset = Interpolation.exp5Out.apply(startYOffset, 0f, progress);
                break;
            }

            case HOLDING: {
                yOffset = 0f;
                break;
            }

            case EXITING: {
                float progress = MathUtils.clamp(stateTimer / exitDuration, 0f, 1f);
                // Reverse of entry: move from final position back down
                yOffset = Interpolation.exp5In.apply(0f, startYOffset, progress);
                break;
            }

            case FINISHED:
                return;
        }

        drawBounds.set(
            bounds.x,
            bounds.y + yOffset,
            bounds.width,
            bounds.height
        );

        drawColor.set(1f, 1f, 1f, alpha);

        Pencil.I().addDrawing(new TextureDrawing(getShadowTexture(),
            drawBounds.x, drawBounds.y - 0.15f, drawBounds.width, drawBounds.height,
            pulseEffect.getScale(), pulseEffect.getRotation(),
            z, new Color(1f, 1f, 1f, Math.min(alpha, Assets.I().shadowColor().a))));
        Pencil.I().addDrawing(
            new TextureDrawing(
                getTexture(),
                drawBounds.x, drawBounds.y, drawBounds.width, drawBounds.height,
                pulseEffect.getScale(),
                pulseEffect.getRotation(),
                z,
                drawColor
            )
        );
    }

    protected abstract TextureRegion getTexture();

    protected abstract TextureRegion getShadowTexture();

    @Override
    public boolean isFinished() {
        return state == State.FINISHED;
    }
}
