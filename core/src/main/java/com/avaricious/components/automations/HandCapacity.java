package com.avaricious.components.automations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class HandCapacity extends AbstractAutomationUpgrade {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private int capacity = 1;

    public HandCapacity() {
        super(50);
        activate();
    }

    @Override
    void onUpgrade() {
        int old = capacity;
        capacity += 1;
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
        return capacity + 1;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

}
