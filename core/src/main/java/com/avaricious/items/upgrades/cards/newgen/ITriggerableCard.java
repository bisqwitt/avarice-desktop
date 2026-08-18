package com.avaricious.items.upgrades.cards.newgen;

import com.avaricious.components.slot.pattern.PatternMatch;

import java.util.List;

public interface ITriggerableCard {

    boolean triggerable(List<PatternMatch> matches, PatternMatch patternMatch);

}
