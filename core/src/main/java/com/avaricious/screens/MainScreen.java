package com.avaricious.screens;

import com.avaricious.Main;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.SettingsMenu;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.MouseCursor;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.RunSaveManager;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

/** The landing screen shown after assets finish loading. */
public final class MainScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;
    private static final Rectangle NEW_RUN =
        new Rectangle(5.25f, 4.15f, 5.50f, 0.78f);
    private static final Rectangle CONTINUE_RUN =
        new Rectangle(5.25f, 3.20f, 5.50f, 0.78f);
    private static final Rectangle SETTINGS =
        new Rectangle(5.25f, 2.25f, 5.50f, 0.78f);
    private static final Rectangle QUIT =
        new Rectangle(5.25f, 1.30f, 5.50f, 0.78f);

    private static final Color BACKGROUND =
        new Color(0.008f, 0.018f, 0.025f, 1f);
    private static final Color PANEL =
        new Color(0.026f, 0.049f, 0.064f, 1f);
    private static final Color BUTTON =
        new Color(0.070f, 0.111f, 0.137f, 1f);
    private static final Color BUTTON_HOVER =
        new Color(0.125f, 0.190f, 0.225f, 1f);
    private static final Color GOLD =
        new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color MUTED =
        new Color(0.58f, 0.66f, 0.71f, 1f);

    private final Main app;
    private final SettingsMenu settingsMenu = new SettingsMenu(false);
    private final Vector2 mouse = new Vector2();
    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final TextureRegion spade = Assets.I().get(AssetKey.SPADE);

    private final GeneratedFabledText title = text("AVARICE", 12f, GOLD, true);
    private final GeneratedFabledText subtitle = text(
        "FORTUNE FAVORS THE GREEDY", 48f, MUTED, false
    );
    private final GeneratedFabledText newRunText = buttonText("NEW RUN", GOLD);
    private final GeneratedFabledText continueRunText = buttonText(
        "CONTINUE RUN", Assets.I().lightColor()
    );
    private final GeneratedFabledText continueRunDisabledText = buttonText(
        "CONTINUE RUN", new Color(0.34f, 0.39f, 0.42f, 1f)
    );
    private final GeneratedFabledText settingsText = buttonText(
        "SETTINGS", Assets.I().lightColor()
    );
    private final GeneratedFabledText quitText = buttonText("QUIT", MUTED);
    private final GeneratedFabledText hintText = text(
        "PRESS ESC FOR SETTINGS", 67f, MUTED, false
    );

    private boolean leftClickWasPressed;
    private MenuAction hoveredAction = MenuAction.NONE;
    private MenuAction pressedAction = MenuAction.NONE;
    private float time;

    private enum MenuAction {
        NONE,
        NEW_RUN,
        CONTINUE_RUN,
        SETTINGS,
        QUIT
    }

    public MainScreen(Main app) {
        this.app = app;
        Pencil.I().setBatch(app.getBatch());
        title.fitWithinWidth(6.8f);
        subtitle.fitWithinWidth(5.8f);
        hintText.fitWithinWidth(3.4f);
    }

    @Override
    public void show() {
        leftClickWasPressed = false;
        hoveredAction = MenuAction.NONE;
        pressedAction = MenuAction.NONE;
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ScreenUtils.clear(
            BACKGROUND.r,
            BACKGROUND.g,
            BACKGROUND.b,
            BACKGROUND.a
        );

        MouseCursor.I().update(app.getViewport(), mouse);
        handleInput();

        app.getViewport().apply();
        SpriteBatch batch = app.getBatch();
        batch.setProjectionMatrix(app.getViewport().getCamera().combined);
        batch.begin();

        drawBackground();
        drawMenu(delta);
        settingsMenu.draw(delta);
        Pencil.I().draw(batch, delta, true);
        drawCrosshair(batch);

        batch.end();
    }

    private void handleInput() {
        boolean pressed = Gdx.input.isTouched();
        if (settingsMenu.handleInput(mouse, pressed, leftClickWasPressed)) {
            leftClickWasPressed = pressed;
            return;
        }

        MenuAction previousHover = hoveredAction;
        hoveredAction = actionAt(mouse);
        if (hoveredAction != MenuAction.NONE
            && hoveredAction != previousHover) {
            AudioManager.I().playHover();
        }

        if (pressed && !leftClickWasPressed) {
            pressedAction = hoveredAction;
        }
        if (!pressed && leftClickWasPressed) {
            if (pressedAction != MenuAction.NONE
                && pressedAction == hoveredAction) {
                activate(pressedAction);
            }
            pressedAction = MenuAction.NONE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            activate(
                RunSaveManager.I().hasSave()
                    ? MenuAction.CONTINUE_RUN
                    : MenuAction.NEW_RUN
            );
        }

        leftClickWasPressed = pressed;
    }

    private MenuAction actionAt(Vector2 position) {
        if (NEW_RUN.contains(position)) return MenuAction.NEW_RUN;
        if (CONTINUE_RUN.contains(position)
            && RunSaveManager.I().hasSave()) {
            return MenuAction.CONTINUE_RUN;
        }
        if (SETTINGS.contains(position)) return MenuAction.SETTINGS;
        if (QUIT.contains(position)) return MenuAction.QUIT;
        return MenuAction.NONE;
    }

    private void activate(MenuAction action) {
        AudioManager.I().playUpgradeSelected();
        switch (action) {
            case NEW_RUN:
                RunSaveManager.I().clear();
                SlotScreen.requestNewRun();
                ScreenManager.I().setScreen(SlotScreen.class);
                break;
            case CONTINUE_RUN:
                SlotScreen.requestContinueRun();
                ScreenManager.I().setScreen(SlotScreen.class);
                break;
            case SETTINGS:
                settingsMenu.open();
                break;
            case QUIT:
                Gdx.app.exit();
                break;
            default:
                break;
        }
    }

    private void drawBackground() {
        rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT, BACKGROUND, 1f, ZIndex.TEXTURE_ECHO);

        for (int index = 0; index < 9; index++) {
            float x = index * 2.15f - 1.2f;
            float pulse = 0.5f
                + 0.5f * MathUtils.sin(time * 0.65f + index * 0.9f);
            rect(
                x,
                0f,
                0.72f,
                WORLD_HEIGHT,
                new Color(0.06f, 0.17f, 0.19f, 1f),
                0.035f + pulse * 0.035f,
                ZIndex.TEXTURE_ECHO
            );
        }

        rect(4.58f, 0.76f, 6.84f, 7.16f,
            Color.BLACK, 0.44f, ZIndex.SHOP);
        rect(4.48f, 0.86f, 6.84f, 7.16f,
            PANEL, 0.94f, ZIndex.SHOP);
        rect(4.48f, 7.96f, 6.84f, 0.06f,
            GOLD, 0.92f, ZIndex.SHOP_CARD);
    }

    private void drawMenu(float delta) {
        drawCentered(title, 6.75f, delta);
        drawCentered(subtitle, 5.95f, delta);

        float spadeSize = 0.34f;
        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            WORLD_WIDTH / 2f - spadeSize / 2f,
            5.40f,
            spadeSize,
            spadeSize,
            ZIndex.SHOP_CARD,
            GOLD
        ));

        drawButton(NEW_RUN, newRunText, MenuAction.NEW_RUN, true, delta);
        boolean canContinue = RunSaveManager.I().hasSave();
        drawButton(
            CONTINUE_RUN,
            canContinue ? continueRunText : continueRunDisabledText,
            MenuAction.CONTINUE_RUN,
            canContinue,
            delta
        );
        drawButton(SETTINGS, settingsText, MenuAction.SETTINGS, true, delta);
        drawButton(QUIT, quitText, MenuAction.QUIT, true, delta);

        hintText.setAbsoluteX(0.38f);
        hintText.setY(0.36f);
        hintText.draw(delta);
    }

    private void drawButton(
        Rectangle bounds,
        GeneratedFabledText label,
        MenuAction action,
        boolean enabled,
        float delta
    ) {
        boolean hovered = enabled && hoveredAction == action;
        boolean pressed = pressedAction == action && leftClickWasPressed;
        float offset = pressed ? -0.05f : 0f;

        rect(bounds.x + 0.07f, bounds.y - 0.08f,
            bounds.width, bounds.height,
            Color.BLACK, 0.55f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y + offset,
            bounds.width, bounds.height,
            hovered ? BUTTON_HOVER : BUTTON,
            enabled ? 1f : 0.42f,
            ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y + offset,
            0.05f, bounds.height,
            hovered ? Assets.I().lightColor() : GOLD,
            enabled ? (hovered ? 1f : 0.62f) : 0.22f,
            ZIndex.SHOP_CARD);

        label.fitWithinWidth(bounds.width - 0.55f);
        label.setAbsoluteX(
            bounds.x + (bounds.width - label.getRenderedWidth()) / 2f
        );
        label.setY(bounds.y + 0.25f + offset);
        label.draw(delta);
    }

    private void drawCrosshair(SpriteBatch batch) {
        float size = 0.5f;
        batch.draw(
            Assets.I().get(AssetKey.CROSSHAIR),
            mouse.x - size / 2f,
            mouse.y - size / 2f,
            size,
            size
        );
    }

    private void drawCentered(
        GeneratedFabledText text,
        float y,
        float delta
    ) {
        text.setAbsoluteX((WORLD_WIDTH - text.getRenderedWidth()) / 2f);
        text.setY(y);
        text.draw(delta);
    }

    private static GeneratedFabledText buttonText(String value, Color color) {
        return text(value, 36f, color, true);
    }

    private static GeneratedFabledText text(
        String value,
        float size,
        Color color,
        boolean bigFirstLetter
    ) {
        GeneratedFabledText result = new GeneratedFabledText(
            value,
            size,
            0.024f,
            0.14f,
            ZIndex.SHOP_CARD,
            bigFirstLetter
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
