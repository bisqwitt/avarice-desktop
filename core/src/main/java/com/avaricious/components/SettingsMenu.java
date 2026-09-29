package com.avaricious.components;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameSettings;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** Modal settings panel shared by the gameplay overlays. */
public final class SettingsMenu {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;

    private static final Rectangle LAUNCHER =
        new Rectangle(13.32f, 8.18f, 2.28f, 0.48f);
    private static final Rectangle PANEL =
        new Rectangle(3.55f, 0.72f, 8.90f, 7.56f);
    private static final Rectangle SENSITIVITY_SLIDER =
        new Rectangle(5.55f, 5.52f, 4.90f, 0.18f);
    private static final Rectangle VOLUME_SLIDER =
        new Rectangle(5.55f, 4.12f, 4.90f, 0.18f);
    private static final Rectangle EFFECTS_TOGGLE =
        new Rectangle(5.10f, 2.55f, 2.65f, 0.74f);
    private static final Rectangle MUSIC_TOGGLE =
        new Rectangle(8.25f, 2.55f, 2.65f, 0.74f);
    private static final Rectangle BACK_BUTTON =
        new Rectangle(6.15f, 1.18f, 3.70f, 0.72f);

    private static final Color BACKDROP =
        new Color(0.006f, 0.012f, 0.018f, 1f);
    private static final Color PANEL_COLOR =
        new Color(0.035f, 0.058f, 0.074f, 1f);
    private static final Color CONTROL =
        new Color(0.075f, 0.112f, 0.135f, 1f);
    private static final Color CONTROL_HOVER =
        new Color(0.125f, 0.185f, 0.215f, 1f);
    private static final Color MUTED =
        new Color(0.56f, 0.64f, 0.69f, 1f);
    private static final Color GOLD =
        new Color(1f, 0.82f, 0.44f, 1f);

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final GeneratedFabledText launcherText = text("ESC SETTINGS", 58f, MUTED);
    private final GeneratedFabledText title = text("SETTINGS", 18f, GOLD);
    private final GeneratedFabledText prompt = text(
        "DRAG SLIDERS OR CLICK TO TOGGLE", 55f, MUTED
    );
    private final GeneratedFabledText sensitivityLabel = text(
        "MOUSE SENSITIVITY", 39f, Assets.I().lightColor()
    );
    private final GeneratedFabledText slowLabel = text("SLOW", 68f, MUTED);
    private final GeneratedFabledText fastLabel = text("FAST", 68f, MUTED);
    private final GeneratedFabledText volumeLabel = text(
        "MASTER VOLUME", 39f, Assets.I().lightColor()
    );
    private final GeneratedFabledText quietLabel = text("QUIET", 68f, MUTED);
    private final GeneratedFabledText fullLabel = text("FULL", 68f, MUTED);
    private final GeneratedFabledText effectsLabel = text("EFFECTS", 47f, MUTED);
    private final GeneratedFabledText musicLabel = text("MUSIC", 47f, MUTED);
    private final GeneratedFabledText onText = text("ON", 39f, GOLD);
    private final GeneratedFabledText mutedText = text(
        "MUTED", 45f, Assets.I().healthRedColor()
    );
    private final GeneratedFabledText backText = text("BACK", 38f, GOLD);
    private final boolean launcherVisible;

    private boolean showing;
    private boolean inputArmed;
    private boolean launcherHovered;
    private boolean launcherPressed;
    private boolean effectsHovered;
    private boolean musicHovered;
    private boolean backHovered;
    private boolean effectsPressed;
    private boolean musicPressed;
    private boolean backPressed;
    private Slider activeSlider = Slider.NONE;

    private enum Slider { NONE, SENSITIVITY, VOLUME }

    public SettingsMenu() {
        this(true);
    }

    public SettingsMenu(boolean launcherVisible) {
        this.launcherVisible = launcherVisible;
    }

    public boolean isShowing() {
        return showing;
    }

    public boolean handleInput(
        Vector2 mouse,
        boolean pressed,
        boolean wasPressed
    ) {
        lastMouse.set(mouse);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (showing) hide();
            else show();
            return true;
        }

        if (!showing) {
            return launcherVisible
                && handleLauncher(mouse, pressed, wasPressed);
        }

        effectsHovered = EFFECTS_TOGGLE.contains(mouse);
        musicHovered = MUSIC_TOGGLE.contains(mouse);
        backHovered = BACK_BUTTON.contains(mouse);

        if (!inputArmed) {
            if (!pressed) inputArmed = true;
            return true;
        }

        if (pressed && !wasPressed) {
            if (sliderContains(SENSITIVITY_SLIDER, mouse)) {
                activeSlider = Slider.SENSITIVITY;
            } else if (sliderContains(VOLUME_SLIDER, mouse)) {
                activeSlider = Slider.VOLUME;
            }
            effectsPressed = effectsHovered;
            musicPressed = musicHovered;
            backPressed = backHovered;
        }

