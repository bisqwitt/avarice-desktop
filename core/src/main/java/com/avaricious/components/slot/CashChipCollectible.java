package com.avaricious.components.slot;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.popups.CreditNumberPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.game.progression.SkillTreeProgress;
import com.avaricious.game.progression.SkillTreeUnlock;
import com.avaricious.game.run.CollectibleStreak;
import com.avaricious.game.run.RoundStats;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.app.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** A secondary, manually collected money reward dropped by a symbol collectible. */
public class CashChipCollectible implements CollectorTarget {

    private static final float SIZE = 0.72f;
    private static final float INPUT_GRACE = 0.16f;
    private static final float BASE_LIFETIME_SECONDS = 3.75f;
    private static final float DISAPPEAR_DURATION = 0.32f;
    private static final float AIR_DRAG = 0.993f;
    private static final float MIN_LAUNCH_SPEED = 5f;
    private static final float MAX_LAUNCH_SPEED = 7.8f;
    private static final float MAX_INITIAL_ROTATION_SPEED = 180f;
    private static final float EXPIRY_WARNING_SECONDS = 1f;

    private final TextureRegion texture = Assets.I().get(AssetKey.POKER_CHIP);
    private final TextureRegion shadow = Assets.I().get(AssetKey.POKER_CHIP_SHADOW);
    private final TextureRegion glow = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final TextureRegion lastCallIndicator =
        Assets.I().get(AssetKey.EXCLAMATION);
    private final TextureRegion oldestIndicator =
        Assets.I().get(AssetKey.CROSSHAIR);
    private final float reward;
    private final RoundStats roundStats;
    private final SkillTreeProgress skillTree;
    private final CollectibleStreak collectibleStreak;
    private final float maxLifetime;

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
    private boolean oldestCollectible;

    public CashChipCollectible(
        RoundStats roundStats,
        SkillTreeProgress skillTree,
        CollectibleStreak collectibleStreak,
        float reward,
        float x,
        float y
    ) {
        this.roundStats = roundStats;
        this.skillTree = skillTree;
        this.collectibleStreak = collectibleStreak;
        this.maxLifetime = skillTree.getCollectibleLifetimeSeconds(
            BASE_LIFETIME_SECONDS
        );
        this.reward = reward;
        this.x = x;
        this.y = y;

        float angle = MathUtils.random(20f, 160f) * MathUtils.degreesToRadians;
        float speed = MathUtils.random(MIN_LAUNCH_SPEED, MAX_LAUNCH_SPEED);
        velocityX = MathUtils.cos(angle) * speed;
        velocityY = MathUtils.sin(angle) * speed;
        rotation = MathUtils.random(-20f, 20f);
        rotationVelocity = MathUtils.random(
            -MAX_INITIAL_ROTATION_SPEED,
            MAX_INITIAL_ROTATION_SPEED
        );
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
        } else if (age >= maxLifetime) {
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

        collect(true);
    }

