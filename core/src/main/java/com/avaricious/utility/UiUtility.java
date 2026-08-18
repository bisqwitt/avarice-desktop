package com.avaricious.utility;

import com.badlogic.gdx.math.Vector2;

public class UiUtility {

    public static Vector2 calcShadowOffset(Vector2 objectCenter) {
        return new Vector2(calcShadowXOffset(objectCenter.x), calcShadowYOffset(objectCenter.y));
    }

    private static float calcShadowXOffset(float x) {
        float screenWidth = GameContext.I().viewport.getWorldWidth();
        float centerX = screenWidth * 0.5f;
        float half = screenWidth * 0.5f;
        if (half <= 0f) return 0f;

        // -1 .. +1
        float t = (x - centerX) / half;
        t = Math.max(-1f, Math.min(1f, t));

        // 1) deadzone around center (prevents jitter / over-response)
        float dead = 0.12f; // tweak: 0.08 .. 0.18
        float a = Math.abs(t);
        if (a < dead) return 0f;

        // remap from [dead..1] -> [0..1]
        float u = (a - dead) / (1f - dead);

        // 2) nonlinear easing: slow near center, stronger towards edges
        // try exponent 1.6 .. 2.4
        float expo = 2.0f;
        float eased = (float) Math.pow(u, expo);

        // restore sign
        float shaped = Math.signum(t) * eased;

        // 3) scale with screen size (world units)
        float maxOffset = screenWidth * 0.02f; // 2% of screen width; tweak 0.01..0.04

        return shaped * maxOffset;
    }

    public static float calcShadowYOffset(float y) {
        float h = GameContext.I().viewport.getWorldHeight();
        if (h <= 0f) return 0f;

        // Light/reference line at 85% height (Y-up world: near the top)
        float lightY = h * 0.85f;

        // Normalize by the farthest distance from lightY to top/bottom
        float half = Math.max(lightY, h - lightY);
        if (half <= 0f) return 0f;

        // -1 .. +1 relative position from the light line
        float t = (y - lightY) / half;
        t = Math.max(-1f, Math.min(1f, t));

        // Deadzone around the light line
        float dead = 0.12f;
        float a = Math.abs(t);
        if (a < dead) return 0f;

        float u = (a - dead) / (1f - dead);

        float expo = 2.0f;
        float eased = (float) Math.pow(u, expo);
        float shaped = Math.signum(t) * eased;

        // Scale with screen height
        float maxOffset = h * 0.02f;

        // If you want shadow to go "down" when object is above the light line:
        // return -shaped * maxOffset;

        // Otherwise (shadow follows sign of t):
        return shaped * maxOffset;
    }


}