        if (pressed && activeSlider != Slider.NONE) {
            updateSlider(mouse.x);
        }

        if (!pressed && wasPressed) {
            if (activeSlider != Slider.NONE) {
                updateSlider(mouse.x);
                activeSlider = Slider.NONE;
                GameSettings.I().flush();
                if (!AudioManager.I().isEffectsMuted()) {
                    AudioManager.I().playHover();
                }
            } else if (effectsPressed && effectsHovered) {
                boolean muted = !GameSettings.I().isEffectsMuted();
                AudioManager.I().setEffectsMuted(muted);
                GameSettings.I().flush();
                if (!muted) AudioManager.I().playHover();
            } else if (musicPressed && musicHovered) {
                AudioManager.I().setMusicMuted(
                    !GameSettings.I().isMusicMuted()
                );
                GameSettings.I().flush();
                AudioManager.I().playHover();
            } else if (backPressed && backHovered) {
                AudioManager.I().playHover();
                hide();
            }

            effectsPressed = false;
            musicPressed = false;
            backPressed = false;
        }

        return true;
    }

    private boolean handleLauncher(
        Vector2 mouse,
        boolean pressed,
        boolean wasPressed
    ) {
        launcherHovered = LAUNCHER.contains(mouse);
        if (pressed && !wasPressed) {
            launcherPressed = launcherHovered;
        }
        if (!pressed && wasPressed) {
            boolean clicked = launcherPressed && launcherHovered;
            launcherPressed = false;
            if (clicked) {
                AudioManager.I().playHover();
                show();
                return true;
            }
        }
        return launcherHovered || launcherPressed;
    }

    private void updateSlider(float mouseX) {
        Rectangle slider = activeSlider == Slider.SENSITIVITY
            ? SENSITIVITY_SLIDER
            : VOLUME_SLIDER;
        float progress = MathUtils.clamp(
            (mouseX - slider.x) / slider.width,
            0f,
            1f
        );

        if (activeSlider == Slider.SENSITIVITY) {
            GameSettings.I().setMouseSensitivity(MathUtils.lerp(
                GameSettings.MIN_MOUSE_SENSITIVITY,
                GameSettings.MAX_MOUSE_SENSITIVITY,
                progress
            ));
        } else {
            AudioManager.I().setMasterVolume(progress);
        }
    }

    public void open() {
        showing = true;
        inputArmed = false;
        clearPressState();
    }

    private void show() {
        open();
    }

    private void hide() {
        showing = false;
        inputArmed = false;
        clearPressState();
        GameSettings.I().flush();
    }

    private void clearPressState() {
        launcherPressed = false;
        effectsPressed = false;
        musicPressed = false;
        backPressed = false;
        activeSlider = Slider.NONE;
    }

    public void draw(float delta) {
        if (!showing) {
            if (launcherVisible) drawLauncher(delta);
            return;
        }

        rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT, BACKDROP, 0.93f,
            ZIndex.SETTINGS_BACKDROP);
        rect(PANEL.x + 0.09f, PANEL.y - 0.10f, PANEL.width, PANEL.height,
            Color.BLACK, 0.65f, ZIndex.SETTINGS_MENU);
        rect(PANEL.x, PANEL.y, PANEL.width, PANEL.height,
            PANEL_COLOR, 1f, ZIndex.SETTINGS_MENU);
        rect(PANEL.x, PANEL.y + PANEL.height - 0.06f, PANEL.width, 0.06f,
            GOLD, 0.92f, ZIndex.SETTINGS_MENU);

        drawCentered(title, 7.34f, delta);
        drawCentered(prompt, 6.86f, delta);

        drawCentered(sensitivityLabel, 6.15f, delta);
        drawSlider(
            SENSITIVITY_SLIDER,
            sensitivityProgress(),
            activeSlider == Slider.SENSITIVITY
                || sliderContains(SENSITIVITY_SLIDER, lastMouse),
            delta
        );
        drawEndpoint(slowLabel, SENSITIVITY_SLIDER.x, 5.19f, false, delta);
        drawEndpoint(fastLabel, SENSITIVITY_SLIDER.x + SENSITIVITY_SLIDER.width,
            5.19f, true, delta);

        drawCentered(volumeLabel, 4.75f, delta);
        drawSlider(
            VOLUME_SLIDER,
            AudioManager.I().getMasterVolume(),
            activeSlider == Slider.VOLUME
                || sliderContains(VOLUME_SLIDER, lastMouse),
            delta
        );
        drawEndpoint(quietLabel, VOLUME_SLIDER.x, 3.79f, false, delta);
        drawEndpoint(fullLabel, VOLUME_SLIDER.x + VOLUME_SLIDER.width,
            3.79f, true, delta);

        drawToggle(EFFECTS_TOGGLE, effectsLabel,
            GameSettings.I().isEffectsMuted(), effectsHovered, delta);
        drawToggle(MUSIC_TOGGLE, musicLabel,
            GameSettings.I().isMusicMuted(), musicHovered, delta);
        drawButton(BACK_BUTTON, backText, backHovered, delta);
    }

    private final Vector2 lastMouse = new Vector2();

    private void drawLauncher(float delta) {
        rect(
            LAUNCHER.x,
            LAUNCHER.y,
            LAUNCHER.width,
            LAUNCHER.height,
            launcherHovered ? CONTROL_HOVER : CONTROL,
            0.88f,
            ZIndex.SETTINGS_MENU
        );
        launcherText.fitWithinWidth(LAUNCHER.width - 0.24f);
        launcherText.setAbsoluteX(
            LAUNCHER.x + (LAUNCHER.width - launcherText.getRenderedWidth()) / 2f
        );
        launcherText.setY(LAUNCHER.y + 0.14f);
        launcherText.draw(delta);
    }

    private void drawSlider(
        Rectangle bounds,
        float progress,
        boolean hovered,
        float delta
    ) {
        rect(bounds.x, bounds.y, bounds.width, bounds.height,
            new Color(0.015f, 0.025f, 0.032f, 1f), 1f,
            ZIndex.SETTINGS_MENU);
        rect(bounds.x, bounds.y, bounds.width * progress, bounds.height,
            hovered ? Assets.I().lightColor() : GOLD, 0.95f,
            ZIndex.SETTINGS_MENU);

        float knobX = bounds.x + bounds.width * progress - 0.09f;
        rect(knobX, bounds.y - 0.12f, 0.18f, bounds.height + 0.24f,
            Assets.I().lightColor(), 1f, ZIndex.SETTINGS_MENU);
    }

    private float sensitivityProgress() {
        return (GameSettings.I().getMouseSensitivity()
            - GameSettings.MIN_MOUSE_SENSITIVITY)
            / (GameSettings.MAX_MOUSE_SENSITIVITY
                - GameSettings.MIN_MOUSE_SENSITIVITY);
    }

    private static boolean sliderContains(
        Rectangle slider,
        Vector2 mouse
    ) {
        return mouse.x >= slider.x
            && mouse.x <= slider.x + slider.width
            && mouse.y >= slider.y - 0.22f
            && mouse.y <= slider.y + slider.height + 0.22f;
    }

    private void drawToggle(
        Rectangle bounds,
        GeneratedFabledText label,
        boolean muted,
        boolean hovered,
        float delta
    ) {
        label.setAbsoluteX(bounds.x + (bounds.width - label.getRenderedWidth()) / 2f);
        label.setY(bounds.y + bounds.height + 0.20f);
        label.draw(delta);
        drawButton(bounds, muted ? mutedText : onText, hovered, delta);
    }

    private void drawButton(
        Rectangle bounds,
        GeneratedFabledText label,
        boolean hovered,
        float delta
    ) {
        rect(bounds.x + 0.05f, bounds.y - 0.07f, bounds.width, bounds.height,
            Color.BLACK, 0.55f, ZIndex.SETTINGS_MENU);
        rect(bounds.x, bounds.y, bounds.width, bounds.height,
            hovered ? CONTROL_HOVER : CONTROL, 1f, ZIndex.SETTINGS_MENU);
        label.fitWithinWidth(bounds.width - 0.40f);
        label.setAbsoluteX(bounds.x + (bounds.width - label.getRenderedWidth()) / 2f);
        label.setY(bounds.y + 0.23f);
        label.draw(delta);
    }

    private void drawEndpoint(
        GeneratedFabledText label,
        float x,
        float y,
        boolean rightAligned,
        float delta
    ) {
        label.setAbsoluteX(rightAligned ? x - label.getRenderedWidth() : x);
        label.setY(y);
        label.draw(delta);
    }

    private void drawCentered(
        GeneratedFabledText label,
        float y,
        float delta
    ) {
        label.setAbsoluteX((WORLD_WIDTH - label.getRenderedWidth()) / 2f);
        label.setY(y);
        label.draw(delta);
    }

    private static GeneratedFabledText text(
        String value,
        float size,
        Color color
    ) {
        GeneratedFabledText result = new GeneratedFabledText(
            value,
            size,
            0.024f,
            0.13f,
            ZIndex.SETTINGS_MENU,
            true
        );
        result.setFloatEffects(0f, 0f);
        result.getWords().forEach(word -> word.setColor(color));
        return result;
    }

    private void rect(
        float x,
        float y,
        float width,
        float height,
        Color color,
        float alpha,
        ZIndex layer
    ) {
        Color tinted = new Color(color);
        tinted.a *= alpha;
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            x,
            y,
            width,
            height,
            layer,
            tinted
        ));
    }
}
