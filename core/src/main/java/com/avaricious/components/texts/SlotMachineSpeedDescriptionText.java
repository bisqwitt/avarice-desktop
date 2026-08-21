package com.avaricious.components.texts;

import com.avaricious.components.automations.Automations;
import com.avaricious.components.automations.SlotMachineSpeed;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlotMachineSpeedDescriptionText extends FabledText {

    private static final float SIZE_RATIO = 27f;
    private static final float NUMBER_SPACING = 0.05f;
    private static final float ARROW_GAP = 0.15f;

    public SlotMachineSpeedDescriptionText() {
        SlotMachineSpeed speed = Automations.I().getSlotMachineSpeed();

        speed.addSpeedTierChangeListener(evt -> {
            if (SlotMachineSpeed.SPEED_TIER.equals(evt.getPropertyName())) {
                updateDescription(
                    speed.getSpeedPercent(),
                    speed.getNextSpeedPercent()
                );
            }
        });

        updateDescription(
            speed.getSpeedPercent(),
            speed.getNextSpeedPercent()
        );
    }

    private void updateDescription(int currentSpeed, int nextSpeed) {
        float y = getWords().isEmpty()
            ? 15f
            : getWords().get(0).getStartingPos().y;
        float x = getWords().isEmpty()
            ? 1.25f
            : getWords().get(0).getStartingPos().x;

        FabledWord currentWord = createSpeedWord(
            currentSpeed,
            new Vector2(x, y)
        );

        float arrowX = currentWord.getStartingPos().x
            + currentWord.getWidth()
            + ARROW_GAP;
        FabledWord arrowWord = new FabledWord(
            Arrays.asList(Assets.I().get(AssetKey.ARROW_LETTER)),
            Arrays.asList(Assets.I().get(AssetKey.ARROW_LETTER_SHADOW)),
            new Vector2(arrowX, y),
            SIZE_RATIO,
            0f,
            ZIndex.SHOP_CARD
        );

        float nextX = arrowX + arrowWord.getWidth() + ARROW_GAP;
        FabledWord nextWord = createSpeedWord(
            nextSpeed,
            new Vector2(nextX, y)
        );

        setWords(currentWord, arrowWord, nextWord);
    }

    private FabledWord createSpeedWord(int speedPercent, Vector2 position) {
        if (speedPercent == SlotMachineSpeed.INSTANT_SPEED_PERCENT) {
            return createInstantWord(position);
        }

        List<TextureRegion> textures = new ArrayList<>();
        List<TextureRegion> shadows = new ArrayList<>();

        String digits = String.valueOf(speedPercent);
        for (int i = 0; i < digits.length(); i++) {
            int digit = Character.getNumericValue(digits.charAt(i));
            textures.add(Assets.I().getDigitalNumber(digit));
            shadows.add(Assets.I().getDigitalNumberShadow(digit));
        }

        textures.add(Assets.I().get(AssetKey.PERCENTAGE_SYMBOL));
        shadows.add(Assets.I().get(AssetKey.PERCENTAGE_SYMBOL_SHADOW));

        return new FabledWord(
            textures,
            shadows,
            position,
            SIZE_RATIO,
            NUMBER_SPACING,
            ZIndex.SHOP_CARD
        );
    }

    private FabledWord createInstantWord(Vector2 position) {
        return new FabledWord(
            Arrays.asList(
                Assets.I().get(AssetKey.I_BIG),
                Assets.I().get(AssetKey.N),
                Assets.I().get(AssetKey.S),
                Assets.I().get(AssetKey.T),
                Assets.I().get(AssetKey.A),
                Assets.I().get(AssetKey.N),
                Assets.I().get(AssetKey.T)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.I_BIG_SHADOW),
                Assets.I().get(AssetKey.N_SHADOW),
                Assets.I().get(AssetKey.S_SHADOW),
                Assets.I().get(AssetKey.T_SHADOW),
                Assets.I().get(AssetKey.A_SHADOW),
                Assets.I().get(AssetKey.N_SHADOW),
                Assets.I().get(AssetKey.T_SHADOW)
            ),
            position,
            SIZE_RATIO,
            NUMBER_SPACING,
            ZIndex.SHOP_CARD
        );
    }
}
