package com.avaricious.components;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.FabledText;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.items.upgrades.UpgradeRarity;
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

    private static final float TITLE_HORIZONTAL_PADDING =
        0.30f;

    private static final float RARITY_BADGE_WIDTH = 79f / 45f;
    private static final float RARITY_BADGE_HEIGHT = 23f / 45f;
    private static final float RARITY_TEXT_MAX_WIDTH = 1.42f;

    /*
     * -------------------------------------------------
     */

    private final FabledText title;
    private final FabledText description;
    private final FabledText rarityText;

    private final Runnable upgrade;
    private final UpgradeRarity rarity;

    private Rectangle bounds;

    private final TextureRegion background =
        Assets.I().get(
            AssetKey.DARK_SLATE_PIXEL
        );

    private final TextureRegion shadowPixel =
        Assets.I().get(
            AssetKey.BLACK_PIXEL
        );

    private final TextureRegion whitePixel =
        Assets.I().get(
            AssetKey.WHITE_PIXEL
        );

    private final TextureRegion texture;

    private final TextureRegion shadowTexture;

    private final TextureRegion whiteTexture;

    private final boolean[][] patternMask;

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

    private boolean lightened = false;

    private float lightenAmount = 0f;

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
    private int shortcutNumber;

    public LevelUpChoice(
        Symbol symbol,
        FabledText title,
        FabledText description,
        Runnable upgrade
    ) {
        this(
            title,
            description,
            upgrade,
            Assets.I().getSymbol(symbol),
            Assets.I().get(symbol.shadowKey()),
            Assets.I().get(symbol.whiteKey()),
            UpgradeRarity.COMMON
        );
    }

    public LevelUpChoice(
        FabledText title,
        FabledText description,
        Runnable upgrade,
        TextureRegion texture,
        TextureRegion shadowTexture,
        TextureRegion whiteTexture
    ) {
        this(
            title,
            description,
            upgrade,
            texture,
            shadowTexture,
            whiteTexture,
            UpgradeRarity.COMMON
        );
    }

    public LevelUpChoice(
        FabledText title,
        FabledText description,
        Runnable upgrade,
        TextureRegion texture,
        TextureRegion shadowTexture,
        TextureRegion whiteTexture,
        UpgradeRarity rarity
    ) {
        this(
            title,
            description,
            upgrade,
            texture,
            shadowTexture,
            whiteTexture,
            null,
            rarity
        );
    }

    private LevelUpChoice(
        FabledText title,
        FabledText description,
        Runnable upgrade,
        TextureRegion texture,
        TextureRegion shadowTexture,
        TextureRegion whiteTexture,
        boolean[][] patternMask,
        UpgradeRarity rarity
    ) {
        this.title = title;
        this.description = description;
        this.upgrade = upgrade;
        this.texture = texture;
        this.shadowTexture = shadowTexture;
        this.whiteTexture = whiteTexture;
        this.patternMask = patternMask == null ? null : copyMask(patternMask);
        this.rarity = rarity == null ? UpgradeRarity.COMMON : rarity;
        this.rarityText = new GeneratedFabledText(
            this.rarity.toString(),
            39f,
            0.024f,
            0.11f,
            ZIndex.SHOP_CARD,
            true
        );
        this.rarityText.fitWithinWidth(RARITY_TEXT_MAX_WIDTH);
        this.rarityText.setFloatEffects(0.008f, 0.85f);
    }

    public LevelUpChoice(
        FabledText title,
        FabledText description,
        Runnable upgrade,
        boolean[][] patternMask
    ) {
        this(
            title,
            description,
            upgrade,
            patternMask,
            UpgradeRarity.COMMON
        );
    }

    public LevelUpChoice(
        FabledText title,
        FabledText description,
        Runnable upgrade,
        boolean[][] patternMask,
        UpgradeRarity rarity
    ) {
        this(
            title,
            description,
            upgrade,
            null,
            null,
            null,
            patternMask,
            rarity
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

        title.fitWithinWidth(
            bounds.width -
                TITLE_HORIZONTAL_PADDING * 2f
        );
        description.fitWithinWidth(
            bounds.width -
                TITLE_HORIZONTAL_PADDING * 2f
        );
        rarityText.fitWithinWidth(RARITY_TEXT_MAX_WIDTH);
    }

    public void setEntranceDelay(
        float entranceDelay
    ) {
        this.entranceDelay =
            entranceDelay;
    }

    public void setShortcutNumber(int shortcutNumber) {
        this.shortcutNumber = shortcutNumber;
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

    public void setLightened(
        boolean lightened
    ) {
        if (
            selected ||
                dismissed
        ) {
            this.lightened = false;
            return;
        }

        this.lightened = lightened;
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
        lightened = false;

        selectionTimer = 0f;

        /*
         * Huge white symbol hit.
         */
        symbolFlash = 1f;
        AudioManager.I().playUpgradeSelected(getRarityTier());
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
        lightened = false;

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

        float lightenTarget =
            lightened
                ? 1f
                : 0f;

        lightenAmount =
            moveTowards(
                lightenAmount,
                lightenTarget,
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

        if (!dismissed) {
            renderCenterY += hoverAmount * 0.10f;
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

        float rarityTextWidth = Math.min(
            rarityText.getNaturalWidth(),
            RARITY_TEXT_MAX_WIDTH
        );
        rarityText.setAbsoluteX(centerX - rarityTextWidth / 2f);
        rarityText.setY(
            centerY -
                bounds.height / 2f +
                0.29f
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

        Color rarityColor = Assets.I().getRarityColor(rarity);
        float rarityPulse =
            (MathUtils.sin(age * 2.8f + getRarityTier() * 0.7f) + 1f) *
                0.5f;

        float glowAlpha =
            0.018f +
                getRarityTier() * 0.010f +
                hoverAmount * 0.085f +
                (selected ? 0.10f + rarityPulse * 0.07f : 0f);
        float glowPadding =
            0.06f + hoverAmount * 0.07f + (selected ? 0.07f : 0f);

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            renderX - glowPadding,
            renderY - glowPadding,
            renderWidth + glowPadding * 2f,
            renderHeight + glowPadding * 2f,
            1f,
            renderRotation,
            ZIndex.SHOP,
            new Color(
                rarityColor.r,
                rarityColor.g,
                rarityColor.b,
                glowAlpha * renderAlpha
            )
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            shadowPixel,
            renderX + 0.10f * renderScale,
            renderY - 0.14f * renderScale,
            renderWidth,
            renderHeight,
            1f,
            renderRotation,
            ZIndex.SHOP,
            new Color(0f, 0f, 0f, 0.46f * renderAlpha)
        ));

        /*
         * -------------------------------------------------
         * BACKGROUND
         * -------------------------------------------------
         */

        float backgroundAlpha =
            MathUtils.lerp(
                0.72f,
                0.64f,
                lightenAmount
            );

        backgroundAlpha =
            MathUtils.lerp(
                backgroundAlpha,
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
                    0.72f,
                    0.74f,
                    0.78f,
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

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            renderX + borderSize,
            renderY + renderHeight - borderSize - 0.055f * renderScale,
            renderWidth - borderSize * 2f,
            0.055f * renderScale,
            1f,
            renderRotation,
            ZIndex.SHOP,
            new Color(
                rarityColor.r,
                rarityColor.g,
                rarityColor.b,
                (0.34f + hoverAmount * 0.38f) * renderAlpha
            )
        ));

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
                0.78f;

        float symbolX =
            renderCenterX -
                symbolSize / 2f;

        float symbolY =
            symbolCenterY -
                symbolSize / 2f;

        float iconPlateSize = symbolSize * 1.12f;
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            renderCenterX - iconPlateSize / 2f,
            symbolCenterY - iconPlateSize / 2f,
            iconPlateSize,
            iconPlateSize,
            1f,
            renderRotation + 45f,
            ZIndex.SHOP,
            new Color(
                rarityColor.r,
                rarityColor.g,
                rarityColor.b,
                (0.035f + hoverAmount * 0.075f +
                    (selected ? 0.07f : 0f)) * renderAlpha
            )
        ));

        if (patternMask != null) {
            drawPatternIcon(
                renderCenterX,
                symbolCenterY,
                symbolSize,
                renderRotation,
                renderAlpha,
                selected ? symbolFlash : 0f
            );
        } else {

        /*
         * Shadow.
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                shadowTexture,
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
                texture,
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
                    whiteTexture,
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

        }

        drawRaritySparkles(
            symbolCenterY,
            symbolSize,
            rarityColor
        );

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
            drawRarityBadge(renderY);
            drawShortcutBadge(renderX, renderY, renderWidth, renderHeight);
            title.draw(delta);
            description.draw(delta);
            rarityText.draw(delta);
        }

        /*
         * Draw this after the image and text so hovering one card pushes the
         * others into the background instead of making them compete for focus.
         */
        if (lightenAmount > 0.001f) {
            Pencil.I().addDrawing(
                new TextureDrawing(
                    shadowPixel,
                    renderX,
                    renderY,
                    renderWidth,
                    renderHeight,
                    1f,
                    renderRotation,
                    ZIndex.SHOP_CARD_TOUCHING,
                    new Color(
                        0f,
                        0f,
                        0f,
                        lightenAmount *
                            (1f - hoverAmount) *
                            0.24f *
                            renderAlpha
                    )
                )
            );
        }
    }

    private void drawPatternIcon(
        float centerX,
        float centerY,
        float size,
        float rotation,
        float alpha,
        float flash
    ) {
        int rows = patternMask.length;
        int columns = patternMask[0].length;
        int longestSide = Math.max(rows, columns);
        float gap = size * 0.065f;
        float cellSize =
            (size - gap * (longestSide - 1)) / longestSide;
        float gridWidth = columns * cellSize + (columns - 1) * gap;
        float gridHeight = rows * cellSize + (rows - 1) * gap;
        float step = cellSize + gap;
        float cos = MathUtils.cosDeg(rotation);
        float sin = MathUtils.sinDeg(rotation);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float localX =
                    -gridWidth / 2f + cellSize / 2f + column * step;
                float localY =
                    gridHeight / 2f - cellSize / 2f - row * step;
                float rotatedX = localX * cos - localY * sin;
                float rotatedY = localX * sin + localY * cos;
                float cellCenterX = centerX + rotatedX;
                float cellCenterY = centerY + rotatedY;
                boolean activeCell = patternMask[row][column];

                if (activeCell) {
                    Pencil.I().addDrawing(new TextureDrawing(
                        whitePixel,
                        cellCenterX - cellSize / 2f,
                        cellCenterY - cellSize / 2f - 0.055f,
                        cellSize,
                        cellSize,
                        1f,
                        rotation,
                        ZIndex.SHOP,
                        new Color(0f, 0f, 0f, 0.62f * alpha)
                    ));
                }

                Pencil.I().addDrawing(new TextureDrawing(
                    whitePixel,
                    cellCenterX - cellSize / 2f,
                    cellCenterY - cellSize / 2f,
                    cellSize,
                    cellSize,
                    1f,
                    rotation,
                    ZIndex.SHOP,
                    activeCell
                        ? new Color(0.88f, 0.91f, 0.88f, alpha)
                        : new Color(0.34f, 0.36f, 0.38f, 0.24f * alpha)
                ));

                if (activeCell && flash > 0f) {
                    Pencil.I().addDrawing(new TextureDrawing(
                        whitePixel,
                        cellCenterX - cellSize / 2f,
                        cellCenterY - cellSize / 2f,
                        cellSize,
                        cellSize,
                        1f + flash * 0.28f,
                        rotation,
                        ZIndex.SHOP,
                        new Color(1f, 1f, 1f, flash * alpha)
                    ));
                }
            }
        }
    }

    private static boolean[][] copyMask(boolean[][] source) {
        if (source == null || source.length == 0 || source[0].length == 0) {
            throw new IllegalArgumentException("A pattern icon needs a mask");
        }

        int columns = source[0].length;
        boolean[][] copy = new boolean[source.length][columns];

        for (int row = 0; row < source.length; row++) {
            if (source[row].length != columns) {
                throw new IllegalArgumentException("Pattern masks must be rectangular");
            }
            System.arraycopy(source[row], 0, copy[row], 0, columns);
        }

        return copy;
    }

    private void drawBorder(
        float x,
        float y,
        float width,
        float height,
        float thickness,
        float alpha
    ) {
        Color rarityColor = Assets.I().getRarityColor(rarity);
        Color color = new Color(
            rarityColor.r,
            rarityColor.g,
            rarityColor.b,
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

    private void drawRarityBadge(float renderY) {
        float badgeWidth = RARITY_BADGE_WIDTH * renderScale;
        float badgeHeight = RARITY_BADGE_HEIGHT * renderScale;

        Pencil.I().addDrawing(new TextureDrawing(
            rarity.getRarityBoxTexture(),
            renderCenterX - badgeWidth / 2f,
            renderY + 0.12f * renderScale,
            badgeWidth,
            badgeHeight,
            1f,
            renderRotation,
            ZIndex.SHOP,
            new Color(1f, 1f, 1f, renderAlpha)
        ));
    }

    private void drawShortcutBadge(
        float renderX,
        float renderY,
        float renderWidth,
        float renderHeight
    ) {
        if (shortcutNumber < 1 || shortcutNumber > 9 || selected || dismissed) {
            return;
        }

        float badgeSize = 0.43f * renderScale;
        float badgeX = renderX + renderWidth - badgeSize - 0.15f * renderScale;
        float badgeY = renderY + renderHeight - badgeSize - 0.15f * renderScale;
        Color rarityColor = Assets.I().getRarityColor(rarity);

        Pencil.I().addDrawing(new TextureDrawing(
            shadowPixel,
            badgeX,
            badgeY - 0.045f * renderScale,
            badgeSize,
            badgeSize,
            1f,
            renderRotation,
            ZIndex.SHOP_CARD,
            new Color(0f, 0f, 0f, 0.52f * renderAlpha)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            badgeX,
            badgeY,
            badgeSize,
            badgeSize,
            1f,
            renderRotation,
            ZIndex.SHOP_CARD,
            new Color(
                rarityColor.r,
                rarityColor.g,
                rarityColor.b,
                (0.62f + hoverAmount * 0.28f) * renderAlpha
            )
        ));

        float numberWidth = 0.15f * renderScale;
        float numberHeight = 0.25f * renderScale;
        float numberX = badgeX + (badgeSize - numberWidth) / 2f;
        float numberY = badgeY + (badgeSize - numberHeight) / 2f;
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().getDigitalNumberShadow(shortcutNumber),
            numberX,
            numberY - 0.025f * renderScale,
            numberWidth,
            numberHeight,
            1f,
            renderRotation,
            ZIndex.SHOP_CARD,
            new Color(1f, 1f, 1f, renderAlpha)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().getDigitalNumber(shortcutNumber),
            numberX,
            numberY,
            numberWidth,
            numberHeight,
            1f,
            renderRotation,
            ZIndex.SHOP_CARD,
            new Color(1f, 1f, 1f, renderAlpha)
        ));
    }

    private void drawRaritySparkles(
        float symbolCenterY,
        float symbolSize,
        Color rarityColor
    ) {
        float amount = Math.max(hoverAmount, selected ? 0.85f : 0f);
        if (amount <= 0.01f) return;

        for (int index = 0; index < 4; index++) {
            float angle = age * (42f + getRarityTier() * 5f) + index * 90f;
            float radius = symbolSize * (0.62f + (index % 2) * 0.09f);
            float size = (0.035f + (index % 2) * 0.018f) * renderScale;
            float sparkleX = renderCenterX + MathUtils.cosDeg(angle) * radius;
            float sparkleY = symbolCenterY + MathUtils.sinDeg(angle) * radius;

            Pencil.I().addDrawing(new TextureDrawing(
                whitePixel,
                sparkleX - size / 2f,
                sparkleY - size / 2f,
                size,
                size,
                1f,
                angle,
                ZIndex.SHOP_CARD,
                new Color(
                    rarityColor.r,
                    rarityColor.g,
                    rarityColor.b,
                    amount * (0.48f + (index % 2) * 0.22f) * renderAlpha
                )
            ));
        }
    }

    public Color getRarityColor() {
        return new Color(Assets.I().getRarityColor(rarity));
    }

    public int getRarityTier() {
        return MathUtils.clamp(rarity.ordinal(), 0, 4);
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
