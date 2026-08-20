package com.avaricious.components.slot;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.CompChipBar;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.LostSymbolPopup;
import com.avaricious.components.popups.SpadePopup;
import com.avaricious.effects.PulseEffect;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
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

public class BouncingSymbol {

    private static final int COMP_CHIP_REWARD = 1;

    private final Symbol symbol;
    private final TextureRegion texture;
    private final TextureRegion whiteTexture;
    private final TextureRegion shadowTexture;

    private final PulseEffect pulseEffect = new PulseEffect();

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
    private float collisionCooldown = 0f;
    private boolean hovered = false;

    /*
     * Lifetime before the symbol counts as missed.
     */
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
    private static final float MAX_LIFETIME = 3f;

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

        /*
         * Bouncing symbol pulse settings.
         */
        pulseEffect.setStrength(0.8f);
        pulseEffect.setSpeed(0.125f);

        scale = 0.2f;
    }

    public void update(float delta) {

        spawnAge += delta;
        collisionCooldown = Math.max(0f, collisionCooldown - delta);
        impactFlash = Math.max(0f, impactFlash - delta * 5.5f);
        hoverAmount = MathUtils.lerp(
            hoverAmount,
            hovered && !claimed ? 1f : 0f,
            Math.min(1f, delta * 12f)
        );

        /*
         * The symbol continues moving while it is available.
         */
        updatePhysics(delta);

        pulseEffect.update(delta);

        handleHorizontalCollisions();
        handleVerticalCollisions();

        /*
         * If the symbol has not been claimed yet,
         * count down its available lifetime.
         */
        if (!claimed) {
            lifetime += delta;

            if (lifetime >= MAX_LIFETIME) {
                miss();
            }

            updateScale();
            return;
        }

        /*
         * Once clicked, start the disappearance timer.
         */
        disappearTime += delta;

        updateScale();

        if (disappearTime >= DISAPPEAR_DURATION) {
            finished = true;
        }
    }

    private void updatePhysics(float delta) {
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
        x += velocityX * delta;
        y += velocityY * delta;

        /*
         * Rotation
         */
        rotation += rotationVelocity * delta;

        /*
         * Small organic wobble.
         */
        rotation += MathUtils.sin(lifetime * wobbleSpeed)
            * wobbleStrength
            * delta;
    }

    public boolean handleInput(
        Vector2 mouse,
        boolean touching,
        boolean wasTouching
    ) {

        hovered = !claimed && getHitbox().contains(mouse);

        if (!touching || !hovered || claimed) {
            return false;
        }

        pulseEffect.pulse(1.65f);

        disappearTime = 0f;
        claimed = true;

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

        PopupManager.I().spawnSpade(new SpadePopup(
            Assets.I().getSymbolColor(symbol),
            getCenterX() + 0.75f,
            getCenterY() + 0.5f,
            () -> CompChipBar.I().addChips(COMP_CHIP_REWARD)
        ));

        AudioManager.I().playCollect(COMP_CHIP_REWARD);
        ScreenShake.I().addTrauma(0.10f);
        impactFlash = 1f;

        return true;
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
        ScreenShake.I().addTrauma(0.055f);
        finished = true;
    }

    private Rectangle getHitbox() {
        float width = getWidth() * 2f;
        float height = getHeight() * 2f;

        float centerX =
            x + SlotMachine.CELL_W / 2f;

        float centerY =
            y + SlotMachine.CELL_H / 2f;

        float drawX =
            centerX - width / 2f;

        float drawY =
            centerY - height / 2f;

        return new Rectangle(
            drawX,
            drawY,
            width,
            height
        );
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

        float progress = MathUtils.clamp(
            disappearTime / DISAPPEAR_DURATION,
            0f,
            1f
        );

        if (progress < 0.18f) {
            scale = MathUtils.lerp(1f, 1.55f, progress / 0.18f);
        } else {
            float collapse = (progress - 0.18f) / 0.82f;
            scale = MathUtils.lerp(1.55f, 0f, collapse * collapse);
        }
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

        float centerX =
            x + SlotMachine.CELL_W / 2f;

        float centerY =
            y + SlotMachine.CELL_H / 2f;

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

        float finalRotation =
            rotation + pulseEffect.getRotation();

        float speed = (float) Math.sqrt(
            velocityX * velocityX + velocityY * velocityY
        );
        float urgency = getUrgency();

        /* Motion echoes make fast launches legible without extra textures. */
        float trailAlpha = MathUtils.clamp(speed / 14f, 0f, 1f) * 0.18f;
        for (int i = 2; i >= 1; i--) {
            float trailOffset = i * 0.028f;
            Pencil.I().addDrawing(
                new TextureDrawing(
                    whiteTexture,
                    drawX - velocityX * trailOffset,
                    drawY - velocityY * trailOffset,
                    width,
                    height,
                    1f - i * 0.08f,
                    finalRotation - rotationVelocity * trailOffset,
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

        if (impactFlash > 0f || claimed) {
            float claimFlash = claimed
                ? Math.max(0f, 1f - disappearTime / 0.16f)
                : 0f;
            float flash = Math.max(impactFlash, claimFlash);

            Pencil.I().addDrawing(
                new TextureDrawing(
                    whiteTexture,
                    drawX,
                    drawY,
                    width,
                    height,
                    1f + flash * 0.22f,
                    finalRotation,
                    ZIndex.SLOT_MACHINE_FOREGROUND,
                    new Color(1f, 1f, 1f, flash * 0.82f)
                )
            );
        }
    }

    private float getUrgency() {
        float value = MathUtils.clamp(lifetime / MAX_LIFETIME, 0f, 1f);
        value = MathUtils.clamp((value - 0.58f) / 0.42f, 0f, 1f);
        return value * value * (3f - 2f * value);
    }

    public boolean isFinished() {
        return finished;
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
