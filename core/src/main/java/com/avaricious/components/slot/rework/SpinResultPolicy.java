package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;
import com.avaricious.components.slot.pattern.PatternFinder;
import com.avaricious.utility.SeededRandomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Builds a deliberate result curve for normal spins. Luck controls whether a
 * spin hits, how many patterns it contains, and how long those patterns are.
 */
public class SpinResultPolicy {

    private static final float MAX_LUCK_BONUS = 0.35f;
    private static final float BASE_EMPTY_CHANCE = 0.32f;
    private static final float MAX_LUCK_EMPTY_CHANCE = 0.02f;
    private static final int MAX_CONSECUTIVE_EMPTY_SPINS = 1;
    private static final int MAX_PATTERNS_PER_SPIN = 5;
    private static final int RESULT_BUILD_ATTEMPTS = 160;
    private static final int MATCHLESS_BUILD_ATTEMPTS = 64;
    private static final float NEAR_MISS_CHANCE = 0.20f;
    private static final int NEAR_MISS_ATTEMPTS = 32;
    private static final float[] BASE_EXTRA_PATTERN_CHANCES = {
        0.08f, 0.01f, 0f, 0f
    };
    private static final float[] MAX_LUCK_EXTRA_PATTERN_CHANCES = {
        0.96f, 0.82f, 0.62f, 0.38f
    };

    private int consecutiveEmptySpins;
    private float luckBonus;

    public SpinResult adjust(SpinResult generatedResult) {
        int columns = generatedResult.cols();
        int rows = generatedResult.rows();
        float luck = getLuckProgress();
        boolean mayBeEmpty = consecutiveEmptySpins < MAX_CONSECUTIVE_EMPTY_SPINS;
        boolean emptyResult = mayBeEmpty &&
            SeededRandomizer.get().nextFloat() < lerp(
                BASE_EMPTY_CHANCE,
                MAX_LUCK_EMPTY_CHANCE,
                luck
            );

        if (emptyResult) {
            SpinResult result = createMatchlessResult(columns, rows);
            if (SeededRandomizer.get().nextFloat() < NEAR_MISS_CHANCE) {
                createNearMiss(result);
            }
            return result;
        }

        int patternCount = rollPatternCount(columns, rows, luck);
        List<Integer> patternLengths = new ArrayList<>();
        for (int pattern = 0; pattern < patternCount; pattern++) {
            patternLengths.add(rollPatternLength(columns, rows, luck));
        }

        return buildControlledResult(columns, rows, patternLengths);
    }

    /** Records the final result after custom manipulators have run. */
    public void record(SpinResult finalResult) {
        if (hasMatch(finalResult)) {
            consecutiveEmptySpins = 0;
        } else {
            consecutiveEmptySpins++;
        }
    }

    private SpinResult buildControlledResult(
        int columns,
        int rows,
        List<Integer> patternLengths
    ) {
        SpinResult closestResult = null;
        int closestDistance = Integer.MAX_VALUE;

        for (int attempt = 0; attempt < RESULT_BUILD_ATTEMPTS; attempt++) {
            SpinResult candidate = createMatchlessResult(columns, rows);
            plantParallelPatterns(candidate, patternLengths);

            int matchCount = PatternFinder.findMatches(candidate.symbols()).size();
            int distance = Math.abs(matchCount - patternLengths.size());
            if (distance < closestDistance) {
                closestResult = candidate;
                closestDistance = distance;
            }
            if (distance == 0) return candidate;
        }

        return closestResult == null
            ? createMatchlessResult(columns, rows)
            : closestResult;
    }

    private void plantParallelPatterns(
        SpinResult result,
        List<Integer> patternLengths
    ) {
        boolean horizontal = result.rows() >= patternLengths.size() &&
            (result.cols() >= result.rows() || result.cols() < patternLengths.size());
        int laneCount = horizontal ? result.rows() : result.cols();
        List<Integer> lanes = new ArrayList<>();
        for (int lane = 0; lane < laneCount; lane++) lanes.add(lane);
        Collections.shuffle(lanes, SeededRandomizer.get());

        EnumSet<Symbol> usedSymbols = EnumSet.noneOf(Symbol.class);
        for (int pattern = 0; pattern < patternLengths.size(); pattern++) {
            int lane = lanes.get(pattern);
            int laneLength = horizontal ? result.cols() : result.rows();
            int length = Math.min(patternLengths.get(pattern), laneLength);
            int start = SeededRandomizer.nextInt(0, laneLength - length);
            Symbol symbol = randomWeightedSymbol(usedSymbols);
            usedSymbols.add(symbol);

            for (int position = start; position < start + length; position++) {
                if (horizontal) result.set(position, lane, symbol);
                else result.set(lane, position, symbol);
            }
            keepRunBounded(result, horizontal, lane, start, length, symbol);
        }
    }

    private void keepRunBounded(
        SpinResult result,
        boolean horizontal,
        int lane,
        int start,
        int length,
        Symbol symbol
    ) {
        int before = start - 1;
        int after = start + length;
        int laneLength = horizontal ? result.cols() : result.rows();

        if (before >= 0) setLaneSymbol(
            result,
            horizontal,
            lane,
            before,
            randomWeightedSymbol(EnumSet.of(symbol))
        );
        if (after < laneLength) setLaneSymbol(
            result,
            horizontal,
            lane,
            after,
            randomWeightedSymbol(EnumSet.of(symbol))
        );
    }

    private void setLaneSymbol(
        SpinResult result,
        boolean horizontal,
        int lane,
        int position,
        Symbol symbol
    ) {
        if (horizontal) result.set(position, lane, symbol);
        else result.set(lane, position, symbol);
    }

