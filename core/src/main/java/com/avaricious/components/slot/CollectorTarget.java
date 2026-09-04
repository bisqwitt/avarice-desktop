package com.avaricious.components.slot;

/** A collectible that an autonomous Collector can seek and claim. */
interface CollectorTarget {

    boolean isAvailableForCollector();

    float getCollectorTargetX();

    float getCollectorTargetY();

    float getCollectorTargetRadius();

    boolean collectByCollector();
}
