package com.avaricious.audio;

import com.avaricious.DevTools;
import com.avaricious.utility.GameSettings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;

public class AudioManager {

    private static final float EFFECTS_MASTER_VOLUME = 0.2f;

    private static AudioManager instance;

    public static AudioManager I() {
        return instance == null ? instance = new AudioManager() : instance;
    }

    private final Sound hit = Gdx.audio.newSound(Gdx.files.internal("audio/hit.wav"));

    private final LoopingSound payout = new LoopingSound(
        "payout-start.wav", "payout-loop.wav", "payout-end.wav",
        0.9f * EFFECTS_MASTER_VOLUME, 1f
    );

    // 0 = base, 2 = whole step, 3 = minor third, 5 = fourth, 7 = fifth
    private static final float[] HIT_LADDER = {0f, 2f, 3f, 5f, 7f};

    private AudioManager() {
        payout.setVolumeMultiplier(GameSettings.I().getMasterVolume());
    }

    public void playHit(float streak) {
        if (isMuted()) return;

        playHitInternal(streak, -2f);
    }

    public void playCriticalHit(float streak) {
        if (isMuted()) return;

        playHitInternal(streak, 5f);
        scheduleOneShot(0.055f, 0.30f, 14f);
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
        playUpgradeSelected(0);
    }

    public void playUpgradeSelected(int rarityTier) {
        int tier = MathUtils.clamp(rarityTier, 0, 4);
        playOneShot(0.75f + tier * 0.035f, 7f + tier);
        scheduleOneShot(0.065f, 0.60f + tier * 0.025f, 12f + tier);
        scheduleOneShot(0.14f, 0.52f + tier * 0.025f, 19f + tier);
        if (tier >= 2) {
            scheduleOneShot(0.21f, 0.34f + tier * 0.025f, 24f);
        }
    }

    public void playShopPurchase(boolean major) {
        if (major) {
            playOneShot(0.82f, 2f);
            scheduleOneShot(0.06f, 0.72f, 9f);
            scheduleOneShot(0.13f, 0.62f, 16f);
        } else {
            playOneShot(0.58f, 5f);
            scheduleOneShot(0.07f, 0.42f, 10f);
        }
    }

    public void playTicketLoaded(int multiplier) {
        float tierPitch = Math.min(12f, 3f + multiplier * 0.9f);
        playOneShot(0.52f, tierPitch);
        scheduleOneShot(0.055f, 0.36f, tierPitch + 7f);
    }

    public void playLevelUp() {
        playOneShot(0.72f, 0f);
        scheduleOneShot(0.07f, 0.62f, 4f);
        scheduleOneShot(0.15f, 0.68f, 7f);
        scheduleOneShot(0.25f, 0.58f, 12f);
    }

    public void startPayout() {
        if (isEffectsMuted()) return;
        payout.setVolumeMultiplier(GameSettings.I().getMasterVolume());
        payout.start();
    }

    public void stopPayout() {
        payout.stop(!isEffectsMuted());
    }

    private void playHitInternal(float streak, float semitoneOffset) {
        float volume = 0.1f;

        int idx = MathUtils.clamp((int) streak, 0, HIT_LADDER.length - 1);

        float semitones = HIT_LADDER[idx] + semitoneOffset;
        float pitch = (float) Math.pow(2f, semitones / 12f);

        hit.play(
            volume * GameSettings.I().getMasterVolume(),
            pitch,
            0f
        );
    }

    private void playOneShot(float volume, float semitones) {
        if (isMuted()) return;

        float pitch = (float) Math.pow(2f, semitones / 12f);
        hit.play(
            volume
                * EFFECTS_MASTER_VOLUME
                * GameSettings.I().getMasterVolume(),
            pitch,
            0f
        );
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
        setEffectsMuted(true);
    }

    public void unmute() {
        setEffectsMuted(false);
    }

    public void toggleMute() {
        setEffectsMuted(!GameSettings.I().isEffectsMuted());
    }

    public boolean isMuted() {
        return isEffectsMuted();
    }

    public void setMasterVolume(float volume) {
        GameSettings.I().setMasterVolume(volume);
        payout.setVolumeMultiplier(GameSettings.I().getMasterVolume());
    }

    public float getMasterVolume() {
        return GameSettings.I().getMasterVolume();
    }

    public void setEffectsMuted(boolean muted) {
        GameSettings.I().setEffectsMuted(muted);
        if (muted) payout.stop(false);
    }

    public boolean isEffectsMuted() {
        return GameSettings.I().isEffectsMuted() || DevTools.audioMuted();
    }

    public void setMusicMuted(boolean muted) {
        GameSettings.I().setMusicMuted(muted);
    }

    public boolean isMusicMuted() {
        return GameSettings.I().isMusicMuted() || DevTools.audioMuted();
    }
}
