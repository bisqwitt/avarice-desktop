package com.avaricious.components.automations;

import com.avaricious.DevTools;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractAutomationUpgrade extends AbstractAutomation {

    private int price;
    private final PropertyChangeSupport priceChangeSupport = new PropertyChangeSupport(this);

    public AbstractAutomationUpgrade(int initialPrice) {
        price = initialPrice;
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
            || DevTools.unlimitedMoney()) && isActive() && !isMaxed();
    }

    private void updatePrice() {
        int oldPrice = price;
        price = (int) Math.ceil(price * 1.5);
        priceChangeSupport.firePropertyChange("price", oldPrice, price);
    }

    @Override
    public int price() {
        return price;
    }

    public void addPriceChangeListener(PropertyChangeListener listener) {
        priceChangeSupport.addPropertyChangeListener(listener);
    }
}
