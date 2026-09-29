package com.avaricious.items.upgrades.cards;

import com.avaricious.components.popups.PopupManager;
import com.avaricious.game.GameplayActions;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class SpinCard extends AbstractCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.DUSK_CARD);

    @Override
    public String description() {
        return "+1 Spin";
    }

    @Override
    protected void onApply() {
    }

    @Override
    public void apply(GameplayActions actions) {
        actions.requestSpin();
    }

    @Override
    public IUpgradeType type() {
        return CardType.DEFENCE;
    }

    @Override
    public TextureRegion texture() {
        return texture;
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> PopupManager.I().spawnNumber(createNumberPopup(20, pos, Assets.I().silver()));
    }
}
