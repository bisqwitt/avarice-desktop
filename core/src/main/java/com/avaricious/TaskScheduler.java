package com.avaricious;

import com.badlogic.gdx.utils.Timer;

import java.util.LinkedList;

public class TaskScheduler {

    private final LinkedList<ScheduledTask> tasks = new LinkedList<>();
    private final float defaultDelay;

    private final Timer timer = new Timer();

    private boolean paused = false;

    public TaskScheduler(float defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void schedule(Runnable r) {
        schedule(r, defaultDelay);
    }

    public void schedule(Runnable r, float delay) {
        tasks.add(new ScheduledTask(r, delay));
    }

    public void scheduleNoDelay(Runnable r) {
        float delay = tasks.getLast().getDelay();
        tasks.getLast().setDelay(0f);
        schedule(r, delay);
    }

    public void runTasks() {
        runTasks(defaultDelay);
    }

    public void runTasks(float initialDelay) {
        float delay = Math.max(0f, initialDelay);

        for (ScheduledTask task : tasks) {
            timer.scheduleTask(create(task.runnable), delay);
            delay += task.delay;
        }

        tasks.clear();
    }

    public void pause() {
        if (paused) return;

        paused = true;
        timer.stop();
    }

    public void resume() {
        if (!paused) return;

        paused = false;
        timer.start();
    }

    public boolean isPaused() {
        return paused;
    }

    private Timer.Task create(Runnable r) {
        return new Timer.Task() {
            @Override
            public void run() {
                r.run();
            }
        };
    }

    private static class ScheduledTask {

        private Runnable runnable;
        private float delay;

        public ScheduledTask(Runnable runnable, float delay) {
            this.runnable = runnable;
            this.delay = delay;
        }

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public void setDelay(float delay) {
            this.delay = delay;
        }

        public float getDelay() {
            return delay;
        }
    }
}
