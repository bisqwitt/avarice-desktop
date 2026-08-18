package com.avaricious.items.upgrades.cards.SymbolTriggerCard;

import com.avaricious.components.slot.SlotMachineMatchFinder;
import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.components.slot.Symbol;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.items.upgrades.cards.CardType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class AbstractSymbolTriggerCard extends AbstractCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.HACK);

    @Override
    public String description() {
        return "Trigger all " + getSymbol().toString() + "'s";
    }

    @Override
    public TextureRegion texture() {
        return texture;
    }

    @Override
    protected void onApply() {
        SlotMachineResultRunner.I().runResult(SlotMachineMatchFinder.I().findSymbol(getSymbol()));
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

    protected abstract Symbol getSymbol();
}
