package com.avaricious.components.automations;

import com.avaricious.DevTools;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.utility.EconomyScaling;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractAutomationUpgrade extends AbstractAutomation {

    private float price;
    private final float priceGrowth;
    private final PropertyChangeSupport priceChangeSupport = new PropertyChangeSupport(this);

    public AbstractAutomationUpgrade(float initialPrice) {
        this(initialPrice, 3f);
    }

    public AbstractAutomationUpgrade(float initialPrice, float priceGrowth) {
        price = initialPrice;
        this.priceGrowth = priceGrowth;
    }

    @Override
    protected void onActivate() {
    }

    public void upgrade() {
        onUpgrade();
        updatePrice();
    }

    abstract void onUpgrade();

    abstract boolean isMaxed();

    @Override
    public boolean isBuyable() {
        return (ScoreDisplay.I().getScoreNumber() >= price()
            || DevTools.unlimitedMoney()
            || DevTools.freeShopPurchases()) && isActive() && !isMaxed();
    }

    private void updatePrice() {
        float oldPrice = price;
        price = nextPrice(price);
        priceChangeSupport.firePropertyChange("price", oldPrice, price);
    }

    protected float nextPrice(float currentPrice) {
        return EconomyScaling.roundPrice(currentPrice * priceGrowth);
    }

    @Override
    public float price() {
        return price;
    }

    public void addPriceChangeListener(PropertyChangeListener listener) {
        priceChangeSupport.addPropertyChangeListener(listener);
    }
}
