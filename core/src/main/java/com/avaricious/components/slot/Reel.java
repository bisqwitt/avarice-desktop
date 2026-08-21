package com.avaricious.components.slot;

import com.avaricious.utility.SeededRandomizer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A continuous reel whose landing symbols are supplied before it stops.
 * Position is measured in symbols and always moves in one direction.
 */
public class Reel {

    public enum Phase {IDLE, STARTING, CRUISING, STOPPING}

    private static final float START_ACCELERATION = 70f;
    private static final float CRUISE_JITTER = 0.35f;
    private static final float CRUISE_JITTER_HZ = 7f;
    private static final float STOP_DURATION = 0.72f;
    private static final float LANDING_CLEARANCE = 2f;

    private final List<Symbol> strip = new ArrayList<>();
    private final int visibleRows;
    private final Runnable onSpinFinished;

    private float pos = 1f;
    private float vel;
    private float configuredCruiseVel = 16f;
    private float activeCruiseVel = 16f;
    private float elapsed;
    private Phase phase = Phase.IDLE;

    private float stopStartPos;
    private float stopTargetPos;
    private float stopStartVelocity;
    private float stopElapsed;
    private boolean spinFinishedNotified;

    public Reel(int visibleRows, Runnable onSpinFinished) {
        this.visibleRows = visibleRows;
        this.onSpinFinished = onSpinFinished;
    }

    public void update(float delta) {
        if (delta <= 0f) return;
        elapsed += delta;

        switch (phase) {
            case IDLE:
                vel = 0f;
                break;

            case STARTING:
                updateStarting(delta);
                break;

            case CRUISING:
                updateCruising(delta);
                break;

            case STOPPING:
                updateStopping(delta);
                break;
        }

        keepPositionPrecise();
    }

    private void updateStarting(float delta) {
        float target = activeCruiseVel * 1.04f;
        vel = approach(vel, target, START_ACCELERATION * delta);
        pos += vel * delta;

        if (vel >= activeCruiseVel) {
            phase = Phase.CRUISING;
        }
    }

    private void updateCruising(float delta) {
        float jitter = MathUtils.sin(
            (elapsed + SeededRandomizer.seed) * MathUtils.PI2 * CRUISE_JITTER_HZ
        ) * CRUISE_JITTER;
        float target = activeCruiseVel + jitter;

        vel = approach(vel, target, START_ACCELERATION * 0.35f * delta);
        pos += vel * delta;
    }

    /**
     * A cubic Hermite curve starts at the reel's current velocity and ends
     * at exactly zero on an integer symbol boundary. It cannot overshoot.
     */
    private void updateStopping(float delta) {
        stopElapsed = Math.min(stopElapsed + delta, STOP_DURATION);
        float progress = stopElapsed / STOP_DURATION;
        float progress2 = progress * progress;
        float progress3 = progress2 * progress;

        float startBasis = 2f * progress3 - 3f * progress2 + 1f;
        float velocityBasis = progress3 - 2f * progress2 + progress;
        float targetBasis = -2f * progress3 + 3f * progress2;

        pos =
            startBasis * stopStartPos +
                velocityBasis * STOP_DURATION * stopStartVelocity +
                targetBasis * stopTargetPos;

        float startDerivative = 6f * progress2 - 6f * progress;
        float velocityDerivative = 3f * progress2 - 4f * progress + 1f;
        float targetDerivative = -6f * progress2 + 6f * progress;

        vel = (
            startDerivative * stopStartPos +
                velocityDerivative * STOP_DURATION * stopStartVelocity +
                targetDerivative * stopTargetPos
        ) / STOP_DURATION;

        if (stopElapsed >= STOP_DURATION) finishStop();
    }

    public void start(float cruiseSpeed) {
        if (strip.isEmpty()) {
            throw new IllegalStateException("Cannot spin a reel without a strip");
        }

        activeCruiseVel = Math.max(0.1f, cruiseSpeed);
        spinFinishedNotified = false;
        phase = Phase.STARTING;
    }

    /**
     * Commits the complete visible column and begins a deterministic stop.
     * The symbols are written ahead of the viewport so they roll naturally
     * into place instead of appearing when the reel finishes.
     */
    public void stopOn(Symbol[] landingSymbols) {
        if (landingSymbols == null || landingSymbols.length != visibleRows) {
            throw new IllegalArgumentException(
                "A reel landing needs exactly " + visibleRows + " symbols"
            );
        }
        if (strip.size() <= visibleRows + (int) LANDING_CLEARANCE) {
            throw new IllegalStateException("Reel strip is too short for a hidden landing window");
        }

        float minimumTravel = Math.max(
            visibleRows + LANDING_CLEARANCE,
            Math.max(vel, 0f) * STOP_DURATION / 3f + 0.5f
        );
        int targetBase = (int) Math.ceil(pos + minimumTravel);
        while (!landingWindowIsHidden(targetBase)) targetBase++;

        for (int row = 0; row < visibleRows; row++) {
            if (landingSymbols[row] == null) {
                throw new IllegalArgumentException("Landing symbols cannot be null");
            }
            strip.set(indexAt(targetBase, row), landingSymbols[row]);
        }

        stopStartPos = pos;
        stopTargetPos = targetBase;
        stopStartVelocity = Math.max(vel, 0f);
        stopElapsed = 0f;
        phase = Phase.STOPPING;
    }

