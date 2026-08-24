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
        this(text, sizeRatio, letterSpacing, wordGap, zIndex, false);
    }

    public GeneratedFabledText(
        String text,
        float sizeRatio,
        float letterSpacing,
        float wordGap,
        ZIndex zIndex,
        boolean bigFirstLetter
    ) {
        super(createWords(text, sizeRatio, letterSpacing, wordGap, zIndex, bigFirstLetter));
        setFloatEffects(0.02f, 1f);
    }

    private static FabledWord[] createWords(
        String text,
        float sizeRatio,
        float letterSpacing,
        float wordGap,
        ZIndex zIndex,
        boolean bigFirstLetter
    ) {
        String[] labels = text.trim().toUpperCase(Locale.ROOT).split("\\s+");
        List<FabledWord> words = new ArrayList<>();
        float x = 0f;

        for (int wordIndex = 0; wordIndex < labels.length; wordIndex++) {
            String label = labels[wordIndex];
            List<TextureRegion> letters = new ArrayList<>();
            List<TextureRegion> shadows = new ArrayList<>();

            for (int index = 0; index < label.length(); index++) {
                String letter = String.valueOf(label.charAt(index));
                boolean useBigLetter = bigFirstLetter && wordIndex == 0 && index == 0
                    && hasBigLetter(letter);
                String assetName = useBigLetter ? letter + "_BIG" : letter;
                letters.add(Assets.I().get(AssetKey.valueOf(assetName)));
                shadows.add(Assets.I().get(AssetKey.valueOf(assetName + "_SHADOW")));
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

    private static boolean hasBigLetter(String letter) {
        switch (letter) {
            case "A": case "B": case "C": case "D": case "E":
            case "H": case "I": case "L": case "P": case "R":
            case "S": case "T": case "V": case "W":
                return true;
            default:
                return false;
        }
    }
}
