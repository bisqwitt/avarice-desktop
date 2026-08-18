package com.avaricious.components.slot;

import com.avaricious.utility.AssetKey;

public enum Symbol {

    LEMON(20, AssetKey.LEMON, AssetKey.LEMON_SHADOW, AssetKey.LEMON_WHITE, true),
    CHERRY(20, AssetKey.CHERRY, AssetKey.CHERRY_SHADOW, AssetKey.CHERRY_WHITE, true),
    CLOVER(15, AssetKey.CLOVER, AssetKey.CLOVER_SHADOW, AssetKey.CLOVER_WHITE, false),
    BELL(15, AssetKey.BELL, AssetKey.BELL_SHADOW, AssetKey.BELL_WHITE, false),
    IRON(11, AssetKey.IRON, AssetKey.IRON_SHADOW, AssetKey.IRON_WHITE, false),
    DIAMOND(11, AssetKey.DIAMOND, AssetKey.DIAMOND_SHADOW, AssetKey.DIAMOND_WHITE, false),
    SEVEN(8, AssetKey.SEVEN, AssetKey.SEVEN_SHADOW, AssetKey.SEVEN_WHITE, false);

    private Integer baseSpawnChance;
    private final AssetKey texture;
    private final AssetKey shadowTexture;
    private final AssetKey whiteTexture;

    private final boolean isFruit;

    Symbol(Integer baseSpawnChance, AssetKey texture, AssetKey shadowTexture, AssetKey whiteTexture, boolean isFruit) {
        this.baseSpawnChance = baseSpawnChance;
        this.texture = texture;
        this.shadowTexture = shadowTexture;
        this.whiteTexture = whiteTexture;
        this.isFruit = isFruit;
    }

    public Integer poolCount() {
        return baseSpawnChance;
    }

    private void setBaseSpawnChance(int value) {
        baseSpawnChance = value;
    }

    public AssetKey textureKey() {
        return texture;
    }

    public AssetKey shadowKey() {
        return shadowTexture;
    }

    public AssetKey whiteKey() {
        return whiteTexture;
    }

    public String toString() {
        return this.name().replace(".png", "");
    }

    public boolean isFruit() {
        return isFruit;
    }

    public static void increaseSpawnChance(Symbol target, int amount) {
        if (amount == 0) return;

        Symbol[] symbols = Symbol.values();
        int othersCount = symbols.length - 1;

        if (othersCount <= 0) return;

        int baseReduction = amount / othersCount;
        int remainder = amount % othersCount;

        // Add to target
        target.baseSpawnChance += amount;

        // Subtract equally from others
        for (Symbol symbol : symbols) {
            if (symbol == target) continue;
            symbol.baseSpawnChance -= baseReduction;
        }

        // Distribute remainder one by one
        for (Symbol symbol : symbols) {
            if (remainder == 0) break;
            if (symbol == target) continue;

            symbol.baseSpawnChance -= 1;
            remainder--;
        }
    }
}
