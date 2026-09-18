package com.avaricious.components;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.FabledText;
import com.avaricious.components.texts.FabledWord;
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

/** A reward card with a quiet reading state and a short, tactile claim animation. */
public class LevelUpChoice {

    private static final float ENTRANCE_DURATION = 0.34f;
    private static final float DISMISS_DURATION = 0.28f;
    private static final float ICON_OFFSET_Y = 0.65f;
    private static final float SELECTED_CENTER_X = 8f;
    private static final Color CARD_COLOR = new Color(0.045f, 0.072f, 0.090f, 1f);
    private static final Color MUTED_TEXT = new Color(0.59f, 0.70f, 0.73f, 1f);
    private static final Color VALUE_TEXT = new Color(0.76f, 1f, 0.87f, 1f);

    private final FabledText title;
    private final FabledText description;
    private final FabledText rarityText;
    private FabledText claimedDescription;
    private final Runnable upgrade;
    private final UpgradeRarity rarity;
    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final TextureRegion texture;
    private final TextureRegion shadowTexture;
    private final TextureRegion whiteTexture;
    private final boolean[][] patternMask;

    private Rectangle bounds;
    private float age;
    private float entranceDelay;
    private float entranceProgress;
    private boolean hovered;
    private float hoverAmount;
    private boolean lightened;
    private float lightenAmount;
    private boolean selected;
    private boolean dismissed;
    private boolean upgradeApplied;
    private float selectionTimer;
    private float dismissTargetX;
    private float renderCenterX;
    private float renderCenterY;
    private float renderScale = 1f;
    private float renderAlpha = 1f;
    private float windowOpacity = 1f;
    private float symbolFlash;
    private int shortcutNumber;

    public LevelUpChoice(Symbol symbol, FabledText title, FabledText description, Runnable upgrade) {
        this(title, description, upgrade, Assets.I().getSymbol(symbol),
            Assets.I().get(symbol.shadowKey()), Assets.I().get(symbol.whiteKey()),
            UpgradeRarity.COMMON);
    }

    public LevelUpChoice(FabledText title, FabledText description, Runnable upgrade,
                         TextureRegion texture, TextureRegion shadowTexture,
                         TextureRegion whiteTexture) {
        this(title, description, upgrade, texture, shadowTexture, whiteTexture,
            UpgradeRarity.COMMON);
    }

    public LevelUpChoice(FabledText title, FabledText description, Runnable upgrade,
                         TextureRegion texture, TextureRegion shadowTexture,
                         TextureRegion whiteTexture, UpgradeRarity rarity) {
        this(title, description, upgrade, texture, shadowTexture, whiteTexture, null, rarity);
    }

    public LevelUpChoice(FabledText title, FabledText description, Runnable upgrade,
                         boolean[][] patternMask) {
        this(title, description, upgrade, patternMask, UpgradeRarity.COMMON);
    }

    public LevelUpChoice(FabledText title, FabledText description, Runnable upgrade,
                         boolean[][] patternMask, UpgradeRarity rarity) {
        this(title, description, upgrade, null, null, null, patternMask, rarity);
    }

    private LevelUpChoice(FabledText title, FabledText description, Runnable upgrade,
                          TextureRegion texture, TextureRegion shadowTexture,
                          TextureRegion whiteTexture, boolean[][] patternMask,
                          UpgradeRarity rarity) {
        this.title = title;
        this.description = description;
        this.upgrade = upgrade;
        this.texture = texture;
        this.shadowTexture = shadowTexture;
        this.whiteTexture = whiteTexture;
        this.patternMask = patternMask == null ? null : copyMask(patternMask);
        this.rarity = rarity == null ? UpgradeRarity.COMMON : rarity;
        rarityText = label(this.rarity.toString(), 46f, 0.024f);
        title.setFloatEffects(0.005f, 0.65f);
        description.setFloatEffects(0f, 0f);
    }

    private static FabledText label(String text, float sizeRatio, float spacing) {
        FabledText result = new GeneratedFabledText(text, sizeRatio, spacing, 0.12f,
            ZIndex.SHOP_CARD);
        result.setFloatEffects(0f, 0f);
        return result;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = new Rectangle(bounds);
        renderCenterX = getCenterX();
        renderCenterY = getCenterY();
    }

    public void setEntranceDelay(float entranceDelay) {
        this.entranceDelay = Math.max(0f, entranceDelay);
    }

