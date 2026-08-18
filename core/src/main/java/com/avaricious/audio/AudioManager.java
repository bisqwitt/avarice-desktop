package com.avaricious.audio;

import com.avaricious.DevTools;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

public class AudioManager {

    private static AudioManager instance;

    public static AudioManager I() {
        return instance == null ? instance = new AudioManager() : instance;
    }

    private final Sound hit = Gdx.audio.newSound(Gdx.files.internal("audio/hit.wav"));

    private final LoopingSound payout = new LoopingSound(
        "payout-start.wav", "payout-loop.wav", "payout-end.wav",
        0.9f, 1f);

    // 0 = base, 2 = whole step, 3 = minor third, 5 = fourth, 7 = fifth
    private static final float[] HIT_LADDER = {0f, 2f, 3f, 5f, 7f};

    private AudioManager() {
    }

    public void playHit(float streak) {
        if (!DevTools.audioMuted()) playHitInternal(streak, -2f);
    }

    private void playHitInternal(float streak, float semitoneOffset) {
        float volume = 0.9f;

        int idx = MathUtils.clamp((int) streak, 0, HIT_LADDER.length - 1);

        float semitones = HIT_LADDER[idx] + semitoneOffset;
        float pitch = (float) Math.pow(2f, semitones / 12f);

        hit.play(volume, pitch, 0f);
    }

}
