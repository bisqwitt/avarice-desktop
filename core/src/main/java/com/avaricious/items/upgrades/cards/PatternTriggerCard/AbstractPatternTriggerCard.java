package com.avaricious.items.upgrades.cards.PatternTriggerCard;

import com.avaricious.components.slot.SlotMachineMatchFinder;
import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.items.upgrades.cards.CardType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Seq;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class AbstractPatternTriggerCard extends AbstractCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.GROS_MICHEL);

    @Override
    public String description() {
        return "Trigger all " + length() + " in a Row's";
    }

    @Override
    public TextureRegion texture() {
        return texture;
    }

    @Override
    protected void onApply() {
        SlotMachineResultRunner.I().runResult(
            Seq.of(SlotMachineMatchFinder.I().findMatches())
                .filter(patternMatch -> patternMatch.getLength() == length())
                .toList()
        );
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> {
        };
    }

    @Override
    public IUpgradeType type() {
        return CardType.ATTACK;
    }

    protected abstract int length();
}
