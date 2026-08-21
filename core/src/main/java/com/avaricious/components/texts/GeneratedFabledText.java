package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Creates short decorative labels from the existing letter atlas. */
public class GeneratedFabledText extends FabledText {

    public GeneratedFabledText(
        String text,
        float sizeRatio,
        float letterSpacing,
        float wordGap,
        ZIndex zIndex
    ) {
        super(createWords(text, sizeRatio, letterSpacing, wordGap, zIndex));
        setFloatEffects(0.02f, 1f);
    }

    private static FabledWord[] createWords(
        String text,
        float sizeRatio,
        float letterSpacing,
        float wordGap,
        ZIndex zIndex
    ) {
        String[] labels = text.trim().toUpperCase(Locale.ROOT).split("\\s+");
        List<FabledWord> words = new ArrayList<>();
        float x = 0f;

        for (String label : labels) {
            List<TextureRegion> letters = new ArrayList<>();
            List<TextureRegion> shadows = new ArrayList<>();

            for (int index = 0; index < label.length(); index++) {
                String letter = String.valueOf(label.charAt(index));
                letters.add(Assets.I().get(AssetKey.valueOf(letter)));
                shadows.add(Assets.I().get(
                    AssetKey.valueOf(letter + "_SHADOW")
                ));
            }

            FabledWord word = new FabledWord(
                letters,
                shadows,
                new Vector2(x, 0f),
                sizeRatio,
                letterSpacing,
                zIndex
            );
            words.add(word);
            x += word.getWidth() + wordGap;
        }

        return words.toArray(new FabledWord[0]);
    }
}
