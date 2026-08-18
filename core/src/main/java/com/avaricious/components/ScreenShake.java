package com.avaricious.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public final class ScreenShake {

    private static ScreenShake instance;

    public static ScreenShake I() {
        return instance == null ? instance = new ScreenShake() : instance;
    }

    private ScreenShake() {
    }

    private static final float SHAKE_EPSILON = 0.0001f;

    private Camera viewportCamera;
    private Camera uiViewportCamera;

    // ------------------------------------------------
    // BASE CAMERA STATE
    // ------------------------------------------------

    private final Vector2 viewportBasePos = new Vector2();
    private final Vector2 uiBasePos = new Vector2();

    private final Vector3 viewportBaseUp = new Vector3();
    private final Vector3 uiBaseUp = new Vector3();

    private boolean baseCaptured = false;
    private boolean wasShaking = false;

    // ------------------------------------------------
    // TRAUMA
    // ------------------------------------------------

    private float trauma = 0f;

    /*
     * 0.05 -> ~0.03 seconds
     * 0.20 -> ~0.13 seconds
     * 0.30 -> ~0.19 seconds
     *
     * The actual visible shake feels slightly longer because
     * the amplitude gradually decreases.
     */
    private float traumaDecayPerSecond = 1.6f;

    // ------------------------------------------------
    // TUNING
    // ------------------------------------------------

    /*
     * IMPORTANT:
     * This is in WORLD UNITS, not actual pixels.
     *
     * 0.60 sounds large, but normal gameplay trauma never
     * reaches 1.0.
     */
    private float maxOffset = 0.60f;

    /*
     * Keep roll significantly weaker than translation.
     * Too much roll makes repeated slot hits uncomfortable.
     */
    private float maxRollDeg = 0.9f;

    /*
     * Your previous value:
     *
     * 18 / 9 = 2 Hz
     *
     * was much too slow for an impact shake.
     */
    private float frequencyHz = 17f;

    // ------------------------------------------------
    // TIME / NOISE
    // ------------------------------------------------

    private float time = 0f;

    private float seedX = MathUtils.random(0f, 9999f);
    private float seedY = MathUtils.random(0f, 9999f);
    private float seedR = MathUtils.random(0f, 9999f);

    // ------------------------------------------------

    public ScreenShake setCameras(
        Camera viewportCamera,
        Camera uiViewportCamera
    ) {
        this.viewportCamera = viewportCamera;
        this.uiViewportCamera = uiViewportCamera;

        baseCaptured = false;
        wasShaking = false;
        trauma = 0f;

        return this;
    }

    // ------------------------------------------------
    // TRAUMA
    // ------------------------------------------------

    public void addTrauma(float amount) {

        amount = MathUtils.clamp(amount, 0f, 1f);

        /*
         * Start a new shake with a new noise pattern.
         */
        if (trauma <= SHAKE_EPSILON) {
            reseed();
        }

        /*
         * Diminishing-return accumulation.
         *
         * Instead of:
         *
         * 0.2 + 0.2 + 0.2 + 0.3 = 0.9
         *
         * we get roughly:
         *
         * 0.20
         * 0.36
         * 0.49
         * 0.64
         *
         * This makes combos stronger without instantly
         * reaching maximum camera shake.
         */
        trauma += amount * (1f - trauma);

        trauma = MathUtils.clamp(trauma, 0f, 1f);
    }

    /**
     * Useful if an event should guarantee a certain shake strength
     * without stacking another full trauma amount.
     */
    public void ensureTrauma(float amount) {
        trauma = Math.max(
            trauma,
            MathUtils.clamp(amount, 0f, 1f)
        );
    }

    // ------------------------------------------------
    // UPDATE
    // ------------------------------------------------

    public void update(float delta) {

        if (viewportCamera == null || uiViewportCamera == null) {
            return;
        }

        if (!baseCaptured) {
            captureBaseNow();
        }

        /*
         * No shake.
         */
        if (trauma <= SHAKE_EPSILON) {

            trauma = 0f;

            if (wasShaking) {
                restore();
                wasShaking = false;
            } else {
                /*
                 * Allows another system to move the camera while
                 * ScreenShake is inactive.
                 */
                captureBaseNow();
            }

            return;
        }

        wasShaking = true;

        time += delta;

        /*
         * IMPORTANT:
         *
         * Calculate amplitude BEFORE reducing trauma.
         *
         * Otherwise 0.05 trauma can lose half its strength
         * before its first rendered frame.
         */
        float shake =
            trauma * (0.35f + 0.65f * trauma);

        float frequency =
            MathUtils.PI2 * frequencyHz;

        // ------------------------------------------------
        // FAST SMOOTH NOISE
        // ------------------------------------------------

        float noiseX =
            smoothNoise(
                seedX + time * frequencyHz * 0.85f
            ) * 2f - 1f;

        float noiseY =
            smoothNoise(
                seedY + time * frequencyHz * 0.92f
            ) * 2f - 1f;

        float noiseR =
            smoothNoise(
                seedR + time * frequencyHz * 0.55f
            ) * 2f - 1f;

        // ------------------------------------------------
        // OSCILLATION
        // ------------------------------------------------

        /*
         * Different X/Y frequencies prevent the camera
         * from moving in an obvious repeating diagonal.
         */

        float sineX =
            MathUtils.sin(
                frequency * time + seedX
            );

        float sineY =
            MathUtils.sin(
                frequency * 1.13f * time + seedY
            );

        /*
         * Mostly sharp oscillation, with some smooth random motion.
         */
        float oscX =
            sineX * 0.72f +
                noiseX * 0.28f;

        float oscY =
            sineY * 0.72f +
                noiseY * 0.28f;

        // ------------------------------------------------
        // TRANSLATION
        // ------------------------------------------------

        float offsetX =
            oscX * maxOffset * shake;

        float offsetY =
            oscY * maxOffset * shake;

        viewportCamera.position.set(
            viewportBasePos.x + offsetX,
            viewportBasePos.y + offsetY,
            viewportCamera.position.z
        );

        /*
         * Automatically calculate UI scale instead of
         * hardcoding "* 100".
         */
        float uiScaleX =
            uiViewportCamera.viewportWidth /
                viewportCamera.viewportWidth;

        float uiScaleY =
            uiViewportCamera.viewportHeight /
                viewportCamera.viewportHeight;

        uiViewportCamera.position.set(
            uiBasePos.x + offsetX * uiScaleX,
            uiBasePos.y + offsetY * uiScaleY,
            uiViewportCamera.position.z
        );

        // ------------------------------------------------
        // ROLL
        // ------------------------------------------------

        float targetRoll =
            noiseR * maxRollDeg * shake;

        applyRoll(targetRoll);

        // ------------------------------------------------
        // DECAY
        // ------------------------------------------------

        trauma = Math.max(
            0f,
            trauma - traumaDecayPerSecond * delta
        );

        viewportCamera.update();
        uiViewportCamera.update();
    }

    // ------------------------------------------------
    // BASE
    // ------------------------------------------------

    public void captureBaseNow() {

        if (viewportCamera == null || uiViewportCamera == null) {
            return;
        }

        viewportBasePos.set(
            viewportCamera.position.x,
            viewportCamera.position.y
        );

        uiBasePos.set(
            uiViewportCamera.position.x,
            uiViewportCamera.position.y
        );

        viewportBaseUp.set(viewportCamera.up);
        uiBaseUp.set(uiViewportCamera.up);

        baseCaptured = true;
    }

    private void restore() {

        viewportCamera.position.set(
            viewportBasePos.x,
            viewportBasePos.y,
            viewportCamera.position.z
        );

        uiViewportCamera.position.set(
            uiBasePos.x,
            uiBasePos.y,
            uiViewportCamera.position.z
        );

        viewportCamera.up.set(viewportBaseUp);
        uiViewportCamera.up.set(uiBaseUp);

        viewportCamera.update();
        uiViewportCamera.update();
    }

    // ------------------------------------------------
    // ROTATION
    // ------------------------------------------------

    private void applyRoll(float targetRollDeg) {

        /*
         * Reset first so rotation can never accumulate/drift.
         */
        viewportCamera.up.set(viewportBaseUp);
        uiViewportCamera.up.set(uiBaseUp);

        if (Math.abs(targetRollDeg) > SHAKE_EPSILON) {

            viewportCamera.up.rotate(
                viewportCamera.direction,
                targetRollDeg
            );

            uiViewportCamera.up.rotate(
                uiViewportCamera.direction,
                targetRollDeg
            );
        }
    }

    // ------------------------------------------------
    // NOISE
    // ------------------------------------------------

    private void reseed() {
        seedX = MathUtils.random(0f, 9999f);
        seedY = MathUtils.random(0f, 9999f);
        seedR = MathUtils.random(0f, 9999f);
    }

    private float smoothNoise(float x) {

        int x0 = (int) Math.floor(x);
        int x1 = x0 + 1;

        float t = x - x0;

        // smoothstep
        t = t * t * (3f - 2f * t);

        float v0 = hashTo01(x0);
        float v1 = hashTo01(x1);

        return MathUtils.lerp(v0, v1, t);
    }

    private float hashTo01(int n) {

        n = (n << 13) ^ n;

        int nn =
            n * (n * n * 15731 + 789221)
                + 1376312589;

        nn &= 0x7fffffff;

        return nn / 2147483647f;
    }

    // ------------------------------------------------
    // GETTERS / SETTERS
    // ------------------------------------------------

    public boolean isShaking() {
        return trauma > SHAKE_EPSILON;
    }

    public float getTrauma() {
        return trauma;
    }

    public ScreenShake setMaxOffset(float maxOffset) {
        this.maxOffset = maxOffset;
        return this;
    }

    public ScreenShake setMaxRollDeg(float maxRollDeg) {
        this.maxRollDeg = maxRollDeg;
        return this;
    }

    public ScreenShake setFrequencyHz(float frequencyHz) {
        this.frequencyHz = frequencyHz;
        return this;
    }

    public ScreenShake setTraumaDecay(float traumaDecayPerSecond) {
        this.traumaDecayPerSecond = traumaDecayPerSecond;
        return this;
    }
}
