package com.avaricious.components.automations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AutoSpinCapacity extends AbstractAutomationUpgrade {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private int capacity = 3;

    public AutoSpinCapacity() {
        super(200);
    }

    @Override
    void onUpgrade() {
        int old = capacity;
        capacity += 2;
        propertyChangeSupport.firePropertyChange("capacity", old, capacity);
    }

    @Override
    boolean isMaxed() {
        return false;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNextCapacity() {
        return capacity + 2;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

}