    public void setShortcutNumber(int shortcutNumber) {
        this.shortcutNumber = shortcutNumber;
    }

    public void setWindowOpacity(float opacity) {
        windowOpacity = MathUtils.clamp(opacity, 0f, 1f);
    }

    public boolean isReady() {
        return bounds != null && entranceProgress >= 0.70f && !selected && !dismissed;
    }

    public void setHovered(boolean hovered) {
        hovered = hovered && !selected && !dismissed;
        if (hovered && !this.hovered) AudioManager.I().playHover();
        this.hovered = hovered;
    }

    public void setLightened(boolean lightened) {
        this.lightened = lightened && !selected && !dismissed;
    }

    public boolean contains(Vector2 position) {
        return isReady() && bounds.contains(position);
    }

    public void select() {
        if (upgradeApplied) return;
        // Stat listeners replace the description's words as soon as the upgrade is
        // applied. Keep the offered before/after values on screen for the claim.
        claimedDescription = new FabledText(description.getWords().toArray(new FabledWord[0]));
        upgradeApplied = true;
        upgrade.run();
    }

    public void beginSelectedAnimation() {
        if (selected) return;
        selected = true;
        dismissed = false;
        hovered = false;
        lightened = false;
        selectionTimer = 0f;
        symbolFlash = 1f;
        AudioManager.I().playUpgradeSelected(getRarityTier());
    }

    public void beginDismissAnimation(float targetX) {
        if (selected || dismissed) return;
        dismissed = true;
        hovered = false;
        lightened = false;
        selectionTimer = 0f;
        dismissTargetX = targetX;
    }

    private void update(float delta) {
        age += delta;
        entranceProgress = MathUtils.clamp((age - entranceDelay) / ENTRANCE_DURATION, 0f, 1f);
        renderCenterX = getCenterX();
        renderCenterY = getCenterY() - (1f - easeOutCubic(entranceProgress)) * 0.30f;
        renderScale = MathUtils.lerp(0.91f, 1f, easeOutBack(entranceProgress));
        renderAlpha = smoothStep(entranceProgress);
        hoverAmount = moveTowards(hoverAmount, hovered ? 1f : 0f, delta * 8f);
        lightenAmount = moveTowards(lightenAmount, lightened ? 1f : 0f, delta * 7f);
        renderScale *= 1f + hoverAmount * 0.027f;
        renderCenterY += hoverAmount * 0.11f;

        if (selected) {
            selectionTimer += delta;
            renderCenterX = MathUtils.lerp(getCenterX(), SELECTED_CENTER_X,
                smoothStep(selectionTimer / 0.30f));
            renderScale *= calculateSelectionScale(selectionTimer);
            symbolFlash = Math.max(0f, symbolFlash - delta * 7.5f);
        }

        if (dismissed) {
            selectionTimer += delta;
            float progress = smoothStep(selectionTimer / DISMISS_DURATION);
            renderCenterX = MathUtils.lerp(getCenterX(), dismissTargetX, progress * 0.18f);
            renderCenterY -= progress * 0.22f;
            renderScale *= MathUtils.lerp(1f, 0.88f, progress);
            renderAlpha *= 1f - progress;
        }
    }

    private static float calculateSelectionScale(float timer) {
        if (timer < 0.06f) return MathUtils.lerp(1f, 0.96f, smoothStep(timer / 0.06f));
        if (timer < 0.19f) {
            return MathUtils.lerp(0.96f, 1.12f, easeOutCubic((timer - 0.06f) / 0.13f));
        }
        return MathUtils.lerp(1.12f, 1.025f, smoothStep((timer - 0.19f) / 0.22f));
    }

