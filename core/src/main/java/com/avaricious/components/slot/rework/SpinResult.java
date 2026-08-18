package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;

public class SpinResult {

    private final Symbol[][] symbols;

    public SpinResult(Symbol[][] symbols) {
        this.symbols = symbols;
    }

    public Symbol get(int col, int row) {
        return symbols[col][row];
    }

    public Symbol[][] symbols() {
        return symbols;
    }
}
