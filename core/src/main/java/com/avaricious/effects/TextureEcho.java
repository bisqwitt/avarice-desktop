package com.avaricious.effects;

import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TextureEcho {

    private final Type parentType;
    private final TextureRegion texture;
    private final Rectangle bounds;

    private final Color GLOW_COLOR = new Color(0.6f, 0.45f, 0.2f, 1f);
    private final float duration = 0.75f;
    private final float maxGlowScale;
    private float age = 0f;

    private static final List<TextureEcho> glows = new ArrayList<>();

    public static void create(TextureRegion texture, Rectangle spawnPoint, Type parentType, float streak) {
//        glows.add(new TextureGlow(texture, spawnPoint, parentType, 1.5f + (0.4f * streak)));
        glows.add(new TextureEcho(texture, spawnPoint, parentType, 3f));
    }

    public static void draw(float delta) {
        List<TextureEcho> dump = new ArrayList<>();
        for (TextureEcho textureEcho : glows) {
            if (textureEcho.parentType.equals(Type.SLOT)) textureEcho._draw(delta);
            if (textureEcho.dead()) dump.add(textureEcho);
        }
        glows.removeAll(dump);
    }

    private TextureEcho(TextureRegion texture, Rectangle spawnPoint, Type parentType, float maxScale) {
        this.texture = texture;
        this.bounds = new Rectangle(spawnPoint);
        this.parentType = parentType;
        this.maxGlowScale = maxScale;
    }

    private void _draw(float delta) {
        age += delta;

        float t = Math.min(age / duration, 1f); // 0 → 1 over lifetime

        // ---- SCALE: grows for entire lifetime ----
        float g = Interpolation.pow2Out.apply(t);
        float glowScale = 1f + (maxGlowScale - 1f) * g;

        // ---- ALPHA: fade to zero over same lifetime ----
        float glowAlpha = Interpolation.fade.apply(1f - t);

        // Center from bounds (bottom-left anchored)
        float cx = bounds.x + bounds.width * 0.5f;
        float cy = bounds.y + bounds.height * 0.5f;

        float drawW = bounds.width * glowScale;
        float drawH = bounds.height * glowScale;

        float x = cx - drawW * 0.5f;
        float y = cy - drawH * 0.5f;


        Pencil.I().addDrawing(new TextureDrawing(
            texture,
            x, y, drawW, drawH,
            ZIndex.TEXTURE_GLOW, new Color(GLOW_COLOR.r, GLOW_COLOR.g, GLOW_COLOR.b, glowAlpha)
        ).setBeforeDrawing(() -> GameContext.I().batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE))
            .setAfterDrawing(() -> GameContext.I().batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)));
    }


    private boolean dead() {
        return age > duration;
    }


    public enum Type {
        SLOT,
        NUMBER
    }

}
