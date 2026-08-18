package com.avaricious.screens;

import com.avaricious.DevTools;
import com.avaricious.Main;
import com.avaricious.Profiler;
import com.avaricious.components.*;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.buttons.OpenShopButton;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.AutoSpinDisplay;
import com.avaricious.components.roundInfoPanel.PlayerHealths;
import com.avaricious.components.roundInfoPanel.PlayerScores;
import com.avaricious.components.roundInfoPanel.RoundInfoPanel;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.shop.Shop;
import com.avaricious.components.slot.BouncingSymbolManager;
import com.avaricious.components.slot.SlotMachine;
import com.avaricious.components.slot.SlotMachineMatchFinder;
import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.effects.CrtEffect;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.IUpgradeWithActionOnSpinButtonPressed;
import com.avaricious.utility.*;
import com.avaricious.utility.runData.RunDataFileManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.OldTvEffect;

public class SlotScreen extends ScreenAdapter {

    private final Main app;

    private final ScreenShake screenShake;
    private final Shop shop = new Shop(this::onReturnedFromShop);
    private final LevelUpWindow levelUpWindow = new LevelUpWindow();

    private final OpenShopButton openShopButton = new OpenShopButton(
        new Rectangle(14f, 7f, 17 / 20f, 17 / 20f), Input.Keys.S
    );

    private final ButtonBoard buttonBoard = ButtonBoard.I()
        .init(this::onSpinButtonPressed, this::onPlayButtonPressed);

    private final TextureRegion charcoalPixel = Assets.I().get(AssetKey.CHARCOAL_PIXEL);

    private final VfxManager vfxManager = new VfxManager(Pixmap.Format.RGBA8888);

    private final Vector2 mouse = new Vector2();
    private boolean leftClickWasPressed = false;
    private int symbolsHitLastSpin = 0;

    public SlotScreen(Main app) {
        this.app = app;
        Pencil.I().setBatch(app.getBatch());

        screenShake = ScreenShake.I().setCameras(app.getViewport().getCamera(), app.getUiViewport().getCamera());
        vfxManager.addEffect(new OldTvEffect());
        vfxManager.addEffect(new CrtEffect());
        SlotMachine.I().setOnLastReelFinished(() -> SlotMachineResultRunner.I().runResult(SlotMachineMatchFinder.I().findMatches()));

        openShopButton.setVisibleAnimated(true);

        if (DevTools.enableProfiler()) Profiler.start();
    }

    @Override
    public void show() {
        RunManager.I().newRun();

//        if (RunManager.I().getRoundsManager().getCurrentRound() == 1)
        drawStartingHand();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                buttonBoard.setVisible(true);
                Automations.I().getAutoSpin().activate();

                Automations.I().getAutoSpinCapacity().upgrade();
                Automations.I().getAutoSpinCapacity().upgrade();
                Automations.I().getAutoSpinCapacity().upgrade();
                Automations.I().getAutoSpinCapacity().upgrade();
                Automations.I().getAutoSpinCapacity().upgrade();
                Automations.I().getAutoSpinCapacity().upgrade();

                Automations.I().getSlotMachineSpeed().upgrade();
                Automations.I().getSlotMachineSpeed().upgrade();
//                Automations.I().getSlotMachineSpeed().upgrade();
//                shop.show();
            }
        }, 1);
