package com.avaricious.components;

import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.game.progression.SkillTreeProgress;
import com.avaricious.game.progression.SlotMachineModel;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Compact model plate that makes machine progression visible during play. */
public final class SlotMachineModelDisplay {

    private static final float WIDTH = 6.50f;
    private static final float HEIGHT = 0.42f;
    private static final float X = GameplayLayout.SLOT_CENTER - WIDTH / 2f;
    private static final float Y = 1.40f;
    private static final Color MUTED = new Color(0.63f, 0.71f, 0.76f, 1f);

    private final SkillTreeProgress skillTree;
    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);

    private SlotMachineModel displayedModel;
    private GeneratedFabledText name;
    private GeneratedFabledText stats;
    private Color accent = new Color(Color.WHITE);

    public SlotMachineModelDisplay(SkillTreeProgress skillTree) {
        this.skillTree = skillTree;
        rebuildLabels();
    }

    public void draw(float delta) {
        if (displayedModel != skillTree.getCurrentSlotMachineModel()) {
            rebuildLabels();
        }

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X + 0.05f,
            Y - 0.055f,
            WIDTH,
            HEIGHT,
            ZIndex.BUTTON_BOARD,
            new Color(0f, 0f, 0f, 0.46f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X,
            Y,
            WIDTH,
            HEIGHT,
            ZIndex.BUTTON_BOARD,
            new Color(0.025f, 0.043f, 0.055f, 0.90f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X,
            Y + HEIGHT - 0.035f,
            WIDTH,
            0.035f,
            ZIndex.BUTTON_BOARD,
            accent
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X + 3.15f,
            Y + 0.08f,
            0.018f,
            HEIGHT - 0.16f,
            ZIndex.BUTTON_BOARD,
            new Color(0.48f, 0.56f, 0.60f, 0.26f)
        ));

        name.draw(delta);
        stats.draw(delta);
    }

    public Rectangle getCollisionBounds() {
        return new Rectangle(X, Y, WIDTH, HEIGHT);
    }

    private void rebuildLabels() {
        displayedModel = skillTree.getCurrentSlotMachineModel();
        accent = new Color(
            displayedModel.accentRed(),
            displayedModel.accentGreen(),
            displayedModel.accentBlue(),
            1f
        );

        name = label(displayedModel.displayName(), 45f, accent);
        name.fitWithinWidth(2.82f);
        name.setAbsoluteX(X + 0.16f);
        name.setY(Y + 0.14f);

        int speed = displayedModel.displaySpeedPercent();
        String speedText = speed == 0
            ? "INSTANT"
            : "SPEED " + speed;
        stats = label(
            speedText + "  LUCK " + displayedModel.luckBonusPercent(),
            53f,
            MUTED
        );
        stats.fitWithinWidth(2.98f);
        stats.setAbsoluteX(X + 3.34f);
        stats.setY(Y + 0.14f);
    }

    private static GeneratedFabledText label(
        String value,
        float size,
        Color color
    ) {
        GeneratedFabledText result = new GeneratedFabledText(
            value,
            size,
            0.015f,
            0.08f,
            ZIndex.BUTTON_BOARD,
            true
        );
        result.setFloatEffects(0f, 0f);
        result.getWords().forEach(word -> word.setColor(color));
        return result;
    }
}
