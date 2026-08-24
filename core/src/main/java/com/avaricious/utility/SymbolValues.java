package com.avaricious.utility;

import com.avaricious.components.slot.Symbol;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class SymbolValues {

    private static SymbolValues instance;

    public static SymbolValues I() {
        return instance == null ? instance = new SymbolValues() : instance;
    }

    private static final float VALUE_MULTIPLIER = 2f;
    private static final float PRICE_MULTIPLIER = 2.18f;

    private final Map<Symbol, Float> symbolValueMap = new HashMap<>();
    private final Map<Symbol, Float> symbolPriceMap = new HashMap<>();

    private final PropertyChangeSupport symbolValueChangeSupport = new PropertyChangeSupport(this);
    private final PropertyChangeSupport symbolPriceChangeSupport = new PropertyChangeSupport(this);

    private SymbolValues() {
        symbolValueMap.put(Symbol.LEMON, 2f);
        symbolValueMap.put(Symbol.CHERRY, 2f);
        symbolValueMap.put(Symbol.CLOVER, 3f);
        symbolValueMap.put(Symbol.BELL, 3f);
        symbolValueMap.put(Symbol.IRON, 5f);
        symbolValueMap.put(Symbol.DIAMOND, 5f);
        symbolValueMap.put(Symbol.SEVEN, 7f);

        symbolPriceMap.put(Symbol.LEMON, 75f);
        symbolPriceMap.put(Symbol.CHERRY, 75f);
        symbolPriceMap.put(Symbol.CLOVER, 100f);
        symbolPriceMap.put(Symbol.BELL, 100f);
        symbolPriceMap.put(Symbol.IRON, 150f);
        symbolPriceMap.put(Symbol.DIAMOND, 150f);
        symbolPriceMap.put(Symbol.SEVEN, 250f);
    }

    public float getValue(Symbol symbol) {
        return symbolValueMap.get(symbol);
    }

    public void increaseValue(Symbol symbol) {
        float oldValue = symbolValueMap.get(symbol);
        float newValue = oldValue * VALUE_MULTIPLIER;
        symbolValueMap.put(symbol, newValue);
        symbolValueChangeSupport.firePropertyChange(symbol.toString(), oldValue, newValue);
        increasePrice(symbol);
    }

    public float getNextValue(Symbol symbol) {
        return symbolValueMap.get(symbol) * VALUE_MULTIPLIER;
    }

    public float getPrice(Symbol symbol) {
        return symbolPriceMap.get(symbol);
    }

    private void increasePrice(Symbol symbol) {
        float oldPrice = symbolPriceMap.get(symbol);
        float newPrice = EconomyScaling.roundPrice(oldPrice * PRICE_MULTIPLIER);
        symbolPriceMap.put(symbol, newPrice);
        symbolPriceChangeSupport.firePropertyChange(symbol.toString(), oldPrice, newPrice);
    }

    public void addValueChangeListener(PropertyChangeListener listener) {
        symbolValueChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPriceChangeListener(PropertyChangeListener listener) {
        symbolPriceChangeSupport.addPropertyChangeListener(listener);
    }

}
