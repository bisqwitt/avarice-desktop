package com.avaricious.components.buttons;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class DisablableButton extends Button {

    // "disabled" now means: not interactive
    private boolean disabled = true;

    // animation progress 0..1 (0 = hidden, 1 = shown)
    private float vis = 0f;
    private float visTarget = 0f;

    // tuning (world units)
    private float slideInOffset = 0.45f;     // how far below it comes from
    private float animSpeed = 10f;           // higher = faster
    private float overshootScale = 1.06f;    // Balatro-ish pop

    private final TextureRegion buttonShadow = Assets.I().get(AssetKey.BUTTON_SHADOW);
    private TextureRegion disabledTexture = null;

    public DisablableButton(
        Runnable onButtonPressedRunnable,
        TextureRegion defaultButtonTexture,
        TextureRegion pressedButtonTexture,
        TextureRegion hoveredButtonTexture,
        Rectangle buttonRectangle,
        int key, ZIndex layer) {
        super(onButtonPressedRunnable, defaultButtonTexture, pressedButtonTexture, hoveredButtonTexture, buttonRectangle, key, layer);
        setVisibleAnimated(false, true); // start hidden (instant)
    }

    /**
     * Call this instead of setDisabled(!visible).
     */
    public void setVisibleAnimated(boolean visible) {
        visTarget = visible ? 1f : 0f;
        disabled = !visible; // immediately block input while animating in/out
        if (visible) currentTexture = defaultButtonTexture;
    }

    /**
     * Optional: force state instantly (e.g., on screen show).
     */
    public void setVisibleAnimated(boolean visible, boolean instant) {
        visTarget = visible ? 1f : 0f;
        if (instant) vis = visTarget;
        disabled = !visible;
        if (visible) currentTexture = defaultButtonTexture;
    }

    public boolean isVisibleNow() {
        return vis > 0.001f;
    }

    /**
     * Update should be called once per frame before draw/input.
     */
    private void update(float delta) {
        // Smoothly move vis toward target
        float step = animSpeed * delta;
        if (vis < visTarget) vis = Math.min(visTarget, vis + step);
        else if (vis > visTarget) vis = Math.max(visTarget, vis - step);

        // When fully shown, enable input. When fully hidden, keep disabled.
        if (vis >= 0.999f) disabled = false;
        if (vis <= 0.001f) disabled = true;
    }

    @Override
    public boolean handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (disabled()) return false;
        // No input unless fully shown (Balatro-style: commit at the end of the animation)
        if (disabled || vis < 0.999f) {
            wasHovered = false;
            return false;
        }
        return super.handleInput(mouse, pressed, wasPressed);
    }

    @Override
    public void draw(float delta) {
        update(delta);
        if (vis <= 0.001f) return;

        // Ease for nicer motion
        float t = Interpolation.pow3Out.apply(vis);

        // Slide from below
        float yOffset = (1f - t) * slideInOffset;

        // Slight pop/overshoot near the end
        float pop = (vis > 0.7f) ? (vis - 0.7f) / 0.3f : 0f; // 0..1
        float scale = 1f + (overshootScale - 1f) * Interpolation.swingOut.apply(pop);

        // Draw with transform around center
        Rectangle r = buttonRectangle;

        float cx = r.x + r.width * 0.5f;
        float cy = r.y + r.height * 0.5f;

        float w = r.width * scale;
        float h = r.height * scale;

        float drawX = cx - w * 0.5f;
        float drawY = (cy - h * 0.5f) - yOffset;

        // Optional: fade in (uncomment if you want)
        // batch.setColor(1f, 1f, 1f, t);

        drawWithShadow(new Rectangle(drawX, drawY, w, h));

        // batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawWithShadow(Rectangle bounds) {
        float alpha = disabled() ? 0.6f : 1f;
        if (showShadow) {
            Color shadowColor = Assets.I().shadowColor();
            Pencil.I().addDrawing(new TextureDrawing(
                buttonShadow, bounds.x, bounds.y - 0.1f, bounds.width, bounds.height,
                layer, new Color(shadowColor.r, shadowColor.g, shadowColor.b, currentTexture == pressedButtonTexture ? 0.1f : 0.25f)
            ));
        }
        Pencil.I().addDrawing(new TextureDrawing(
            disabled() && disabledTexture != null ? disabledTexture : currentTexture,
            bounds.x, bounds.y, bounds.width, bounds.height,
            layer, new Color(1f, 1f, 1f, alpha)
        ));
    }

    public void setDisabledTexture(TextureRegion disabledTexture) {
        this.disabledTexture = disabledTexture;
    }

    public abstract boolean disabled();
}
