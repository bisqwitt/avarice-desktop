package com.avaricious.items.upgrades.cards;

import com.avaricious.components.slot.SlotMachine;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class ShiftSymbolCard extends AbstractCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.SHORTCUT);

    @Override
    public String description() {
        return "Move a symbol by one";
    }

    @Override
    public TextureRegion texture() {
        return texture;
    }

    @Override
    protected void onApply() {
        SlotMachine.I().shiftSymbol();
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> {
        };
    }

    @Override
    public IUpgradeType type() {
        return CardType.UTILITY;
    }
}
