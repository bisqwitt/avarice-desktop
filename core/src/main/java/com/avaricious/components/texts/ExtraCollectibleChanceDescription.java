package com.avaricious.components.texts;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtraCollectibleChanceDescription extends FabledText {

    private static final float SIZE_RATIO = 27f;

    /*
     * Tight spacing inside:
     *
     * 10%
     */
    private static final float NUMBER_SPACING = 0.05f;

    /*
     * Exact space on BOTH sides of the arrow:
     *
     * 10% [GAP] -> [GAP] 20%
     */
    private static final float ARROW_GAP = 0.15f;

    public ExtraCollectibleChanceDescription(Symbol symbol) {

        SymbolValues.I().addValueChangeListener(evt -> {
            if (evt.getPropertyName().equals(symbol.toString())) {

                int currentValue =
                    SymbolValues.I().getExtraCollectibleSpawnChance(symbol);

                updateDescription(
                    currentValue,
                    currentValue + 10
                );
            }
        });

        int currentValue =
            SymbolValues.I().getExtraCollectibleSpawnChance(symbol);

        updateDescription(
            currentValue,
            currentValue + 10
        );
    }

    public void updateDescription(
        int currentValue,
        int nextValue
    ) {

        /*
         * First percentage:
         *
         * 10%
         */
        FabledWord currentWord =
            createPercentageWord(
                currentValue,
                new Vector2(0f, 0f)
            );

        /*
         * Arrow starts exactly ARROW_GAP after
         * the visible end of currentWord.
         */
        float arrowX =
            currentWord.getWidth()
                + ARROW_GAP;

        FabledWord arrowWord =
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(
                        AssetKey.ARROW_LETTER
                    )
                ),
                Arrays.asList(
                    Assets.I().get(
                        AssetKey.ARROW_LETTER_SHADOW
                    )
                ),
                new Vector2(
                    arrowX,
                    0f
                ),
                SIZE_RATIO,
                0f,
                ZIndex.SHOP_CARD
            );

        /*
         * Next percentage starts exactly ARROW_GAP
         * after the visible end of the arrow.
         */
        float nextValueX =
            arrowX
                + arrowWord.getWidth()
                + ARROW_GAP;

        FabledWord nextWord =
            createPercentageWord(
                nextValue,
                new Vector2(
                    nextValueX,
                    0f
                )
            );

        setWords(
            currentWord,
            arrowWord,
            nextWord
        );
    }

    private FabledWord createPercentageWord(
        int value,
        Vector2 position
    ) {

        List<TextureRegion> textures =
            new ArrayList<>();

        List<TextureRegion> shadows =
            new ArrayList<>();

        addNumber(
            textures,
            value
        );

        addNumberShadows(
            shadows,
            value
        );

        textures.add(
            Assets.I().get(
                AssetKey.PERCENTAGE_SYMBOL
            )
        );

        shadows.add(
            Assets.I().get(
                AssetKey.PERCENTAGE_SYMBOL
            )
        );

        return new FabledWord(
            textures,
            shadows,
            position,
            SIZE_RATIO,
            NUMBER_SPACING,
            ZIndex.SHOP_CARD
        );
    }

    private void addNumber(
        List<TextureRegion> textures,
        int number
    ) {

        String value =
            String.valueOf(number);

        for (
            int i = 0;
            i < value.length();
            i++
        ) {

            int digit =
                Character.getNumericValue(
                    value.charAt(i)
                );

            textures.add(
                Assets.I().getDigitalNumber(
                    digit
                )
            );
        }
    }

    private void addNumberShadows(
        List<TextureRegion> textures,
        int number
    ) {

        String value =
            String.valueOf(number);

        for (
            int i = 0;
            i < value.length();
            i++
        ) {

            int digit =
                Character.getNumericValue(
                    value.charAt(i)
                );

            textures.add(
                Assets.I().getDigitalNumberShadow(
                    digit
                )
            );
        }
    }
}
