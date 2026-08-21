package com.avaricious.components.slot.pattern;

import com.avaricious.components.slot.Symbol;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Run progression for non-linear slot patterns. */
public final class PatternUnlocks {

    private static PatternUnlocks instance;

    public static PatternUnlocks I() {
        return instance == null ? instance = new PatternUnlocks() : instance;
    }

    private final EnumSet<UnlockablePattern> unlocked =
        EnumSet.noneOf(UnlockablePattern.class);

    private PatternUnlocks() {
    }

    public void unlock(UnlockablePattern pattern) {
        if (pattern != null) unlocked.add(pattern);
    }

    public boolean isUnlocked(UnlockablePattern pattern) {
        return unlocked.contains(pattern);
    }

    public List<UnlockablePattern> getLockedPatterns() {
        List<UnlockablePattern> locked = new ArrayList<>();
        for (UnlockablePattern pattern : UnlockablePattern.values()) {
            if (!isUnlocked(pattern)) locked.add(pattern);
        }
        return locked;
    }

    public List<PatternMatch> findMatches(Symbol[][] symbols) {
        List<PatternMatch> matches = new ArrayList<>();

        for (UnlockablePattern pattern : unlocked) {
            findMatches(symbols, pattern, matches);
        }

        return matches;
    }

    private void findMatches(
        Symbol[][] symbols,
        UnlockablePattern pattern,
        List<PatternMatch> matches
    ) {
        boolean[][] mask = pattern.mask();
        int gridColumns = symbols.length;
        int gridRows = symbols[0].length;
        int maskRows = mask.length;
        int maskColumns = mask[0].length;

        for (int startX = 0; startX <= gridColumns - maskColumns; startX++) {
            for (int startY = 0; startY <= gridRows - maskRows; startY++) {
                addMatchAt(symbols, pattern, startX, startY, matches);
            }
        }
    }

    private void addMatchAt(
        Symbol[][] symbols,
        UnlockablePattern pattern,
        int startX,
        int startY,
        List<PatternMatch> matches
    ) {
        boolean[][] mask = pattern.mask();
        Symbol target = null;
        List<Vector2> positions = new ArrayList<>();

        for (int maskY = 0; maskY < mask.length; maskY++) {
            for (int maskX = 0; maskX < mask[maskY].length; maskX++) {
                if (!mask[maskY][maskX]) continue;

                Symbol symbol = symbols[startX + maskX][startY + maskY];
                if (symbol == null || (target != null && symbol != target)) {
                    return;
                }

                target = symbol;
                positions.add(new Vector2(startX + maskX, startY + maskY));
            }
        }

        if (target != null) {
            matches.add(new PatternMatch(
                target,
                positions.size(),
                positions,
                pattern
            ));
        }
    }
}
