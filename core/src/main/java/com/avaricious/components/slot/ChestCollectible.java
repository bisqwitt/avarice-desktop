package com.avaricious.components.slot;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.popups.CreditNumberPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.AssetAnimationKey;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.EconomyScaling;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** One persistent chest from its slot-machine pop through its payout. */
final class ChestCollectible {

    enum State {
        DROPPING,
        STACKED,
        FOCUSING,
        FOCUSED,
        OPENING,
        DISMISSING,
        FINISHED
    }

    private static final float BASE_SIZE = 0.96f;
    private static final float FOCUSED_SCALE = 3.15f;
    private static final float DROP_DURATION = 0.82f;
    private static final float FOCUS_DURATION = 0.48f;
    private static final float OPEN_FRAME_DURATION = 0.085f;
    private static final float OPEN_HOLD_DURATION = 0.46f;
    private static final float DISMISS_DURATION = 0.38f;
    private static final float FAST_FORWARD_MULTIPLIER = 4f;
    private static final float BASE_REWARD = 25f;
    /** Starting values: 2 + 2 + 3 + 3 + 5 + 5 + 7. */
    private static final float STARTING_TOTAL_SYMBOL_VALUE = 27f;

    private final TextureRegion closedTexture =
        Assets.I().get(AssetKey.CHEST_CLOSED);
    private final Animation<TextureRegion> openingAnimation =
        Assets.I().getAnimation(
            AssetAnimationKey.CHEST_OPEN,
            OPEN_FRAME_DURATION,
            Animation.PlayMode.NORMAL
        );
    private final float reward = rollReward();

    private final float spawnX;
    private final float spawnY;

    private State state = State.DROPPING;
    private float stateTime;
    private float x;
    private float y;
    private float startX;
    private float startY;
    private float stackTargetX;
    private float stackTargetY;
    private float scale;
    private float rotation;
    private float alpha = 1f;
    private float hoverAmount;
    private boolean hovered;
    private boolean rewardGranted;
    private boolean fastForwarding;

