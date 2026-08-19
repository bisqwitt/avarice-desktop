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
import com.badlogic.gdx.graphics.Texture;
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
        new Rectangle(14f, 7f, 17 / 20f, 17 / 20f),
        Input.Keys.S
    );

    private final ButtonBoard buttonBoard = ButtonBoard.I()
        .init(
            this::onSpinButtonPressed,
            this::onPlayButtonPressed
        );

    private final TextureRegion charcoalPixel =
        Assets.I().get(AssetKey.CHARCOAL_PIXEL);

    private final VfxManager vfxManager =
        new VfxManager(Pixmap.Format.RGBA8888);

    private final Vector2 mouse = new Vector2();

    private boolean leftClickWasPressed = false;

    private int symbolsHitLastSpin = 0;


    // ============================================================
    // LEVEL UP IMPACT
    // ============================================================

    /*
     * Used to detect the exact frame where the level-up window
     * changes from hidden -> visible.
     */
    private boolean levelUpWasShowing = false;


    /*
     * Very short flash when the level-up starts.
     *
     * This deliberately continues updating while gameplay itself
     * is frozen.
     */
    private float levelUpImpactTimer = 0f;

    private static final float LEVEL_UP_IMPACT_DURATION = 0.22f;


    /*
     * Strength of the camera impact.
     *
     * ScreenShake.ensureTrauma() guarantees at least this amount
     * without blindly adding another large trauma value on top
     * of an already active shake.
     */
    private static final float LEVEL_UP_SHAKE_TRAUMA = 0.45f;


    /*
     * Maximum opacity of the white flash.
     *
     * Keep this significantly below 1.0. The goal is an impact,
     * not completely hiding the nice gameplay frame.
     */
    private static final float LEVEL_UP_FLASH_ALPHA = 0.32f;


    /*
     * We generate our own 1x1 white texture so this does not depend
     * on WHITE_PIXEL existing in AssetKey.
     */
    private final Texture levelUpFlashTexture;


    public SlotScreen(Main app) {

        this.app = app;

        Pencil.I().setBatch(app.getBatch());


        // ------------------------------------------------------------
        // SCREEN SHAKE
        // ------------------------------------------------------------

        screenShake = ScreenShake.I().setCameras(
            app.getViewport().getCamera(),
            app.getUiViewport().getCamera()
        );


        // ------------------------------------------------------------
        // VFX
        // ------------------------------------------------------------

        vfxManager.addEffect(new OldTvEffect());

        vfxManager.addEffect(new CrtEffect());


        // ------------------------------------------------------------
        // LEVEL-UP FLASH TEXTURE
        // ------------------------------------------------------------

        Pixmap flashPixmap = new Pixmap(
            1,
            1,
            Pixmap.Format.RGBA8888
        );

        flashPixmap.setColor(Color.WHITE);

        flashPixmap.fill();

        levelUpFlashTexture = new Texture(flashPixmap);

        flashPixmap.dispose();


        // ------------------------------------------------------------
        // SLOT MACHINE
        // ------------------------------------------------------------

        SlotMachine.I().setOnLastReelFinished(
            () -> SlotMachineResultRunner.I().runResult(
                SlotMachineMatchFinder.I().findMatches()
            )
        );


        openShopButton.setVisibleAnimated(true);


        if (DevTools.enableProfiler()) {
            Profiler.start();
        }
    }


    // ============================================================
    // SHOW
    // ============================================================

    @Override
    public void show() {

        RunManager.I().newRun();


//        if (RunManager.I().getRoundsManager().getCurrentRound() == 1)

        drawStartingHand();


        Timer.schedule(
            new Timer.Task() {

                @Override
                public void run() {

                    buttonBoard.setVisible(true);

                    Automations.I().getAutoSpin().activate();


                    Automations.I()
                        .getAutoSpinCapacity()
                        .upgrade();

                    Automations.I()
                        .getAutoSpinCapacity()
                        .upgrade();

                    Automations.I()
                        .getAutoSpinCapacity()
                        .upgrade();

                    Automations.I()
                        .getAutoSpinCapacity()
                        .upgrade();

                    Automations.I()
                        .getAutoSpinCapacity()
                        .upgrade();

                    Automations.I()
                        .getAutoSpinCapacity()
                        .upgrade();


                    Automations.I()
                        .getSlotMachineSpeed()
                        .upgrade();

                    Automations.I()
                        .getSlotMachineSpeed()
                        .upgrade();


//                    Automations.I().getSlotMachineSpeed().upgrade();
//                    shop.show();
//                    XpBar.I().addXp(10);
                }

            },
            1
        );


//        Timer.schedule(new Timer.Task() {
//            @Override
//            public void run() {
//                onSpinButtonPressed();
//            }
//        }, 1.5f);
    }


    // ============================================================
    // RENDER
    // ============================================================

    @Override
    public void render(float delta) {

        RunDataFileManager.I().update(delta);


        // ------------------------------------------------------------
        // LEVEL-UP STATE
        // ------------------------------------------------------------

        boolean levelUpShowing =
            levelUpWindow.isShowing();


        /*
         * Detect the first rendered frame of the level-up.
         */
        if (
            levelUpShowing &&
                !levelUpWasShowing
        ) {

            onLevelUpStarted();
        }


        /*
         * The impact timer continues running even though the
         * gameplay simulation below is paused.
         */
        if (levelUpImpactTimer > 0f) {

            levelUpImpactTimer -= delta;

            if (levelUpImpactTimer < 0f) {
                levelUpImpactTimer = 0f;
            }
        }


        levelUpWasShowing = levelUpShowing;


        // ------------------------------------------------------------
        // GAMEPLAY UPDATE
        // ------------------------------------------------------------

        /*
         * IMPORTANT:
         *
         * Freeze the actual gameplay frame while the level-up
         * menu is showing.
         *
         * This freezes:
         *
         * - reels
         * - bouncing symbols
         * - gameplay particles
         * - popup numbers
         *
         * That means the exact chaotic frame that caused the
         * level-up stays visible behind the menu.
         */
        if (!levelUpShowing) {

            SlotMachine.I().update(delta);

            BouncingSymbolManager.I()
                .updateFallingSymbols(delta);

            ParticleManager.I().update(delta);

            PopupManager.I().update(delta);
        }


        // ------------------------------------------------------------
        // CLEAR
        // ------------------------------------------------------------

        Gdx.gl.glViewport(
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        ScreenUtils.clear(
            0f,
            0f,
            0f,
            1f
        );


        SpriteBatch batch = app.getBatch();


        // ------------------------------------------------------------
        // INPUT
        // ------------------------------------------------------------

        handleInput(delta);


        // ------------------------------------------------------------
        // WORLD CAMERA
        // ------------------------------------------------------------

        app.getViewport().apply();

        Camera camera =
            app.getViewport().getCamera();


        /*
         * DO NOT freeze this during level-up.
         *
         * We explicitly want the level-up impact shake to continue
         * while the underlying gameplay frame remains static.
         */
        screenShake.update(delta);

        camera.update();


        batch.setProjectionMatrix(
            camera.combined
        );


        // ------------------------------------------------------------
        // QUEUE WORLD DRAWINGS
        // ------------------------------------------------------------

        Pencil.I().addDrawing(
            new TextureDrawing(
                Assets.I().get(
                    AssetKey.CHARCOAL_PIXEL
                ),
                0,
                0,
                16,
                9,
                ZIndex.TEXTURE_ECHO,
                Assets.I().shadowColor()
            )
        );


        Pencil.I().drawDarkenWindow();


        RoundInfoPanel.I().draw(delta);

        PlayerScores.I().draw(delta);

        PlayerHealths.I().draw(delta);

        ScoreDisplay.I().draw(delta);

        RunManager.I()
            .getRoundsManager()
            .getRoundTimer()
            .draw(delta);


        if (!SlotMachine.I().isStale()) {
            buttonBoard.draw(delta);
        }


        AutoSpinDisplay.I().draw(delta);


        openShopButton.draw(delta);


//        DeckUi.I().draw();
//        ItemBag.I().draw(delta);


        ParticleManager.I().draw(
            batch,
            delta
        );


        SlotMachine.I().draw(delta);


//        HandUi.I().draw(delta);


//        TextureGlow.draw(
//            batch,
//            delta,
//            TextureGlow.Type.NUMBER
//        );


        XpBar.I().draw();

        shop.draw(delta);


//        bossLootWindow.draw(delta);


//        feltBackground.render(delta);


//        slotScreenBackground.render(
//            delta,
//            0,
//            SlotMachine.originY - 0.2f,
//            9f,
//            6.25f
//        );


//        background.render(batch, delta);


//        ScreenUtils.clear(
//            0.95f,
//            0.93f,
//            0.89f,
//            1f
//        );


        // ------------------------------------------------------------
        // CRT / POST PROCESSING
        // ------------------------------------------------------------

        /*
         * IMPORTANT:
         *
         * Keep VFX updating during the level-up.
         *
         * The GAME frame freezes, but the simulated display
         * continues living.
         *
         * This gives us moving CRT noise / scanlines / distortion
         * over a completely static gameplay frame.
         */
        vfxManager.update(delta);

        vfxManager.cleanUpBuffers();

        vfxManager.beginInputCapture();


        batch.begin();

        Pencil.I().draw(
            batch,
            delta,
            false
        );

        batch.end();


        vfxManager.endInputCapture();

        vfxManager.applyEffects();


        vfxManager.renderToScreen(
            app.getViewport().getScreenX(),
            app.getViewport().getScreenY(),
            app.getViewport().getScreenWidth(),
            app.getViewport().getScreenHeight()
        );


        // ------------------------------------------------------------
        // FOREGROUND / NON-POST-PROCESSED CONTENT
        // ------------------------------------------------------------

        batch.begin();


        if (SlotMachine.I().isStale()) {
            buttonBoard.draw(delta);
        }


        BouncingSymbolManager.I()
            .drawFallingSymbols(delta);


        SlotMachine.I()
            .drawSymbolsInPatternHit();


        PopupManager.I().draw(delta);


        /*
         * Draw the impact flash BEFORE the level-up window.
         *
         * This means the frozen gameplay gets punched by the flash,
         * but the upgrade UI itself remains readable.
         */
        drawLevelUpImpact(batch);


        levelUpWindow.draw(delta);


        Pencil.I().draw(
            batch,
            delta,
            true
        );


        // ------------------------------------------------------------
        // CROSSHAIR
        // ------------------------------------------------------------

        float crosshairWidth = 0.5f;

        float crosshairHeight = 0.5f;


//        batch.setColor(
//            new Color(
//                1.0f,
//                0.08f,
//                0.05f,
//                1.0f
//            )
//        );


        batch.draw(
            Assets.I().get(
                AssetKey.CROSSHAIR
            ),
            mouse.x - crosshairWidth / 2f,
            mouse.y - crosshairHeight / 2f,
            crosshairWidth,
            crosshairHeight
        );


        batch.end();


        batch.setColor(Color.BLACK);
    }


    // ============================================================
    // LEVEL UP IMPACT
    // ============================================================

    private void onLevelUpStarted() {

        /*
         * Start flash.
         */
        levelUpImpactTimer =
            LEVEL_UP_IMPACT_DURATION;


        /*
         * Guarantee a strong impact without adding another full
         * trauma amount on top of a shake that may already be active
         * from the symbol/pattern that caused the level-up.
         */
        screenShake.ensureTrauma(
            LEVEL_UP_SHAKE_TRAUMA
        );
    }


    private void drawLevelUpImpact(
        SpriteBatch batch
    ) {

        if (levelUpImpactTimer <= 0f) {
            return;
        }


        /*
         * 0 when impact begins.
         * 1 when impact finishes.
         */
        float progress =
            1f -
                levelUpImpactTimer /
                    LEVEL_UP_IMPACT_DURATION;


        /*
         * Very fast attack, slower decay.
         *
         *
         * alpha
         *
         * 1.0      /\
         *         /  \
         *        /    \
         * 0.0 __/      \________
         *
         */
        float flashStrength;


        /*
         * Flash reaches maximum strength after only
         * 18% of its duration.
         */
        final float attackEnd = 0.18f;


        if (progress < attackEnd) {

            flashStrength =
                progress / attackEnd;

        } else {

            float decayProgress =
                (progress - attackEnd) /
                    (1f - attackEnd);


            /*
             * Quadratic falloff.
             *
             * It drops quickly at first and then leaves a tiny
             * residual glow for the remaining frames.
             */
            float remaining =
                1f - decayProgress;


            flashStrength =
                remaining * remaining;
        }


        float alpha =
            flashStrength *
                LEVEL_UP_FLASH_ALPHA;


        /*
         * Preserve whatever SpriteBatch color another renderer
         * may have left behind.
         */
        Color previousColor =
            new Color(batch.getColor());


        batch.setColor(
            1f,
            1f,
            1f,
            alpha
        );


        batch.draw(
            levelUpFlashTexture,
            0f,
            0f,
            16f,
            9f
        );


        batch.setColor(previousColor);
    }


    // ============================================================
    // INPUT
    // ============================================================

    private void handleInput(float delta) {

        mouse.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );


        app.getViewport().unproject(mouse);


        boolean leftClickPressed =
            Gdx.input.isTouched();


//        if (
//            leftClickPressed &&
//            !leftClickWasPressed
//        ) {
//            ParticleManager.I().startTrail(
//                mouse.x,
//                mouse.y,
//                ParticleType.TRAIL,
//                0.02f,
//                ZIndex.UNFOLDED_DECK_CARD
//            );
//        }


//        if (
//            leftClickPressed &&
//            leftClickWasPressed
//        ) {
//            ParticleManager.I().moveTrail(
//                mouse.x,
//                mouse.y
//            );
//        }


//        if (
//            !leftClickPressed &&
//            leftClickWasPressed
//        ) {
//            ParticleManager.I().stopTrail();
//        }


        if (shop.isShowing()) {

            shop.handleInput(
                mouse,
                leftClickPressed,
                leftClickWasPressed,
                delta
            );

        } else if (
            levelUpWindow.isShowing()
        ) {

            /*
             * While gameplay itself is frozen, the user can still
             * interact with the level-up cards.
             */
            levelUpWindow.handleInput(
                mouse,
                leftClickPressed,
                leftClickWasPressed
            );

        } else {

            SlotMachine.I().handleInput(
                mouse,
                leftClickPressed,
                leftClickWasPressed,
                delta
            );


            BouncingSymbolManager.I()
                .handleInput(
                    mouse,
                    leftClickPressed,
                    leftClickWasPressed
                );


            buttonBoard.handleInput(
                mouse,
                leftClickPressed,
                leftClickWasPressed
            );


            openShopButton.handleInput(
                mouse,
                leftClickPressed,
                leftClickWasPressed
            );
        }


        leftClickWasPressed =
            leftClickPressed;
    }


    // ============================================================
    // PLAY BUTTON
    // ============================================================

    public void onPlayButtonPressed() {

        HandUi.I().applySelectedCard();
    }


    // ============================================================
    // SPIN BUTTON
    // ============================================================

    public void onSpinButtonPressed() {

        if (
            Automations.I()
                .getAutoSpin()
                .isActive() &&
                AutoSpinDisplay.I()
                    .getSpins() < 1
        ) {
            return;
        }


        SlotMachine.I().setAlpha(1f);

        SlotMachine.I().spin();


        for (
            IUpgradeWithActionOnSpinButtonPressed
                relicWithActionAfterSpin :
            Hand.I().getUpgradesOfClass(
                IUpgradeWithActionOnSpinButtonPressed.class
            )
        ) {

            relicWithActionAfterSpin
                .onSpinButtonPressed();
        }


        if (
            !Automations.I()
                .getAutoSpin()
                .isActive()
        ) {

            ScoreDisplay.I()
                .removeFromScore(50);

        } else {

            AutoSpinDisplay.I()
                .removeSpin();
        }
    }


    // ============================================================
    // ROUND
    // ============================================================

    public void onRoundEnd() {

//        if (
//            NetworkController.I()
//                .getSocketClient()
//                .isConnected()
//        ) {
//            NetworkController.I()
//                .match()
//                .sendRoundEnded();
//        } else {
//            onBothPlayersEndedRound();
//        }
    }


    public void onBothPlayersEndedRound() {

        PlayerScores playerScores =
            PlayerScores.I();

        PlayerHealths playerHealths =
            PlayerHealths.I();


        if (
            playerScores.getPlayerScore() >
                playerScores.getEnemyScore()
        ) {

            playerHealths.setEnemyHealth(
                (int)
                    playerHealths
                        .getEnemyHealth()
                    - 20
            );

        } else {

            playerHealths.setPlayerHealth(
                (int)
                    playerHealths
                        .getPlayerHealth()
                    - 20
            );
        }


        playerScores.setPlayerScoreNumber(0);

        playerScores.setEnemyScoreNumber(0);


        Timer.schedule(
            new Timer.Task() {

                @Override
                public void run() {

                    shop.show();
                }

            },
            1
        );
    }


    // ============================================================
    // RESIZE
    // ============================================================

    @Override
    public void resize(
        int width,
        int height
    ) {

        super.resize(
            width,
            height
        );


        vfxManager.resize(
            width,
            height
        );


        screenShake.captureBaseNow();
    }


    // ============================================================
    // SHOP
    // ============================================================

    private void onReturnedFromShop() {

        RunManager.I()
            .getRoundsManager()
            .nextRound();


        if (
            Automations.I()
                .getAutoSpin()
                .isActive()
        ) {

            onSpinButtonPressed();
        }
    }


    // ============================================================
    // STARTING HAND
    // ============================================================

    private void drawStartingHand() {

        Timer.schedule(
            new Timer.Task() {

                @Override
                public void run() {

                    int drawAmount = 3;

                    Hand.I().drawCards(
                        drawAmount
                    );
                }

            },
            0.25f
        );
    }


    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public int getSymbolsHitLastSpin() {

        return symbolsHitLastSpin;
    }


    public void showWaitingForOpponentText() {

    }


    public void setSymbolsHitLastSpin(
        int symbolsHitLastSpin
    ) {

        this.symbolsHitLastSpin =
            symbolsHitLastSpin;
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


    // ============================================================
    // DISPOSE
    // ============================================================

    @Override
    public void dispose() {

        /*
         * This texture is created by SlotScreen itself,
         * so SlotScreen owns and disposes it.
         */
        levelUpFlashTexture.dispose();


        vfxManager.dispose();
    }
}
