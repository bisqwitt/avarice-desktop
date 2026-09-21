package com.avaricious.screens;

import com.avaricious.DevTools;
import com.avaricious.Main;
import com.avaricious.Profiler;
import com.avaricious.RoundStats;
import com.avaricious.RoundsManager;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.*;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.AutoSpinDisplay;
import com.avaricious.components.roundInfoPanel.PlayerHealths;
import com.avaricious.components.roundInfoPanel.PlayerScores;
import com.avaricious.components.roundInfoPanel.RoundInfoPanel;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.shop.Shop;
import com.avaricious.components.slot.BouncingSymbolManager;
import com.avaricious.components.slot.ChestManager;
import com.avaricious.components.slot.CollectorManager;
import com.avaricious.components.slot.SlotMachine;
import com.avaricious.components.slot.SlotMachineMatchFinder;
import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.IUpgradeWithActionOnSpinButtonPressed;
import com.avaricious.utility.*;
import com.avaricious.utility.runData.RunDataFileManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
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
import com.crashinvaders.vfx.effects.CrtEffect;
import com.crashinvaders.vfx.effects.OldTvEffect;

public class SlotScreen extends ScreenAdapter {

    private final Main app;

    private final ScreenShake screenShake;

    private final Shop shop = new Shop(this::onReturnedFromShop);

    private final LevelUpWindow levelUpWindow = new LevelUpWindow();

    private final RoundResultWindow roundResultWindow = new RoundResultWindow();

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
    private final Vector2 scrollMouse = new Vector2();

    private boolean leftClickWasPressed = false;
    private boolean roundClockActive = false;
    private boolean autoSpinWaitingForCollectibles = false;

    private int symbolsHitLastSpin = 0;

    private final InputProcessor shopScrollInput = new InputAdapter() {
        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (shop.isShowing()) {
                scrollMouse.set(Gdx.input.getX(), Gdx.input.getY());
                app.getViewport().unproject(scrollMouse);
                return shop.scrollItems(amountY, scrollMouse);
            }
            return false;
        }
    };


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

        Gdx.input.setInputProcessor(shopScrollInput);


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

