package com.avaricious.components.buttons;

import com.badlogic.gdx.math.Vector2;

public class ShopListToggleBoard {

    private final SymbolToggleButton symbolToggleButton;
    private final AutomationToggleButton automationToggleButton;

    public ShopListToggleBoard() {
        symbolToggleButton = new SymbolToggleButton(this::onSymbolButtonToggled, this::onSymbolButtonUntoggled);
        automationToggleButton = new AutomationToggleButton(this::onAutomationButtonToggled, this::onAutomationButtonUntoggled);
    }

    public void draw(float delta) {
        symbolToggleButton.draw(delta);
        automationToggleButton.draw(delta);
    }

    public void handleInput(Vector2 mouse, boolean pressing, boolean wasPressing) {
        symbolToggleButton.handleInput(mouse, pressing, wasPressing);
        automationToggleButton.handleInput(mouse, pressing, wasPressing);
    }

    private void onSymbolButtonToggled() {
        automationToggleButton.onButtonPressed();
    }

    private void onSymbolButtonUntoggled() {

    }

    private void onAutomationButtonToggled() {
        symbolToggleButton.onButtonPressed();
    }

    private void onAutomationButtonUntoggled() {

    }

    public void setY(float y) {
        symbolToggleButton.setY(y);
        automationToggleButton.setY(y);
    }

    public boolean symbolButtonIsToggeled() {
        return symbolToggleButton.isToggled();
    }

    public boolean automationButtonIsToggeled() {
        return automationToggleButton.isToggled();
    }

}
