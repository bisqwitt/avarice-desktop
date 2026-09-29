package com.avaricious.game.run;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Mutable cash balance for one game run. */
public final class CashBalance {

    private final PropertyChangeSupport changes =
        new PropertyChangeSupport(this);

    private float value;

    public float get() {
        return value;
    }

    public void set(float newValue) {
        float oldValue = value;
        value = newValue;
        changes.firePropertyChange("cash", oldValue, value);
    }

    public void add(float amount) {
        set(value + amount);
    }

    public void subtract(float amount) {
        set(value - amount);
    }

    public void onChange(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }
}
