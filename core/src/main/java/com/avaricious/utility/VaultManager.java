package com.avaricious.utility;

import com.avaricious.components.roundInfoPanel.ScoreDisplay;

/** One run-scoped term deposit that earns interest over completed rounds. */
public final class VaultManager {

    public static final int SHORT_TERM = 3;
    public static final int MEDIUM_TERM = 5;
    public static final int LONG_TERM = 7;

    private static VaultManager instance;

    public static VaultManager I() {
        return instance == null ? instance = new VaultManager() : instance;
    }

    private float principal;
    private int termRounds;
    private int maturityRound;
    private float lastPayout;

    private VaultManager() {
    }

    public boolean deposit(float amount, int term, int currentRound) {
        float safeAmount = Math.max(0f, amount);
        if (hasActiveDeposit()
            || safeAmount <= 0f
            || safeAmount > ScoreDisplay.I().getScoreNumber()
            || !isValidTerm(term)) {
            return false;
        }

        principal = safeAmount;
        termRounds = term;
        maturityRound = Math.max(1, currentRound) + term;
        lastPayout = 0f;
        ScoreDisplay.I().removeFromScore(safeAmount);
        return true;
    }

    public float collectIfMature(int currentRound) {
        if (!hasActiveDeposit() || currentRound < maturityRound) return 0f;

        float payout = getProjectedPayout();
        clearDeposit();
        lastPayout = payout;
        ScoreDisplay.I().setScoreNumber(
            ScoreDisplay.I().getScoreNumber() + payout
        );
        return payout;
    }

    public boolean hasActiveDeposit() {
        return principal > 0f && termRounds > 0 && maturityRound > 0;
    }

    public float getPrincipal() {
        return principal;
    }

    public int getTermRounds() {
        return termRounds;
    }

    public int getMaturityRound() {
        return maturityRound;
    }

    public int getRoundsRemaining(int currentRound) {
        if (!hasActiveDeposit()) return 0;
        return Math.max(0, maturityRound - currentRound);
    }

    public float getProjectedPayout() {
        return EconomyScaling.roundPrice(
            principal * (1f + interestRate(termRounds))
        );
    }

    public float getLastPayout() {
        return lastPayout;
    }

    public void clearLastPayout() {
        lastPayout = 0f;
    }

    public void reset() {
        clearDeposit();
        lastPayout = 0f;
    }

    public void restore(float amount, int term, int savedMaturityRound) {
        if (amount <= 0f || !isValidTerm(term) || savedMaturityRound <= 0) {
            reset();
            return;
        }

        principal = amount;
        termRounds = term;
        maturityRound = savedMaturityRound;
        lastPayout = 0f;
    }

    public static float interestRate(int term) {
        switch (term) {
            case SHORT_TERM:
                return 0.15f;
            case MEDIUM_TERM:
                return 0.35f;
            case LONG_TERM:
                return 0.60f;
            default:
                return 0f;
        }
    }

    private static boolean isValidTerm(int term) {
        return term == SHORT_TERM
            || term == MEDIUM_TERM
            || term == LONG_TERM;
    }

    private void clearDeposit() {
        principal = 0f;
        termRounds = 0;
        maturityRound = 0;
    }
}