    public void draw(float delta) {
        if (bounds == null) return;
        update(delta);
        renderAlpha *= windowOpacity;
        if (renderAlpha <= 0.005f) return;

        Color accent = getRarityColor();
        float halfWidth = bounds.width / 2f;
        float halfHeight = bounds.height / 2f;
        float emphasis = Math.max(hoverAmount, selected ? 1f : 0f);
        drawFrame(halfWidth, halfHeight, emphasis, accent);
        drawIcon(emphasis, accent);
        drawShortcutBadge(halfWidth, halfHeight, accent);

        float textAlpha = renderAlpha * (1f - lightenAmount * 0.26f);
        prepareText(rarityText, 1.85f, textAlpha);
        tint(rarityText, new Color(accent).lerp(Color.WHITE, 0.28f));
        rarityText.setAbsoluteX(renderCenterX - rarityText.getRenderedWidth() / 2f);
        rarityText.setY(renderCenterY + (halfHeight - 0.47f) * renderScale);
        rarityText.draw(delta);

        prepareText(title, bounds.width - 0.64f, textAlpha);
        centerText(title, -0.59f);
        title.draw(delta);

        FabledText valueText = claimedDescription == null ? description : claimedDescription;
        valueText.setFloatEffects(0f, 0f);
        prepareText(valueText, bounds.width - 0.91f, textAlpha);
        if (valueText.getWords().size() == 3) {
            valueText.getWords().get(0).setColor(MUTED_TEXT);
            valueText.getWords().get(1).setColor(new Color(accent).lerp(Color.WHITE, 0.30f));
            valueText.getWords().get(2).setColor(VALUE_TEXT);
        }
        centerText(valueText, -1.37f);
        valueText.draw(delta);
    }

    private void drawFrame(float halfWidth, float halfHeight, float emphasis, Color accent) {
        float width = bounds.width;
        float height = bounds.height;
        rect(-halfWidth + 0.07f, -halfHeight - 0.10f, width, height,
            new Color(0f, 0f, 0f, 0.42f));
        if (emphasis > 0f) {
            rect(-halfWidth - 0.035f, -halfHeight - 0.035f,
                width + 0.07f, height + 0.07f, withAlpha(accent, emphasis * 0.16f));
        }
        Color edge = new Color(accent).lerp(CARD_COLOR, 0.64f - emphasis * 0.22f);
        rect(-halfWidth, -halfHeight, width, height, edge);
        rect(-halfWidth + 0.035f, -halfHeight + 0.035f,
            width - 0.07f, height - 0.07f, CARD_COLOR);
        rect(-halfWidth + 0.035f, halfHeight - 0.075f,
            width - 0.07f, 0.04f,
            withAlpha(accent, 0.52f + emphasis * 0.35f));
        rect(-halfWidth + 0.28f, -0.94f, width - 0.56f, 0.018f,
            new Color(0.40f, 0.49f, 0.52f, 0.20f));
    }

    private void drawIcon(float emphasis, Color accent) {
        float symbolScale = 1f + hoverAmount * 0.075f + symbolFlash * 0.10f;
        float size = 1.31f * renderScale * symbolScale;
        float centerY = getIconCenterY();
        if (patternMask != null) {
            drawPatternIcon(renderCenterX, centerY, size);
        } else {
            float x = renderCenterX - size / 2f;
            float y = centerY - size / 2f;
            Pencil.I().addDrawing(new TextureDrawing(shadowTexture, x, y - 0.075f * renderScale,
                size, size, ZIndex.SHOP, new Color(0f, 0f, 0f, 0.55f * renderAlpha)));
            float iconLight = 1f - lightenAmount * 0.15f;
            Pencil.I().addDrawing(new TextureDrawing(texture, x, y, size, size, ZIndex.SHOP,
                new Color(iconLight, iconLight, iconLight, renderAlpha)));
            if (symbolFlash > 0f) {
                float flashSize = size * (1f + symbolFlash * 0.08f);
                Pencil.I().addDrawing(new TextureDrawing(whiteTexture,
                    renderCenterX - flashSize / 2f, centerY - flashSize / 2f,
                    flashSize, flashSize, ZIndex.SHOP,
                    new Color(1f, 1f, 1f, symbolFlash * renderAlpha)));
            }
        }
    }

    private void drawShortcutBadge(float halfWidth, float halfHeight, Color accent) {
        if (shortcutNumber < 1 || shortcutNumber > 9 || selected || dismissed) return;
        float x = halfWidth - 0.65f;
        float y = halfHeight - 0.61f;
        rect(x, y - 0.025f, 0.38f, 0.38f, new Color(0f, 0f, 0f, 0.34f));
        rect(x, y, 0.38f, 0.38f,
            new Color(accent.r, accent.g, accent.b, 0.18f + hoverAmount * 0.18f));
        Pencil.I().addDrawing(new TextureDrawing(Assets.I().getDigitalNumber(shortcutNumber),
            renderCenterX + (x + 0.115f) * renderScale,
            renderCenterY + (y + 0.067f) * renderScale,
            0.15f * renderScale, 0.245f * renderScale, ZIndex.SHOP_CARD,
            new Color(0.81f, 0.90f, 0.90f, renderAlpha * (0.72f + hoverAmount * 0.28f))));
    }

