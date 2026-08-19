package com.avaricious.components;

import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.avaricious.utility.*;
import com.badlogic.gdx.graphics.Color;

public class XpBar {

    private static XpBar instance;

    public static XpBar I() {
        return instance == null ? instance = new XpBar() : instance;
    }

    private static final float X = 0f;
    private static final float Y = 8.82f;

    private static final float WIDTH = 16f;
    private static final float HEIGHT = 0.18f;

    private static final Color BACKGROUND_COLOR =
        new Color(0.12f, 0.12f, 0.15f, 1f);

    private static final Color XP_COLOR =
        new Color(0.58f, 0.32f, 0.95f, 1f);

    private int level = 1;
    private int xp = 0;
    private int xpRequired = 50;

    private XpBar() {
    }

    public void draw() {
        float progress = Math.min(
            1f,
            (float) xp / xpRequired
        );

        /*
         * Background
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                Assets.I().get(AssetKey.WHITE_PIXEL),
                X,
                Y,
                WIDTH,
                HEIGHT,
                ZIndex.SHOP,
                BACKGROUND_COLOR
            )
        );

        /*
         * XP fill
         */
        Pencil.I().addDrawing(
            new TextureDrawing(
                Assets.I().get(AssetKey.WHITE_PIXEL),
                X,
                Y,
                WIDTH * progress,
                HEIGHT,
                ZIndex.SHOP,
                XP_COLOR
            )
        );
    }

    public void addXp(int amount) {
        xp += amount;

        while (xp >= xpRequired) {
            xp -= xpRequired;
            levelUp();
        }
    }

    private void levelUp() {
        level++;

        xpRequired = calculateXpRequired(level);

        ScreenManager.I().getScreen(SlotScreen.class).getLevelUpWindow().show();
    }

    private int calculateXpRequired(int level) {
        return 50 + (level - 1) * 5;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getXpRequired() {
        return xpRequired;
    }

    public float getProgress() {
        return (float) xp / xpRequired;
    }
}
