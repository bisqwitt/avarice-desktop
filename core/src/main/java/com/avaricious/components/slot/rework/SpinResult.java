package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;

public class SpinResult {

    private final Symbol[][] symbols;

    public SpinResult(Symbol[][] symbols) {
        if (symbols == null || symbols.length == 0) {
            throw new IllegalArgumentException("A spin result needs at least one column");
        }

        int rows = symbols[0] == null ? 0 : symbols[0].length;
        if (rows == 0) {
            throw new IllegalArgumentException("A spin result needs at least one row");
        }

        this.symbols = new Symbol[symbols.length][rows];
        for (int col = 0; col < symbols.length; col++) {
            if (symbols[col] == null || symbols[col].length != rows) {
                throw new IllegalArgumentException("Every spin-result column must have the same row count");
            }

            for (int row = 0; row < rows; row++) {
                set(col, row, symbols[col][row]);
            }
        }
    }

    public Symbol get(int col, int row) {
        return symbols[col][row];
    }

    public void set(int col, int row, Symbol symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Spin-result symbols cannot be null");
        }
        symbols[col][row] = symbol;
    }

    public int cols() {
        return symbols.length;
    }

    public int rows() {
        return symbols[0].length;
    }

    public Symbol[] column(int col) {
        Symbol[] column = new Symbol[rows()];
        System.arraycopy(symbols[col], 0, column, 0, rows());
        return column;
    }

    public SpinResult copy() {
        return new SpinResult(symbols);
    }

    public void copyFrom(SpinResult other) {
        requireDimensions(other.cols(), other.rows());
        for (int col = 0; col < cols(); col++) {
            for (int row = 0; row < rows(); row++) {
                set(col, row, other.get(col, row));
            }
        }
    }

    public void requireDimensions(int expectedCols, int expectedRows) {
        if (cols() != expectedCols || rows() != expectedRows) {
            throw new IllegalArgumentException(
                "Expected a " + expectedCols + "x" + expectedRows +
                    " spin result, got " + cols() + "x" + rows()
            );
        }
    }

    public Symbol[][] symbols() {
        Symbol[][] copy = new Symbol[cols()][rows()];
        for (int col = 0; col < cols(); col++) {
            System.arraycopy(symbols[col], 0, copy[col], 0, rows());
        }
        return copy;
    }
}
