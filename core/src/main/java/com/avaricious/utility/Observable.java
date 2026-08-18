package com.avaricious.utility;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Observable<T> {

    private final List<Listener<T>> listeners = new CopyOnWriteArrayList<>();

    /** Return the current value to immediately push on subscribe. */
    protected abstract T snapshot();

    /** Notify listeners with a new snapshot (or provided value). */
    protected void notifyChanged(T value) {
        for (Listener<T> l : listeners) l.accept(value);
    }

    /** Subscribe and get an AutoCloseable unsubscribe handle. */
    public AutoCloseable onChange(Listener<T> listener) {
        listeners.add(listener);
        listener.accept(snapshot());
        return () -> listeners.remove(listener);
    }
}
