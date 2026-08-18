package com.avaricious.components.slot.rework;

import com.badlogic.gdx.math.MathUtils;

public class ReelSpinPlan {

    public final float startTime;
    public final float settleStartTime;
    public final float finishTime;

    public final float startPos;
    public final float targetPos;

    private final float overshootAmount;
    private final float jitterSeed;

    public ReelSpinPlan(
        float startTime,
        float settleStartTime,
        float finishTime,
        float startPos,
        float targetPos,
        float overshootAmount,
        float jitterSeed
    ) {
        this.startTime = startTime;
        this.settleStartTime = settleStartTime;
        this.finishTime = finishTime;
        this.startPos = startPos;
        this.targetPos = targetPos;
        this.overshootAmount = overshootAmount;
        this.jitterSeed = jitterSeed;
    }

    public float samplePosition(float time) {
        if (time <= startTime) {
            return startPos;
        }

        if (time >= finishTime) {
            return targetPos;
        }

        if (time < settleStartTime) {
            return sampleMainSpin(time);
        }

        return sampleSettle(time);
    }

    public float sampleVisualPosition(float time) {
        if (time <= startTime) {
            return startPos;
        }

        if (time >= finishTime) {
            return targetPos;
        }

        if (time < settleStartTime) {
            return sampleMainSpin(time);
        }

        return sampleSettle(time);
    }

    private float sampleMainSpin(float time) {
        float duration = settleStartTime - startTime;
        float t = MathUtils.clamp((time - startTime) / duration, 0f, 1f);

        // Important: visual movement may overshoot.
        float mainTarget = targetPos + overshootAmount;

        float progress = premiumSpinProgress(t);

        float pos = MathUtils.lerp(startPos, mainTarget, progress);

        float jitterFadeIn = smoothStep(0.12f, 0.28f, t);
        float jitterFadeOut = 1f - smoothStep(0.65f, 0.95f, t);
        float jitterStrength = jitterFadeIn * jitterFadeOut;

        float jitter = MathUtils.sin((time + jitterSeed) * MathUtils.PI2 * 7f)
            * 0.035f
            * jitterStrength;

        return pos + jitter;
    }

    private float sampleSettle(float time) {
        float duration = finishTime - settleStartTime;
        float t = MathUtils.clamp((time - settleStartTime) / duration, 0f, 1f);

        float baseOffset = overshootAmount * (1f - easeOutCubic(t));

        float bounce = MathUtils.sin(t * MathUtils.PI2)
            * 0.055f
            * (1f - t);

        return targetPos + baseOffset + bounce;
    }

    private static float premiumSpinProgress(float t) {
        // Piecewise movement:
        // 1. slow acceleration
        // 2. fast cruise
        // 3. smooth deceleration
        float accelEnd = 0.18f;
        float cruiseEnd = 0.72f;

        float accelProgress = 0.08f;
        float cruiseProgress = 0.62f;
        float decelProgress = 0.30f;

        if (t < accelEnd) {
            float local = t / accelEnd;
            return accelProgress * easeInCubic(local);
        }

        if (t < cruiseEnd) {
            float local = (t - accelEnd) / (cruiseEnd - accelEnd);
            return accelProgress + cruiseProgress * local;
        }

        float local = (t - cruiseEnd) / (1f - cruiseEnd);
        return accelProgress + cruiseProgress + decelProgress * easeOutCubic(local);
    }

    private static float easeInCubic(float t) {
        return t * t * t;
    }

    private static float easeOutCubic(float t) {
        float p = 1f - t;
        return 1f - p * p * p;
    }

    private static float smoothStep(float edge0, float edge1, float x) {
        float t = MathUtils.clamp((x - edge0) / (edge1 - edge0), 0f, 1f);
        return t * t * (3f - 2f * t);
    }
}
