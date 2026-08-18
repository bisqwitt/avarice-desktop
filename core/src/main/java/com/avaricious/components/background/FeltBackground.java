package com.avaricious.components.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class FeltBackground implements Disposable {

    private ShaderProgram shader;
    private Mesh mesh;
    private float time = 0f;

    public FeltBackground() {
        ShaderProgram.pedantic = false;

        shader = new ShaderProgram(
            Gdx.files.internal("shader/tv_static.vert"),
            Gdx.files.internal("shader/tv_static.frag")
        );

        if (!shader.isCompiled()) {
            Gdx.app.error("TvStatic", shader.getLog());
        }

        // Full-screen quad  (x, y, u, v)
        mesh = new Mesh(true, 4, 6,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
        );

        mesh.setVertices(new float[]{
            -1f, -1f, 0f, 0f,   // bottom-left
            1f, -1f, 1f, 0f,   // bottom-right
            1f, 1f, 1f, 1f,   // top-right
            -1f, 1f, 0f, 1f    // top-left
        });

        mesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});
    }

    public void render(float delta) {
        time += delta;

        shader.bind();
        shader.setUniformf("u_time", time);
        shader.setUniformf("u_resolution",
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());

        // Identity matrix — clip-space quad needs no camera transform
        shader.setUniformMatrix("u_projTrans", new Matrix4().idt());

        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    @Override
    public void dispose() {
        shader.dispose();
        mesh.dispose();
    }
}
