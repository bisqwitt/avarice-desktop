package com.avaricious.components.slot;

import com.avaricious.utility.SeededRandomizer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Premium reel motion:
 * - inertia start
 * - cruise jitter
 * - ease-out stop
 * - snap into an aligned settle
 * <p>
 * Units:
 * pos and vel are in "symbols", not pixels.
 */
public class Reel {

    public enum Phase {IDLE, STARTING, CRUISING, STOPPING, SETTLING}

    private final List<Symbol> strip = new ArrayList<>();
    private final int visibleRows;

    // Continuous motion in symbol units:
    private float pos = 0f;     // increases downward (choose one direction consistently)
    private float vel = 0f;

    private Phase phase = Phase.IDLE;

    // Tuning (Balatro-ish feel)
    private float cruiseVel = 5f;         // symbols/sec
    private float accel = 70f;             // symbols/sec^2
    private float stopDecel = 90f;         // symbols/sec^2
    private float settleSpring = 180f;     // snap strength
    private float settleDamp = 28f;        // damping for settle
    private static final float SETTLE_POSITION_EPSILON = 0.006f;
    private static final float SETTLE_VELOCITY_EPSILON = 0.10f;

    // Noise / “alive” feeling
    private float cruiseJitterAmp = 0.35f; // +/- vel jitter
    private float cruiseJitterHz = 7.0f;

    // Stop targeting
    private boolean stopRequested = false;
    private float stopTargetPos = 0f;      // absolute symbol position where center row aligns
    private float settleTargetPos = 0f;
    private float settleVel = 0f;

    // Used for jitter
    private float t = 0f;
    private float seed = SeededRandomizer.seed;

    private final Runnable onSpinFinished;
    private boolean spinFinishedNotified = false;

    public Reel(int visibleRows, Runnable onSpinFinished) {
        this.visibleRows = visibleRows;

        this.onSpinFinished = onSpinFinished;
        // randomize initial position so reels don't look identical at boot
        this.pos = 1;
    }

    /**
     * Call every frame
     */
    public void update(float dt) {
        t += dt;

        switch (phase) {
            case IDLE:
                // do nothing
                vel = 0f;
                break;

            case STARTING: {
                // accelerate into cruise with a tiny anticipation "kick"
                // (slight overshoot of cruise then settle to cruise)
                float target = cruiseVel * 1.06f;
                vel = approach(vel, target, accel * dt);
                pos += vel * dt;

                if (vel >= cruiseVel * 1.02f) {
                    phase = Phase.CRUISING;
                }
                break;
            }

            case CRUISING: {
                // stable speed + subtle jitter so it feels alive
                float jitter = MathUtils.sin((t + seed) * MathUtils.PI2 * cruiseJitterHz) * cruiseJitterAmp;
                float target = cruiseVel + jitter;

                vel = approach(vel, target, (accel * 0.35f) * dt);
                pos += vel * dt;

                if (stopRequested) {
                    phase = Phase.STOPPING;
                }
                break;
            }

            case STOPPING: {
                // decelerate until we are close enough to enter settling.
                // But we must ensure we reach stopTargetPos (absolute).
                // We'll decelerate based on remaining distance.
                float remaining = stopTargetPos - pos;

                // Never reverse back to a target that was crossed in one frame.
                if (remaining <= 0f) {
                    finishStop();
                    break;
                }

                // Compute "needed stopping speed" v = sqrt(2*a*d)  (classic kinematics)
                float desiredVel = (float) Math.sqrt(Math.max(0f, 2f * stopDecel * remaining));
                desiredVel = Math.min(desiredVel, vel);

                // Bring current vel down toward desiredVel
                vel = approach(vel, desiredVel, stopDecel * dt);
                pos += vel * dt;

                float remainingAfterMove = stopTargetPos - pos;

                if (remainingAfterMove <= 0f) {
                    finishStop();
                    break;
                }

                // When close, switch to settle spring.
                if (remainingAfterMove < 0.65f && vel < 6.5f) {
                    // Keep only enough velocity to approach the target
                    // without carrying the reel through it.
                    settleTargetPos = stopTargetPos;
                    float maximumSettleVelocity =
                        (float) Math.sqrt(settleSpring) *
                            remainingAfterMove;
                    settleVel = Math.min(vel, maximumSettleVelocity);
                    phase = Phase.SETTLING;
                }
                break;
            }

            case SETTLING: {
                // Critically damped-looking settle that is clamped at the
                // aligned target so it never dips below and corrects back.
                float x = pos - settleTargetPos; // displacement
                float a = -settleSpring * x - settleDamp * settleVel;

                settleVel += a * dt;
                float nextPos = pos + settleVel * dt;

                if (nextPos >= settleTargetPos) {
                    finishStop();
                    break;
                }

                pos = nextPos;

                // finish when close enough
                if (
                    Math.abs(pos - settleTargetPos) < SETTLE_POSITION_EPSILON &&
                        Math.abs(settleVel) < SETTLE_VELOCITY_EPSILON
                ) {
                    finishStop();
                }
                break;
            }
        }

        // keep pos bounded to avoid float blowup, but preserve absolute for stop logic:
        // We'll allow pos to grow, but occasionally wrap both pos and targets.
        if (pos > 100000f) {
            pos -= 100000f;
            stopTargetPos -= 100000f;
            settleTargetPos -= 100000f;
        }
    }

    public void start(float cruiseSpeed) {
        this.cruiseVel = cruiseSpeed;
        this.stopRequested = false;
        this.phase = Phase.STARTING;
        spinFinishedNotified = false;
    }

