package com.avaricious.components.slot.rework;

import com.avaricious.components.slot.Symbol;

import java.util.ArrayList;
import java.util.List;

public class ReelNew {

    private final List<Symbol> strip = new ArrayList<>();
    private final int visibleRows;

    private float visualPos = 0f;
    private float indexPos = 0f;
    private ReelSpinPlan plan;

    private boolean finishedNotified = true;
    private Runnable onFinished;

    public ReelNew(int visibleRows) {
        this.visibleRows = visibleRows;
        this.visualPos = 1f;
        this.indexPos = 1f;
    }

    public void update(float delta, float spinTime) {
        if (plan == null) {
            return;
        }

        visualPos = plan.sampleVisualPosition(spinTime);

        // Before the reel reaches the target, indexing follows the moving reel.
        // Once it reaches/passes the target, lock indexing to the target.
        if (visualPos >= plan.targetPos) {
            indexPos = plan.targetPos;
        } else {
            indexPos = visualPos;
        }

        if (!finishedNotified && spinTime >= plan.finishTime) {
            visualPos = plan.targetPos;
            indexPos = plan.targetPos;
            finishedNotified = true;

            if (onFinished != null) {
                onFinished.run();
            }
        }
    }

    public void play(ReelSpinPlan plan, Runnable onFinished) {
        this.plan = plan;
        this.onFinished = onFinished;
        this.finishedNotified = false;
    }

    public boolean isFinished() {
        return plan == null || finishedNotified;
    }

    public float currentPos() {
        return visualPos;
    }

    public float frac() {
        return visualPos - (float) Math.floor(indexPos);
    }

    public Symbol symbolAtRow(int row) {
        int idx = (int) Math.floor(indexPos - row);
        return strip.get(mod(idx, strip.size()));
    }

    public int stripSize() {
        return strip.size();
    }

    public int effectIndexAtRow(int row) {
        int idx = (int) Math.floor(indexPos - row);
        return mod(idx, visibleRows);
    }

    public void setStrip(List<Symbol> strip) {
        this.strip.clear();
        this.strip.addAll(strip);
    }

    public int visibleRows() {
        return visibleRows;
    }

    private static int mod(int x, int m) {
        int r = x % m;
        return r < 0 ? r + m : r;
    }
}
