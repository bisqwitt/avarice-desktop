package com.avaricious.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class LoopingSound {

    private final Sound start;
    private final Sound loop;
    private final Sound end;

    private final float volume;
    private final float pitch;

    private long loopId = -1;
    private boolean active = false;

    public LoopingSound(String startPath, String loopPath, String endPath, float volume, float pitch) {
        start = Gdx.audio.newSound(Gdx.files.internal("audio/" + startPath));
        loop  = Gdx.audio.newSound(Gdx.files.internal("audio/" + loopPath));
        end   = Gdx.audio.newSound(Gdx.files.internal("audio/" + endPath));
        this.volume = volume;
        this.pitch = pitch;
    }

    public void start() {
        if (active) return;
        active = true;

//        start.play(volume, pitch, 0f);

        loopId = loop.play(volume, pitch, 0f);
        loop.setLooping(loopId, true);
    }

    public void stop() {
        if (!active) return;
        active = false;

        // 3️⃣ Stop looping part
        if (loopId != -1) {
            loop.stop(loopId);
            loopId = -1;
        }

        // 4️⃣ Play end (one-shot)
        end.play(volume, pitch, 0f);
    }
}
