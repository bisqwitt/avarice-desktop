package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;
import com.avaricious.components.slot.pattern.PatternFinder;
import com.avaricious.utility.SeededRandomizer;

/**
 * Applies game-facing outcome rules while a spin still exists only as data.
 * The visible reels are never rewritten after they land.
 */
public class SpinResultPolicy {

    private static final float EMPTY_RESULT_RESCUE_CHANCE = 0.65f;
    private static final int MAX_CONSECUTIVE_EMPTY_SPINS = 1;
    private static final float NEAR_MISS_CHANCE = 0.20f;
    private static final int NEAR_MISS_ATTEMPTS = 32;

    private int consecutiveEmptySpins;
    private float rescueChanceBonus;

    public SpinResult adjust(SpinResult generatedResult) {
        SpinResult result = generatedResult.copy();
        if (hasMatch(result)) return result;

        boolean rescueResult =
            consecutiveEmptySpins >= MAX_CONSECUTIVE_EMPTY_SPINS ||
                SeededRandomizer.get().nextFloat() < getRescueChance();

        if (rescueResult) {
            createHorizontalMatch(result);
        } else if (SeededRandomizer.get().nextFloat() < NEAR_MISS_CHANCE) {
            createNearMiss(result);
        }

        return result;
    }

    /**
     * Records the final result after custom manipulators have run.
     */
    public void record(SpinResult finalResult) {
        if (hasMatch(finalResult)) {
            consecutiveEmptySpins = 0;
        } else {
            consecutiveEmptySpins++;
        }
    }

    private void createHorizontalMatch(SpinResult result) {
        int row = SeededRandomizer.nextInt(0, result.rows() - 1);
        int startColumn = SeededRandomizer.nextInt(0, result.cols() - 3);
        Symbol target = result.get(startColumn, row);

        for (int column = startColumn; column < startColumn + 3; column++) {
            result.set(column, row, target);
        }
    }

    /**
     * Places the third matching symbol one row away from a horizontal pair.
     * Candidates are accepted only if the whole board remains a losing board.
     */
    private void createNearMiss(SpinResult result) {
        if (result.cols() < 3 || result.rows() < 2) return;

        for (int attempt = 0; attempt < NEAR_MISS_ATTEMPTS; attempt++) {
            SpinResult candidate = result.copy();
            int startColumn = SeededRandomizer.nextInt(0, result.cols() - 3);
            int row = SeededRandomizer.nextInt(0, result.rows() - 1);
            int adjacentRow = chooseAdjacentRow(row, result.rows());
            Symbol target = candidate.get(startColumn, row);

            candidate.set(startColumn + 1, row, target);
            candidate.set(startColumn + 2, adjacentRow, target);
            candidate.set(
                startColumn + 2,
                row,
                differentSymbol(target)
            );

            if (!hasMatch(candidate)) {
                result.copyFrom(candidate);
                return;
            }
        }
    }

    private int chooseAdjacentRow(int row, int rows) {
        if (row == 0) return 1;
        if (row == rows - 1) return rows - 2;
        return SeededRandomizer.get().nextBoolean() ? row - 1 : row + 1;
    }

    private Symbol differentSymbol(Symbol target) {
        Symbol[] symbols = Symbol.values();
        int offset = SeededRandomizer.nextInt(1, symbols.length - 1);
        return symbols[(target.ordinal() + offset) % symbols.length];
    }

    private boolean hasMatch(SpinResult result) {
        return !PatternFinder.findMatches(result.symbols()).isEmpty();
    }

    public void setRescueChanceBonus(float rescueChanceBonus) {
        this.rescueChanceBonus = Math.max(
            0f,
            Math.min(1f - EMPTY_RESULT_RESCUE_CHANCE, rescueChanceBonus)
        );
    }

    public float getRescueChance() {
        return EMPTY_RESULT_RESCUE_CHANCE + rescueChanceBonus;
    }
}
