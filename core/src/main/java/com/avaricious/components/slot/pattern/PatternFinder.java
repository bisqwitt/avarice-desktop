package com.avaricious.components.slot.pattern;

import com.avaricious.components.slot.Symbol;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternFinder {

    /**
     * Finds all base lines of length >= 3 plus unlocked shape matches.
     * Assumes symbolMap[x][y] => x = column, y = row.
     */
    public static List<PatternMatch> findMatches(Symbol[][] symbolMap) {
        List<PatternMatch> matches = new ArrayList<>();

        int cols = symbolMap.length;
        if (cols == 0) return matches;
        int rows = symbolMap[0].length;

        // Directions: right, down, down-right, up-right
        int[][] dirs = {
            {1, 0}, // horizontal
            {0, 1}, // vertical
            {1, 1}, // diagonal down
            {1, -1}  // diagonal up
        };

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Symbol symbol = symbolMap[x][y];
                if (symbol == null) continue; // skip empty

                for (int d = 0; d < dirs.length; d++) {
                    int dx = dirs[d][0];
                    int dy = dirs[d][1];

                    // Only start a run if the previous cell in this direction
                    // is out of bounds or a different symbol.
                    int prevX = x - dx;
                    int prevY = y - dy;
                    if (inBounds(prevX, prevY, cols, rows) &&
                        equalsSymbol(symbolMap[prevX][prevY], symbol)) {
                        continue; // this run was/will be counted from an earlier cell
                    }

                    // Walk forward and count how many same symbols we have
                    List<Vector2> positions = new ArrayList<>();
                    int cx = x;
                    int cy = y;

                    while (inBounds(cx, cy, cols, rows) &&
                        equalsSymbol(symbolMap[cx][cy], symbol)) {
                        positions.add(new Vector2((float) cx, (float) cy));
                        cx += dx;
                        cy += dy;
                    }

                    int length = positions.size();
                    if (length >= 3) {
                        if (positions.get(0).y > positions.get(1).y) Collections.reverse(positions);

                        matches.add(new PatternMatch(symbol, length, positions));
                    }
                }
            }
        }

        matches.addAll(PatternUnlocks.I().findMatches(symbolMap));

        return matches;
    }

    private static boolean inBounds(int x, int y, int cols, int rows) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    private static boolean equalsSymbol(Symbol a, Symbol b) {
        if (a == null || b == null) return false;
        return a == b;
    }
}
