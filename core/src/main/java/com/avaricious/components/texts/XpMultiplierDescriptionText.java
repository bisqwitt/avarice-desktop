package com.avaricious.components.texts;

import com.avaricious.components.automations.Automations;
import com.avaricious.components.automations.XpMultiplier;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XpMultiplierDescriptionText extends FabledText {

    private static final float SIZE_RATIO = 27f;
    private static final float NUMBER_SPACING = 0.05f;
    private static final float ARROW_GAP = 0.15f;

    public XpMultiplierDescriptionText() {
        XpMultiplier multiplier = Automations.I().getXpMultiplier();
        multiplier.addMultiplierChangeListener(evt -> {
            if (XpMultiplier.MULTIPLIER.equals(evt.getPropertyName())) {
                updateDescription(
                    multiplier.getMultiplier(),
                    multiplier.getNextMultiplier()
                );
            }
        });
        updateDescription(
            multiplier.getMultiplier(),
            multiplier.getNextMultiplier()
        );
    }

    private void updateDescription(int current, int next) {
        float x = getWords().isEmpty()
            ? 1.25f
            : getWords().get(0).getStartingPos().x;
        float y = getWords().isEmpty()
            ? 14f
            : getWords().get(0).getStartingPos().y;

        FabledWord currentWord = multiplierWord(current, new Vector2(x, y));
        float arrowX = x + currentWord.getWidth() + ARROW_GAP;
        FabledWord arrow = new FabledWord(
            Arrays.asList(Assets.I().get(AssetKey.ARROW_LETTER)),
            Arrays.asList(Assets.I().get(AssetKey.ARROW_LETTER_SHADOW)),
            new Vector2(arrowX, y),
            SIZE_RATIO,
            0f,
            ZIndex.SHOP_CARD
        );
        float nextX = arrowX + arrow.getWidth() + ARROW_GAP;
        setWords(
            currentWord,
            arrow,
            multiplierWord(next, new Vector2(nextX, y))
        );
    }

    private FabledWord multiplierWord(int multiplier, Vector2 position) {
        List<TextureRegion> textures = new ArrayList<>();
        List<TextureRegion> shadows = new ArrayList<>();
        String digits = String.valueOf(multiplier);
        for (int index = 0; index < digits.length(); index++) {
            int digit = Character.getNumericValue(digits.charAt(index));
            textures.add(Assets.I().getDigitalNumber(digit));
            shadows.add(Assets.I().getDigitalNumberShadow(digit));
        }
        textures.add(Assets.I().get(AssetKey.X));
        shadows.add(Assets.I().get(AssetKey.X_SHADOW));
        return new FabledWord(
            textures,
            shadows,
            position,
            SIZE_RATIO,
            NUMBER_SPACING,
            ZIndex.SHOP_CARD
        );
    }
}