    private SpinResult createMatchlessResult(int columns, int rows) {
        for (int attempt = 0; attempt < MATCHLESS_BUILD_ATTEMPTS; attempt++) {
            Symbol[][] symbols = new Symbol[columns][rows];
            for (int column = 0; column < columns; column++) {
                for (int row = 0; row < rows; row++) {
                    symbols[column][row] = chooseSafeWeightedSymbol(
                        symbols,
                        column,
                        row,
                        columns,
                        rows
                    );
                }
            }

            SpinResult result = new SpinResult(symbols);
            if (!hasMatch(result)) return result;
        }

        return createGuaranteedMatchlessResult(columns, rows);
    }

    private Symbol chooseSafeWeightedSymbol(
        Symbol[][] symbols,
        int column,
        int row,
        int columns,
        int rows
    ) {
        EnumSet<Symbol> excluded = EnumSet.noneOf(Symbol.class);
        for (Symbol symbol : Symbol.values()) {
            if (completesStraightRun(
                symbols,
                column,
                row,
                columns,
                rows,
                symbol
            )) excluded.add(symbol);
        }
        return randomWeightedSymbol(excluded);
    }

    private boolean completesStraightRun(
        Symbol[][] symbols,
        int column,
        int row,
        int columns,
        int rows,
        Symbol symbol
    ) {
        return samePair(symbols, column - 1, row, column - 2, row, columns, rows, symbol) ||
            samePair(symbols, column, row - 1, column, row - 2, columns, rows, symbol) ||
            samePair(symbols, column - 1, row - 1, column - 2, row - 2, columns, rows, symbol) ||
            samePair(symbols, column - 1, row + 1, column - 2, row + 2, columns, rows, symbol);
    }

    private boolean samePair(
        Symbol[][] symbols,
        int firstColumn,
        int firstRow,
        int secondColumn,
        int secondRow,
        int columns,
        int rows,
        Symbol target
    ) {
        return inBounds(firstColumn, firstRow, columns, rows) &&
            inBounds(secondColumn, secondRow, columns, rows) &&
            symbols[firstColumn][firstRow] == target &&
            symbols[secondColumn][secondRow] == target;
    }

    private boolean inBounds(int column, int row, int columns, int rows) {
        return column >= 0 && column < columns && row >= 0 && row < rows;
    }

    private SpinResult createGuaranteedMatchlessResult(int columns, int rows) {
        Symbol[] symbols = Symbol.values();
        Symbol[][] result = new Symbol[columns][rows];
        int offset = SeededRandomizer.nextInt(0, symbols.length - 1);
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                result[column][row] = symbols[
                    (offset + column + row * 2) % symbols.length
                ];
            }
        }
        return new SpinResult(result);
    }

    private Symbol randomWeightedSymbol(EnumSet<Symbol> excluded) {
        int totalWeight = 0;
        for (Symbol symbol : Symbol.values()) {
            if (!excluded.contains(symbol)) {
                totalWeight += Math.max(0, symbol.poolCount());
            }
        }

        if (totalWeight <= 0) {
            for (Symbol symbol : Symbol.values()) {
                if (!excluded.contains(symbol)) return symbol;
            }
            return Symbol.values()[0];
        }

        int roll = SeededRandomizer.nextInt(1, totalWeight);
        for (Symbol symbol : Symbol.values()) {
            if (excluded.contains(symbol)) continue;
            roll -= Math.max(0, symbol.poolCount());
            if (roll <= 0) return symbol;
        }
        return Symbol.values()[0];
    }

    private int rollPatternCount(int columns, int rows, float luck) {
        int count = 1;
        int maximumCount = Math.min(
            MAX_PATTERNS_PER_SPIN,
            Math.max(columns, rows)
        );
        for (int index = 0; index < maximumCount - 1; index++) {
            if (SeededRandomizer.get().nextFloat() >= lerp(
                BASE_EXTRA_PATTERN_CHANCES[index],
                MAX_LUCK_EXTRA_PATTERN_CHANCES[index],
                luck
            )) break;
            count++;
        }
        return count;
    }

    private int rollPatternLength(int columns, int rows, float luck) {
        int maximumLength = Math.max(columns, rows);
        int length = Math.min(3, maximumLength);
        if (maximumLength >= 4 && SeededRandomizer.get().nextFloat() <
            lerp(0.05f, 0.75f, luck)) {
            length = 4;
        }
        if (maximumLength >= 5 && length == 4 &&
            SeededRandomizer.get().nextFloat() < lerp(0.05f, 0.60f, luck)) {
            length = 5;
        }
        if (maximumLength >= 6 && length == 5 &&
            SeededRandomizer.get().nextFloat() < lerp(0.02f, 0.35f, luck)) {
            length = 6;
        }
        return length;
    }

    private float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
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
                randomWeightedSymbol(EnumSet.of(target))
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

    private boolean hasMatch(SpinResult result) {
        return !PatternFinder.findMatches(result.symbols()).isEmpty();
    }

    public void setLuckBonus(float luckBonus) {
        this.luckBonus = Math.max(0f, Math.min(MAX_LUCK_BONUS, luckBonus));
    }

    public float getLuckProgress() {
        return luckBonus / MAX_LUCK_BONUS;
    }

    public float getExpectedHitChance() {
        return 1f - lerp(
            BASE_EMPTY_CHANCE,
            MAX_LUCK_EMPTY_CHANCE,
            getLuckProgress()
        );
    }

    /** Kept for callers that still use the old rescue-policy terminology. */
    public void setRescueChanceBonus(float rescueChanceBonus) {
        setLuckBonus(rescueChanceBonus);
    }

    /** Kept for callers that still use the old rescue-policy terminology. */
    public float getRescueChance() {
        return getExpectedHitChance();
    }
}
