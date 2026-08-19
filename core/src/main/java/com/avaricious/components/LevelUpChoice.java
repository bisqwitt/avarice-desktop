package com.avaricious.components;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.FabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class LevelUpChoice {

    /*
     * -------------------------------------------------
     * ANIMATION TUNING
     * -------------------------------------------------
     */

    private static final float ENTRANCE_DURATION =
        0.30f;

    private static final float HOVER_SCALE =
        1.05f;

    private static final float SYMBOL_HOVER_SCALE =
        1.10f;

    private static final float DISMISS_DURATION =
        0.32f;

    /*
     * -------------------------------------------------
     */

    private final Symbol symbol;

    private final FabledText title;
    private final FabledText description;

    private final Runnable upgrade;

    private Rectangle bounds;

    private final TextureRegion background =
        Assets.I().get(
            AssetKey.BLACK_PIXEL
        );

    private final TextureRegion whitePixel =
        Assets.I().get(
            AssetKey.WHITE_PIXEL
        );

    private final TextureRegion symbolTexture;

    private final TextureRegion symbolShadowTexture;

    private final TextureRegion symbolWhiteTexture;

    /*
     * Entry animation.
     */
    private float age = 0f;

    private float entranceDelay = 0f;

    /*
     * Hover.
     */
    private boolean hovered = false;

    private float hoverAmount = 0f;

    /*
     * Selection.
     */
    private boolean selected = false;

    private boolean dismissed = false;

    private float selectionTimer = 0f;

    private float dismissTargetX = 0f;

    private boolean upgradeApplied = false;

    /*
     * Calculated every frame.
     */
    private float renderCenterX;
    private float renderCenterY;

    private float renderScale = 1f;
    private float renderAlpha = 1f;

    private float symbolFlash = 0f;

    private float renderRotation = 0f;

    private float entranceProgress = 0f;

    public LevelUpChoice(
        Symbol symbol,
        FabledText title,
        FabledText description,
        Runnable upgrade
    ) {
        this.symbol = symbol;

        this.title = title;
        this.description = description;

        this.upgrade = upgrade;

        symbolTexture =
            Assets.I().getSymbol(symbol);

        symbolShadowTexture =
            Assets.I().get(
                symbol.shadowKey()
            );

        symbolWhiteTexture =
            Assets.I().get(
                symbol.whiteKey()
            );
    }

    public void setBounds(
        Rectangle bounds
    ) {
        this.bounds =
            new Rectangle(bounds);

        updateTextPosition(
            bounds.x +
                bounds.width / 2f,
            bounds.y +
                bounds.height / 2f
        );
    }

    public void setEntranceDelay(
        float entranceDelay
    ) {
        this.entranceDelay =
            entranceDelay;
    }

    public void setHovered(
        boolean hovered
    ) {
        if (
            selected ||
                dismissed
        ) {
            this.hovered = false;
            return;
        }

        if (hovered && !this.hovered) {
            AudioManager.I().playHover();
        }

        this.hovered = hovered;
    }

    public boolean contains(
        Vector2 position
    ) {
        /*
         * Do not allow selecting a card before it has
         * mostly finished appearing.
         */
        return bounds != null &&
            entranceProgress >= 0.70f &&
            bounds.contains(position);
    }

    /*
     * Gameplay effect.
     */
    public void select() {
        if (upgradeApplied) return;

        upgradeApplied = true;

        upgrade.run();
    }

    /*
     * Visual selected-card animation.
     */
    public void beginSelectedAnimation() {
        if (selected) return;

        selected = true;
        dismissed = false;
        hovered = false;

        selectionTimer = 0f;

        /*
         * Huge white symbol hit.
         */
        symbolFlash = 1f;
        AudioManager.I().playUpgradeSelected();
    }

    /*
     * Visual unselected-card animation.
     */
    public void beginDismissAnimation(
        float targetX
    ) {
        if (
            selected ||
                dismissed
        ) {
            return;
        }

        dismissed = true;
        hovered = false;

        selectionTimer = 0f;

        dismissTargetX =
            targetX;
    }

    private void update(
        float delta
    ) {
        if (bounds == null) return;

        age += delta;

        /*
         * -------------------------------------------------
         * ENTRANCE
         * -------------------------------------------------
         */

        entranceProgress =
            MathUtils.clamp(
                (
                    age -
                        entranceDelay
                ) /
                    ENTRANCE_DURATION,
                0f,
                1f
            );

        float entranceEase =
            easeOutBack(
                entranceProgress
            );

        float entranceScale =
            MathUtils.lerp(
                0.82f,
                1f,
                entranceEase
            );

        float entranceAlpha =
            smoothStep(
                entranceProgress
            );

        float baseCenterX =
            bounds.x +
                bounds.width / 2f;

        float baseCenterY =
            bounds.y +
                bounds.height / 2f;

        renderCenterX =
            baseCenterX;

        renderCenterY =
            baseCenterY +
                MathUtils.lerp(
                    -0.35f,
                    0f,
                    smoothStep(
                        entranceProgress
                    )
                );

        renderScale =
            entranceScale;

        renderAlpha =
            entranceAlpha;

        renderRotation = 0f;

        /*
         * -------------------------------------------------
         * HOVER
         * -------------------------------------------------
         */

        float hoverTarget =
            hovered
                ? 1f
                : 0f;

        hoverAmount =
            moveTowards(
                hoverAmount,
                hoverTarget,
                delta * 8f
            );

        renderScale *=
            MathUtils.lerp(
                1f,
                HOVER_SCALE,
                hoverAmount
            );

        /*
         * -------------------------------------------------
         * SELECTED
         * -------------------------------------------------
         */

        if (selected) {
            selectionTimer += delta;

            float selectionScale =
                calculateSelectionScale(
                    selectionTimer
                );

            renderScale *=
                selectionScale;

            /*
             * Tiny impact rotation wobble.
             */
            float wobbleFade =
                1f -
                    MathUtils.clamp(
                        selectionTimer /
                            0.30f,
                        0f,
                        1f
                    );

            renderRotation =
                MathUtils.sin(
                    selectionTimer *
                        42f
                ) *
                    1.8f *
                    wobbleFade;

            /*
             * Symbol flash is extremely short.
             */
            symbolFlash -=
                delta * 8.5f;

            if (symbolFlash < 0f) {
                symbolFlash = 0f;
            }
        }

        /*
         * -------------------------------------------------
         * DISMISSED
         * -------------------------------------------------
         */

        if (dismissed) {
            selectionTimer += delta;

            float progress =
                MathUtils.clamp(
                    selectionTimer /
                        DISMISS_DURATION,
                    0f,
                    1f
                );

            float eased =
                smoothStep(progress);

            /*
             * Fly into the winning card.
             */
            renderCenterX =
                MathUtils.lerp(
                    baseCenterX,
                    dismissTargetX,
                    eased
                );

            /*
             * Slight upward vacuum pull.
             */
            renderCenterY =
                baseCenterY +
                    eased * 0.18f;

            /*
             * Collapse aggressively.
             */
            renderScale *=
                MathUtils.lerp(
                    1f,
                    0.12f,
                    eased
                );

            renderAlpha *=
                1f - eased;
        }

        updateTextPosition(
            renderCenterX,
            renderCenterY
        );
    }

    private float calculateSelectionScale(
        float timer
    ) {
        /*
         * 0.00 - 0.07
         *
         * Compress.
         */
        if (timer < 0.07f) {
            float t =
                timer / 0.07f;

            return MathUtils.lerp(
                1f,
                0.92f,
                smoothStep(t)
            );
        }

        /*
         * 0.07 - 0.18
         *
         * Explode outward.
         */
        if (timer < 0.18f) {
            float t =
                (
                    timer -
                        0.07f
                ) /
                    0.11f;

            return MathUtils.lerp(
                0.92f,
                1.28f,
                easeOutCubic(t)
            );
        }

        /*
         * 0.18 - 0.36
         *
         * Snap back.
         */
        if (timer < 0.36f) {
            float t =
                (
                    timer -
                        0.18f
                ) /
                    0.18f;

            return MathUtils.lerp(
                1.28f,
                1.04f,
                smoothStep(t)
            );
        }

        /*
         * Final settle.
         */
        float t =
            MathUtils.clamp(
                (
                    timer -
                        0.36f
                ) /
                    0.18f,
                0f,
                1f
            );

        return MathUtils.lerp(
            1.04f,
            1f,
            smoothStep(t)
        );
    }

    private void updateTextPosition(
        float centerX,
        float centerY
    ) {
        /*
         * Same original offsets as your old layout:
         *
         * card y = 2
         * title y = 5.5
         * description y = 4.5
         *
         * Card center is 4.25.
         */
        float contentLeft =
            centerX -
                bounds.width / 2f +
                0.25f;

        title.setAbsoluteX(
            contentLeft
        );

        description.setAbsoluteX(
            contentLeft
        );

        title.setY(
            centerY +
                1.25f
        );

        description.setY(
            centerY +
                0.25f
        );
    }

    public void draw(float delta) {
        if (bounds == null) return;

        update(delta);

        if (renderAlpha <= 0.01f) {
            return;
        }

        float renderWidth =
            bounds.width *
                renderScale;

        float renderHeight =
            bounds.height *
                renderScale;

        float renderX =
            renderCenterX -
                renderWidth / 2f;

        float renderY =
            renderCenterY -
                renderHeight / 2f;

        /*
         * -------------------------------------------------
         * BACKGROUND
         * -------------------------------------------------
         */

        float backgroundAlpha =
            MathUtils.lerp(
                0.50f,
                0.68f,
                hoverAmount
            );

        if (selected) {
            backgroundAlpha = 0.78f;
        }

        Pencil.I().addDrawing(
            new TextureDrawing(
                background,
                renderX,
                renderY,
                renderWidth,
                renderHeight,
                1f,
                renderRotation,
                ZIndex.SHOP,
                new Color(
                    0f,
                    0f,
                    0f,
                    backgroundAlpha *
                        renderAlpha
                )
            )
        );

        /*
         * -------------------------------------------------
         * BORDER
         * -------------------------------------------------
         */

        float borderAlpha =
            0.13f +
                hoverAmount * 0.72f;

        if (selected) {
            borderAlpha =
                0.9f;
        }

        borderAlpha *=
            renderAlpha;

        float borderSize =
            0.035f +
                hoverAmount * 0.025f;

        drawBorder(
            renderX,
            renderY,
            renderWidth,
            renderHeight,
            borderSize,
            borderAlpha
        );

        /*
         * -------------------------------------------------
         * SYMBOL
         * -------------------------------------------------
         */

        float symbolScale =
            MathUtils.lerp(
                1f,
                SYMBOL_HOVER_SCALE,
                hoverAmount
            );

        if (selected) {
            /*
             * Selected icon gets a little more punch than
             * the card itself.
             */
            symbolScale *=
                1f +
                    symbolFlash * 0.14f;
        }

        float symbolSize =
            1.28f *
                renderScale *
                symbolScale;

        float symbolCenterY =
            renderCenterY -
                1.05f;

        float symbolX =
            renderCenterX -
                symbolSize / 2f;

        float symbolY =
            symbolCenterY -
                symbolSize / 2f;

        /*
         * Shadow.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                symbolShadowTexture,
                symbolX,
                symbolY - 0.08f,
                symbolSize,
                symbolSize,
                1f,
                renderRotation,
                ZIndex.SHOP,
                new Color(
                    1f,
                    1f,
                    1f,
                    Math.min(
                        Assets.I()
                            .shadowColor()
                            .a,
                        renderAlpha
                    )
                )
            )
        );

        /*
         * Real symbol.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                symbolTexture,
                symbolX,
                symbolY,
                symbolSize,
                symbolSize,
                1f,
                renderRotation,
                ZIndex.SHOP,
                new Color(
                    1f,
                    1f,
                    1f,
                    renderAlpha
                )
            )
        );

        /*
         * White symbol impact overlay.
         *
         * This gives you the ~100 ms "slot reward hit"
         * without needing another shader.
         */
        if (
            selected &&
                symbolFlash > 0f
        ) {
            float flashScale =
                1f +
                    symbolFlash * 0.22f;

            float flashSize =
                symbolSize *
                    flashScale;

            Pencil.I().addDrawing(
                new TextureDrawing(
                    symbolWhiteTexture,
                    renderCenterX -
                        flashSize / 2f,
                    symbolCenterY -
                        flashSize / 2f,
                    flashSize,
                    flashSize,
                    1f,
                    renderRotation,
                    ZIndex.SHOP,
                    new Color(
                        1f,
                        1f,
                        1f,
                        symbolFlash *
                            renderAlpha
                    )
                )
            );
        }

        /*
         * -------------------------------------------------
         * TEXT
         * -------------------------------------------------
         *
         * FabledText doesn't currently have opacity, so
         * instead of allowing text to awkwardly remain
         * full-opacity while a card disappears, stop
         * drawing it almost immediately during dismiss.
         */

        boolean drawText =
            entranceProgress > 0.42f &&
                (
                    !dismissed ||
                        selectionTimer < 0.055f
                );

        if (drawText) {
            title.draw(delta);
            description.draw(delta);
        }
    }

    private void drawBorder(
        float x,
        float y,
        float width,
        float height,
        float thickness,
        float alpha
    ) {
        Color color =
            new Color(
                1f,
                1f,
                1f,
                alpha
            );

        /*
         * Bottom.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                whitePixel,
                x,
                y,
                width,
                thickness,
                ZIndex.SHOP,
                color
            )
        );

        /*
         * Top.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                whitePixel,
                x,
                y +
                    height -
                    thickness,
                width,
                thickness,
                ZIndex.SHOP,
                color
            )
        );

        /*
         * Left.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                whitePixel,
                x,
                y,
                thickness,
                height,
                ZIndex.SHOP,
                color
            )
        );

        /*
         * Right.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                whitePixel,
                x +
                    width -
                    thickness,
                y,
                thickness,
                height,
                ZIndex.SHOP,
                color
            )
        );
    }

    public float getCenterX() {
        if (bounds == null) {
            return 0f;
        }

        return bounds.x +
            bounds.width / 2f;
    }

    public float getCenterY() {
        if (bounds == null) {
            return 0f;
        }

        return bounds.y +
            bounds.height / 2f;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    private static float moveTowards(
        float current,
        float target,
        float maxDelta
    ) {
        if (
            Math.abs(
                target - current
            ) <= maxDelta
        ) {
            return target;
        }

        return current +
            Math.signum(
                target - current
            ) *
                maxDelta;
    }

    private static float smoothStep(
        float value
    ) {
        value =
            MathUtils.clamp(
                value,
                0f,
                1f
            );

        return value *
            value *
            (3f - 2f * value);
    }

    private static float easeOutCubic(
        float value
    ) {
        value =
            MathUtils.clamp(
                value,
                0f,
                1f
            );

        float inverse =
            1f - value;

        return 1f -
            inverse *
                inverse *
                inverse;
    }

    /*
     * Slight overshoot.
     *
     * Excellent for UI rewards because the card feels
     * physically "thrown" into position instead of merely
     * fading in.
     */
    private static float easeOutBack(
        float value
    ) {
        value =
            MathUtils.clamp(
                value,
                0f,
                1f
            );

        float c1 = 1.70158f;
        float c3 = c1 + 1f;

        float t =
            value - 1f;

        return 1f +
            c3 *
                t *
                t *
                t +
            c1 *
                t *
                t;
    }
}