//        vfxManager.addEffect(new CrtEffect());


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
            () -> {
                ChestManager.I().rollForChest();
                SlotMachineResultRunner.I().runResult(
                    SlotMachineMatchFinder.I().findMatches(
                        SlotMachine.I().getCurrentSpinResult()
                    )
                );
            }
        );


        if (DevTools.enableProfiler()) {
            Profiler.start();
        }
    }


    // ============================================================
    // SHOW
    // ============================================================

    @Override
    public void show() {
        resetRunState();
        drawStartingHand();

        Timer.schedule(
            new Timer.Task() {
                @Override
                public void run() {
                    buttonBoard.setVisible(true);
                    roundClockActive = true;
                }
            },
            1
        );
    }

    private void resetRunState() {
        roundClockActive = false;
        autoSpinWaitingForCollectibles = false;
        ScoreDisplay.I().setScoreNumber(0f);
        RunManager.I().newRun();
        ChestManager.I().reset();
        CollectorManager.I().reset();
        BouncingSymbolManager.I().reset();
        TicketPressSystem.I().reset();
        roundResultWindow.hide();
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


        // The clock runs through spins, but pauses for decision overlays.
        if (
                !levelUpShowing &&
                !roundResultWindow.isShowing() &&
                !shop.isShowing() &&
                roundClockActive
        ) {
            boolean timerExpired = RunManager.I()
                .getRoundsManager()
                .updateTimer(delta);
            if (timerExpired && SlotMachine.I().isStale()) {
                onSpinResolved();
            }
        }


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
        if (
            !levelUpShowing &&
            !roundResultWindow.isShowing() &&
            !shop.isShowing()
        ) {

            SlotMachine.I().update(delta);

            BouncingSymbolManager.I()
                .updateFallingSymbols(
                    delta,
                    ScoreDisplay.I().getCollisionBounds(),
                    RoundInfoPanel.I().getCollisionBounds(),
                    buttonBoard.getSpinButtonCollisionBounds()
                );

            CollectorManager.I().update(delta);

            resumeAutoSpinAfterCollecting();

            ChestManager.I().update(delta);

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

        if (!SlotMachine.I().isStale()) {
            buttonBoard.draw(delta);
        }


        AutoSpinDisplay.I().draw(delta);


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


        CompChipBar.I().draw(delta);

        shop.drawPostProcessedItems(delta);


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

        boolean fullShopShowing = shop.isShowing();

        Pencil.I().draw(
            batch,
            delta,
            fullShopShowing
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

        if (!fullShopShowing) {
            Pencil.I().discardPostProcessedOnlyDrawings();
        }

        // ------------------------------------------------------------
        // FOREGROUND / NON-POST-PROCESSED CONTENT
        // ------------------------------------------------------------

        batch.begin();


        if (SlotMachine.I().isStale()) {
            buttonBoard.draw(delta);
        }

        shop.draw(delta);

        // Keep the standalone wallet crisp and outside the screen-wide CRT pass.
        // The full shop renders its own cash balance instead.
        if (!fullShopShowing) {
            ScoreDisplay.I().draw(delta);
        }

        BouncingSymbolManager.I()
            .drawFallingSymbols(delta, mouse);
        CollectorManager.I().draw();
        ChestManager.I().draw();
        SlotMachine.I()
            .drawSymbolsInPatternHit();
        PopupManager.I().draw(delta);

        /*
         * Finish the gameplay foreground before queuing the modal.
         *
         * Flushing here gives decision overlays a real compositing boundary:
         * the gameplay scene is drawn first, then the modal backdrop and
         * choices are drawn over it.
         */
        if (levelUpWindow.isShowing() || roundResultWindow.isShowing()) {
            Pencil.I().draw(
                batch,
                delta,
                true
            );
        }

        /*
         * Draw the impact flash BEFORE the level-up window.
         *
         * This means the frozen gameplay gets punched by the flash,
         * but the upgrade UI itself remains readable.
         */
        drawLevelUpImpact(batch);


        levelUpWindow.draw(delta);

        roundResultWindow.draw(delta);


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

        handleDeveloperShortcuts();

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

        } else if (roundResultWindow.isShowing()) {

            roundResultWindow.handleInput(
                mouse,
                leftClickPressed,
                leftClickWasPressed
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

            boolean chestConsumedInput =
                ChestManager.I().handleInput(
                    mouse,
                    leftClickPressed,
                    leftClickWasPressed
                );

            if (!chestConsumedInput) {

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
                        leftClickWasPressed,
                        delta
                    );


                buttonBoard.handleInput(
                    mouse,
                    leftClickPressed,
                    leftClickWasPressed
                );
            }
        }


        leftClickWasPressed =
            leftClickPressed;
    }

    private void handleDeveloperShortcuts() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            return;
        }

        boolean enabled =
            DevTools.toggleFreeShopPurchases();

        if (enabled) {
            AudioManager.I().playUpgradeSelected();
            ScreenShake.I().addTrauma(0.08f);
        } else {
            AudioManager.I().playHover();
        }
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

        if (!SlotMachine.I().isStale()) {
            return;
        }

        if (waitingForCollectibles()) {
            autoSpinWaitingForCollectibles = autoSpinEnabled();
            return;
        }

        autoSpinWaitingForCollectibles = false;

        if (
            Automations.I()
                .getAutoSpin()
                .isActive() &&
                !Automations.I()
                    .getFullAutoSpin()
                    .isActive() &&
                AutoSpinDisplay.I()
                    .getSpins() < 1
        ) {
            return;
        }

        if (!RunManager.I().getRoundsManager().tryStartSpin()) {
            AudioManager.I().playMiss();
            return;
        }


        SlotMachine.I().setAlpha(1f);

        TicketPressSystem.I().beginSpin();

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
            Automations.I().getAutoSpin().isActive() &&
                !Automations.I().getFullAutoSpin().isActive()
        ) {

            AutoSpinDisplay.I()
                .removeSpin();
        }
    }

    private boolean waitingForCollectibles() {
        return !Automations.I().getQuickSpin().isActive()
            && BouncingSymbolManager.I().hasUnclaimedCollectibles();
    }

    private boolean autoSpinEnabled() {
        return Automations.I().getAutoSpin().isActive()
            || Automations.I().getFullAutoSpin().isActive();
    }

    private void resumeAutoSpinAfterCollecting() {
        if (!autoSpinWaitingForCollectibles
            || waitingForCollectibles()
            || !SlotMachine.I().isStale()) {
            return;
        }

        if (!RunManager.I().getRoundsManager().canSpin()) {
            autoSpinWaitingForCollectibles = false;
            return;
        }

        onSpinButtonPressed();
    }


    // ============================================================
    // ROUND
    // ============================================================

    public void onRoundEnd() {
        onSpinResolved();
    }

    public boolean onSpinResolved() {
        RoundsManager rounds = RunManager.I().getRoundsManager();
        float cash = ScoreDisplay.I().getScoreNumber();
        float bill = rounds.getRoundTarget();
        RoundsManager.RoundOutcome outcome = rounds.resolveRound(cash);

        if (outcome == RoundsManager.RoundOutcome.IN_PROGRESS) {
            return false;
        }
        if (roundResultWindow.isShowing()) {
            return true;
        }

        if (outcome == RoundsManager.RoundOutcome.CLEARED) {
            AudioManager.I().playUpgradeSelected();
            ScreenShake.I().addTrauma(0.16f);
            roundResultWindow.showCleared(
                cash,
                bill,
                () -> payBillAndShowRoundReward(bill)
            );
        } else {
            AudioManager.I().playMiss();
            ScreenShake.I().addTrauma(0.22f);
            roundResultWindow.showFailed(cash, bill, this::restartFailedRun);
        }
        return true;
    }

    private void restartFailedRun() {
        resetRunState();
        buttonBoard.setVisible(true);
        roundClockActive = true;
    }

    private void showRoundReward() {
        levelUpWindow.show(shop::show);
    }

    private void payBillAndShowRoundReward(float bill) {
        ScoreDisplay.I().removeFromScore(bill);
        showRoundReward();
    }

    private void startNextRound() {
        RunManager.I()
            .getRoundsManager()
            .nextRound();
        roundClockActive = true;

        if (
            Automations.I().getAutoSpin().isActive() ||
                Automations.I().getFullAutoSpin().isActive()
        ) {
            onSpinButtonPressed();
        }
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
                    showRoundReward();
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
        startNextRound();
    }

    public boolean isShopShowing() {
        return shop.isShowing();
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
        RoundStats.I().recordSymbolHit();
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

        if (Gdx.input.getInputProcessor() == shopScrollInput) {
            Gdx.input.setInputProcessor(null);
        }

        /*
         * This texture is created by SlotScreen itself,
         * so SlotScreen owns and disposes it.
         */
        levelUpFlashTexture.dispose();


        vfxManager.dispose();
    }
}
