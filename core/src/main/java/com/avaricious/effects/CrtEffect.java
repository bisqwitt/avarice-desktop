package com.avaricious.effects;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

public class CrtEffect extends ShaderVfxEffect implements ChainVfxEffect {

    private static final String U_TEXTURE = "u_texture0";
    private static final String U_TIME = "u_time";
    private static final String U_RESOLUTION = "u_resolution";

    private float time;

    private float width;
    private float height;

    public CrtEffect() {
        super(VfxGLUtils.compileShader(
            Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
            Gdx.files.internal("shaders/crt.frag")
        ));

        rebind();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        time += delta;

        setUniform(U_TIME, time);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        this.width = width;
        this.height = height;

//        setUniform(U_RESOLUTION, width, height);
    }

    @Override
    public void rebind() {
        super.rebind();

        program.begin();

        program.setUniformi(U_TEXTURE, TEXTURE_HANDLE0);
        program.setUniformf(U_TIME, time);
        program.setUniformf(U_RESOLUTION, width, height);

        program.end();
    }

    @Override
    public void render(
        VfxRenderContext context,
        VfxPingPongWrapper buffers
    ) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    private void render(
        VfxRenderContext context,
        VfxFrameBuffer src,
        VfxFrameBuffer dst
    ) {
        src.getTexture().bind(TEXTURE_HANDLE0);

        renderShader(context, dst);
    }
}
