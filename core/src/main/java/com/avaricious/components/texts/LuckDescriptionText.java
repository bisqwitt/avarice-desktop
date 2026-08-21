package com.avaricious.components.texts;

import com.avaricious.components.automations.Automations;
import com.avaricious.components.automations.Luck;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LuckDescriptionText extends FabledText {

    private static final float SIZE_RATIO = 27f;
    private static final float NUMBER_SPACING = 0.05f;
    private static final float ARROW_GAP = 0.15f;

    public LuckDescriptionText() {
        Luck luck = Automations.I().getLuck();

        luck.addPropertyChangeListener(evt -> updateDescription(
            luck.getBonusPercent(),
            luck.getNextBonusPercent()
        ));

        updateDescription(
            luck.getBonusPercent(),
            luck.getNextBonusPercent()
        );
    }

    private void updateDescription(int currentValue, int nextValue) {
        float y = getWords().isEmpty()
            ? 14f
            : getWords().get(0).getStartingPos().y;

        FabledWord currentWord = createPercentageWord(
            currentValue,
            new Vector2(1.25f, y)
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
        FabledWord nextWord = createPercentageWord(
            nextValue,
            new Vector2(nextX, y)
        );

        setWords(currentWord, arrowWord, nextWord);
    }

    private FabledWord createPercentageWord(int value, Vector2 position) {
        List<TextureRegion> textures = new ArrayList<>();
        List<TextureRegion> shadows = new ArrayList<>();

        String digits = String.valueOf(value);
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
}
