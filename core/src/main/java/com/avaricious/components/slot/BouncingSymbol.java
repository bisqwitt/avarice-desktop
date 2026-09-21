package com.avaricious.components.slot;

import com.avaricious.audio.AudioManager;
import com.avaricious.RoundStats;
import com.avaricious.components.CompChipBar;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.popups.LostSymbolPopup;
import com.avaricious.components.popups.NumberPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.SpadePopup;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.effects.PulseEffect;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.CollectibleValues;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.EconomyScaling;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class BouncingSymbol implements CollectorTarget {

    private static final int COMP_CHIP_REWARD = 1;
    private static final float COLLECTOR_GRACE_PERIOD = 0.12f;
    private static final float MAX_HOVER_HEALTH = 1f;
    private static final float HOVER_CLAIM_DURATION = 1f;
    private static final float MIN_HOVER_DAMAGE_MULTIPLIER = 0.50f;
    private static final float MAX_HOVER_DAMAGE_MULTIPLIER = 1.65f;
    private static final float DRAIN_MOVEMENT_SCALE = 0.62f;
    private static final float DRAIN_SHAKE_X = 0.030f;
    private static final float DRAIN_SHAKE_Y = 0.022f;
    private static final float CLICK_CLAIM_DURATION = 0.68f;
    private static final float CLICK_CONFIRM_DURATION = 0.26f;
    private static final float CLICK_BAR_DRAIN_DURATION = 0.14f;

    private final Symbol symbol;
    private final TextureRegion texture;
    private final TextureRegion whiteTexture;
    private final TextureRegion shadowTexture;
    private final TextureRegion healthBarTexture;

    private final PulseEffect pulseEffect = new PulseEffect();
    private final float drainVisualPhase;

    private float x;
    private float y;

    private float velocityX;
    private float velocityY;

    private float rotation;
    private float rotationVelocity;

    private float scale = 1f;
    private float spawnAge = 0f;
    private float impactFlash = 0f;
    private float hoverAmount = 0f;
    private float hoverHealth = MAX_HOVER_HEALTH;
    private float clickClaimStartHealth = MAX_HOVER_HEALTH;
    private float collisionCooldown = 0f;
    private boolean hovered = false;
    private boolean clickClaimed = false;

    /* Tracks active, non-draining time before this symbol expires. */
    private float lifetime = 0f;

    /*
     * Only used for the disappearance animation
     * after the symbol has been clicked.
     */
    private float disappearTime = 0f;

    private boolean claimed = false;
    private boolean finished = false;

    /*
     * Physics
     */
    private static final float AIR_DRAG = 0.998f;
    private static final float ROTATION_DRAG = 0.997f;

    private static final float MIN_BOUNCE = 0.75f;
    private static final float MAX_BOUNCE = 0.97f;

    /*
     * Maximum time the player has to claim the symbol.
     */
    private static final float MAX_LIFETIME = 6f;

    /*
     * How long the symbol takes to shrink away
     * after being clicked.
     */
    private static final float DISAPPEAR_DURATION = 0.5f;

    /*
     * Organic movement
     */
    private final float wobbleSpeed;
    private final float wobbleStrength;

    public BouncingSymbol(Symbol symbol, float x, float y) {
        this(symbol, x, y, 1f);
    }

    public BouncingSymbol(Symbol symbol, float x, float y, float launchPower) {
        this.symbol = symbol;
        this.texture = Assets.I().getSymbol(symbol);
        this.whiteTexture = Assets.I().get(symbol.whiteKey());
        this.shadowTexture = Assets.I().get(symbol.shadowKey());
        this.healthBarTexture = Assets.I().get(AssetKey.WHITE_PIXEL);

        this.x = x;
        this.y = y;

        /*
         * Launch in a random direction.
        */
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float speed = MathUtils.random(6f, 12f) * launchPower;

        velocityX = MathUtils.cos(angle) * speed;
        velocityY = MathUtils.sin(angle) * speed;

        /*
         * Initial rotation.
         */
        rotation = MathUtils.random(-10f, 10f);
        rotationVelocity = MathUtils.random(-180f, 180f);

        /*
         * Slightly different wobble for every symbol.
         */
        wobbleSpeed = MathUtils.random(7f, 12f);
        wobbleStrength = MathUtils.random(4f, 10f);
        drainVisualPhase = MathUtils.random(0f, MathUtils.PI2);

        /*
         * Bouncing symbol pulse settings.
         */
        pulseEffect.setStrength(0.8f);
        pulseEffect.setSpeed(0.125f);

        scale = 0.2f;
    }

    public void update(float delta) {

        boolean draining = hovered && !claimed;
        spawnAge += delta;
        collisionCooldown = Math.max(0f, collisionCooldown - delta);
        impactFlash = Math.max(0f, impactFlash - delta * 5.5f);
        hoverAmount = MathUtils.lerp(
            hoverAmount,
            draining ? 1f : 0f,
            Math.min(1f, delta * 12f)
        );
        hovered = false;

        /*
         * The symbol continues moving while it is available.
         */
        updatePhysics(delta, draining);

        pulseEffect.update(delta);

        handleHorizontalCollisions();
        handleVerticalCollisions();

        /*
         * If the symbol has not been claimed yet,
         * count down its available lifetime.
         */
        if (!claimed) {
            if (!draining) {
                lifetime += delta;
            }

            if (lifetime >= MAX_LIFETIME) {
                miss();
            }

            updateScale();
            return;
        }

        /*
         * Once claimed, start the disappearance timer.
         */
        disappearTime += delta;

        updateScale();

        if (disappearTime >= getDisappearDuration()) {
            finished = true;
        }
    }

    private void updatePhysics(float delta, boolean draining) {
//        if (claimed) {
//            return;
//        }

        float movementDrag =
            (float) Math.pow(
                AIR_DRAG,
                delta * 60f
            );

        velocityX *= movementDrag;
        velocityY *= movementDrag;

        rotationVelocity *=
            (float) Math.pow(
                ROTATION_DRAG,
                delta * 60f
            );

        /*
         * Movement
         */
        float movementScale = draining ? DRAIN_MOVEMENT_SCALE : 1f;
        x += velocityX * delta * movementScale;
        y += velocityY * delta * movementScale;

        /*
         * Rotation
         */
        rotation += rotationVelocity * delta * movementScale;

        /*
         * Small organic wobble.
         */
        rotation += MathUtils.sin(lifetime * wobbleSpeed)
            * wobbleStrength
            * delta;
    }

    boolean updateHoverHealth(float hoverStrength, float delta) {
        this.hovered = hoverStrength >= 0f && !claimed;
        if (claimed) {
            return false;
        }

        if (this.hovered) {
            float centeredStrength = MathUtils.clamp(hoverStrength, 0f, 1f);
            centeredStrength = centeredStrength * centeredStrength
                * (3f - 2f * centeredStrength);
            float damageMultiplier = MathUtils.lerp(
                MIN_HOVER_DAMAGE_MULTIPLIER,
                MAX_HOVER_DAMAGE_MULTIPLIER,
                centeredStrength
            );
            hoverHealth = Math.max(
                0f,
                hoverHealth
                    - delta
                    * damageMultiplier
                    / HOVER_CLAIM_DURATION
            );
            if (hoverHealth <= 0f) return collect();
        }

        return false;
    }

    boolean collectImmediately() {
        if (claimed || finished) return false;

        clickClaimed = true;
        clickClaimStartHealth = hoverHealth;
        hoverHealth = 0f;
        return collect();
    }

    private boolean collect() {
        if (claimed || finished) return false;

        pulseEffect.pulse(clickClaimed ? 2.05f : 1.65f);

        disappearTime = 0f;
        claimed = true;
        RoundStats.I().recordSymbolCollected(spawnAge);

        float cashReward = SymbolValues.I().getValue(symbol);
        ScoreDisplay.I().addToScore(cashReward);
        PopupManager.I().spawnNumber(new NumberPopup(
            cashReward,
            Assets.I().getSymbolColor(symbol),
            new Rectangle(
                getCenterX() - 0.22f,
                getCenterY() + 0.20f,
                7 / 18f,
                11 / 18f
            ),
            false,
            false
        ));

        ParticleManager.I().create(
            x,
            y,
            ParticleType.COMP_CHIP,
            0.035f,
            90f,
            ZIndex.SYMBOL_HIT_PARTICLES
        );

        ParticleManager.I().create(
            x,
            y,
            ParticleType.WHITE,
            0.018f,
            42f,
            ZIndex.SLOT_MACHINE_FOREGROUND
        );

        spawnSpade(0.75f);

        int extraSpadeChance =
            CollectibleValues.I().getExtraSpadeSpawnChance();

        if (
            extraSpadeChance >= 100 ||
                extraSpadeChance > 0 &&
                    SeededRandomizer.get().nextFloat() * 100f < extraSpadeChance
        ) {
            spawnSpade(0.45f);
        }

        trySpawnCashChip();

        AudioManager.I().playCollect(COMP_CHIP_REWARD);
        impactFlash = 1f;

        return true;
    }

    private void trySpawnCashChip() {
        int chance = CollectibleValues.I().getCashChipSpawnChance();
        if (chance <= 0 ||
            chance < 100 && SeededRandomizer.get().nextFloat() * 100f >= chance) {
            return;
        }

        float reward = Math.max(
            25f,
            EconomyScaling.roundPrice(SymbolValues.I().getValue(symbol) * 5f)
        );
        BouncingSymbolManager.I().createCashChip(
            reward,
            getCenterX(),
            getCenterY()
        );
    }

    private void spawnSpade(float xOffset) {
        PopupManager.I().spawnSpade(new SpadePopup(
            Assets.I().getSymbolColor(symbol),
            getCenterX() + xOffset,
            getCenterY() + 0.5f,
            () -> CompChipBar.I().addChips(
                COMP_CHIP_REWARD *
                    Automations.I().getXpMultiplier().getMultiplier()
            )
        ));
    }

    private void miss() {
        PopupManager.I().spawnLostSymbol(new LostSymbolPopup(
            symbol,
            getCenterX(),
            getCenterY(),
            getWidth(),
            getHeight(),
            rotation + pulseEffect.getRotation()
        ));

        AudioManager.I().playMiss();
        finished = true;
    }

    private void handleHorizontalCollisions() {
        float width = getWidth();

        float screenLeft =
            GameContext.I().viewport.getCamera().position.x
                - GameContext.I().viewport.getWorldWidth() / 2f;

        float screenRight =
            GameContext.I().viewport.getCamera().position.x
                + GameContext.I().viewport.getWorldWidth() / 2f;

        float centerX =
            x + SlotMachine.CELL_W / 2f;

        float drawX =
            centerX - width / 2f;

        float left = drawX;
        float right = drawX + width;

        /*
         * LEFT WALL
         */
        if (left < screenLeft) {
            float impactSpeed = Math.abs(velocityX);
            float overlap = screenLeft - left;

            x += overlap;

            velocityX =
                Math.abs(velocityX) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);

            triggerImpact(impactSpeed);
        }

        /*
         * RIGHT WALL
         */
        if (right > screenRight) {
            float impactSpeed = Math.abs(velocityX);
            float overlap = right - screenRight;

            x -= overlap;

            velocityX =
                -Math.abs(velocityX) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);

            triggerImpact(impactSpeed);
        }
    }

    private void handleVerticalCollisions() {
        float height = getHeight();

        float screenBottom =
            GameContext.I().viewport.getCamera().position.y
                - GameContext.I().viewport.getWorldHeight() / 2f;

        float screenTop =
            GameContext.I().viewport.getCamera().position.y
                + GameContext.I().viewport.getWorldHeight() / 2f;

        float centerY =
            y + SlotMachine.CELL_H / 2f;

        float drawY =
            centerY - height / 2f;

        float bottom = drawY;
        float top = drawY + height;

        /*
         * BOTTOM WALL
         */
        if (bottom < screenBottom) {
            float impactSpeed = Math.abs(velocityY);
            float overlap = screenBottom - bottom;

            y += overlap;

            velocityY =
                Math.abs(velocityY) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);

            triggerImpact(impactSpeed);
        }

        /*
         * TOP WALL
         */
        if (top > screenTop) {
            float impactSpeed = Math.abs(velocityY);
            float overlap = top - screenTop;

            y -= overlap;

            velocityY =
                -Math.abs(velocityY) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);

            triggerImpact(impactSpeed);
        }
    }

    /*
     * Only called after the symbol has been claimed.
     */
    private void updateScale() {
        if (!claimed) {
            float entrance = MathUtils.clamp(spawnAge / 0.20f, 0f, 1f);
            float entranceOvershoot =
                1f + MathUtils.sin(entrance * MathUtils.PI) * 0.24f;
            float urgency = getUrgency();
            float urgencyPulse = MathUtils.sin(spawnAge * 22f) * 0.06f * urgency;

            scale = entrance * entranceOvershoot
                * (1f + hoverAmount * 0.16f + impactFlash * 0.15f + urgencyPulse);
            return;
        }

        if (clickClaimed && disappearTime < CLICK_CONFIRM_DURATION) {
            float confirmProgress = MathUtils.clamp(
                disappearTime / CLICK_CONFIRM_DURATION,
                0f,
                1f
            );
            float punch = MathUtils.sin(confirmProgress * MathUtils.PI) * 0.28f;
            scale = 1f + punch;
            return;
        }

        float collapseStart = clickClaimed ? CLICK_CONFIRM_DURATION : 0f;
        float collapseDuration = getDisappearDuration() - collapseStart;
        float progress = MathUtils.clamp(
            (disappearTime - collapseStart) / collapseDuration,
            0f,
            1f
        );

        if (!clickClaimed && progress < 0.18f) {
            scale = MathUtils.lerp(1f, 1.55f, progress / 0.18f);
        } else {
            float collapse = clickClaimed
                ? progress
                : (progress - 0.18f) / 0.82f;
            scale = MathUtils.lerp(
                clickClaimed ? 1f : 1.55f,
                0f,
                collapse * collapse
            );
        }
    }

    private float getDisappearDuration() {
        return clickClaimed ? CLICK_CLAIM_DURATION : DISAPPEAR_DURATION;
    }

    public void triggerImpact(float force) {
        if (claimed || collisionCooldown > 0f || force < 2.2f) return;

        collisionCooldown = 0.085f;
        impactFlash = Math.min(1f, 0.35f + force / 14f);
        pulseEffect.pulse(MathUtils.clamp(force / 16f, 0.28f, 0.62f));

        if (force > 5f) {
            ParticleManager.I().create(
                x,
                y,
                ParticleType.WHITE,
                0.008f,
                MathUtils.clamp(force * 1.5f, 8f, 22f),
                ZIndex.SLOT_MACHINE_FOREGROUND
            );
        }
    }

    private float randomBounce() {
        return MathUtils.random(
            MIN_BOUNCE,
            MAX_BOUNCE
        );
    }

    private float getWidth() {
        return SlotMachine.CELL_W
            * 0.75f
            * scale
            * pulseEffect.getScale();
    }

    private float getHeight() {
        return SlotMachine.CELL_H
            * 0.75f
            * scale
            * pulseEffect.getScale();
    }

    public void draw() {
        float width = getWidth();
        float height = getHeight();

        float drainShakeAmount = hoverAmount * hoverAmount;
        float visualOffsetX = MathUtils.sin(
            spawnAge * 72f + drainVisualPhase
        ) * DRAIN_SHAKE_X * drainShakeAmount;
        float visualOffsetY = MathUtils.sin(
            spawnAge * 91f + drainVisualPhase * 1.61f
        ) * DRAIN_SHAKE_Y * drainShakeAmount;

        float centerX =
            x + SlotMachine.CELL_W / 2f + visualOffsetX;

        float centerY =
            y + SlotMachine.CELL_H / 2f + visualOffsetY;

        float drawX =
            centerX - width / 2f;

        float drawY =
            centerY - height / 2f;

        /*
         * White glow.
         */
        float glowScale = 3f;

        float glowWidth =
            width * glowScale;

        float glowHeight =
            height * glowScale;

        float glowX =
            centerX - glowWidth / 2f;

        float glowY =
            centerY - glowHeight / 2f;

        float drainRotation = MathUtils.sin(
            spawnAge * 67f + drainVisualPhase * 0.73f
        ) * 1.8f * drainShakeAmount;
        float finalRotation =
            rotation + pulseEffect.getRotation() + drainRotation;

        float renderedMovementScale = MathUtils.lerp(
            1f,
            DRAIN_MOVEMENT_SCALE,
            hoverAmount
        );
        float speed = (float) Math.sqrt(
            velocityX * velocityX + velocityY * velocityY
        ) * renderedMovementScale;
        float urgency = getUrgency();

        /* Motion echoes make fast launches legible without extra textures. */
        float trailAlpha = MathUtils.clamp(speed / 14f, 0f, 1f) * 0.18f;
        for (int i = 2; i >= 1; i--) {
            float trailOffset = i * 0.028f;
            Pencil.I().addDrawing(
                new TextureDrawing(
                    whiteTexture,
                    drawX - velocityX * trailOffset * renderedMovementScale,
                    drawY - velocityY * trailOffset * renderedMovementScale,
                    width,
                    height,
                    1f - i * 0.08f,
                    finalRotation
                        - rotationVelocity * trailOffset * renderedMovementScale,
                    ZIndex.SLOT_MACHINE,
                    new Color(1f, 1f, 1f, trailAlpha / i)
                )
            );
        }

        Pencil.I().addDrawing(
            new TextureDrawing(
                shadowTexture,
                drawX,
                drawY - 0.08f,
                width,
                height,
                1f,
                finalRotation,
                ZIndex.SLOT_MACHINE,
                Assets.I().shadowColor()
            )
        );

        /*
         * White silhouette / glow.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                whiteTexture,
                glowX,
                glowY,
                glowWidth,
                glowHeight,
                0.35f,
                finalRotation,
                ZIndex.SLOT_MACHINE,
                new Color(
                    1f,
                    0.92f + 0.08f * urgency,
                    0.72f + 0.28f * (1f - urgency),
                    0.10f + hoverAmount * 0.18f + urgency * 0.16f
                )
            )
        );

        /*
         * Actual symbol.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                texture,
                drawX,
                drawY,
                width,
                height,
                1f,
                finalRotation,
                ZIndex.SLOT_MACHINE
            )
        );

        boolean showingClickConfirmation = clickClaimed
            && disappearTime < CLICK_CONFIRM_DURATION;
        if (!claimed) {
            drawLifetimeBar(drawX, drawY, width, height);
            drawHealthBar(
                drawX,
                drawY,
                width,
                height,
                hoverHealth / MAX_HOVER_HEALTH
            );
        } else if (showingClickConfirmation) {
            float drainProgress = MathUtils.clamp(
                disappearTime / CLICK_BAR_DRAIN_DURATION,
                0f,
                1f
            );
            drawHealthBar(
                drawX,
                drawY,
                width,
                height,
                clickClaimStartHealth
                    / MAX_HOVER_HEALTH
                    * (1f - drainProgress)
            );
        }

        float drainFlicker = hoverAmount * (
            0.16f +
                (0.5f + 0.5f * MathUtils.sin(
                    spawnAge * 27f + drainVisualPhase
                )) * 0.26f
        );
        if (impactFlash > 0f || claimed || drainFlicker > 0.01f) {
            float claimFlashDuration = clickClaimed
                ? CLICK_CONFIRM_DURATION
                : 0.16f;
            float claimFlash = claimed
                ? Math.max(0f, 1f - disappearTime / claimFlashDuration)
                : 0f;
            float flash = Math.max(
                drainFlicker,
                Math.max(impactFlash, claimFlash)
            );

            Pencil.I().addDrawing(
                new TextureDrawing(
                    whiteTexture,
                    drawX,
                    drawY,
                    width,
                    height,
                    1f + flash * (clickClaimed ? 0.32f : 0.22f),
                    finalRotation,
                    ZIndex.SLOT_MACHINE_FOREGROUND,
                    new Color(
                        1f,
                        1f,
                        1f,
                        flash * (clickClaimed ? 1f : 0.82f)
                    )
                )
            );
        }
    }

    private void drawHealthBar(
        float drawX,
        float drawY,
        float width,
        float height,
        float healthRatio
    ) {
        healthRatio = MathUtils.clamp(healthRatio, 0f, 1f);
        float barWidth = Math.max(0.34f, width * 0.72f);
        float barHeight = 0.055f;
        float barX = drawX + (width - barWidth) * 0.5f;
        float barY = drawY + height + 0.09f;

        Pencil.I().addDrawing(new TextureDrawing(
            healthBarTexture,
            barX - 0.025f,
            barY - 0.02f,
            barWidth + 0.05f,
            barHeight + 0.04f,
            ZIndex.SYMBOL_STATUS,
            new Color(0.015f, 0.022f, 0.026f, 0.78f)
        ));

        if (healthRatio <= 0f) return;

        float damage = 1f - healthRatio;
        Pencil.I().addDrawing(new TextureDrawing(
            healthBarTexture,
            barX,
            barY,
            barWidth * healthRatio,
            barHeight,
            ZIndex.SYMBOL_STATUS,
            new Color(
                1f,
                0.78f - damage * 0.43f,
                0.22f - damage * 0.08f,
                0.94f
            )
        ));

    }

    private void drawLifetimeBar(float drawX, float drawY, float width, float height) {
        float remaining = 1f - MathUtils.clamp(
            lifetime / MAX_LIFETIME,
            0f,
            1f
        );
        float barWidth = Math.max(0.34f, width * 0.72f);
        float barHeight = 0.032f;
        float barX = drawX + (width - barWidth) * 0.5f;
        float barY = drawY + height + 0.20f;

        Pencil.I().addDrawing(new TextureDrawing(
            healthBarTexture,
            barX - 0.025f,
            barY - 0.015f,
            barWidth + 0.05f,
            barHeight + 0.03f,
            ZIndex.SYMBOL_STATUS,
            new Color(0.015f, 0.022f, 0.026f, 0.78f)
        ));

        if (remaining <= 0f) return;

        float danger = 1f - remaining;
        Pencil.I().addDrawing(new TextureDrawing(
            healthBarTexture,
            barX,
            barY,
            barWidth * remaining,
            barHeight,
            ZIndex.SYMBOL_STATUS,
            new Color(
                MathUtils.lerp(0.38f, 1f, danger),
                MathUtils.lerp(0.82f, 0.18f, danger),
                MathUtils.lerp(1f, 0.12f, danger),
                0.92f
            )
        ));
    }

    private float getUrgency() {
        float value = MathUtils.clamp(lifetime / MAX_LIFETIME, 0f, 1f);
        value = MathUtils.clamp((value - 0.58f) / 0.42f, 0f, 1f);
        return value * value * (3f - 2f * value);
    }

    public boolean isFinished() {
        return finished;
    }

    boolean isUnclaimed() {
        return !claimed && !finished;
    }

    /*
     * Collision helpers for symbol-to-symbol collisions.
     */

    public float getCenterX() {
        return x + SlotMachine.CELL_W / 2f;
    }

    public float getCenterY() {
        return y + SlotMachine.CELL_H / 2f;
    }

    public float getRadius() {
        return Math.min(
            getWidth(),
            getHeight()
        ) * 0.42f;
    }

    @Override
    public boolean isAvailableForCollector() {
        return !claimed && !finished && spawnAge >= COLLECTOR_GRACE_PERIOD;
    }

    @Override
    public float getCollectorTargetX() {
        return getCenterX();
    }

    @Override
    public float getCollectorTargetY() {
        return getCenterY();
    }

    @Override
    public float getCollectorTargetRadius() {
        return getRadius();
    }

    @Override
    public boolean collectByCollector() {
        return collect();
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public void move(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void addRotationVelocity(float amount) {
        rotationVelocity += amount;
    }
}
