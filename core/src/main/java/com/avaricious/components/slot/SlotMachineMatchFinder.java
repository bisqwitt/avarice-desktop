package com.avaricious.components.slot;

import com.avaricious.components.slot.pattern.PatternFinder;
import com.avaricious.components.slot.pattern.PatternMatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SlotMachineMatchFinder {

    private static SlotMachineMatchFinder instance;

    public static SlotMachineMatchFinder I() {
        return instance == null ? instance = new SlotMachineMatchFinder() : instance;
    }

    private final int cols;
    private final int rows;

    private SlotMachineMatchFinder() {
        this.cols = SlotMachine.colCount;
        this.rows = SlotMachine.rowCount;
    }

    public List<PatternMatch> findMatches() {
        Symbol[][] symbolMap = getCurrentSymbolMap();
        List<PatternMatch> matches = PatternFinder.findMatches(symbolMap);

        Collections.sort(matches, new Comparator<PatternMatch>() {
            @Override
            public int compare(PatternMatch a, PatternMatch b) {
                int ai = a.getSymbol().ordinal();
                int bi = b.getSymbol().ordinal();

                if (ai < bi) return -1;
                if (ai > bi) return 1;
                return 0;
            }
        });
        return matches;
    }

    public PatternMatch findSymbol(Symbol targetSymbol) {
        Symbol[][] symbolMap = getCurrentSymbolMap();
        List<Vector2> positions = new ArrayList<>();
        for (int col = 0; col < symbolMap.length; col++) {
            for (int row = 0; row < symbolMap[col].length; row++) {
                Symbol symbol = symbolMap[col][row];
                if (!inGrid(col, row) || symbol != targetSymbol) continue;
                positions.add(new Vector2(col, row));
            }
        }

        Collections.sort(positions, new Comparator<Vector2>() {
            @Override
            public int compare(Vector2 o1, Vector2 o2) {
                int rowCompare = Integer.compare((int) o1.y, (int) o2.y);
                if (rowCompare != 0) return rowCompare;

                return Integer.compare((int) o1.x, (int) o2.x);
            }
        });
        return new PatternMatch(targetSymbol, positions.size(), positions);
    }

    private Symbol[][] getCurrentSymbolMap() {
        Symbol[][] symbolMap = new Symbol[cols][rows];
        List<Reel> reels = SlotMachine.I().getReels();

        for (int c = 0; c < reels.size(); c++) {
            for (int row = 0; row < rows; row++) {
                symbolMap[c][row] = reels.get(c).symbolAtRow(row);
            }
        }
        return symbolMap;
    }

    private boolean inGrid(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

}
