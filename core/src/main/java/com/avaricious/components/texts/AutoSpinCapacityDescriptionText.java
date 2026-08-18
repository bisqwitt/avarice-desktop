package com.avaricious.components.texts;

import com.avaricious.components.automations.AutoSpinCapacity;
import com.avaricious.components.automations.Automations;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class AutoSpinCapacityDescriptionText extends FabledText {

    public AutoSpinCapacityDescriptionText() {
        AutoSpinCapacity autoSpinCapacity = Automations.I().getAutoSpinCapacity();

        autoSpinCapacity.addPropertyChangeListener(evt -> {
            updateDescription((int) evt.getNewValue(), autoSpinCapacity.getNextCapacity());
        });

        updateDescription(autoSpinCapacity.getCapacity(), autoSpinCapacity.getNextCapacity());
    }

    public void updateDescription(int currentCapacity, int nextCapacity) {
        float y = 14f;
        if (!getWords().isEmpty()) y = getWords().get(0).getStartingPos().y;
        setWords(
            new FabledWord(
                Arrays.asList(
                    Assets.I().getDigitalNumber(currentCapacity),
                    Assets.I().get(AssetKey.ARROW_LETTER),
                    Assets.I().getDigitalNumber(nextCapacity)
                ),
                Arrays.asList(
                    Assets.I().getDigitalNumberShadow(currentCapacity),
                    Assets.I().get(AssetKey.ARROW_LETTER_SHADOW),
                    Assets.I().getDigitalNumberShadow(nextCapacity)
                ), new Vector2(1.25f, y), 27f, 0.3f, ZIndex.SHOP_CARD)
        );
    }

}
