package com.avaricious.components.texts;

import com.avaricious.utility.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FabledText {

    private final List<FabledWord> words = new ArrayList<>();

    public FabledText(FabledWord... words) {
        this.words.addAll(Arrays.asList(words));
    }

    public void draw(float delta) {
        Seq.of(words).forEach(word -> word.draw(delta));
    }

    public void setY(float y) {
        Seq.of(words).forEach(word -> word.getStartingPos().y = y);
    }

    protected void setWords(FabledWord... newWords) {
        words.clear();
        words.addAll(Arrays.asList(newWords));
    }

    public List<FabledWord> getWords() {
        return words;
    }

    public void setFloatEffects(float amplitude, float speed) {
        Seq.of(words)
            .forEach(word -> Seq.of(word.floatEffects)
                .forEach(effect -> effect.setStrength(amplitude, speed)));
    }
}
