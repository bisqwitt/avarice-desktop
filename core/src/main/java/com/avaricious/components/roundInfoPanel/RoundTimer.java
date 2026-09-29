package com.avaricious.components.roundInfoPanel;

/** Frame-driven round clock so gameplay overlays can pause it cleanly. */
public final class RoundTimer {

    public static final float ROUND_DURATION_SECONDS = 60f;

    private float secondsRemaining;
    private float elapsedSeconds;

    public void startTimer() {
        secondsRemaining = ROUND_DURATION_SECONDS;
        elapsedSeconds = 0f;
    }

    public void restore(float savedSecondsRemaining) {
        secondsRemaining = Math.max(0f, savedSecondsRemaining);
        elapsedSeconds = Math.max(
            0f,
            ROUND_DURATION_SECONDS - secondsRemaining
        );
    }

    /**
     * Advances the clock and returns true only on the frame it expires.
     */
    public boolean update(float delta) {
        if (timerEnded() || delta <= 0f) return false;

        float elapsed = Math.min(delta, secondsRemaining);
        secondsRemaining -= elapsed;
        elapsedSeconds += elapsed;

        if (secondsRemaining <= 0f) {
            secondsRemaining = 0f;
            return true;
        }
        return false;
    }

    public int getSecondsRemaining() {
        return (int) Math.ceil(secondsRemaining);
    }

    public float getPreciseSecondsRemaining() {
        return secondsRemaining;
    }

    public void addSeconds(float seconds) {
        secondsRemaining += Math.max(0f, seconds);
    }

    public long msSinceRoundStart() {
        return (long) (elapsedSeconds * 1_000f);
    }

    public boolean timerEnded() {
        return secondsRemaining <= 0f;
    }
}
