package com.avaricious.components.slot;

import com.avaricious.components.automations.Automations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Owns active Collectors and assigns each seeker a distinct target. */
public final class CollectorManager {

    private static CollectorManager instance;

    public static CollectorManager I() {
        return instance == null ? instance = new CollectorManager() : instance;
    }

    private final List<Collector> collectors = new ArrayList<>();
    private final Set<CollectorTarget> reservedTargets =
        Collections.newSetFromMap(
            new IdentityHashMap<CollectorTarget, Boolean>()
        );

    private CollectorManager() {
    }

    public void update(float delta) {
        synchronizeCount();

        List<CollectorTarget> targets =
            BouncingSymbolManager.I().getCollectorTargets();
        reservedTargets.clear();

        for (Collector collector : collectors) {
            CollectorTarget target = collector.getTarget();
            if (
                target == null ||
                    !target.isAvailableForCollector() ||
                    !reservedTargets.add(target)
            ) {
                collector.setTarget(null);
            }
        }

        for (Collector collector : collectors) {
            if (collector.getTarget() != null) continue;

            CollectorTarget target = nearestAvailableTarget(
                collector,
                targets,
                reservedTargets
            );
            if (target != null) {
                collector.setTarget(target);
                reservedTargets.add(target);
            }
        }

        for (Collector collector : collectors) {
            collector.update(delta);
        }
    }

    public void draw() {
        for (Collector collector : collectors) {
            collector.draw();
        }
    }

    public void reset() {
        collectors.clear();
    }

    private void synchronizeCount() {
        int desiredCount = Automations.I().getCollectorCapacity().getCount();
        while (collectors.size() < desiredCount) {
            collectors.add(new Collector(collectors.size()));
        }
        while (collectors.size() > desiredCount) {
            collectors.remove(collectors.size() - 1);
        }
    }

    private CollectorTarget nearestAvailableTarget(
        Collector collector,
        List<CollectorTarget> targets,
        Set<CollectorTarget> reserved
    ) {
        CollectorTarget nearest = null;
        float nearestDistanceSquared = Float.MAX_VALUE;
        for (CollectorTarget target : targets) {
            if (!target.isAvailableForCollector() || reserved.contains(target)) {
                continue;
            }

            float dx = target.getCollectorTargetX() - collector.getX();
            float dy = target.getCollectorTargetY() - collector.getY();
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared < nearestDistanceSquared) {
                nearest = target;
                nearestDistanceSquared = distanceSquared;
            }
        }
        return nearest;
    }
}