    private boolean collect(boolean manualClaim) {
        if (claimed || finished || age < INPUT_GRACE) return false;

        float lifetimeProgress = age / maxLifetime;
        claimed = true;
        disappearTime = 0f;
        roundStats.recordCollectibleClaim(age);
        boolean hotHandClaim = manualClaim && skillTree.isUnlocked(
            SkillTreeUnlock.HOT_HAND
        );
        float valueMultiplier = hotHandClaim
            ? collectibleStreak.claim(
                skillTree.getStreakGain(
                    lifetimeProgress,
                    oldestCollectible
                ),
                skillTree.getStreakValueStep(),
                skillTree.getStreakValueLimit()
            )
            : 1f;
        float streakReward = Math.max(1f, Math.round(reward * valueMultiplier));
        ScoreDisplay.I().addToScore(streakReward);
        PopupManager.I().spawnNumber(new CreditNumberPopup(
            streakReward,
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
        return true;
    }

    public void draw() {
        if (finished) return;

        float entrance = MathUtils.clamp(age / 0.14f, 0f, 1f);
        float scale = entrance * (1f + MathUtils.sin(entrance * MathUtils.PI) * 0.28f);
        scale *= 1f + hoverAmount * 0.18f;

        if (!claimed && age > maxLifetime - EXPIRY_WARNING_SECONDS) {
            float urgency = (
                age - (maxLifetime - EXPIRY_WARNING_SECONDS)
            ) / EXPIRY_WARNING_SECONDS;
            scale *= 1f + MathUtils.sin(age * 16f) * 0.05f * urgency;
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

        boolean lastCall = isLastCallWindow();
        Pencil.I().addDrawing(new TextureDrawing(
            glow,
            x - size * 0.72f,
            y - size * 0.72f,
            size * 1.44f,
            size * 1.44f,
            1f,
            rotation,
            ZIndex.SLOT_MACHINE,
            lastCall
                ? new Color(
                    1f,
                    0.22f,
                    0.06f,
                    (0.25f + MathUtils.sin(age * 20f) * 0.07f) * alpha
                )
                : new Color(
                    1f,
                    0.75f,
                    0.18f,
                    (0.10f + hoverAmount * 0.13f) * alpha
                )
        ));
        if (!claimed && oldestCollectible) {
            drawOldestIndicator(size);
        }
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

        if (!claimed && skillTree.isUnlocked(SkillTreeUnlock.LAST_CALL)) {
            drawLastCallCue(drawX, drawY, size);
        }
    }

    private void drawLastCallCue(float drawX, float drawY, float size) {
        float remaining = 1f - MathUtils.clamp(age / maxLifetime, 0f, 1f);
        float barWidth = Math.max(0.34f, size * 0.72f);
        float barHeight = 0.032f;
        float barX = drawX + (size - barWidth) * 0.5f;
        float barY = drawY + size + 0.09f;
        float thresholdX = barX + barWidth
            * (1f - SkillTreeProgress.LAST_CALL_LIFETIME_THRESHOLD);

        Pencil.I().addDrawing(new TextureDrawing(
            glow,
            barX - 0.025f,
            barY - 0.015f,
            barWidth + 0.05f,
            barHeight + 0.03f,
            ZIndex.SYMBOL_STATUS,
            new Color(0.015f, 0.022f, 0.026f, 0.78f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            glow,
            thresholdX - 0.012f,
            barY - 0.025f,
            0.024f,
            barHeight + 0.05f,
            ZIndex.SYMBOL_STATUS,
            new Color(1f, 0.72f, 0.18f, 0.92f)
        ));
        if (remaining > 0f) {
            Pencil.I().addDrawing(new TextureDrawing(
                glow,
                barX,
                barY,
                barWidth * remaining,
                barHeight,
                ZIndex.SYMBOL_STATUS,
                isLastCallWindow()
                    ? new Color(1f, 0.28f, 0.08f, 1f)
                    : new Color(0.46f, 0.82f, 1f, 0.92f)
            ));
        }

        if (!isLastCallWindow()) return;
        float pulse = 1f + MathUtils.sin(age * 14f) * 0.10f;
        float indicatorSize = 0.27f * pulse;
        Pencil.I().addDrawing(new TextureDrawing(
            lastCallIndicator,
            x - indicatorSize / 2f,
            barY + 0.12f,
            indicatorSize,
            indicatorSize,
            ZIndex.SYMBOL_STATUS,
            new Color(1f, 0.38f, 0.10f, 1f)
        ));
    }

    private boolean isLastCallWindow() {
        return !claimed && skillTree.isLastCallWindow(age / maxLifetime);
    }

    private void drawOldestIndicator(float size) {
        float pulse = 1f + MathUtils.sin(age * 6f) * 0.05f;
        float indicatorSize = size * 1.36f * pulse;
        Pencil.I().addDrawing(new TextureDrawing(
            oldestIndicator,
            x - indicatorSize / 2f,
            y - indicatorSize / 2f,
            indicatorSize,
            indicatorSize,
            1f,
            -age * 24f,
            ZIndex.SLOT_MACHINE,
            new Color(0.30f, 0.92f, 1f, 0.72f)
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
            velocityX = Math.abs(velocityX) * MathUtils.random(0.65f, 0.84f);
        } else if (x > right) {
            x = right;
            velocityX = -Math.abs(velocityX) * MathUtils.random(0.65f, 0.84f);
        }
        if (y < bottom) {
            y = bottom;
            velocityY = Math.abs(velocityY) * MathUtils.random(0.65f, 0.84f);
        } else if (y > top) {
            y = top;
            velocityY = -Math.abs(velocityY) * MathUtils.random(0.65f, 0.84f);
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

    boolean isUnclaimed() {
        return !claimed && !finished;
    }

    float getLifetimeProgress() {
        return MathUtils.clamp(age / maxLifetime, 0f, 1f);
    }

    void setOldestCollectible(boolean oldestCollectible) {
        this.oldestCollectible = oldestCollectible;
    }

    @Override
    public boolean isAvailableForCollector() {
        return !claimed && !finished && age >= INPUT_GRACE;
    }

    @Override
    public float getCollectorTargetX() {
        return x;
    }

    @Override
    public float getCollectorTargetY() {
        return y;
    }

    @Override
    public float getCollectorTargetRadius() {
        return SIZE * 0.42f;
    }

    @Override
    public boolean collectByCollector() {
        return collect(false);
    }
}
