package com.avaricious;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.Timer;

public final class Profiler {

    private static GLProfiler profiler;

    public static void start() {
        if (profiler != null) return;

        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.log("perf",
                    "fps=" + Gdx.graphics.getFramesPerSecond() +
                        " draws=" + profiler.getDrawCalls() +
                        " texBinds=" + profiler.getTextureBindings() +
                        " heap=" + Gdx.app.getJavaHeap() +
                        " nativeHeap=" + Gdx.app.getNativeHeap() +
                        " delta=" + Gdx.graphics.getDeltaTime());

                profiler.reset();
            }
        }, 1f, 1f);
    }
}
