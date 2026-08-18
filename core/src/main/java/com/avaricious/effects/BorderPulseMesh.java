package com.avaricious.effects;

import com.avaricious.utility.GameContext;
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
 * Border pulse rendered as a ring mesh in screen space.
 * <p>
 * Changes vs your original:
 * - Adds globalTime so rainbow can move continuously (not tied to pulse time t)
 * - Adds phaseOffset randomized per trigger so colors don't start in same place
 * - Uses phaseSpeed as cycles/sec to scroll hue around the perimeter
 * - Optional subtle hue wobble (traveling wave) to avoid rigid linear gradient
 */
public class BorderPulseMesh {

    private static BorderPulseMesh instance;

    public static BorderPulseMesh I() {
        return instance == null ? (instance = new BorderPulseMesh()) : instance;
    }

    // Pulse timing
    private float duration = 0.8f;
    private float t = 0f;
    private boolean active = false;

    // Continuous time for hue animation (keeps moving across triggers)
    private float globalTime = 0f;

    // Random per-trigger hue offset so it doesn't always align the same
    private float phaseOffset = 0f;

    // Thickness in PIXELS (screen space)
    public float baseThickness = 0f;
    public float pulseExtraThickness = 0.55f;

    /**
     * Rainbow scroll speed around the border, in cycles per second.
     * Example: 0.25 = one full revolution every 4 seconds.
     */
    public float phaseSpeed = 0.25f;

    /**
     * Optional hue wobble to make the gradient feel more "alive".
     * Set strength to 0 to disable.
     */
    public float hueWobbleStrength = 0.06f; // 0..~0.15 is reasonable
    public float hueWobbleWaves = 6f;       // number of ripples around perimeter
    public float hueWobbleSpeed = 0.8f;     // cycles per second

    // Mesh quality: higher = smoother rainbow around edges
    public int segmentsPerEdge = 96;

    private Mesh mesh;
    private ShaderProgram shader;
    private final Matrix4 proj = new Matrix4();

    // cached sizing to rebuild buffers when needed
    private int cachedSegments = -1;
    private float cachedW = -1, cachedH = -1;

    // working buffers
    // Vertex layout: x,y,r,g,b,a,fade (7 floats per vertex)
    // fade: 0 = outer edge, 1 = inner edge (used in fragment shader to fade off)
    private float[] vertices;
    private short[] indices;

    private final Color tmp = new Color();
    private Type type = Type.RAINBOW;

    private BorderPulseMesh() {
        // ShaderProgram.pedantic = false;
    }

    public void triggerOnce(Type type) {
        t = 0f;
        active = true;
        this.type = type;

        // Randomize starting hue alignment each pulse (so colors aren't always identical)
        phaseOffset = MathUtils.random(); // 0..1
    }

    public boolean isActive() {
        return active;
    }

    public void update(float delta) {
        // Keep global time moving always (so hue drift doesn't reset)
        globalTime += delta;

        if (!active) return;

        t += delta;
        if (t >= duration) {
            t = duration;
            active = false;
        }
    }

    /**
     * Call this while batch is drawing; it will end/begin safely.
     */
    public void render(SpriteBatch batch, float delta) {
        update(delta);
        if (!active) return;

        boolean wasDrawing = batch.isDrawing();
        if (wasDrawing) batch.end();

        renderInternal();

        if (wasDrawing) batch.begin();
    }

