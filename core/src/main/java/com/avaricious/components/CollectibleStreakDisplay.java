package com.avaricious.components;

import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.game.progression.SkillTreeProgress;
import com.avaricious.game.progression.SkillTreeUnlock;
import com.avaricious.game.run.CollectibleStreak;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Large bottom-right HUD showing the current Hot Hand streak and cash multiplier. */
public final class CollectibleStreakDisplay {

    private static final float PANEL_X = 13.05f;
    private static final float PANEL_Y = 0.42f;
    private static final float PANEL_WIDTH = 2.50f;
    private static final float PANEL_HEIGHT = 2.28f;
    private static final float CONTENT_X = PANEL_X + 0.14f;
    private static final float TIMER_X = PANEL_X + PANEL_WIDTH - 0.29f;
    private static final float CONTENT_WIDTH = TIMER_X - CONTENT_X - 0.14f;
    private static final float TIMER_Y = PANEL_Y + 0.18f;
    private static final float TIMER_WIDTH = 0.12f;
    private static final float TIMER_HEIGHT = PANEL_HEIGHT - 0.36f;
    private static final Color MUTED = new Color(0.63f, 0.71f, 0.76f, 1f);
    private static final Color GOLD = new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color PANEL_COLOR =
        new Color(0.025f, 0.043f, 0.055f, 1f);

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final GeneratedFabledText streakLabel = label("HOT STREAK", 38f);
    private final GeneratedFabledText valueLabel = label("VALUE X", 49f);
    private final DigitalNumber streakNumber = new DigitalNumber(
        0f,
        GOLD,
        new Rectangle(0f, PANEL_Y + 1.10f, 0.34f, 0.58f),
        0.42f
    ).setZIndex(ZIndex.BUTTON_BOARD);
    private final DigitalNumber multiplierNumber = new DigitalNumber(
        1f,
        GOLD,
        new Rectangle(0f, PANEL_Y + 0.25f, 0.24f, 0.40f),
        0.30f
    ).setAsDecimal().setZIndex(ZIndex.BUTTON_BOARD);

    private final CollectibleStreak streak;
    private final SkillTreeProgress skillTree;
    private int displayedCount = Integer.MIN_VALUE;
    private float displayedMultiplier = Float.NaN;

    public CollectibleStreakDisplay(
        CollectibleStreak streak,
        SkillTreeProgress skillTree
    ) {
        this.streak = streak;
        this.skillTree = skillTree;
        prepareLabel(streakLabel, PANEL_Y + 1.92f);
        prepareLabel(valueLabel, PANEL_Y + 0.74f);
        streakNumber.getIdleScaleEffect().setAllowed(false);
        multiplierNumber.getIdleScaleEffect().setAllowed(false);
    }

    public void draw(float delta) {
        if (!skillTree.isUnlocked(SkillTreeUnlock.HOT_HAND)) return;

        updateNumbers();
        float urgency = 1f - streak.getTimeRemainingProgress();
        Color activeColor = streak.getCount() == 0
            ? MUTED
            : urgency > 0.72f ? Assets.I().healthRedColor() : GOLD;
        streakNumber.setColor(activeColor);
        multiplierNumber.setColor(activeColor);

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            PANEL_X + 0.06f,
            PANEL_Y - 0.07f,
            PANEL_WIDTH,
            PANEL_HEIGHT,
            ZIndex.BUTTON_BOARD,
            new Color(0f, 0f, 0f, 0.48f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            PANEL_X,
            PANEL_Y,
            PANEL_WIDTH,
            PANEL_HEIGHT,
            ZIndex.BUTTON_BOARD,
            new Color(PANEL_COLOR.r, PANEL_COLOR.g, PANEL_COLOR.b, 0.78f)
        ));

        drawDivider();
        drawTimer(activeColor);
        streakLabel.draw(delta);
        valueLabel.draw(delta);
        streakNumber.draw(delta);
        multiplierNumber.draw(delta);
    }

    public Rectangle getCollisionBounds() {
        if (!skillTree.isUnlocked(SkillTreeUnlock.HOT_HAND)) return null;

        float padding = 0.06f;
        return new Rectangle(
            PANEL_X - padding,
            PANEL_Y - padding,
            PANEL_WIDTH + padding * 2f,
            PANEL_HEIGHT + padding * 2f
        );
    }

    private void updateNumbers() {
        int count = streak.getCount();
        float multiplier = streak.getValueMultiplier(
            skillTree.getStreakValueStep(),
            skillTree.getStreakValueLimit()
        );
        if (displayedCount != count) {
            displayedCount = count;
            streakNumber.setValue(count);
        }
        if (Float.compare(displayedMultiplier, multiplier) != 0) {
            displayedMultiplier = multiplier;
            multiplierNumber.setValue(multiplier);
        }

        center(streakNumber);
        center(multiplierNumber);
    }

    private void drawTimer(Color color) {
        float progress = streak.getTimeRemainingProgress();
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            TIMER_X - 0.035f,
            TIMER_Y - 0.035f,
            TIMER_WIDTH + 0.07f,
            TIMER_HEIGHT + 0.07f,
            ZIndex.BUTTON_BOARD,
            new Color(0f, 0f, 0f, 0.52f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            TIMER_X,
            TIMER_Y,
            TIMER_WIDTH,
            TIMER_HEIGHT,
            ZIndex.BUTTON_BOARD,
            new Color(0.30f, 0.35f, 0.38f, 0.42f)
        ));
        if (progress <= 0f) return;

        float fillHeight = TIMER_HEIGHT * progress;
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            TIMER_X,
            TIMER_Y,
            TIMER_WIDTH,
            fillHeight,
            ZIndex.BUTTON_BOARD,
            new Color(color.r, color.g, color.b, 0.94f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            TIMER_X - 0.025f,
            TIMER_Y + fillHeight - 0.022f,
            TIMER_WIDTH + 0.05f,
            0.044f,
            ZIndex.BUTTON_BOARD,
            new Color(color.r, color.g, color.b, 1f)
        ));
    }

    private void drawDivider() {
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            CONTENT_X + 0.05f,
            PANEL_Y + 0.92f,
            CONTENT_WIDTH - 0.10f,
            0.018f,
            ZIndex.BUTTON_BOARD,
            new Color(0.48f, 0.56f, 0.60f, 0.22f)
        ));
    }

    private void center(DigitalNumber number) {
        number.getFirstDigitBounds().x = CONTENT_X
            + (CONTENT_WIDTH - number.getWidth()) / 2f;
    }

    private void prepareLabel(GeneratedFabledText text, float y) {
        text.fitWithinWidth(CONTENT_WIDTH - 0.10f);
        text.setAbsoluteX(CONTENT_X
            + (CONTENT_WIDTH - text.getRenderedWidth()) / 2f);
        text.setY(y);
    }

    private static GeneratedFabledText label(String value, float sizeRatio) {
        GeneratedFabledText result = new GeneratedFabledText(
            value,
            sizeRatio,
            0.018f,
            0.10f,
            ZIndex.BUTTON_BOARD
        );
        result.setFloatEffects(0f, 0f);
        result.getWords().forEach(word -> word.setColor(MUTED));
        return result;
    }
}