    private boolean landingWindowIsHidden(int targetBase) {
        int currentBase = (int) Math.floor(pos);

        for (int landingRow = 0; landingRow < visibleRows; landingRow++) {
            int landingIndex = indexAt(targetBase, landingRow);

            /* Include row -1 because it is drawn just above the clipping area. */
            for (int visibleRow = -1; visibleRow < visibleRows; visibleRow++) {
                if (landingIndex == indexAt(currentBase, visibleRow)) return false;
            }
        }
        return true;
    }

    private void finishStop() {
        pos = stopTargetPos;
        vel = 0f;
        phase = Phase.IDLE;

        if (!spinFinishedNotified) {
            spinFinishedNotified = true;
            onSpinFinished.run();
        }
    }

    /**
     * The fractional scroll offset used by rendering. Symbols move down;
     * therefore the strip index for lower rows moves in the opposite direction.
     */
    public float frac() {
        return pos - (float) Math.floor(pos);
    }

    public Symbol symbolAtRow(int row) {
        requireStrip();
        return strip.get(indexAt((int) Math.floor(pos), row));
    }

    public void setSymbolAtRow(int row, Symbol symbol) {
        requireStrip();
        if (symbol == null) throw new IllegalArgumentException("Symbol cannot be null");
        strip.set(indexAt((int) Math.floor(pos), row), symbol);
    }

    public int stripSize() {
        return strip.size();
    }

    public void setStrip(List<Symbol> newStrip) {
        validateStrip(newStrip);
        strip.clear();
        strip.addAll(newStrip);
    }

    /**
     * Rebuilds the off-screen strip without changing anything the player can see.
     */
    public void setStripPreservingVisible(List<Symbol> newStrip) {
        validateStrip(newStrip);

        Symbol[] visibleSymbols = new Symbol[visibleRows];
        for (int row = 0; row < visibleRows; row++) {
            visibleSymbols[row] = symbolAtRow(row);
        }

        strip.clear();
        strip.addAll(newStrip);

        int currentBase = (int) Math.floor(pos);
        for (int row = 0; row < visibleRows; row++) {
            strip.set(indexAt(currentBase, row), visibleSymbols[row]);
        }
    }

    public void shiftSymbolUp(int row) {
        if (row <= 0) return;
        swapRows(row, row - 1);
    }

    public void shiftSymbolDown(int row) {
        if (row >= visibleRows - 1) return;
        swapRows(row, row + 1);
    }

    private void swapRows(int firstRow, int secondRow) {
        int base = (int) Math.floor(pos);
        int firstIndex = indexAt(base, firstRow);
        int secondIndex = indexAt(base, secondRow);
        Symbol first = strip.get(firstIndex);
        strip.set(firstIndex, strip.get(secondIndex));
        strip.set(secondIndex, first);
    }

    private int indexAt(int base, int row) {
        return mod(base - row, strip.size());
    }

    private void validateStrip(List<Symbol> newStrip) {
        if (newStrip == null || newStrip.size() <= visibleRows + LANDING_CLEARANCE) {
            throw new IllegalArgumentException("Reel strip is too short");
        }
        for (Symbol symbol : newStrip) {
            if (symbol == null) throw new IllegalArgumentException("Reel strip cannot contain null symbols");
        }
    }

    private void requireStrip() {
        if (strip.isEmpty()) throw new IllegalStateException("Reel strip has not been built");
    }

    private void keepPositionPrecise() {
        if (elapsed > 10000f) elapsed -= 10000f;
        if (pos <= 100000f) return;

        pos -= 100000f;
        stopStartPos -= 100000f;
        stopTargetPos -= 100000f;
    }

    private static int mod(int value, int modulus) {
        int result = value % modulus;
        return result < 0 ? result + modulus : result;
    }

    private static float approach(float value, float target, float maxDelta) {
        if (value < target) return Math.min(value + maxDelta, target);
        return Math.max(value - maxDelta, target);
    }

    public void setSpeed(float speed) {
        configuredCruiseVel = Math.max(0.1f, speed);
    }

    public float getSpeed() {
        return configuredCruiseVel;
    }

    public Phase getPhase() {
        return phase;
    }
}
