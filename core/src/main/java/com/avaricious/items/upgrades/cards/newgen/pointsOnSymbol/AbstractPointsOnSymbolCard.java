package com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol;

import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.slot.pattern.PatternMatch;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.items.upgrades.cards.CardType;
import com.avaricious.items.upgrades.cards.newgen.AbstractQuestCard;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public abstract class AbstractPointsOnSymbolCard extends AbstractQuestCard {

    private final TextureRegion texture;
    private final TextureRegion completedTexture;

    public AbstractPointsOnSymbolCard(TextureRegion texture, TextureRegion completedTexture) {
        this.texture = texture;
        this.completedTexture = completedTexture;
    }

    abstract Symbol symbol();

    @Override
    public String description() {
        return "+50\n" + "Trigger a Pattern including " + symbol().toString();
    }

    @Override
    protected void onApply() {
        body.pulse();
//        ScoreDisplay.I().addPotentialValue(ScoreDisplay.Type.POINTS, 5);
        ScoreDisplay.I().addToScore(50);
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> PopupManager.I().spawnNumber(createNumberPopup(50, pos, Assets.I().lightColor()));
    }

    @Override
    public IUpgradeType type() {
        return CardType.ATTACK;
    }

    @Override
    public boolean condition(List<PatternMatch> matches, PatternMatch match) {
        return !isCompleted() && match.getSymbol() == symbol();
    }

    @Override
    public TextureRegion texture() {
        return isCompleted() ? completedTexture : texture;
    }
}
