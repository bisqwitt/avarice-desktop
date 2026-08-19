package com.avaricious.utility;

public enum ZIndex {
    TEXTURE_ECHO(0),
    DIGITAL_NUMBER(1),
    TEXTURE_GLOW(1),
    PATTERN_DISPLAY(2),
    DECK_UI_BOX(6),
    DECK_UI_CARD(6),
    RELIC_BAG(7),
    SYMBOL_HIT_PARTICLES(8),
    CARD_APPLY_PARTICLES(9),
    HAND_UI_CARD(10),
    SLOT_MACHINE(10),
    SLOT_MACHINE_FOREGROUND(11),
    BUTTON_BOARD(12),
    HAND_UI_CARD_DRAGGING(13),
    HAND_UI_SELECTING_CARD_TO_DISCARD(14),
    CREDIT_SCORE(15),
    SHOP(15),
    SHOP_CARD(16),
    SHOP_CARD_TOUCHING(17),
    POPUP_DEFAULT(14),
    PACK_OPENING_BACKGROUND(17),
    PACK_OPENING(18),
    PACK_OPENING_SELECTED(19),
    UNFOLDED_DECK_BACKGROUND(20),
    UNFOLDED_DECK_CARD(25),
    CROSSHAIR(26);

    private final int index;

    ZIndex(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