    private void renderInternal() {
        final float screenW = GameContext.I().viewport.getWorldWidth();
        final float screenH = GameContext.I().viewport.getWorldHeight();

        float x, y, w, h;

//        if (type == Type.RAINBOW) {
//            x = SlotMachine.windowBounds.x;
//            y = SlotMachine.windowBounds.y;
//            w = SlotMachine.windowBounds.width;
//            h = SlotMachine.windowBounds.height;
//        } else {
        x = 0f;
        y = 0f;
        w = screenW;
        h = screenH;
//        }

        ensureResources(w, h);

        float u = (duration <= 0f) ? 1f : (t / duration);
        float pulse = easeOutCubic(1f - Math.abs(2f * u - 1f));
        float alpha = 0.15f + 0.85f * pulse;
        float thickness = baseThickness + pulseExtraThickness * pulse;

        float phase = (phaseOffset + (phaseSpeed * globalTime)) % 1f;

        buildBorderRingVertices(x, y, w, h, thickness, alpha, phase);

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        proj.setToOrtho2D(0, 0, screenW, screenH);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniformMatrix("u_projTrans", proj);
        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    private void ensureResources(float W, float H) {
        if (mesh == null || shader == null || cachedSegments != segmentsPerEdge || cachedW != W || cachedH != H) {
            dispose(); // clean old

            cachedSegments = segmentsPerEdge;
            cachedW = W;
            cachedH = H;

            // Total perimeter "points":
            // points = 4*segmentsPerEdge + 1 (closing duplicate of start)
            int points = 4 * segmentsPerEdge + 1;
            int vertsCount = points * 2;            // outer+inner per point
            int floatsPerVert = 7;                 // x,y,r,g,b,a,fade
            vertices = new float[vertsCount * floatsPerVert];

            // Triangles: for each segment between point i and i+1, two triangles = 6 indices
            int segments = points - 1;
            indices = new short[segments * 6];

            // Build indices once (topology constant)
            int idx = 0;
            for (int i = 0; i < segments; i++) {
                short o0 = (short) (2 * i);
                short i0 = (short) (2 * i + 1);
                short o1 = (short) (2 * (i + 1));
                short i1 = (short) (2 * (i + 1) + 1);

                // Triangle 1: o0, i0, o1
                indices[idx++] = o0;
                indices[idx++] = i0;
                indices[idx++] = o1;

                // Triangle 2: i0, i1, o1
                indices[idx++] = i0;
                indices[idx++] = i1;
                indices[idx++] = o1;
            }

            mesh = new Mesh(false, vertsCount, indices.length,
                new VertexAttribute(Usage.Position, 2, "a_position"),
                new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(Usage.Generic, 1, "a_fade"));

            shader = buildShader();
            if (!shader.isCompiled()) {
                throw new IllegalStateException("RainbowBorder shader compile error:\n" + shader.getLog());
            }
        }
    }

    private ShaderProgram buildShader() {
        final String vert =
            "attribute vec2 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute float a_fade;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "varying float v_fade;\n" +
                "void main(){\n" +
                "  v_color = a_color;\n" +
                "  v_fade = a_fade;\n" +
                "  gl_Position = u_projTrans * vec4(a_position.xy, 0.0, 1.0);\n" +
                "}\n";

        final String frag =
            "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying float v_fade;\n" +
                "void main(){\n" +
                "  // v_fade: 0 outer -> 1 inner\n" +
                "  float falloff = 1.0 - smoothstep(0.0, 1.0, v_fade);\n" +
                "  falloff = pow(falloff, 1.8);\n" +
                "  gl_FragColor = vec4(v_color.rgb, v_color.a * falloff);\n" +
                "}\n";

        return new ShaderProgram(vert, frag);
    }

    /**
     * Builds a continuous perimeter ring:
     * - bottom:  i=0..S    (includes (0,0) and (W,0))
     * - right:   i=1..S    (excludes (W,0), includes (W,H))
     * - top:     i=1..S    (excludes (W,H), includes (0,H))
     * - left:    i=1..S-1  (excludes (0,H) and (0,0))
     * then duplicates start point to close strip.
     */
    private void buildBorderRingVertices(float startX, float startY, float W, float H, float thickness, float alpha, float phase) {
        float P = 2f * (W + H); // perimeter

        // Clamp thickness so it never eats whole screen (safety)
        thickness = Math.min(thickness, Math.min(W, H) * 0.49f);

        int S = segmentsPerEdge;

        final int[] v = {0}; // float index in vertices array

        class Writer {
            void write(float ox, float oy, float ix, float iy, float s) {
                if (type == Type.RAINBOW) {
                    float base = (s / P + phase) % 1f;

                    // Optional moving wobble (traveling wave) to avoid rigid gradient
                    if (hueWobbleStrength != 0f && hueWobbleWaves != 0f && hueWobbleSpeed != 0f) {
                        float wobblePhase = (s / P) * MathUtils.PI2 * hueWobbleWaves
                            + (globalTime * MathUtils.PI2 * hueWobbleSpeed);
                        float wobble = hueWobbleStrength * MathUtils.sin(wobblePhase);
                        base = (base + wobble) % 1f;
                        if (base < 0f) base += 1f;
                    }

                    tmp.fromHsv(base * 360f, 1f, 1f);
                } else {
                    tmp.set(1f, 0f, 0f, 1f);
                }
                tmp.a = alpha;

                // outer vertex (fade = 0)
                vertices[v[0]++] = ox;
                vertices[v[0]++] = oy;
                vertices[v[0]++] = tmp.r;
                vertices[v[0]++] = tmp.g;
                vertices[v[0]++] = tmp.b;
                vertices[v[0]++] = tmp.a;
                vertices[v[0]++] = 0f;

                // inner vertex (fade = 1)
                vertices[v[0]++] = ix;
                vertices[v[0]++] = iy;
                vertices[v[0]++] = tmp.r;
                vertices[v[0]++] = tmp.g;
                vertices[v[0]++] = tmp.b;
                vertices[v[0]++] = tmp.a;
                vertices[v[0]++] = 1f;
            }
        }
        Writer w = new Writer();

        // Bottom edge (0,0) -> (W,0)
        for (int i = 0; i <= S; i++) {
            float localX = (W * i) / S;
            float localY = 0f;

            float px = startX + localX;
            float py = startY + localY;

            float s = localX;
            w.write(px, py, px, py + thickness, s);
        }

        // Right edge (W,0) -> (W,H), skip first corner
        for (int i = 1; i <= S; i++) {
            float localX = W;
            float localY = (H * i) / S;

            float px = startX + localX;
            float py = startY + localY;

            float s = W + localY;
            w.write(px, py, px - thickness, py, s);
        }

        // Top edge (W,H) -> (0,H), skip first corner
        for (int i = 1; i <= S; i++) {
            float localX = W - (W * i) / S;
            float localY = H;

            float px = startX + localX;
            float py = startY + localY;

            float s = W + H + (W - localX);
            w.write(px, py, px, py - thickness, s);
        }

        // Left edge (0,H) -> (0,0), skip both corners
        for (int i = 1; i <= S - 1; i++) {
            float localX = 0f;
            float localY = H - (H * i) / S;

            float px = startX + localX;
            float py = startY + localY;

            float s = 2f * W + H + (H - localY);
            w.write(px, py, px + thickness, py, s);
        }

        // Close the loop by duplicating the first point (0,0)
        {
            float px = startX, py = startY;
            float s = 0f;
            w.write(px, py, px, py + thickness, s);
        }
    }

    private static float easeOutCubic(float x) {
        x = MathUtils.clamp(x, 0f, 1f);
        return 1f - (float) Math.pow(1f - x, 3);
    }

    public void dispose() {
        if (mesh != null) {
            mesh.dispose();
            mesh = null;
        }
        if (shader != null) {
            shader.dispose();
            shader = null;
        }
    }

    public enum Type {
        RAINBOW,
        BLOODY
    }
}
