package com.avaricious.effects.particle;

public enum PatternType {

    HOR (),
    VERT (),
    DIAG (),
    HOR_L (),
    HOR_XL (),
    UP (new boolean[][]{
        {false, false, true, false, false},
        {false, true, false, true, false},
        {true, false, false, false, true}
    }),
    DOWN (new boolean[][]{
        {true, false, false, false, true},
        {false, true, false, true, false},
        {false, false, true, false, false}
    }),
    EYE (new boolean[][]{
        {false, true, true, true, false},
        {true, false, false, false, true},
        {false, true, true, true, false}
    }),
    JACKPOT ();

    private final boolean[][] mask;

    PatternType() {
        this(null);
    }

    PatternType(boolean[][] mask) {
        this.mask = mask;
    }

    public String displayName() {
        return name().replace('_', '-');
    }

    public boolean[][] mask() {
        return mask;
    }
}
