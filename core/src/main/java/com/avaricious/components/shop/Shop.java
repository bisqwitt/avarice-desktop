package com.avaricious.components.shop;

import com.avaricious.CreditNumber;
import com.avaricious.components.ButtonBoard;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.buttons.Button;
import com.avaricious.components.buttons.ExitShopButton;
import com.avaricious.components.buttons.ShopListToggleBoard;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.texts.ShopWord;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Shop {

    private static final float WINDOW_X = -1f;
    private static final float WINDOW_Y = 0f;
    private static final float WINDOW_WIDTH = 375 / 35f;
    public static final float WINDOW_HEIGHT = 800 / 35f;

    // fully above the screen
    private static final float OFFSCREEN_TOP_Y = WINDOW_Y + WINDOW_HEIGHT + 2f;

    private float currentWindowY = OFFSCREEN_TOP_Y;

    private final ShopWord shopText = new ShopWord(new Vector2(WINDOW_X + 3.5f, currentWindowY + 14.65f),
        8f, 0.25f, ZIndex.SHOP);
    private final TextureRegion window = Assets.I().get(AssetKey.CHARCOAL_PIXEL_DARKER);

    private final Button exitShopButton;
    private final CreditNumber creditScore;

    private final SymbolShopList symbolShopList = new SymbolShopList(new Rectangle(
        WINDOW_X + 1.5f,
        0f,
        WINDOW_WIDTH - 3f,
        12.5f
    ));
    private final AutomationShopList automationShopList = new AutomationShopList(new Rectangle(
        WINDOW_X + 1.5f,
        0f,
        WINDOW_WIDTH - 3f,
        12.5f
    ));

    private final ShopListToggleBoard shopListToggleBoard = new ShopListToggleBoard();

    private final Runnable onReturnedFromShop;

    private enum State {HIDDEN, ENTERING, SHOWN, EXITING}

    private State state = State.HIDDEN;
    private static final float GRAVITY_ENTER = -55f;
    private static final float GRAVITY_EXIT = 70f;
    private static final float BOUNCE_DAMPING = 0.28f;
    private static final float MIN_BOUNCE_VELOCITY = 4f;
    private float windowVelocityY = 0f;

    private final float uiMoveDistance = 5f;

    private final float baseCreditScoreY = 0.5f;
    private final float baseNextRoundButtonY = 0.25f;

    public Shop(Runnable onReturnedFromShop) {
        this.onReturnedFromShop = onReturnedFromShop;

        creditScore = new CreditNumber((int) ScoreDisplay.I().getScoreNumber(),
            new Rectangle(0.75f, baseCreditScoreY, 7 / 15f, 11 / 15f),
            0.7f
        ).setZIndex(ZIndex.SHOP_CARD);
        ScoreDisplay.I().addScoreChangeListener(evt -> creditScore.setValue((Float) evt.getNewValue()));

        exitShopButton = new ExitShopButton(new Rectangle(5.1f, 0.5f, 79 / 27f, 25 / 27f));

        // start hidden below like HealthUi.moveOut() result
        creditScore.getFirstDigitBounds().y = baseCreditScoreY - uiMoveDistance;
        exitShopButton.getBounds().y = baseNextRoundButtonY - uiMoveDistance;
    }

    public void draw(float delta) {
        if (state == State.HIDDEN) return;

        updateAnimation(delta);

        Pencil.I().addDrawing(new TextureDrawing(
            window,
            WINDOW_X, currentWindowY, WINDOW_WIDTH, WINDOW_HEIGHT,
            ZIndex.SHOP
        ));
        shopText.draw(delta);
        if (shopListToggleBoard.automationButtonIsToggeled()) automationShopList.draw(delta);
        if (shopListToggleBoard.symbolButtonIsToggeled()) symbolShopList.draw(delta);

        creditScore.draw(delta);
        exitShopButton.draw(delta);
        shopListToggleBoard.draw(delta);
    }

    public void show() {
        currentWindowY = OFFSCREEN_TOP_Y;
        windowVelocityY = 0f;
        state = State.ENTERING;

        // keep them out while entering
        creditScore.getFirstDigitBounds().y = baseCreditScoreY - uiMoveDistance;
        exitShopButton.getBounds().y = baseNextRoundButtonY - uiMoveDistance;

        ButtonBoard.I().moveOut();
    }

    public void exit() {
        state = State.EXITING;
        windowVelocityY = 0f;
        ButtonBoard.I().moveIn();
    }

    private void updateAnimation(float delta) {
        switch (state) {
            case ENTERING:
                windowVelocityY += GRAVITY_ENTER * delta;
                currentWindowY += windowVelocityY * delta;

                if (currentWindowY <= WINDOW_Y) {
                    currentWindowY = WINDOW_Y;
                    ScreenShake.I().addTrauma(Math.abs(windowVelocityY * 0.01f));

                    if (Math.abs(windowVelocityY) < MIN_BOUNCE_VELOCITY) {
                        windowVelocityY = 0f;
                        state = State.SHOWN;
                    } else {
                        windowVelocityY = -windowVelocityY * BOUNCE_DAMPING;
                    }
                }
                break;

            case EXITING:
                windowVelocityY += GRAVITY_EXIT * delta;
                currentWindowY += windowVelocityY * delta;

                if (currentWindowY >= OFFSCREEN_TOP_Y) {
                    currentWindowY = OFFSCREEN_TOP_Y;
                    windowVelocityY = 0f;
                    onReturnedFromShop.run();
                    state = State.HIDDEN;
                }
                break;

            case SHOWN:
            case HIDDEN:
            default:
                break;
        }

        // keep child UI synced with the window position
        if (state == State.ENTERING || state == State.EXITING) {
            shopText.getStartingPos().y = currentWindowY + 17f;
            symbolShopList.setY(currentWindowY + 2.5f);
            automationShopList.setY(currentWindowY + 2.5f);
            exitShopButton.getBounds().setY(currentWindowY + 0.75f);
            creditScore.getFirstDigitBounds().setY(currentWindowY + 0.9f);
            shopListToggleBoard.setY(currentWindowY + 15.5f);
        }
    }

    public boolean isShowing() {
        return state != State.HIDDEN;
    }

    public void handleInput(Vector2 mouse, boolean leftClickPressed, boolean leftClickWasPressed, float delta) {
        if (state != State.SHOWN) return;

        shopListToggleBoard.handleInput(mouse, leftClickPressed, leftClickWasPressed);
        if (shopListToggleBoard.symbolButtonIsToggeled())
            symbolShopList.handleInput(mouse, leftClickPressed, leftClickWasPressed);
        if (shopListToggleBoard.automationButtonIsToggeled())
            automationShopList.handleInput(mouse, leftClickPressed, leftClickWasPressed);
        exitShopButton.handleInput(mouse, leftClickPressed, leftClickWasPressed);
    }
}
