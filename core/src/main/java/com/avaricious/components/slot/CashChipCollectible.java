package com.avaricious.components.slot;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.popups.CreditNumberPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** A secondary, manually collected money reward dropped by a symbol collectible. */
public class CashChipCollectible {

    private static final float SIZE = 0.72f;
    private static final float INPUT_GRACE = 0.16f;
    private static final float MAX_LIFETIME = 4.5f;
    private static final float DISAPPEAR_DURATION = 0.32f;
    private static final float AIR_DRAG = 0.997f;

    private final TextureRegion texture = Assets.I().get(AssetKey.POKER_CHIP);
    private final TextureRegion shadow = Assets.I().get(AssetKey.POKER_CHIP_SHADOW);
    private final TextureRegion glow = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final float reward;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float rotation;
    private float rotationVelocity;
    private float age;
    private float disappearTime;
    private float hoverAmount;
    private boolean claimed;
    private boolean finished;

    public CashChipCollectible(float reward, float x, float y) {
        this.reward = reward;
        this.x = x;
        this.y = y;

        float angle = MathUtils.random(20f, 160f) * MathUtils.degreesToRadians;
        float speed = MathUtils.random(7.5f, 11.5f);
        velocityX = MathUtils.cos(angle) * speed;
        velocityY = MathUtils.sin(angle) * speed;
        rotation = MathUtils.random(-20f, 20f);
        rotationVelocity = MathUtils.random(-300f, 300f);
    }

    public void update(float delta) {
        age += delta;

        float drag = (float) Math.pow(AIR_DRAG, delta * 60f);
        velocityX *= drag;
        velocityY *= drag;
        x += velocityX * delta;
        y += velocityY * delta;
        rotation += rotationVelocity * delta;
        rotationVelocity *= drag;
        bounceOffScreen();

        if (claimed) {
            disappearTime += delta;
            if (disappearTime >= DISAPPEAR_DURATION) finished = true;
        } else if (age >= MAX_LIFETIME) {
            claimed = true;
            disappearTime = 0f;
        }
    }

    public void handleInput(Vector2 mouse, boolean touching) {
        boolean hovered = !claimed && age >= INPUT_GRACE && hitbox().contains(mouse);
        hoverAmount = MathUtils.lerp(
            hoverAmount,
            hovered ? 1f : 0f,
            0.32f
        );
        if (!touching || !hovered) return;

        claimed = true;
        disappearTime = 0f;
        ScoreDisplay.I().addToScore(reward);
        PopupManager.I().spawnNumber(new CreditNumberPopup(
            reward,
            new Rectangle(x - 0.22f, y + 0.20f, 7 / 18f, 11 / 18f),
            false,
            false
        ));

        ParticleManager.I().create(
            x,
            y,
            ParticleType.COMP_CHIP,
            0.035f,
            95f,
            ZIndex.SYMBOL_HIT_PARTICLES
        );
        ParticleManager.I().create(
            x,
            y,
            ParticleType.WHITE,
            0.018f,
            48f,
            ZIndex.SLOT_MACHINE_FOREGROUND
        );
        AudioManager.I().playCollect(25);
        ScreenShake.I().addTrauma(0.09f);
    }

    public void draw() {
        if (finished) return;

        float entrance = MathUtils.clamp(age / 0.14f, 0f, 1f);
        float scale = entrance * (1f + MathUtils.sin(entrance * MathUtils.PI) * 0.28f);
        scale *= 1f + hoverAmount * 0.18f;

        if (!claimed && age > MAX_LIFETIME - 1.25f) {
            float urgency = (age - (MAX_LIFETIME - 1.25f)) / 1.25f;
            scale *= 1f + MathUtils.sin(age * 25f) * 0.07f * urgency;
        }
        if (claimed) {
            float progress = MathUtils.clamp(
                disappearTime / DISAPPEAR_DURATION,
                0f,
                1f
            );
            scale *= progress < 0.22f
                ? MathUtils.lerp(1f, 1.55f, progress / 0.22f)
                : MathUtils.lerp(1.55f, 0f, (progress - 0.22f) / 0.78f);
        }

        float size = SIZE * scale;
        float drawX = x - size / 2f;
        float drawY = y - size / 2f;
        float alpha = claimed
            ? 1f - MathUtils.clamp(disappearTime / DISAPPEAR_DURATION, 0f, 1f)
            : 1f;

        Pencil.I().addDrawing(new TextureDrawing(
            glow,
            x - size * 0.72f,
            y - size * 0.72f,
            size * 1.44f,
            size * 1.44f,
            1f,
            rotation,
            ZIndex.SLOT_MACHINE,
            new Color(1f, 0.75f, 0.18f, (0.10f + hoverAmount * 0.13f) * alpha)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            shadow,
            drawX,
            drawY - 0.07f,
            size,
            size,
            1f,
            rotation,
            ZIndex.SLOT_MACHINE_FOREGROUND,
            new Color(1f, 1f, 1f, Assets.I().shadowColor().a * alpha)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            texture,
            drawX,
            drawY,
            size,
            size,
            1f,
            rotation,
            ZIndex.SLOT_MACHINE_FOREGROUND,
            new Color(1f, 1f, 1f, alpha)
        ));
    }

    private void bounceOffScreen() {
        float half = SIZE / 2f;
        float left = GameContext.I().viewport.getCamera().position.x
            - GameContext.I().viewport.getWorldWidth() / 2f + half;
        float right = GameContext.I().viewport.getCamera().position.x
            + GameContext.I().viewport.getWorldWidth() / 2f - half;
        float bottom = GameContext.I().viewport.getCamera().position.y
            - GameContext.I().viewport.getWorldHeight() / 2f + half;
        float top = GameContext.I().viewport.getCamera().position.y
            + GameContext.I().viewport.getWorldHeight() / 2f - half;

        if (x < left) {
            x = left;
            velocityX = Math.abs(velocityX) * MathUtils.random(0.78f, 0.94f);
        } else if (x > right) {
            x = right;
            velocityX = -Math.abs(velocityX) * MathUtils.random(0.78f, 0.94f);
        }
        if (y < bottom) {
            y = bottom;
            velocityY = Math.abs(velocityY) * MathUtils.random(0.78f, 0.94f);
        } else if (y > top) {
            y = top;
            velocityY = -Math.abs(velocityY) * MathUtils.random(0.78f, 0.94f);
        }
    }

    private Rectangle hitbox() {
        float hitSize = SIZE * 1.55f;
        return new Rectangle(
            x - hitSize / 2f,
            y - hitSize / 2f,
            hitSize,
            hitSize
        );
    }

    public boolean isFinished() {
        return finished;
    }
}
