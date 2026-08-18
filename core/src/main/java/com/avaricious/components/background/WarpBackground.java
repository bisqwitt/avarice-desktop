package com.avaricious.components.background;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class WarpBackground {

    private final ShaderProgram shader;
    private float time;

    private final TextureRegion whiteTexture;

    public WarpBackground() {
        ShaderProgram.pedantic = false;

        whiteTexture = Assets.I().get(AssetKey.WHITE_PIXEL);
        shader = new ShaderProgram(
            Gdx.files.internal("shader/felt_background.vert"),
            Gdx.files.internal("shader/felt_background.frag")
        );
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Shader compile error:\n" + shader.getLog());
        }
    }

    public void render(SpriteBatch batch, float delta) {
        time += delta;
        
        shader.bind();
        shader.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shader.setUniformf("u_time", time);

        batch.setShader(shader);
        batch.begin();
        batch.draw(whiteTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        batch.setShader(null);
    }
}
