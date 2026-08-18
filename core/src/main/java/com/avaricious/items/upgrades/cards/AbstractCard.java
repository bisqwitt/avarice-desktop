package com.avaricious.items.upgrades.cards;

import com.avaricious.components.popups.NumberPopup;
import com.avaricious.items.upgrades.AbstractUpgrade;
import com.avaricious.items.upgrades.cards.PatternTriggerCard.FiveInARowTriggerCard;
import com.avaricious.items.upgrades.cards.PatternTriggerCard.FourInARowTriggerCard;
import com.avaricious.items.upgrades.cards.PatternTriggerCard.ThreeInARowTriggerCard;
import com.avaricious.items.upgrades.cards.SymbolTriggerCard.BellTriggerCard;
import com.avaricious.items.upgrades.cards.SymbolTriggerCard.CherryTriggerCard;
import com.avaricious.items.upgrades.cards.SymbolTriggerCard.CloverTriggerCard;
import com.avaricious.items.upgrades.cards.SymbolTriggerCard.DiamondTriggerCard;
import com.avaricious.items.upgrades.cards.SymbolTriggerCard.IronTriggerCard;
import com.avaricious.items.upgrades.cards.SymbolTriggerCard.LemonTriggerCard;
import com.avaricious.items.upgrades.cards.SymbolTriggerCard.SevenTriggerCard;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.RunManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCard extends AbstractUpgrade {

    public static float WIDTH = 142;
    public static float HEIGHT = 190;

    public abstract String description();

    protected abstract void onApply();

    public abstract Runnable createPopupRunnable(Vector2 pos);

    protected NumberPopup createNumberPopup(int value, Vector2 pos, Color color) {
        return new NumberPopup(value, color, posToBounds(pos), false, false);
    }

    protected Rectangle posToBounds(Vector2 pos) {
        return new Rectangle(pos.x, pos.y, NumberPopup.defaultWidth * 1.3f, NumberPopup.defaultHeight * 1.3f);
    }

    public void apply() {
        onApply();
    }

    @Override
    public TextureRegion shadowTexture() {
        return Assets.I().get(AssetKey.JOKER_CARD_SHADOW);
    }

    @Override
    public String title() {
        return "Card";
    }

    @Override
    public int price() {
        return 3;
    }

    public boolean isDisabled() {
        if (this instanceof IConditionalApplyCard && !((IConditionalApplyCard) this).condition())
            return true;
        if (RunManager.I().getRoundsManager().defenceTypeCardsDisabled() && this.type() == CardType.DEFENCE)
            return true;

        return false;
    }

    @Override
    public float getTextureHeight() {
        return HEIGHT;
    }

    @Override
    public float getTextureWidth() {
        return WIDTH;
    }

    @Override
    public float getTooltipYOffset() {
        return 2.6f;
    }

    public static AbstractCard randomCard() {
        return instantiateItem(allCardClasses.get((int) (Math.random() * allCardClasses.size())));
    }

    @Override
    public void addBody(Rectangle initialBounds) {
        super.addBody(initialBounds);
        body.getIdleScaleEffect().setAllowed(false);
    }

    public static final List<Class<? extends AbstractCard>> allCardClasses = Collections.unmodifiableList(Arrays.asList(
//        PointsCard.class,
//        MultiCard.class,
        SpinCard.class,
//        OneDollarCard.class,
//        PointsForEachCardInHandCard.class,
//        DrawACardIfLastCard.class,
//        MultiForEveryCardDiscarded.class,
//        DrawTwoCardsMinusOneTry.class,
//        PointsForEverySymbolHit.class,
//        PointsForEveryFruitCard.class,
//        EitherDoublePointsOrHalveMulti.class,
//        MultiForEveryAttackInHandCard.class,
//        DrawACardDefenceCardsDisabledCard.class,
//        MultiForEveryDisabledCard.class,
//        DrawACardDisabledUntilTwoCardsPlayedCard.class,
//        DrawCardsEqualToCurrentStreak.class,
//        DrawAndDiscardACard.class,
//        ImpersonatorCard.class,
//        ShiftSymbolCard.class,
        LemonTriggerCard.class,
        CherryTriggerCard.class,
        CloverTriggerCard.class,
        BellTriggerCard.class,
        IronTriggerCard.class,
        DiamondTriggerCard.class,
        SevenTriggerCard.class,
        ThreeInARowTriggerCard.class,
        FourInARowTriggerCard.class,
        FiveInARowTriggerCard.class
    ));
}
