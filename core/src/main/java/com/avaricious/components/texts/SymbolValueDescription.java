package com.avaricious.components.texts;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.EconomyScaling;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SymbolValueDescription extends FabledText {

    private static final float SIZE_RATIO = 27f;

    // Spacing between digits of the same number.
    private static final float NUMBER_SPACING = 0.05f;

    // Equal spacing before and after the arrow.
    private static final float ARROW_GAP = 0.15f;

    public SymbolValueDescription(Symbol symbol) {
        SymbolValues.I().addValueChangeListener(evt -> {
            if (evt.getPropertyName().equals(symbol.toString())) {
                float newValue = ((Number) evt.getNewValue()).floatValue();

                updateDescription(
                    newValue,
                    SymbolValues.I().getNextValue(symbol)
                );
            }
        });

        float currentValue =
            SymbolValues.I().getValue(symbol);

        updateDescription(
            currentValue,
            SymbolValues.I().getNextValue(symbol)
        );
    }

    public void updateDescription(
        float currentValue,
        float nextValue
    ) {
        FabledWord currentWord =
            createNumberWord(
                currentValue,
                new Vector2(0f, 0f)
            );

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

        float nextValueX =
            arrowX
                + arrowWord.getWidth()
                + ARROW_GAP;

        FabledWord nextWord =
            createNumberWord(
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

    private FabledWord createNumberWord(
        float value,
        Vector2 position
    ) {
        List<TextureRegion> textures =
            new ArrayList<>();

        List<TextureRegion> shadows =
            new ArrayList<>();

        addNumber(textures, value, false);
        addNumber(shadows, value, true);

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
        float number,
        boolean shadow
    ) {
        String value = EconomyScaling.compact(number);
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (Character.isDigit(character)) {
                int digit = Character.getNumericValue(character);
                textures.add(shadow
                    ? Assets.I().getDigitalNumberShadow(digit)
                    : Assets.I().getDigitalNumber(digit));
                continue;
            }
            textures.add(Assets.I().get(numberAsset(character, shadow)));
        }
    }

    private AssetKey numberAsset(char character, boolean shadow) {
        switch (character) {
            case '.': return AssetKey.DOT_SYMBOL;
            case 'k': return shadow ? AssetKey.K_SHADOW : AssetKey.K;
            case 'm': return shadow ? AssetKey.M_SHADOW : AssetKey.M;
            case 'b': return shadow ? AssetKey.B_SHADOW : AssetKey.B;
            case 't': return shadow ? AssetKey.T_SHADOW : AssetKey.T;
            case 'q': return shadow ? AssetKey.Q_SHADOW : AssetKey.Q;
            default: throw new IllegalArgumentException("Unsupported compact number symbol");
        }
    }
}
