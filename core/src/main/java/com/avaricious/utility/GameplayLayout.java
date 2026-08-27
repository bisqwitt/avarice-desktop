package com.avaricious.utility;

/** Shared anchors for the persistent gameplay HUD and play field. */
public final class GameplayLayout {
    public static final float WORLD_WIDTH = 16f;
    public static final float HUD_WIDTH = 3.72f;
    public static final float HUD_TO_SLOT_GAP = 1.275f;
    public static final float SLOT_WIDTH = 7.75f;

    public static final float OUTER_MARGIN =
        (WORLD_WIDTH - HUD_WIDTH - HUD_TO_SLOT_GAP - SLOT_WIDTH) / 2f;

    public static final float HUD_LEFT = OUTER_MARGIN;
    public static final float HUD_CENTER = HUD_LEFT + HUD_WIDTH / 2f;
    public static final float HUD_Y_OFFSET = -0.40f;

    public static final float SLOT_X = HUD_LEFT + HUD_WIDTH + HUD_TO_SLOT_GAP;
    public static final float SLOT_Y = 2.05f;
    public static final float SLOT_CENTER = SLOT_X + SLOT_WIDTH / 2f;

    private GameplayLayout() {
    }
}
