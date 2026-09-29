package com.avaricious.components.texts;

import com.avaricious.game.progression.SkillTreeProgress;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Displays seconds gained on a successful claim as current -> next. */
public final class TimeGainedDescriptionText extends FabledText {

    private static final float SIZE_RATIO = 27f;
    private static final float NUMBER_SPACING = 0.05f;
    private static final float ARROW_GAP = 0.15f;

    public TimeGainedDescriptionText(
        SkillTreeProgress progress,
        int increaseAmount
    ) {
        progress.addTimeGainChangeListener(evt -> updateDescription(
            progress.getTimeGainSeconds(),
            progress.getNextTimeGainSeconds(increaseAmount)
        ));
        updateDescription(
            progress.getTimeGainSeconds(),
            progress.getNextTimeGainSeconds(increaseAmount)
        );
    }

    private void updateDescription(int currentValue, int nextValue) {
        FabledWord current = createSecondsWord(currentValue, new Vector2());
        float arrowX = current.getWidth() + ARROW_GAP;
        FabledWord arrow = new FabledWord(
            Arrays.asList(Assets.I().get(AssetKey.ARROW_LETTER)),
            Arrays.asList(Assets.I().get(AssetKey.ARROW_LETTER_SHADOW)),
            new Vector2(arrowX, 0f),
            SIZE_RATIO,
            0f,
            ZIndex.SHOP_CARD
        );
        FabledWord next = createSecondsWord(
            nextValue,
            new Vector2(arrowX + arrow.getWidth() + ARROW_GAP, 0f)
        );
        setWords(current, arrow, next);
    }

    private FabledWord createSecondsWord(int seconds, Vector2 position) {
        List<TextureRegion> textures = new ArrayList<>();
        List<TextureRegion> shadows = new ArrayList<>();
        String digits = String.valueOf(seconds);
        for (int i = 0; i < digits.length(); i++) {
            int digit = Character.getNumericValue(digits.charAt(i));
            textures.add(Assets.I().getDigitalNumber(digit));
            shadows.add(Assets.I().getDigitalNumberShadow(digit));
        }
        textures.add(Assets.I().get(AssetKey.S));
        shadows.add(Assets.I().get(AssetKey.S_SHADOW));
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
