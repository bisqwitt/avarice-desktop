package com.avaricious.components;

import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.*;
import com.avaricious.utility.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LevelUpWindow {

    private final TextureRegion background =
        Assets.I().get(AssetKey.CHARCOAL_PIXEL);

    private final LevelUpText title = new LevelUpText();

    private final List<LevelUpChoice> choices = new ArrayList<>();

    private boolean showing = false;

    private boolean pressReleased = false;

    public void show() {
        showing = true;
        pressReleased = false;
        SlotMachineResultRunner.I().getScheduler().pause();

        generateChoices();
    }

    public void hide() {
        showing = false;
        choices.clear();
        SlotMachineResultRunner.I().getScheduler().resume();
    }

    private void generateChoices() {
        choices.clear();

        List<LevelUpChoice> possibleChoices = new ArrayList<>();

        possibleChoices.add(new LevelUpChoice(
            new LemonValueText(),
            new SymbolDescriptionText(Symbol.LEMON),
            () -> SymbolValues.I().increaseValue(Symbol.LEMON)
        ));
        possibleChoices.add(new LevelUpChoice(
            new CherryValueText(),
            new SymbolDescriptionText(Symbol.CHERRY),
            () -> SymbolValues.I().increaseValue(Symbol.CHERRY)
        ));
        possibleChoices.add(new LevelUpChoice(
            new CloverValueText(),
            new SymbolDescriptionText(Symbol.CLOVER),
            () -> SymbolValues.I().increaseValue(Symbol.CLOVER)
        ));
        possibleChoices.add(new LevelUpChoice(
            new BellValueText(),
            new SymbolDescriptionText(Symbol.BELL),
            () -> SymbolValues.I().increaseValue(Symbol.BELL)
        ));
        possibleChoices.add(new LevelUpChoice(
            new IronValueText(),
            new SymbolDescriptionText(Symbol.IRON),
            () -> SymbolValues.I().increaseValue(Symbol.IRON)
        ));
        possibleChoices.add(new LevelUpChoice(
            new DiamondValueText(),
            new SymbolDescriptionText(Symbol.DIAMOND),
            () -> SymbolValues.I().increaseValue(Symbol.DIAMOND)
        ));
        possibleChoices.add(new LevelUpChoice(
            new SevenValueText(),
            new SymbolDescriptionText(Symbol.SEVEN),
            () -> SymbolValues.I().increaseValue(Symbol.SEVEN)
        ));

        Collections.shuffle(possibleChoices);

        for (int i = 0; i < Math.min(3, possibleChoices.size()); i++) {
            choices.add(possibleChoices.get(i));
        }

        updateChoiceBounds();
    }

    private void updateChoiceBounds() {
        float width = 4f;
        float height = 4.5f;

        float gap = 0.5f;

        float totalWidth =
            width * 3 +
                gap * 2;

        float startX =
            (16f - totalWidth) / 2f;

        float y = 2f;

        for (int i = 0; i < choices.size(); i++) {
            choices.get(i).setBounds(
                new Rectangle(
                    startX + i * (width + gap),
                    y,
                    width,
                    height
                )
            );
        }
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (!showing) return;

        if(!pressed && !pressReleased) pressReleased = true;

        if (!pressed || !pressReleased) {
            return;
        }

        for (LevelUpChoice choice : choices) {
            if (choice.contains(mouse)) {
                choice.select();
                hide();
                return;
            }
        }
    }

    public void draw(float delta) {
        if (!showing) return;

        Pencil.I().addDrawing(new TextureDrawing(
            background,
            0,
            0,
            16,
            9,
            ZIndex.SHOP,
            Assets.I().shadowColor()
        ));

        title.draw(delta);

        for (LevelUpChoice choice : choices) {
            choice.draw(delta);
        }
    }

    public boolean isShowing() {
        return showing;
    }
}
