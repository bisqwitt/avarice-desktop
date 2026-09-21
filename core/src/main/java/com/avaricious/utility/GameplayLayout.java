package com.avaricious.utility;

/** Shared anchors for the persistent gameplay HUD and play field. */
public final class GameplayLayout {
    public static final float WORLD_WIDTH = 16f;
    public static final float SLOT_WIDTH = 7.75f;
    public static final float SLOT_X = (WORLD_WIDTH - SLOT_WIDTH) / 2f;
    public static final float SLOT_Y = 1.88f;
    public static final float SLOT_CENTER = SLOT_X + SLOT_WIDTH / 2f;

    /** Standalone cash wallet, kept away from the round-status header. */
    public static final float CASH_LEFT = 0.78f;
    public static final float CASH_WIDTH = 3.00f;
    public static final float CASH_Y = 7.48f;

    /** Legacy anchors retained for the retired QuickShop and draw-card layout. */
    public static final float HUD_LEFT = SLOT_X + 0.14f;
    public static final float HUD_WIDTH = 1.55f;
    public static final float HUD_CENTER = HUD_LEFT + HUD_WIDTH / 2f;
    public static final float HUD_Y = 7.68f;

    /** Compatibility for the retired QuickShop source, which is no longer drawn. */
    @Deprecated
    public static final float HUD_Y_OFFSET = 0f;

    private GameplayLayout() {
    }
}
