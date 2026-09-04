package com.avaricious.components.automations;

import java.util.function.BooleanSupplier;

/** Adapts an existing run stat into a permanently purchasable shop upgrade. */
public final class ShopStatUpgrade extends AbstractAutomationUpgrade {

    private final Runnable upgradeAction;
    private final BooleanSupplier maxed;
    private final BooleanSupplier available;

    public ShopStatUpgrade(
        float initialPrice,
        float priceGrowth,
        Runnable upgradeAction,
        BooleanSupplier maxed
    ) {
        this(initialPrice, priceGrowth, upgradeAction, maxed, () -> true);
    }

    public ShopStatUpgrade(
        float initialPrice,
        float priceGrowth,
        Runnable upgradeAction,
        BooleanSupplier maxed,
        BooleanSupplier available
    ) {
        super(initialPrice, priceGrowth);
        this.upgradeAction = upgradeAction;
        this.maxed = maxed;
        this.available = available;
        activate();
    }

    @Override
    void onUpgrade() {
        if (!isMaxed()) upgradeAction.run();
    }

    @Override
    boolean isMaxed() {
        return maxed.getAsBoolean();
    }

    @Override
    public boolean isBuyable() {
        return available.getAsBoolean() && super.isBuyable();
    }
}
