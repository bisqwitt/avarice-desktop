package com.avaricious.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;
import java.util.Map;

public final class ScreenShake {

    private static ScreenShake instance;

    public static ScreenShake I() {
        return instance == null ? instance = new ScreenShake() : instance;
    }

    private ScreenShake() {
    }

    private Camera viewportCamera;
    private Camera uiViewportCamera;

    // Baseline
    private final Map<Camera, Vector2> basePos = new HashMap<>();
    private final Map<Camera, Vector3> baseUp = new HashMap<>();
    private boolean baseCaptured = false;

    // Trauma model (0..1)
    private float trauma = 0f;
    private float traumaDecayPerSecond = 1.4f;

    // Tuning
    private float tune = 9f;
    private float maxOffsetPixels = 14f / tune;
    private float maxRollDeg = 1.8f / tune;
    private float frequencyHz = 18f / tune;

    // Time & seeds
    private float time = 0f;
    private float seedX = MathUtils.random(0f, 9999f);
    private float seedY = MathUtils.random(0f, 9999f);
    private float seedR = MathUtils.random(0f, 9999f);

    // Track current roll so we can undo cleanly
    private float currentRollDeg = 0f;

    public ScreenShake setCameras(Camera viewportCamera, Camera uiViewportCamera) {
        this.viewportCamera = viewportCamera;
        this.uiViewportCamera = uiViewportCamera;
        this.baseCaptured = false; // recapture baseline next update
        this.currentRollDeg = 0f;
        return this;
    }

    public void addTrauma(float amount) {
        trauma = MathUtils.clamp(trauma + amount, 0f, 1f);
    }

    /**
     * Call after resize or whenever you "snap" the camera to a new base.
     */
    public void captureBaseNow() {
        basePos.put(viewportCamera, new Vector2(viewportCamera.position.x, viewportCamera.position.y));
        baseUp.put(viewportCamera, new Vector3(viewportCamera.up));
        basePos.put(uiViewportCamera, new Vector2(uiViewportCamera.position.x, uiViewportCamera.position.y));
        baseUp.put(uiViewportCamera, new Vector3(uiViewportCamera.up));
        baseCaptured = true;
    }

    public void update(float delta) {
        if (!baseCaptured) {
            captureBaseNow();
        }

        if (trauma <= 0.0001f) {
            restore();
            return;
        }

        time += delta;
        trauma = MathUtils.clamp(trauma - traumaDecayPerSecond * delta, 0f, 1f);

        float shake = trauma * trauma; // nonlinear = nicer

        float w = MathUtils.PI2 * frequencyHz;

        float nx = smoothNoise(seedX + time * 1.7f);
        float ny = smoothNoise(seedY + time * 1.9f);
        float nr = smoothNoise(seedR + time * 1.3f);

        float oscX = MathUtils.sin(w * time) * 0.55f + (nx - 0.5f) * 0.9f;
        float oscY = MathUtils.sin(w * time + 1.7f) * 0.55f + (ny - 0.5f) * 0.9f;

        float offsetX = oscX * (maxOffsetPixels * shake);
        float offsetY = oscY * (maxOffsetPixels * shake);

        // Translation
        viewportCamera.position.set(basePos.get(viewportCamera).x + offsetX, basePos.get(viewportCamera).y + offsetY, viewportCamera.position.z);
        uiViewportCamera.position.set(basePos.get(uiViewportCamera).x + offsetX * 100, basePos.get(uiViewportCamera).y + offsetY * 100, uiViewportCamera.position.z);

        // Roll (rotate UP around the camera's forward axis)
        float targetRoll = (nr - 0.5f) * 2f * (maxRollDeg * shake);
        applyRoll(targetRoll);

        viewportCamera.update();
        uiViewportCamera.update();
    }

    public boolean isShaking() {
        return trauma > 0.0001f;
    }

    public ScreenShake setMaxOffsetPixels(float px) {
        this.maxOffsetPixels = px;
        return this;
    }

    public ScreenShake setMaxRollDeg(float deg) {
        this.maxRollDeg = deg;
        return this;
    }

    public ScreenShake setFrequencyHz(float hz) {
        this.frequencyHz = hz;
        return this;
    }

    public ScreenShake setTraumaDecay(float perSec) {
        this.traumaDecayPerSecond = perSec;
        return this;
    }

    private void restore() {
        // Restore orientation and position
        viewportCamera.up.set(baseUp.get(viewportCamera));
        uiViewportCamera.up.set(baseUp.get(uiViewportCamera));
        currentRollDeg = 0f;
        viewportCamera.position.set(basePos.get(viewportCamera).x, basePos.get(viewportCamera).y, viewportCamera.position.z);
        uiViewportCamera.position.set(basePos.get(uiViewportCamera).x, basePos.get(uiViewportCamera).y, uiViewportCamera.position.z);
    }

    private void applyRoll(float targetRollDeg) {
        // Reset to base up, then apply target roll (prevents drift)
        viewportCamera.up.set(baseUp.get(viewportCamera));
        uiViewportCamera.up.set(baseUp.get(uiViewportCamera));

        if (Math.abs(targetRollDeg) > 0.0001f) {
            // Rotate UP vector around the forward axis (direction)
            viewportCamera.up.rotate(viewportCamera.direction, targetRollDeg);
            uiViewportCamera.up.rotate(uiViewportCamera.direction, targetRollDeg);
        }
        currentRollDeg = targetRollDeg;
    }

    // --- smooth noise helpers ---

    private float smoothNoise(float x) {
        int x0 = (int) Math.floor(x);
        int x1 = x0 + 1;

        float t = x - x0;
        t = t * t * (3f - 2f * t); // smoothstep

        float v0 = hashTo01(x0);
        float v1 = hashTo01(x1);

        return MathUtils.lerp(v0, v1, t);
    }

    private float hashTo01(int n) {
        n = (n << 13) ^ n;
        int nn = (n * (n * n * 15731 + 789221) + 1376312589);
        nn &= 0x7fffffff;
        return nn / 2147483647f;
    }
}
