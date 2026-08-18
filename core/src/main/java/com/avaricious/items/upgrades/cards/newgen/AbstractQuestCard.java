package com.avaricious.items.upgrades.cards.newgen;

import com.avaricious.components.slot.pattern.PatternMatch;
import com.avaricious.items.upgrades.cards.AbstractCard;

import java.util.List;

public abstract class AbstractQuestCard extends AbstractCard {

    private boolean completed = false;

    public abstract boolean condition(List<PatternMatch> matches, PatternMatch match);

    public void complete() {
        completed = true;
        body.pulse();
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void apply() {
        super.apply();
    }

    public void reset() {
        completed = false;
    }
}
