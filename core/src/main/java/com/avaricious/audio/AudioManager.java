package com.avaricious.audio;

import com.avaricious.DevTools;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;

public class AudioManager {

    private static final float MASTER_VOLUME = 0.2f;

    private static AudioManager instance;

    public static AudioManager I() {
        return instance == null ? instance = new AudioManager() : instance;
    }

    private final Sound hit = Gdx.audio.newSound(Gdx.files.internal("audio/hit.wav"));

    private final LoopingSound payout = new LoopingSound(
        "payout-start.wav", "payout-loop.wav", "payout-end.wav",
        0.9f * MASTER_VOLUME, 1f
    );

    // 0 = base, 2 = whole step, 3 = minor third, 5 = fourth, 7 = fifth
    private static final float[] HIT_LADDER = {0f, 2f, 3f, 5f, 7f};

    private boolean muted = false;

    private AudioManager() {
    }

    public void playHit(float streak) {
        if (isMuted()) return;

        playHitInternal(streak, -2f);
    }

    public void playSpinStart() {
        playOneShot(0.28f, -7f);
    }

    public void playReelStop(int reelIndex, boolean finalReel) {
        float[] stopLadder = {-5f, -2f, 0f, 3f, 7f};
        int index = MathUtils.clamp(reelIndex, 0, stopLadder.length - 1);
        playOneShot(finalReel ? 0.62f : 0.34f, stopLadder[index]);

        if (finalReel) {
            scheduleOneShot(0.075f, 0.34f, 12f);
        }
    }

    public void playCollect(int symbolValue) {
        float pitchStep = MathUtils.clamp(symbolValue / 5f, 0f, 7f);
        playOneShot(0.52f, 5f + pitchStep);
    }

    public void playMiss() {
        playOneShot(0.24f, -12f);
    }

    public void playHover() {
        playOneShot(0.16f, 9f);
    }

    public void playUpgradeSelected() {
        playOneShot(0.75f, 7f);
        scheduleOneShot(0.065f, 0.60f, 12f);
        scheduleOneShot(0.14f, 0.52f, 19f);
    }

    public void playLevelUp() {
        playOneShot(0.72f, 0f);
        scheduleOneShot(0.07f, 0.62f, 4f);
        scheduleOneShot(0.15f, 0.68f, 7f);
        scheduleOneShot(0.25f, 0.58f, 12f);
    }

    public void startPayout() {
        if (!isMuted()) payout.start();
    }

    public void stopPayout() {
        payout.stop();
    }

    private void playHitInternal(float streak, float semitoneOffset) {
        float volume = 0.1f;

        int idx = MathUtils.clamp((int) streak, 0, HIT_LADDER.length - 1);

        float semitones = HIT_LADDER[idx] + semitoneOffset;
        float pitch = (float) Math.pow(2f, semitones / 12f);

        hit.play(volume, pitch, 0f);
    }

    private void playOneShot(float volume, float semitones) {
        if (isMuted()) return;

        float pitch = (float) Math.pow(2f, semitones / 12f);
        hit.play(volume * MASTER_VOLUME, pitch, 0f);
    }

    private void scheduleOneShot(
        float delay,
        float volume,
        float semitones
    ) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                playOneShot(volume, semitones);
            }
        }, delay);
    }

    public void mute() {
        muted = true;
        payout.stop();
    }

    public void unmute() {
        muted = false;
    }

    public void toggleMute() {
        if (muted) {
            unmute();
        } else {
            mute();
        }
    }

    public boolean isMuted() {
        return muted || DevTools.audioMuted();
    }
}
