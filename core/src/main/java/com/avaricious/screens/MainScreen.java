package com.avaricious.screens;

import com.avaricious.Main;
import com.avaricious.components.AvariceWord;
import com.avaricious.components.buttons.Button;
import com.avaricious.network.NetworkController;
import com.avaricious.utility.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainScreen extends ScreenAdapter {
    private final Main app;

    private final AvariceWord title = new AvariceWord();

    private final Button joinQueueButton = new Button(this::onJoinQueueButtonPressed,
        Assets.I().get(AssetKey.JOIN_QUEUE_BUTTON),
        Assets.I().get(AssetKey.JOIN_QUEUE_BUTTON_PRESSED),
        Assets.I().get(AssetKey.JOIN_QUEUE_BUTTON),
        new Rectangle(2.5f, 9f, 79 / 20f, 25 / 20f),
        Input.Keys.ENTER, ZIndex.SLOT_MACHINE);
    private final Button newRunButton = new Button(this::onNewRunButtonPressed,
        Assets.I().get(AssetKey.NEW_RUN_BUTTON),
        Assets.I().get(AssetKey.NEW_RUN_BUTTON_PRESSED),
        Assets.I().get(AssetKey.NEW_RUN_BUTTON),
        new Rectangle(2.5f, 7f, 79 / 20f, 25 / 20f),
        Input.Keys.ENTER, ZIndex.SLOT_MACHINE);

    private boolean leftClickWasPressed = false;

    public MainScreen(Main app) {
        this.app = app;
    }

    private void handleInput(float delta) {
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        app.getViewport().unproject(mouse);
        boolean leftClickPressed = Gdx.input.isTouched();

        joinQueueButton.handleInput(mouse, leftClickPressed, leftClickWasPressed);
        newRunButton.handleInput(mouse, leftClickPressed, leftClickWasPressed);

        leftClickWasPressed = leftClickPressed;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        SpriteBatch batch = app.getBatch();
        handleInput(delta);

        app.getViewport().apply();

//        background.render(batch, delta);

        title.draw(delta);

        batch.setProjectionMatrix(app.getViewport().getCamera().combined);
        batch.begin();

        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        app.getViewport().unproject(mouse);

        joinQueueButton.draw(delta);
        newRunButton.draw(delta);

        Pencil.I().draw(batch, delta);

        float crosshairWidth = 0.5f;
        float crosshairHeight = 0.5f;
        batch.draw(Assets.I().get(AssetKey.CROSSHAIR),
            mouse.x - crosshairWidth / 2f,
            mouse.y - crosshairHeight / 2f,
            crosshairWidth, crosshairHeight);

        batch.end();
    }

    private void onJoinQueueButtonPressed() {
        NetworkController.I().matchmaking().joinQueue();
    }

    private void onNewRunButtonPressed() {
        ScreenManager.I().setScreen(SlotScreen.class);
    }
}
