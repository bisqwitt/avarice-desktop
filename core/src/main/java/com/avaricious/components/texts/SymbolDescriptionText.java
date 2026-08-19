package com.avaricious.components.texts;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class SymbolDescriptionText extends FabledText {

    public SymbolDescriptionText(Symbol symbol) {
        SymbolValues.I().addValueChangeListener(evt -> {
            if (evt.getPropertyName().equals(symbol.toString())) {
                int newValue = (int) evt.getNewValue();
                updateDescription(newValue, newValue + 1);
            }
        });

        updateDescription(SymbolValues.I().getValue(symbol), SymbolValues.I().getValue(symbol) + 1);
    }

    public void updateDescription(int currentValue, int nextValue) {
        setWords(
            new FabledWord(
                Arrays.asList(
                    Assets.I().getDigitalNumber(currentValue),
                    Assets.I().get(AssetKey.ARROW_LETTER),
                    Assets.I().getDigitalNumber(nextValue)
                ),
                Arrays.asList(
                    Assets.I().getDigitalNumberShadow(currentValue),
                    Assets.I().get(AssetKey.ARROW_LETTER_SHADOW),
                    Assets.I().getDigitalNumberShadow(nextValue)
                ), new Vector2(0f, 0f), 27f, 0.3f, ZIndex.SHOP_CARD)
        );
    }

}