    /**
     * Requests stopping aligned so that the center visible row lands on the next exact symbol boundary.
     * Use this as your replacement for stopSoonAlignCenter().
     */
    public void requestStopAlignCenter(int extraSpinsMin) {
        stopRequested = true;

        // center row index in visible window: if rows=3 -> center=1
        int centerRow = visibleRows / 2;

        // Leave enough distance for the current velocity to decelerate
        // before choosing the next aligned symbol boundary.
        float stoppingDistance =
            vel * vel / (2f * stopDecel);
        float minimumDistance = Math.max(
            extraSpinsMin * strip.size(),
            stoppingDistance + 0.25f
        );
        float minimumCenter =
            pos + minimumDistance + centerRow;
        float targetCenter =
            (float) Math.ceil(minimumCenter - 1e-6f);
        stopTargetPos = targetCenter - centerRow;

        if (phase == Phase.IDLE) {
            // if stopped and request stop, just settle immediately
            settleTargetPos = stopTargetPos;
            settleVel = 0f;
            phase = Phase.SETTLING;
        } else if (
            phase == Phase.STARTING ||
                phase == Phase.CRUISING
        ) {
            phase = Phase.STOPPING;
        }
    }

    private void finishStop() {
        pos = stopTargetPos;
        vel = 0f;
        settleVel = 0f;
        stopRequested = false;
        phase = Phase.IDLE;

        if (!spinFinishedNotified) {
            spinFinishedNotified = true;
            onSpinFinished.run();
        }
    }

    public void requestStopAtIndex(int targetIndex, int minDistance) {
        stopRequested = true;

        int centerRow = visibleRows / 2;
        int stripLen = strip.size();

        int currentBase = (int) Math.floor(pos);
        int currentCenterIndex = mod(currentBase + centerRow, stripLen);

        int forward = mod(targetIndex - currentCenterIndex, stripLen);

        while (forward < minDistance) {
            forward += stripLen;
        }

        stopTargetPos = currentBase + forward;
    }

    /**
     * Fraction between current symbol and next for rendering offset.
     */
    public float frac() {
        // fraction part of pos (0..1)
        return pos - (float) Math.floor(pos);
    }

    /**
     * Returns the SymbolInstance that appears at a given visible row (0..visibleRows-1),
     * and also supports rows outside for overdraw (like -1 and visibleRows).
     */
    public Symbol symbolAtRow(int row) {
        // The symbol index for a row is floor(pos + row)
        int idx = (int) Math.floor(pos + row);
        return strip.get(mod(idx, strip.size()));
    }

    public int stripSize() {
        return strip.size();
    }

    public void setSymbolAtRow(int row, Symbol symbol) {
        int idx = (int) Math.floor(pos + row);
        strip.set(mod(idx, strip.size()), symbol);
    }

    /**
     * Moves a symbol into a visible row by swapping with an off-screen
     * copy. This keeps the reel's symbol distribution unchanged when
     * the spin pity system nudges an empty result into a match.
     */
    public void placeSymbolAtRowPreservingStrip(int row, Symbol symbol) {
        int targetIndex = mod((int) Math.floor(pos + row), strip.size());

        if (strip.get(targetIndex) == symbol) return;

        for (int candidateIndex = 0; candidateIndex < strip.size(); candidateIndex++) {
            if (strip.get(candidateIndex) != symbol || isVisibleIndex(candidateIndex)) {
                continue;
            }

            Symbol replaced = strip.get(targetIndex);
            strip.set(targetIndex, symbol);
            strip.set(candidateIndex, replaced);
            return;
        }

        /* Defensive fallback for a custom strip with no off-screen copy. */
        strip.set(targetIndex, symbol);
    }

    private boolean isVisibleIndex(int index) {
        for (int row = 0; row < visibleRows; row++) {
            int visibleIndex = mod((int) Math.floor(pos + row), strip.size());
            if (visibleIndex == index) return true;
        }
        return false;
    }

    private static int mod(int x, int m) {
        int r = x % m;
        return (r < 0) ? (r + m) : r;
    }

    private static float approach(float value, float target, float maxDelta) {
        if (value < target) return Math.min(value + maxDelta, target);
        return Math.max(value - maxDelta, target);
    }

    public void setStrip(List<Symbol> strip) {
        this.strip.clear();
        this.strip.addAll(strip);
    }

    /**
     * Shifts the symbol at the given visible row one step UP (toward row 0).
     * Swaps it with the symbol at (row - 1).
     */
    public void shiftSymbolUp(int row) {
        if (row <= 0) return;
        int idxA = mod((int) Math.floor(pos + row), strip.size());
        int idxB = mod((int) Math.floor(pos + row - 1), strip.size());
        Symbol tmp = strip.get(idxA);
        strip.set(idxA, strip.get(idxB));
        strip.set(idxB, tmp);
    }

    /**
     * Shifts the symbol at the given visible row one step DOWN (toward row max).
     * Swaps it with the symbol at (row + 1).
     */
    public void shiftSymbolDown(int row) {
        if (row >= visibleRows - 1) return;
        int idxA = mod((int) Math.floor(pos + row), strip.size());
        int idxB = mod((int) Math.floor(pos + row + 1), strip.size());
        Symbol tmp = strip.get(idxA);
        strip.set(idxA, strip.get(idxB));
        strip.set(idxB, tmp);
    }

    public void setSpeed(float speed) {
        this.cruiseVel = speed;
    }

    public float getSpeed() {
        return cruiseVel;
    }
}
