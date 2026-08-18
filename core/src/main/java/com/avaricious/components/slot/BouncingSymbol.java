package com.avaricious.components.slot;

import com.avaricious.components.ScreenShake;
import com.avaricious.components.XpBar;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.effects.PulseEffect;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class BouncingSymbol {

    private final Symbol symbol;
    private final TextureRegion texture;
    private final TextureRegion whiteTexture;

    private final PulseEffect pulseEffect = new PulseEffect();

    private float x;
    private float y;

    private float velocityX;
    private float velocityY;

    private float rotation;
    private float rotationVelocity;

    private float scale = 1f;

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

    private static final float MIN_BOUNCE = 0.85f;
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
        this.symbol = symbol;
        this.texture = Assets.I().getSymbol(symbol);
        this.whiteTexture = Assets.I().get(symbol.whiteKey());

        this.x = x;
        this.y = y;

        /*
         * Launch in a random direction.
         */
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float speed = MathUtils.random(6f, 12f);

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
        pulseEffect.setStrength(3.25f);
        pulseEffect.setSpeed(0.125f);
    }

    public void update(float delta) {

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
        if (claimed) {
            return;
        }

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

        if (!touching || !getHitbox().contains(mouse) || claimed) {
            return false;
        }

        pulseEffect.pulse();

        disappearTime = 0f;
        claimed = true;

        ParticleManager.I().create(
            x,
            y,
            ParticleType.WHITE,
            0.02f,
            50f,
            ZIndex.SLOT_MACHINE
        );

        PopupManager.I().spawnNumber(
            SymbolValues.I().getValue(symbol),
            Assets.I().getSymbolColor(symbol),
            x + getWidth(),
            y + getHeight(),
            false
        );

        ScoreDisplay.I().addToScore(SymbolValues.I().getValue(symbol));

        XpBar.I().addXp(1);

         ScreenShake.I().addTrauma(0.05f);

        return true;
    }

    private void miss() {
        Vector2 spawnPos = new Vector2(
            getCenterX(),
            getCenterY()
        );

        PopupManager.I().createRedCross(spawnPos);

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
            float overlap = screenLeft - left;

            x += overlap;

            velocityX =
                Math.abs(velocityX) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);
        }

        /*
         * RIGHT WALL
         */
        if (right > screenRight) {
            float overlap = right - screenRight;

            x -= overlap;

            velocityX =
                -Math.abs(velocityX) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);
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
            float overlap = screenBottom - bottom;

            y += overlap;

            velocityY =
                Math.abs(velocityY) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);
        }

        /*
         * TOP WALL
         */
        if (top > screenTop) {
            float overlap = top - screenTop;

            y -= overlap;

            velocityY =
                -Math.abs(velocityY) * randomBounce();

            rotationVelocity +=
                MathUtils.random(-90f, 90f);
        }
    }

    /*
     * Only called after the symbol has been claimed.
     */
    private void updateScale() {
        float progress = MathUtils.clamp(
            disappearTime / DISAPPEAR_DURATION,
            0f,
            1f
        );

        scale = MathUtils.lerp(
            1f,
            0f,
            progress
        );
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
                ZIndex.SLOT_MACHINE
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
