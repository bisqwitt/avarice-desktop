package com.avaricious.components.slot.pattern;

public enum UnlockablePattern {

    BLOCK(
        "BLOCK PATTERN",
        new boolean[][] {
            {true, true},
            {true, true}
        }
    ),

    PLUS(
        "PLUS PATTERN",
        new boolean[][] {
            {false, true, false},
            {true, true, true},
            {false, true, false}
        }
    ),

    X(
        "X PATTERN",
        new boolean[][] {
            {true, false, true},
            {false, true, false},
            {true, false, true}
        }
    );

    private final String displayName;
    private final boolean[][] mask;

    UnlockablePattern(String displayName, boolean[][] mask) {
        this.displayName = displayName;
        this.mask = mask;
    }

    public String displayName() {
        return displayName;
    }

    public boolean[][] mask() {
        return mask;
    }
}
