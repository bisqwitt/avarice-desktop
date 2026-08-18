package com.avaricious.effects;

import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class GlowBorder {

    private static float stateTime = 0f;
    private static float hover = 0f;

    public static void drawGlowBorder(TextureRegion textureRegion, Rectangle r, boolean hovering, ZIndex layer, float delta) {
        stateTime += delta;
        hover = approach(hover, hovering ? 1f : 0f, 12f, delta);

        // pulse 0..1
        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 8f);

        // hover is already smooth 0..1
        float thickness = 0.05f + 0.04f * hover;

        float baseAlpha = (0.65f + 0.25f * pulse) + 0.25f * hover;
        float glowAlpha = (0.18f + 0.12f * pulse) + 0.18f * hover;

        baseAlpha = Math.min(baseAlpha, 1f);
        glowAlpha = Math.min(glowAlpha, 1f);

        drawRectOutline(textureRegion, r, thickness, new Color(1f, 1f, 1f, baseAlpha), layer);

        drawRectOutline(textureRegion, r, thickness * 2.5f, new Color(1f, 1f, 1f, glowAlpha), layer);

        // Extra pass scales with hover (instead of boolean)
        if (hover > 0.001f) {
            float extraAlpha = (0.10f + 0.08f * pulse) * hover; // fade in/out smoothly
            drawRectOutline(textureRegion, r, thickness * (4.0f + hover), new Color(1f, 1f, 1f, extraAlpha), layer);
        }
    }

    private static void drawRectOutline(TextureRegion px, Rectangle r, float t, Color color, ZIndex layer) {
        // bottom
        Pencil.I().addDrawing(new TextureDrawing(
            px, r.x - t, r.y - t, r.width + 2f * t, t,
            layer, color
        ));
        // top
        Pencil.I().addDrawing(new TextureDrawing(
            px, r.x - t, r.y + r.height, r.width + 2f * t, t,
            layer, color
        ));
        // left
        Pencil.I().addDrawing(new TextureDrawing(
            px, r.x - t, r.y, t, r.height,
            layer, color
        ));
        // right
        Pencil.I().addDrawing(new TextureDrawing(
            px, r.x + r.width, r.y, t, r.height,
            layer, color
        ));
    }

    private static float approach(float current, float target, float speed, float delta) {
        float diff = target - current;
        float step = speed * delta;
        if (Math.abs(diff) <= step) return target;
        return current + Math.signum(diff) * step;
    }
}
