package com.avaricious.components.background;

import com.avaricious.utility.GameContext;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public class SlotScreenBackground implements Disposable {

    private ShaderProgram shader;
    private Mesh mesh;
    private float time = 0f;

    public SlotScreenBackground() {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(
            Gdx.files.internal("shader/tv_static.vert"), // reuse same vert
            Gdx.files.internal("shader/slot_screen.frag")
        );
        if (!shader.isCompiled()) {
            Gdx.app.error("SlotScreen", shader.getLog());
        }

        mesh = new Mesh(true, 4, 6,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
        );

        // indices
        mesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});
    }

    /**
     * Call this every frame AFTER the felt background, BEFORE your SpriteBatch.
     *
     * @param x      left edge of slot grid in screen pixels
     * @param y      bottom edge of slot grid in screen pixels
     * @param width  width of slot grid in screen pixels
     * @param height height of slot grid in screen pixels
     */
    public void render(float delta, float x, float y, float width, float height) {
        time += delta;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        // Convert pixel rect → clip space (-1 to 1)
        float x0 = (x / sw) * 2f - 1f;
        float x1 = ((x + width) / sw) * 2f - 1f;
        float y0 = (y / sh) * 2f - 1f;
        float y1 = ((y + height) / sh) * 2f - 1f;

        mesh.setVertices(new float[]{
            x, y, 0f, 0f,
            x + width, y, 1f, 0f,
            x + width, y + height, 1f, 1f,
            x, y + height, 0f, 1f,
        });

        shader.bind();
        shader.setUniformf("u_time", time);
        shader.setUniformf("u_resolution", width, height); // note: now in world units, adjust scanline density in frag if needed
        shader.setUniformMatrix("u_projTrans", GameContext.I().viewport.getCamera().combined); // <-- your camera.combined
        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    @Override
    public void dispose() {
        shader.dispose();
        mesh.dispose();
    }
}