    private void drawPatternIcon(float centerX, float centerY, float size) {
        int rows = patternMask.length;
        int columns = patternMask[0].length;
        int longestSide = Math.max(rows, columns);
        float gap = size * 0.065f;
        float cellSize = (size - gap * (longestSide - 1)) / longestSide;
        float gridWidth = columns * cellSize + (columns - 1) * gap;
        float gridHeight = rows * cellSize + (rows - 1) * gap;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float x = centerX - gridWidth / 2f + column * (cellSize + gap);
                float y = centerY + gridHeight / 2f - cellSize - row * (cellSize + gap);
                boolean active = patternMask[row][column];
                if (active) {
                    Pencil.I().addDrawing(new TextureDrawing(whitePixel, x,
                        y - 0.055f * renderScale, cellSize, cellSize, ZIndex.SHOP,
                        new Color(0f, 0f, 0f, 0.55f * renderAlpha)));
                }
                Color cellColor = active
                    ? new Color(0.83f, 0.94f, 0.90f, renderAlpha)
                    : new Color(0.30f, 0.41f, 0.43f, 0.40f * renderAlpha);
                if (active && symbolFlash > 0f) cellColor.lerp(Color.WHITE, symbolFlash);
                cellColor.a = active ? renderAlpha : renderAlpha * 0.4f;
                Pencil.I().addDrawing(new TextureDrawing(whitePixel, x, y,
                    cellSize, cellSize, ZIndex.SHOP, cellColor));
            }
        }
    }

    private void prepareText(FabledText text, float maxWidth, float opacity) {
        text.fitWithinWidth(maxWidth);
        text.setAnimationScale(renderScale);
        text.setOpacity(opacity);
    }

    private void centerText(FabledText text, float offsetY) {
        text.setAbsoluteX(renderCenterX - text.getRenderedWidth() / 2f);
        text.setY(renderCenterY + offsetY * renderScale);
    }

    private static void tint(FabledText text, Color color) {
        for (FabledWord word : text.getWords()) word.setColor(color);
    }

    private void rect(float x, float y, float width, float height, Color color) {
        Pencil.I().addDrawing(new TextureDrawing(whitePixel,
            renderCenterX + x * renderScale, renderCenterY + y * renderScale,
            width * renderScale, height * renderScale, ZIndex.SHOP,
            withAlpha(color, color.a * renderAlpha)));
    }

    private static Color withAlpha(Color color, float alpha) {
        return new Color(color.r, color.g, color.b, alpha);
    }

    private static boolean[][] copyMask(boolean[][] source) {
        if (source == null || source.length == 0 || source[0] == null || source[0].length == 0) {
            throw new IllegalArgumentException("A pattern icon needs a mask");
        }
        int columns = source[0].length;
        boolean[][] copy = new boolean[source.length][columns];
        for (int row = 0; row < source.length; row++) {
            if (source[row] == null || source[row].length != columns) {
                throw new IllegalArgumentException("Pattern masks must be rectangular");
            }
            System.arraycopy(source[row], 0, copy[row], 0, columns);
        }
        return copy;
    }

    public Color getRarityColor() {
        return new Color(Assets.I().getRarityColor(rarity));
    }

    public int getRarityTier() {
        return MathUtils.clamp(rarity.ordinal(), 0, 4);
    }

    public float getCenterX() {
        return bounds == null ? 0f : bounds.x + bounds.width / 2f;
    }

    public float getCenterY() {
        return bounds == null ? 0f : bounds.y + bounds.height / 2f;
    }

    public float getIconCenterY() {
        return renderCenterY + ICON_OFFSET_Y * renderScale;
    }

    private static float moveTowards(float current, float target, float maxDelta) {
        if (Math.abs(target - current) <= maxDelta) return target;
        return current + Math.signum(target - current) * maxDelta;
    }

    private static float smoothStep(float value) {
        value = MathUtils.clamp(value, 0f, 1f);
        return value * value * (3f - 2f * value);
    }

    private static float easeOutCubic(float value) {
        float inverse = 1f - MathUtils.clamp(value, 0f, 1f);
        return 1f - inverse * inverse * inverse;
    }

    private static float easeOutBack(float value) {
        float t = MathUtils.clamp(value, 0f, 1f) - 1f;
        return 1f + 2.70158f * t * t * t + 1.70158f * t * t;
    }
}
