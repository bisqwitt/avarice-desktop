package com.avaricious;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

/**
 * Horizontal progress bar with animated rainbow fill.
 * <p>
 * Features:
 * - maxValue / currentValue
 * - smoothly animates displayed value toward target value
 * - moving rainbow phase (continuous over time)
 * - optional soft vertical alpha falloff
 * <p>
 * Vertex layout:
 * x, y, r, g, b, a, fade
 */
public class RainbowProgressBar {

    private static RainbowProgressBar instance;

    public static RainbowProgressBar I() {
        return instance == null ? instance = new RainbowProgressBar() : instance;
    }

    private float maxValue = 100f;
    private float targetValue = 0f;
    private float displayedValue = 0f;

    /**
     * Units per second toward target.
     * Increase for faster snapping.
     */
    public float moveSpeed = 120f;

    /**
     * If true, uses exponential smoothing instead of fixed speed.
     */
    public boolean useSmoothDamp = false;

    /**
     * Only used when useSmoothDamp = true.
     * Higher = faster convergence.
     */
    public float smoothSharpness = 10f;

    /**
     * Rainbow movement in cycles/sec.
     * Example: 0.15 = full rainbow scroll every ~6.67 sec.
     */
    public float phaseSpeed = 0.25f;

    /**
     * Random hue offset on reset if you want variation.
     */
    public float phaseOffset = 0f;

    /**
     * Optional wobble to make the rainbow feel more alive.
     */
    public float hueWobbleStrength = 0.05f;
    public float hueWobbleWaves = 3f;
    public float hueWobbleSpeed = 0.75f;

    /**
     * Bar visuals.
     */
    public final Color backgroundColor = new Color(0f, 0f, 0f, 0.35f);
    public float fillAlpha = 1f;
    public int segments = 96;

    private float globalTime = 0f;

    private Mesh bgMesh;
    private Mesh fillMesh;
    private ShaderProgram shader;
    private final Matrix4 proj = new Matrix4();

    // cache
    private int cachedSegments = -1;

    // background quad vertices/indices
    private float[] bgVertices;
    private short[] bgIndices;

    // fill strip vertices/indices
    private float[] fillVertices;
    private short[] fillIndices;

    private final Color tmp = new Color();

