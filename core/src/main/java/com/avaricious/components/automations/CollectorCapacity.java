package com.avaricious.components.automations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Controls how many autonomous collectible seekers are active. */
public class CollectorCapacity extends AbstractAutomationUpgrade {

    public static final String COUNT = "collectorCount";

    private final PropertyChangeSupport changeSupport =
        new PropertyChangeSupport(this);

    private int count;

    public CollectorCapacity() {
        super(5_000f, 4f);
        activate();
    }

    @Override
    void onUpgrade() {
        int oldCount = count;
        count++;
        changeSupport.firePropertyChange(COUNT, oldCount, count);
    }

    @Override
    boolean isMaxed() {
        return false;
    }

    public int getCount() {
        return count;
    }

    public int getNextCount() {
        return count + 1;
    }

    public void addCountChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
}
