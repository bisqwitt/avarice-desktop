package com.avaricious;

/** Accumulates gameplay statistics for the currently active round. */
public final class RoundStats {

    private static RoundStats instance;

    public static RoundStats I() {
        return instance == null ? instance = new RoundStats() : instance;
    }

    private int symbolsHit;
    private int spins;
    private float moneyGained;
    private int collectiblesClaimed;
    private int symbolsCollected;
    private float totalCollectibleClaimTime;

    private RoundStats() {
    }

    public void reset() {
        symbolsHit = 0;
        spins = 0;
        moneyGained = 0f;
        collectiblesClaimed = 0;
        symbolsCollected = 0;
        totalCollectibleClaimTime = 0f;
    }

    public void recordSymbolHit() {
        symbolsHit++;
    }

    public void recordSpin() {
        spins++;
    }

    public void recordMoneyGained(float amount) {
        if (amount > 0f) moneyGained += amount;
    }

    public void recordCollectibleClaim(float secondsSinceSpawn) {
        collectiblesClaimed++;
        totalCollectibleClaimTime += Math.max(0f, secondsSinceSpawn);
    }

    public void recordSymbolCollected(float secondsSinceSpawn) {
        symbolsCollected++;
        recordCollectibleClaim(secondsSinceSpawn);
    }

    public int getSymbolsHit() {
        return symbolsHit;
    }

    public int getSpins() {
        return spins;
    }

    public float getMoneyGained() {
        return moneyGained;
    }

    public int getSymbolsCollected() {
        return symbolsCollected;
    }

    public float getAverageCollectibleClaimTime() {
        if (collectiblesClaimed == 0) return 0f;
        return totalCollectibleClaimTime / collectiblesClaimed;
    }
}