    private RainbowProgressBar() {
        // ShaderProgram.pedantic = false;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = Math.max(0.0001f, maxValue);
        targetValue = MathUtils.clamp(targetValue, 0f, this.maxValue);
        displayedValue = MathUtils.clamp(displayedValue, 0f, this.maxValue);
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setValue(float value) {
        targetValue = MathUtils.clamp(value, 0f, maxValue);
    }

    public void setValueImmediate(float value) {
        targetValue = MathUtils.clamp(value, 0f, maxValue);
        displayedValue = targetValue;
    }

    public float getTargetValue() {
        return targetValue;
    }

    public float getDisplayedValue() {
        return displayedValue;
    }

    public float getProgress01() {
        return displayedValue / maxValue;
    }

    public void update(float delta) {
        globalTime += delta;

        if (useSmoothDamp) {
            float t = 1f - (float) Math.exp(-smoothSharpness * delta);
            displayedValue += (targetValue - displayedValue) * t;
        } else {
            displayedValue = approach(displayedValue, targetValue, moveSpeed * delta);
        }

        if (Math.abs(displayedValue - targetValue) < 0.0001f) {
            displayedValue = targetValue;
        }
    }

    private static float approach(float current, float target, float maxDelta) {
        if (current < target) return Math.min(current + maxDelta, target);
        if (current > target) return Math.max(current - maxDelta, target);
        return target;
    }

    /**
     * Safe to call while batch is drawing.
     */
    public void render(SpriteBatch batch, float x, float y, float width, float height, float delta) {
        update(delta);

        boolean wasDrawing = batch.isDrawing();
        Matrix4 batchProj = new Matrix4(batch.getProjectionMatrix());

        if (wasDrawing) batch.end();

        renderInternal(x, y, width, height, batchProj);

        if (wasDrawing) batch.begin();
    }

    private void renderInternal(float x, float y, float width, float height, Matrix4 batchProj) {
        if (width <= 0f || height <= 0f) return;

        ensureResources();

        float progress = MathUtils.clamp(displayedValue / maxValue, 0f, 1f);
        float fillWidth = width * progress;

        buildBackgroundVertices(x, y, width, height);

        if (fillWidth > 0.001f) {
            float phase = (phaseOffset + globalTime * phaseSpeed) % 1f;
            buildFillVertices(x, y, fillWidth, height, phase);
        }

        proj.set(batchProj);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniformMatrix("u_projTrans", proj);

        bgMesh.setVertices(bgVertices);
        bgMesh.setIndices(bgIndices);
        bgMesh.render(shader, GL20.GL_TRIANGLES);

        if (fillWidth > 0.001f) {
            fillMesh.setVertices(fillVertices);
            fillMesh.setIndices(fillIndices);
            fillMesh.render(shader, GL20.GL_TRIANGLES);
        }
    }

    private void ensureResources() {
        if (shader == null) {
            shader = buildShader();
            if (!shader.isCompiled()) {
                throw new IllegalStateException("RainbowProgressBar shader compile error:\n" + shader.getLog());
            }
        }

        if (bgMesh == null) {
            bgVertices = new float[4 * 7];
            bgIndices = new short[]{0, 1, 2, 2, 1, 3};

            bgMesh = new Mesh(false, 4, 6,
                new VertexAttribute(Usage.Position, 2, "a_position"),
                new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(Usage.Generic, 1, "a_fade"));
        }

        if (fillMesh == null || cachedSegments != segments) {
            if (fillMesh != null) fillMesh.dispose();

            cachedSegments = Math.max(1, segments);

            int points = cachedSegments + 1;
            int vertsCount = points * 2; // bottom + top per point
            fillVertices = new float[vertsCount * 7];
            fillIndices = new short[cachedSegments * 6];

            int idx = 0;
            for (int i = 0; i < cachedSegments; i++) {
                short b0 = (short) (2 * i);
                short t0 = (short) (2 * i + 1);
                short b1 = (short) (2 * (i + 1));
                short t1 = (short) (2 * (i + 1) + 1);

                fillIndices[idx++] = b0;
                fillIndices[idx++] = t0;
                fillIndices[idx++] = b1;

                fillIndices[idx++] = t0;
                fillIndices[idx++] = t1;
                fillIndices[idx++] = b1;
            }

            fillMesh = new Mesh(false, vertsCount, fillIndices.length,
                new VertexAttribute(Usage.Position, 2, "a_position"),
                new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(Usage.Generic, 1, "a_fade"));
        }
    }

    private ShaderProgram buildShader() {
        String vert =
            "attribute vec2 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute float a_fade;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "varying float v_fade;\n" +
                "void main(){\n" +
                "    v_color = a_color;\n" +
                "    v_fade = a_fade;\n" +
                "    gl_Position = u_projTrans * vec4(a_position.xy, 0.0, 1.0);\n" +
                "}\n";

        String frag =
            "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying float v_fade;\n" +
                "void main(){\n" +
                "    float falloff = 1.0 - abs(v_fade * 2.0 - 1.0);\n" +
                "    falloff = pow(falloff, 0.9);\n" +
                "    gl_FragColor = vec4(v_color.rgb, v_color.a * falloff);\n" +
                "}\n";

        return new ShaderProgram(vert, frag);
    }

    private void buildBackgroundVertices(float x, float y, float width, float height) {
        // 4 vertices * 7 floats
        int i = 0;

        // bottom-left
        bgVertices[i++] = x;
        bgVertices[i++] = y;
        bgVertices[i++] = backgroundColor.r;
        bgVertices[i++] = backgroundColor.g;
        bgVertices[i++] = backgroundColor.b;
        bgVertices[i++] = backgroundColor.a;
        bgVertices[i++] = 0f;

        // top-left
        bgVertices[i++] = x;
        bgVertices[i++] = y + height;
        bgVertices[i++] = backgroundColor.r;
        bgVertices[i++] = backgroundColor.g;
        bgVertices[i++] = backgroundColor.b;
        bgVertices[i++] = backgroundColor.a;
        bgVertices[i++] = 1f;

        // bottom-right
        bgVertices[i++] = x + width;
        bgVertices[i++] = y;
        bgVertices[i++] = backgroundColor.r;
        bgVertices[i++] = backgroundColor.g;
        bgVertices[i++] = backgroundColor.b;
        bgVertices[i++] = backgroundColor.a;
        bgVertices[i++] = 0f;

        // top-right
        bgVertices[i++] = x + width;
        bgVertices[i++] = y + height;
        bgVertices[i++] = backgroundColor.r;
        bgVertices[i++] = backgroundColor.g;
        bgVertices[i++] = backgroundColor.b;
        bgVertices[i++] = backgroundColor.a;
        bgVertices[i++] = 1f;
    }

    private void buildFillVertices(float x, float y, float width, float height, float phase) {
        int S = cachedSegments;
        float total = Math.max(1f, width);

        int vi = 0;

        for (int s = 0; s <= S; s++) {
            float a = (float) s / S;
            float px = x + width * a;

            float hue = (a + phase) % 1f;

            if (hueWobbleStrength != 0f && hueWobbleWaves != 0f && hueWobbleSpeed != 0f) {
                float wobblePhase = a * MathUtils.PI2 * hueWobbleWaves
                    + globalTime * MathUtils.PI2 * hueWobbleSpeed;
                hue += hueWobbleStrength * MathUtils.sin(wobblePhase);
                hue %= 1f;
                if (hue < 0f) hue += 1f;
            }

            tmp.fromHsv(hue * 360f, 1f, 1f);
            tmp.a = fillAlpha;

            // bottom vertex
            fillVertices[vi++] = px;
            fillVertices[vi++] = y;
            fillVertices[vi++] = tmp.r;
            fillVertices[vi++] = tmp.g;
            fillVertices[vi++] = tmp.b;
            fillVertices[vi++] = tmp.a;
            fillVertices[vi++] = 0f;

            // top vertex
            fillVertices[vi++] = px;
            fillVertices[vi++] = y + height;
            fillVertices[vi++] = tmp.r;
            fillVertices[vi++] = tmp.g;
            fillVertices[vi++] = tmp.b;
            fillVertices[vi++] = tmp.a;
            fillVertices[vi++] = 1f;
        }
    }

    public void dispose() {
        if (bgMesh != null) {
            bgMesh.dispose();
            bgMesh = null;
        }
        if (fillMesh != null) {
            fillMesh.dispose();
            fillMesh = null;
        }
        if (shader != null) {
            shader.dispose();
            shader = null;
        }
    }
}
