package com.avaricious.components;

import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameSettings;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Small, responsive frame-rate diagnostic controlled by player settings. */
public final class FpsDisplay {

    private static final float SAMPLE_SECONDS = 0.25f;
    private static final float X = 0.22f;
    private static final float Y = 0.24f;
    private static final float WIDTH = 1.55f;
    private static final float HEIGHT = 0.62f;
    private static final Color MUTED = new Color(0.63f, 0.71f, 0.76f, 1f);
    private static final Color GOLD = new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color PANEL = new Color(0.025f, 0.043f, 0.055f, 1f);

    private static FpsDisplay instance;

    public static FpsDisplay I() {
        return instance == null ? instance = new FpsDisplay() : instance;
    }

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final GeneratedFabledText label = new GeneratedFabledText(
        "FPS", 54f, 0.018f, 0.10f, ZIndex.SETTINGS_MENU
    );
    private final DigitalNumber number = new DigitalNumber(
        0f,
        MUTED,
        new Rectangle(0f, Y + 0.16f, 0.17f, 0.30f),
        0.21f
    ).setZIndex(ZIndex.SETTINGS_MENU);

    private float sampleTime;
    private int sampledFrames;
    private int displayedFps;

    private FpsDisplay() {
        label.setFloatEffects(0f, 0f);
        label.getWords().forEach(word -> word.setColor(MUTED));
        label.setAbsoluteX(X + 0.15f);
        label.setY(Y + 0.23f);
        number.getIdleScaleEffect().setAllowed(false);
        number.getPulseEffect().setStrength(0f);
    }

    public void draw(float delta) {
        updateSample(delta);
        if (!GameSettings.I().isFpsVisible()) return;

        Color fpsColor = displayedFps >= 55
            ? Assets.I().lightColor()
            : displayedFps >= 30 ? GOLD : Assets.I().healthRedColor();
        number.setColor(fpsColor);
        number.getFirstDigitBounds().x = X + 0.72f
            + (0.68f - number.getWidth()) / 2f;

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X + 0.05f,
            Y - 0.06f,
            WIDTH,
            HEIGHT,
            ZIndex.SETTINGS_MENU,
            new Color(0f, 0f, 0f, 0.48f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X,
            Y,
            WIDTH,
            HEIGHT,
            ZIndex.SETTINGS_MENU,
            new Color(PANEL.r, PANEL.g, PANEL.b, 0.86f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X,
            Y + HEIGHT - 0.03f,
            WIDTH,
            0.03f,
            ZIndex.SETTINGS_MENU,
            new Color(fpsColor.r, fpsColor.g, fpsColor.b, 0.78f)
        ));

        label.draw(delta);
        number.draw(delta);
    }

    private void updateSample(float delta) {
        if (delta <= 0f) return;

        sampleTime += delta;
        sampledFrames++;
        if (sampleTime < SAMPLE_SECONDS) return;

        int fps = Math.max(0, Math.round(sampledFrames / sampleTime));
        if (fps != displayedFps) {
            displayedFps = fps;
            number.setValue(fps);
        }
        sampleTime = 0f;
        sampledFrames = 0;
    }
}
