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

    private final Map<Symbol, Integer> symbolValueMap = new HashMap<>();
    private final Map<Symbol, Integer> symbolPriceMap = new HashMap<>();

    private final PropertyChangeSupport symbolValueChangeSupport = new PropertyChangeSupport(this);
    private final PropertyChangeSupport symbolPriceChangeSupport = new PropertyChangeSupport(this);

    private SymbolValues() {
        symbolValueMap.put(Symbol.LEMON, 2);
        symbolValueMap.put(Symbol.CHERRY, 2);
        symbolValueMap.put(Symbol.CLOVER, 3);
        symbolValueMap.put(Symbol.BELL, 3);
        symbolValueMap.put(Symbol.IRON, 5);
        symbolValueMap.put(Symbol.DIAMOND, 5);
        symbolValueMap.put(Symbol.SEVEN, 7);

        Seq.of(Symbol.values()).forEach(symbol -> symbolPriceMap.put(symbol, 50));
    }

    public int getValue(Symbol symbol) {
        return symbolValueMap.get(symbol);
    }

    public void increaseValue(Symbol symbol) {
        int oldValue = symbolValueMap.get(symbol);
        symbolValueMap.put(symbol, oldValue + 1);
        symbolValueChangeSupport.firePropertyChange(symbol.toString(), oldValue, (int) symbolValueMap.get(symbol));
        increasePrice(symbol);
    }

    public int getPrice(Symbol symbol) {
        return symbolPriceMap.get(symbol);
    }

    private void increasePrice(Symbol symbol) {
        int oldPrice = symbolPriceMap.get(symbol);
        symbolPriceMap.put(symbol, (int) Math.ceil(oldPrice * 1.5));
        symbolPriceChangeSupport.firePropertyChange(symbol.toString(), oldPrice, (int) symbolPriceMap.get(symbol));
    }

    public void addValueChangeListener(PropertyChangeListener listener) {
        symbolValueChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPriceChangeListener(PropertyChangeListener listener) {
        symbolPriceChangeSupport.addPropertyChangeListener(listener);
    }

}
