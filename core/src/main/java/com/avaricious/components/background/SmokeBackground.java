package com.avaricious.components.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SmokeBackground {

    private ShaderProgram shader;
    private final Mesh mesh;
    private float time;

    public SmokeBackground() {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(
            Gdx.files.internal("backgroundShader/smoke.vert"),
            Gdx.files.internal("backgroundShader/smoke.frag")
        );
        if(!shader.isCompiled()) {
            throw new GdxRuntimeException("Shader compile error:\n" + shader.getLog());
        }
        mesh = new Mesh(
            true,
            4, 6,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position")
        );

        float[] verts = new float[] {
            -1f, -1f,  // bottom-left
            1f, -1f,  // bottom-right
            1f,  1f,  // top-right
            -1f,  1f   // top-left
        };

        short[] indices = new short[] {
            0, 1, 2,
            2, 3, 0
        };

        mesh.setVertices(verts);
        mesh.setIndices(indices);
    }

    public void render(float delta) {
        time += delta * 2;

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw shader background
        shader.bind();
        shader.setUniformf("u_pixelSize", 8.0f);
        shader.setUniformf("u_tint", 0.396f, 0.137f, 0.141f);
        shader.setUniformf("u_time", time);
        shader.setUniformf(
            "u_resolution",
            (float) Gdx.graphics.getWidth(),
            (float) Gdx.graphics.getHeight()
        );

        // If you want mouse in screen coords, otherwise remove this uniform
        shader.setUniformf(
            "u_mouse",
            (float) Gdx.input.getX(),
            (float) (Gdx.graphics.getHeight() - Gdx.input.getY())
        );

        mesh.render(shader, GL20.GL_TRIANGLES);

//        batch.begin();
//        // Draw a single fullscreen quad
//        batch.draw(whiteTex,
//            0, 0,
//            Gdx.graphics.getWidth(),
//            Gdx.graphics.getHeight());
//        batch.end();
    }

}