//        Timer.schedule(new Timer.Task() {
//            @Override
//            public void run() {
//                onSpinButtonPressed();
//            }
//        }, 1.5f);
    }

    @Override
    public void render(float delta) {
        RunDataFileManager.I().update(delta);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        SpriteBatch batch = app.getBatch();
        handleInput(delta);

        app.getViewport().apply();
        Camera camera = app.getViewport().getCamera();

        screenShake.update(delta);
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        Pencil.I().drawDarkenWindow();
        RoundInfoPanel.I().draw(delta);
        PlayerScores.I().draw(delta);
        PlayerHealths.I().draw(delta);
        ScoreDisplay.I().draw(delta);
        RunManager.I().getRoundsManager().getRoundTimer().draw(delta);
        if(!SlotMachine.I().isStale()) buttonBoard.draw(delta);
        AutoSpinDisplay.I().draw(delta);

        openShopButton.draw(delta);

//        DeckUi.I().draw();
//        ItemBag.I().draw(delta);
        ParticleManager.I().draw(batch, delta);
        SlotMachine.I().draw(delta);   // 10


//        HandUi.I().draw(delta);

//        TextureGlow.draw(batch, delta, TextureGlow.Type.NUMBER);
        XpBar.I().draw();
        levelUpWindow.draw(delta);
        shop.draw(delta);
//        bossLootWindow.draw(delta);


//        feltBackground.render(delta);
//        slotScreenBackground.render(delta, 0, SlotMachine.originY - 0.2f, 9f, 6.25f);

//        background.render(batch, delta);

//        ScreenUtils.clear(0.95f, 0.93f, 0.89f, 1f);
        vfxManager.update(delta);
        vfxManager.cleanUpBuffers();
        vfxManager.beginInputCapture();
        batch.begin();
        Pencil.I().draw(batch, delta, false);
        batch.end();
        vfxManager.endInputCapture();

        vfxManager.applyEffects();
        vfxManager.renderToScreen(
            app.getViewport().getScreenX(),
            app.getViewport().getScreenY(),
            app.getViewport().getScreenWidth(),
            app.getViewport().getScreenHeight()
        );

        batch.begin();

        if(SlotMachine.I().isStale()) buttonBoard.draw(delta);
        BouncingSymbolManager.I().drawFallingSymbols(delta);
        SlotMachine.I().drawSymbolsInPatternHit();
        PopupManager.I().draw(delta);


        Pencil.I().draw(batch, delta, true);
        float crosshairWidth = 0.75f;
        float crosshairHeight = 0.75f;
//        batch.setColor(new Color(1.0f, 0.08f, 0.05f, 1.0f));
        batch.draw(Assets.I().get(AssetKey.CROSSHAIR),
            mouse.x - crosshairWidth / 2f,
            mouse.y - crosshairHeight / 2f,
            crosshairWidth, crosshairHeight);
        batch.end();
        batch.setColor(Color.BLACK);
    }

    private void handleInput(float delta) {
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        app.getViewport().unproject(mouse);
        boolean leftClickPressed = Gdx.input.isTouched();

//        if (leftClickPressed && !leftClickWasPressed)
//            ParticleManager.I().startTrail(mouse.x, mouse.y, ParticleType.TRAIL, 0.02f, ZIndex.UNFOLDED_DECK_CARD);
//
//        if (leftClickPressed && leftClickWasPressed)
//            ParticleManager.I().moveTrail(mouse.x, mouse.y);
//
//        if (!leftClickPressed && leftClickWasPressed)
//            ParticleManager.I().stopTrail();


        if (shop.isShowing()) {
            shop.handleInput(mouse, leftClickPressed, leftClickWasPressed, delta);
        } else if(levelUpWindow.isShowing()) {
            levelUpWindow.handleInput(mouse, leftClickPressed, leftClickWasPressed);
        }else {
            SlotMachine.I().handleInput(mouse, leftClickPressed, leftClickWasPressed, delta);
            BouncingSymbolManager.I().handleInput(mouse, leftClickPressed, leftClickWasPressed);
            buttonBoard.handleInput(mouse, leftClickPressed, leftClickWasPressed);
            openShopButton.handleInput(mouse, leftClickPressed, leftClickWasPressed);
        }
        leftClickWasPressed = leftClickPressed;
    }

    public void onPlayButtonPressed() {
        HandUi.I().applySelectedCard();
    }

    public void onSpinButtonPressed() {
        if (Automations.I().getAutoSpin().isActive() && AutoSpinDisplay.I().getSpins() < 1) return;
        SlotMachine.I().setAlpha(1f);
        SlotMachine.I().spin();

        for (IUpgradeWithActionOnSpinButtonPressed relicWithActionAfterSpin : Hand.I().getUpgradesOfClass(IUpgradeWithActionOnSpinButtonPressed.class)) {
            relicWithActionAfterSpin.onSpinButtonPressed();
        }

        if (!Automations.I().getAutoSpin().isActive())
            ScoreDisplay.I().removeFromScore(50);
        else AutoSpinDisplay.I().removeSpin();
    }

    public void onRoundEnd() {
//        if (NetworkController.I().getSocketClient().isConnected())
//            NetworkController.I().match().sendRoundEnded();
//        else onBothPlayersEndedRound();
    }

    public void onBothPlayersEndedRound() {
        PlayerScores playerScores = PlayerScores.I();
        PlayerHealths playerHealths = PlayerHealths.I();

        if (playerScores.getPlayerScore() > playerScores.getEnemyScore()) {
            playerHealths.setEnemyHealth((int) playerHealths.getEnemyHealth() - 20);
        } else {
            playerHealths.setPlayerHealth((int) playerHealths.getPlayerHealth() - 20);
        }

        playerScores.setPlayerScoreNumber(0);
        playerScores.setEnemyScoreNumber(0);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                shop.show();
            }
        }, 1);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        vfxManager.resize(width, height);

        screenShake.captureBaseNow();
    }

    private void onReturnedFromShop() {
        RunManager.I().getRoundsManager().nextRound();
        if (Automations.I().getAutoSpin().isActive())
            onSpinButtonPressed();
    }

    private void drawStartingHand() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                int drawAmount = 3;
                Hand.I().drawCards(drawAmount);
            }
        }, 0.25f);
    }

    public int getSymbolsHitLastSpin() {
        return symbolsHitLastSpin;
    }

    public void showWaitingForOpponentText() {
    }

    public void setSymbolsHitLastSpin(int symbolsHitLastSpin) {
        this.symbolsHitLastSpin = symbolsHitLastSpin;
    }

    public void addSymbolsHitLastSpin() {
        symbolsHitLastSpin++;
    }

    public Shop getShop() {
        return shop;
    }

    public LevelUpWindow getLevelUpWindow() {
        return levelUpWindow;
    }
}
