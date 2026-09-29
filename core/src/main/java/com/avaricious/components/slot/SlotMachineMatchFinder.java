package com.avaricious.components.slot;

import com.avaricious.components.slot.pattern.PatternFinder;
import com.avaricious.components.slot.pattern.PatternMatch;
import com.avaricious.components.slot.rework.SpinResult;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class SlotMachineMatchFinder {

    private SlotMachineMatchFinder() {
    }

    public static List<PatternMatch> findMatches() {
        return findMatches(new SpinResult(getCurrentSymbolMap()));
    }

    public static List<PatternMatch> findMatches(SpinResult result) {
        List<PatternMatch> matches = PatternFinder.findMatches(result.symbols());

        sortMatches(matches);
        return matches;
    }

    /**
     * Kept for callers that still use the old name. Outcome policies now run
     * before animation and this method never rewrites a landed reel.
     */
    public static List<PatternMatch> findMatchesAfterSpin() {
        return findMatches(SlotMachine.I().getCurrentSpinResult());
    }

    private static void sortMatches(List<PatternMatch> matches) {

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
    }

    public static PatternMatch findSymbol(Symbol targetSymbol) {
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

    private static Symbol[][] getCurrentSymbolMap() {
        return SlotMachine.I().getSymbolMap();
    }

    private static boolean inGrid(int x, int y) {
        return x >= 0 && x < SlotMachine.colCount
            && y >= 0 && y < SlotMachine.rowCount;
    }

}
