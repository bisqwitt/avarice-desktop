package com.avaricious.components.texts;

import com.avaricious.utility.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FabledText {

    private final List<FabledWord> words =
        new ArrayList<>();

    public FabledText(
        FabledWord... words
    ) {
        this.words.addAll(
            Arrays.asList(words)
        );
    }

    public void draw(float delta) {
        Seq.of(words)
            .forEach(
                word ->
                    word.draw(delta)
            );
    }

    /*
     * Sets every word to the supplied Y position.
     *
     * This keeps your existing behaviour unchanged.
     */
    public void setY(float y) {
        Seq.of(words)
            .forEach(
                word ->
                    word
                        .getStartingPos()
                        .y = y
            );
    }

    /*
     * IMPORTANT:
     *
     * This keeps the semantics of your old setX().
     *
     * It is an OFFSET, not an absolute position.
     *
     * I did not change that behaviour because other
     * FabledText subclasses may already depend on it.
     */
    public void setX(float x) {
        Seq.of(words)
            .forEach(
                word ->
                    word
                        .getStartingPos()
                        .x += x
            );
    }

    /*
     * Absolute X positioning while preserving the
     * spacing between all words.
     *
     * LevelUpChoice uses this because its card can move
     * every frame during animations. Using the old setX()
     * there would cause the text to drift endlessly.
     */
    public void setAbsoluteX(float x) {
        if (words.isEmpty()) return;

        float currentX =
            words.get(0)
                .getStartingPos()
                .x;

        float delta =
            x - currentX;

        for (FabledWord word : words) {
            word.getStartingPos().x +=
                delta;
        }
    }

    /*
     * Move all words together without destroying their
     * relative spacing.
     */
    public void translate(
        float x,
        float y
    ) {
        for (FabledWord word : words) {
            word.getStartingPos().x += x;
            word.getStartingPos().y += y;
        }
    }

    public float getX() {
        if (words.isEmpty()) {
            return 0f;
        }

        return words.get(0)
            .getStartingPos()
            .x;
    }

    public float getY() {
        if (words.isEmpty()) {
            return 0f;
        }

        return words.get(0)
            .getStartingPos()
            .y;
    }

    protected void setWords(
        FabledWord... newWords
    ) {
        words.clear();

        words.addAll(
            Arrays.asList(newWords)
        );
    }

    public List<FabledWord> getWords() {
        return words;
    }

    public void setFloatEffects(
        float amplitude,
        float speed
    ) {
        Seq.of(words)
            .forEach(
                word ->
                    Seq.of(
                            word.floatEffects
                        )
                        .forEach(
                            effect ->
                                effect.setStrength(
                                    amplitude,
                                    speed
                                )
                        )
            );
    }
}
