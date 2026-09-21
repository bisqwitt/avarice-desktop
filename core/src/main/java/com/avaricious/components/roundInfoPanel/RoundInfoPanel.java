package com.avaricious.components.roundInfoPanel;

import com.avaricious.CreditNumber;
import com.avaricious.RoundsManager;
import com.avaricious.components.DigitalNumber;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.RunManager;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Compact HUD for the current round target and countdown. */
public class RoundInfoPanel {

    private static RoundInfoPanel instance;

    public static RoundInfoPanel I() {
        return instance == null ? instance = new RoundInfoPanel() : instance;
    }

    private static final float PANEL_X = GameplayLayout.SLOT_X;
    private static final float PANEL_Y = GameplayLayout.HUD_Y;
    private static final float PANEL_WIDTH = GameplayLayout.SLOT_WIDTH;
    private static final float PANEL_HEIGHT = 0.84f;

    private static final float ROUND_LEFT = PANEL_X + 0.14f;
    private static final float ROUND_WIDTH = 1.64f;
    private static final float TARGET_LEFT = PANEL_X + 2.04f;
    private static final float TARGET_WIDTH = 3.30f;
    private static final float TIME_LEFT = PANEL_X + 5.66f;
    private static final float TIME_WIDTH = 1.95f;

    private static final Color MUTED = new Color(0.63f, 0.71f, 0.76f, 1f);
    private static final Color PANEL_COLOR = new Color(0.025f, 0.043f, 0.055f, 1f);

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final GeneratedFabledText roundLabel = label("ROUND");
    private final GeneratedFabledText targetLabel = label("NEXT BILL");
    private final GeneratedFabledText timeLabel = label("TIME");

    private final DigitalNumber roundNumber = new DigitalNumber(
        1, Assets.I().lightColor(),
        new Rectangle(0f, PANEL_Y + 0.12f, 0.18f, 0.29f), 0.22f
    ).setZIndex(ZIndex.BUTTON_BOARD);
    private final CreditNumber targetRemaining = new CreditNumber(
        0f, new Rectangle(0f, PANEL_Y + 0.10f, 0.18f, 0.29f), 0.22f
    ).setZIndex(ZIndex.BUTTON_BOARD);
    private final DigitalNumber timeRemaining = new DigitalNumber(
        (int) RoundTimer.ROUND_DURATION_SECONDS, Assets.I().lightColor(),
        new Rectangle(0f, PANEL_Y + 0.12f, 0.18f, 0.29f), 0.22f
    ).setZIndex(ZIndex.BUTTON_BOARD);

    private int displayedRound = Integer.MIN_VALUE;
    private int displayedTime = Integer.MIN_VALUE;
    private float displayedTarget = Float.NaN;

    private RoundInfoPanel() {
        positionLabel(roundLabel, ROUND_LEFT, ROUND_WIDTH);
        positionLabel(targetLabel, TARGET_LEFT, TARGET_WIDTH);
        positionLabel(timeLabel, TIME_LEFT, TIME_WIDTH);
        roundNumber.getIdleScaleEffect().setAllowed(false);
        targetRemaining.getIdleScaleEffect().setAllowed(false);
        timeRemaining.getIdleScaleEffect().setAllowed(false);
    }

    private static GeneratedFabledText label(String text) {
        GeneratedFabledText result = new GeneratedFabledText(
            text, 51f, 0.018f, 0.10f, ZIndex.BUTTON_BOARD
        );
        result.setFloatEffects(0f, 0f);
        result.getWords().forEach(word -> word.setColor(MUTED));
        return result;
    }

    private static void positionLabel(
        GeneratedFabledText label,
        float left,
        float width
    ) {
        label.fitWithinWidth(width);
        label.setAbsoluteX(left + (width - label.getRenderedWidth()) / 2f);
        label.setY(PANEL_Y + 0.56f);
    }

    public void draw(float delta) {
        updateValues();

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

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            PANEL_X,
            PANEL_Y + PANEL_HEIGHT - 0.035f,
            PANEL_WIDTH,
            0.035f,
            ZIndex.BUTTON_BOARD,
            new Color(1f, 0.82f, 0.44f, 0.42f)
        ));

        drawDivider(PANEL_X + 1.90f);
        drawDivider(PANEL_X + 5.50f);

        roundLabel.draw(delta);
        targetLabel.draw(delta);
        timeLabel.draw(delta);
        roundNumber.draw(delta);
        targetRemaining.draw(delta);
        timeRemaining.draw(delta);
    }

    private void updateValues() {
        RoundsManager rounds = RunManager.I().getRoundsManager();
        int round = rounds.getCurrentRound();
        int seconds = rounds.getSecondsRemaining();
        float target = rounds.getRoundTarget();

        if (displayedRound != round) {
            displayedRound = round;
            roundNumber.setValue(round);
        }
        if (displayedTime != seconds) {
            displayedTime = seconds;
            timeRemaining.setValue(seconds);
        }
        if (Float.compare(displayedTarget, target) != 0) {
            displayedTarget = target;
            targetRemaining.setValue(target);
        }

        timeRemaining.setColor(seconds <= 10
            ? Assets.I().healthRedColor()
            : Assets.I().lightColor());

        centerNumber(roundNumber, ROUND_LEFT, ROUND_WIDTH);
        centerNumber(targetRemaining, TARGET_LEFT, TARGET_WIDTH);
        centerNumber(timeRemaining, TIME_LEFT, TIME_WIDTH);
    }

    private void centerNumber(DigitalNumber number, float left, float width) {
        number.getFirstDigitBounds().x = left + (width - number.getWidth()) / 2f;
    }

    public Rectangle getCollisionBounds() {
        float padding = 0.06f;
        return new Rectangle(
            PANEL_X - padding,
            PANEL_Y - padding,
            PANEL_WIDTH + padding * 2f,
            PANEL_HEIGHT + padding * 2f
        );
    }

    private void drawDivider(float x) {
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            x,
            PANEL_Y + 0.12f,
            0.018f,
            PANEL_HEIGHT - 0.24f,
            ZIndex.BUTTON_BOARD,
            new Color(0.48f, 0.56f, 0.60f, 0.22f)
        ));
    }
}