    ChestCollectible(float spawnX, float spawnY) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.x = spawnX;
        this.y = spawnY;
    }

    private static float rollReward() {
        Symbol[] symbols = Symbol.values();
        double totalSymbolValue = 0d;
        for (Symbol symbol : symbols) {
            totalSymbolValue += SymbolValues.I().getValue(symbol);
        }

        double progressionScale = Math.max(
            1d,
            totalSymbolValue / STARTING_TOTAL_SYMBOL_VALUE
        );
        int rewardTier = SeededRandomizer.nextInt(1, 4);

        return EconomyScaling.roundPrice(
            BASE_REWARD * rewardTier * progressionScale
        );
    }

    void update(float delta) {
        stateTime += delta * (fastForwarding
            ? FAST_FORWARD_MULTIPLIER
            : 1f);
        hoverAmount = MathUtils.lerp(
            hoverAmount,
            hovered && state == State.STACKED ? 1f : 0f,
            Math.min(1f, delta * 13f)
        );

        switch (state) {
            case DROPPING:
                updateDrop();
                break;
            case STACKED:
                updateStack(delta);
                break;
            case FOCUSING:
                updateFocus();
                break;
            case FOCUSED:
                scale = FOCUSED_SCALE
                    * (1f + MathUtils.sin(stateTime * 2.8f) * 0.012f);
                rotation = MathUtils.sin(stateTime * 2.2f) * 0.55f;
                break;
            case OPENING:
                updateOpening();
                break;
            case DISMISSING:
                updateDismiss();
                break;
            case FINISHED:
                break;
        }
    }

    private void updateDrop() {
        float progress = MathUtils.clamp(stateTime / DROP_DURATION, 0f, 1f);
        float flight = Interpolation.smooth.apply(progress);

        x = cubicBezier(
            spawnX,
            spawnX - 0.55f,
            stackTargetX + 0.38f,
            stackTargetX,
            flight
        );
        y = cubicBezier(
            spawnY,
            spawnY + 1.30f,
            stackTargetY + 1.05f,
            stackTargetY,
            flight
        );

        float entrance = MathUtils.clamp(progress / 0.18f, 0f, 1f);
        scale = entrance
            * (1f + MathUtils.sin(entrance * MathUtils.PI) * 0.42f)
            * (1f + MathUtils.sin(progress * MathUtils.PI) * 0.08f);
        rotation = MathUtils.sin(progress * MathUtils.PI2) * 7f;

        if (progress >= 1f) {
            transitionTo(State.STACKED);
            scale = 1f;
            rotation = 0f;
            AudioManager.I().playCollect(5);
            ParticleManager.I().create(
                x,
                y,
                ParticleType.COMP_CHIP,
                0.018f,
                34f,
                ZIndex.SYMBOL_HIT_PARTICLES
            );
        }
    }

    private void updateStack(float delta) {
        float follow = Math.min(1f, delta * 12f);
        x = MathUtils.lerp(x, stackTargetX, follow);
        y = MathUtils.lerp(y, stackTargetY, follow);
        scale = 1f
            + hoverAmount * 0.15f
            + MathUtils.sin(stateTime * 3.5f) * 0.012f;
        rotation = MathUtils.lerp(rotation, hoverAmount * -2f, follow);
    }

    private void updateFocus() {
        float progress = MathUtils.clamp(stateTime / FOCUS_DURATION, 0f, 1f);
        float movement = Interpolation.pow3Out.apply(progress);

        x = MathUtils.lerp(startX, GameplayLayout.WORLD_WIDTH / 2f, movement);
        y = MathUtils.lerp(startY, 4.42f, movement)
            + MathUtils.sin(progress * MathUtils.PI) * 0.30f;

        float overshoot = MathUtils.sin(progress * MathUtils.PI) * 0.16f;
        scale = MathUtils.lerp(1f, FOCUSED_SCALE, movement) + overshoot;
        rotation = MathUtils.sin(progress * MathUtils.PI) * -4f;

        if (progress >= 1f) {
            transitionTo(State.FOCUSED);
            scale = FOCUSED_SCALE;
            rotation = 0f;
            AudioManager.I().playHover();
        }
    }

    private void updateOpening() {
        float animationDuration = openingAnimation.getAnimationDuration();
        float openingProgress = MathUtils.clamp(
            stateTime / animationDuration,
            0f,
            1f
        );

        float shake = (1f - openingProgress)
            * MathUtils.sin(stateTime * 48f);
        scale = FOCUSED_SCALE * (1f + shake * 0.018f);
        rotation = shake * 1.8f;

        if (!rewardGranted && stateTime >= animationDuration * 0.72f) {
            grantReward();
        }

        if (stateTime >= animationDuration + OPEN_HOLD_DURATION) {
            startY = y;
            transitionTo(State.DISMISSING);
        }
    }

    private void grantReward() {
        rewardGranted = true;
        ScoreDisplay.I().addToScore(reward);

        PopupManager.I().spawnNumber(new CreditNumberPopup(
            reward,
            new Rectangle(x - 0.23f, y + 1.52f, 7 / 14f, 11 / 14f),
            false,
            false
        ).setZIndex(ZIndex.CHEST_MODAL));

        ParticleManager.I().create(
            x,
            y + 0.15f,
            ParticleType.COMP_CHIP,
            0.045f,
            125f,
            ZIndex.CHEST_MODAL
        );
        ParticleManager.I().create(
            x,
            y + 0.15f,
            ParticleType.WHITE,
            0.024f,
            72f,
            ZIndex.CHEST_MODAL
        );
        ScreenShake.I().addTrauma(0.28f);
        AudioManager.I().playCollect(25);
    }

    private void updateDismiss() {
        float progress = MathUtils.clamp(stateTime / DISMISS_DURATION, 0f, 1f);
        float eased = Interpolation.pow2In.apply(progress);
        scale = MathUtils.lerp(FOCUSED_SCALE, 0f, eased);
        y = MathUtils.lerp(
            startY,
            startY + 0.55f,
            Interpolation.pow2Out.apply(progress)
        );
        rotation = MathUtils.lerp(0f, 8f, progress);
        alpha = 1f - progress;

        if (progress >= 1f) {
            transitionTo(State.FINISHED);
        }
    }

    void draw(boolean modal) {
        if (state == State.FINISHED || scale <= 0f || alpha <= 0f) return;

        TextureRegion frame = currentFrame();
        float size = BASE_SIZE * scale;
        float drawX = x - size / 2f;
        float drawY = y - size / 2f;
        ZIndex layer = modal ? ZIndex.CHEST_MODAL : ZIndex.CHEST_STACK;

        if (modal) {
            float glowPulse = 1f + MathUtils.sin(stateTime * 4f) * 0.035f;
            float glowSize = size * 1.22f * glowPulse;
            Pencil.I().addDrawing(new TextureDrawing(
                frame,
                x - glowSize / 2f,
                y - glowSize / 2f,
                glowSize,
                glowSize,
                1f,
                rotation,
                layer,
                new Color(1f, 0.74f, 0.18f, 0.17f * alpha)
            ));
        }

        Pencil.I().addDrawing(new TextureDrawing(
            frame,
            drawX + 0.07f * scale,
            drawY - 0.08f * scale,
            size,
            size,
            1f,
            rotation,
            layer,
            new Color(0f, 0f, 0f, 0.34f * alpha)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            frame,
            drawX,
            drawY,
            size,
            size,
            1f,
            rotation,
            layer,
            new Color(1f, 1f, 1f, alpha)
        ));
    }

    private TextureRegion currentFrame() {
        if (state == State.OPENING) {
            return openingAnimation.getKeyFrame(stateTime, false);
        }
        if (state == State.DISMISSING) {
            return openingAnimation.getKeyFrame(
                openingAnimation.getAnimationDuration(),
                false
            );
        }
        return closedTexture;
    }

    boolean contains(Vector2 mouse) {
        float hitScale = state == State.FOCUSED
            ? FOCUSED_SCALE
            : Math.max(1f, scale);
        float hitSize = BASE_SIZE * hitScale * 1.10f;
        return new Rectangle(
            x - hitSize / 2f,
            y - hitSize / 2f,
            hitSize,
            hitSize
        ).contains(mouse);
    }

    void beginFocus() {
        if (state != State.STACKED) return;
        startX = x;
        startY = y;
        transitionTo(State.FOCUSING);
        hovered = false;
    }

    void beginOpening() {
        if (state != State.FOCUSED) return;
        transitionTo(State.OPENING);
        AudioManager.I().playUpgradeSelected();
        ScreenShake.I().addTrauma(0.10f);
    }

    boolean fastForwardActiveAnimation() {
        if (state != State.FOCUSING && state != State.OPENING) return false;
        fastForwarding = true;
        return true;
    }

    void setStackTarget(float targetX, float targetY) {
        stackTargetX = targetX;
        stackTargetY = targetY;
    }

    void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    State getState() {
        return state;
    }

    boolean isFinished() {
        return state == State.FINISHED;
    }

    private void transitionTo(State nextState) {
        state = nextState;
        stateTime = 0f;
        fastForwarding = false;
    }

    private static float cubicBezier(
        float start,
        float control1,
        float control2,
        float end,
        float progress
    ) {
        float inverse = 1f - progress;
        return inverse * inverse * inverse * start
            + 3f * inverse * inverse * progress * control1
            + 3f * inverse * progress * progress * control2
            + progress * progress * progress * end;
    }
}
