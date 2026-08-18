package com.avaricious.components;

import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.TooltipPopup;
import com.avaricious.components.slot.DragableBody;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.items.upgrades.cards.newgen.AbstractQuestCard;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.FontDrawing;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.RunManager;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.UiUtility;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HandUi {

    private static HandUi instance;

    public static HandUi I() {
        return instance == null ? instance = new HandUi() : instance;
    }

    private final float Y = 3.5f;
    private final float CARD_SIZE_DIVISOR = 75;
    private final float CARD_OFFSET = 1.5f;

    private final TextureRegion jokerCard = Assets.I().get(AssetKey.JOKER_CARD);
    private final TextureRegion jokerCardShadow = Assets.I().get(AssetKey.JOKER_CARD_SHADOW);

    private final List<AbstractCard> cards = new ArrayList<>();
    private final GlyphLayout cardsHoldingTxt = new GlyphLayout();

    private AbstractCard selectedCard = null;
    private List<AbstractCard> applyingCards = new ArrayList<>();

    private List<? extends AbstractCard> pendingHand;
    private TooltipPopup tooltipPopup;
    private final Vector2 mouseTouchdownLocation = new Vector2();

    private boolean selectingCardToDiscard = false;
    private final GlyphLayout discardACardTxt = new GlyphLayout();

    private HandUi() {
        Hand.I().onChange(newHand -> pendingHand = newHand);
        discardACardTxt.setText(Assets.I().getBigFont(), "Select a card to discard", Color.WHITE, 500f, Align.top | Align.center, true);
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed, float delta) {
        if (pendingHand != null) {
            loadCards(pendingHand);
            pendingHand = null;
        }

        if (pressed && !wasPressed) {
            List<AbstractCard> sorted = getEntriesSortedByX();
            Collections.reverse(sorted);

            for (AbstractCard card : sorted) {
                if (card.getBody().getBounds().contains(mouse)) {
                    onCardTouchDown(card, mouse);
                    return;
                }
            }

            deselectCard(true); // deselect if touching somewhere on screen
        }

        if (!pressed && wasPressed && selectedCard != null) {
            onCardTouchReleased(selectedCard, mouse);
        }

        if (selectedCard != null) {
            Vector2 cardRenderPos = selectedCard.getBody().getRenderPos(new Vector2());
            PopupManager.I().updateTooltip(
                new Vector2(cardRenderPos.x - 1.5f, cardRenderPos.y + 3.25f), true);
        }
    }

    private void onCardTouchDown(AbstractCard card, Vector2 mouse) {
        if (selectedCard != null && card == selectedCard) {
            if (((AbstractQuestCard) card).isCompleted()) {
                applyCard(card);
            } else {
                deselectCard(true);
            }
            return;
        }

        if (selectedCard != null) deselectCard(true);
        selectedCard = card;
//        card.getBody().targetScale = 1.3f;
        Vector2 renderPos = card.getBody().getRenderPos(new Vector2());
        card.getBody().beginDrag(renderPos.x, renderPos.y, 0);
        card.getBody().dragTo(renderPos.x, Y + 0.75f, 0);
        card.getBody().setIdleEffectsEnabled(false);
//        card.getBody().beginDrag(renderPos.x, renderPos.y + 0.75f, 0);

        mouseTouchdownLocation.set(mouse);
        tooltipPopup = PopupManager.I().createTooltip(card, card.getBody().getRenderPos(new Vector2()));
    }

    private void onCardTouchReleased(AbstractCard card, Vector2 mouse) {
//        boolean isClick = mouseTouchdownLocation.dst2(mouse) <= 0.2f * 0.2f;
//        if (isClick) {
//            if (selectedCard == card) deselectCard(true);
//            else {
//                if (selectedCard != null) deselectCard(false);
//                selectedCard = card;
//            }
//        } else selectedCard = card;
    }

    public void draw(float delta) {
        Seq.of(cards).forEach(card -> card.getBody().update(delta));

        Seq.of(getEntriesSortedByX())
            .filter(card -> !applyingCards.contains(card))
            .forEach(this::drawCard);

        Seq.of(applyingCards).forEach(this::drawCard);

        cardsHoldingTxt.setText(Assets.I().getSmallFont(), cards.size() + " / 7", Color.WHITE, 200f, Align.top | Align.center, true);
//        Pencil.I().addDrawing(new FontDrawing(Assets.I().getSmallFont(), cardsHoldingTxt, cardsHoldingPos, ZIndex.HAND_UI_CARD));

        if (selectingCardToDiscard) {
            Pencil.I().addDrawing(new FontDrawing(Assets.I().getBigFont(), discardACardTxt, new Vector2(0.9f * 100, 6.75f * 100), ZIndex.HAND_UI_SELECTING_CARD_TO_DISCARD));
        }
    }

    private void drawCard(AbstractCard card) {
        DragableBody body = card.getBody();
        Rectangle bounds = body.getBounds();

        float scale = body.getScale();
        final float rotation = body.getRotation()
            + (card != selectedCard && !applyingCards.contains(card) ? getHandRotation(card) : 0);

        float alpha = body.getAlpha();
        if ((!selectingCardToDiscard && card.isDisabled())) {
            alpha -= 0.5f;
        }

        Vector2 position = body.getRenderPos(new Vector2());
        position.y += body.getIdleFloatYOffset();
        ZIndex zIndex = applyingCards.contains(card) ? ZIndex.HAND_UI_CARD_DRAGGING : ZIndex.HAND_UI_CARD;
        if (selectingCardToDiscard) zIndex = ZIndex.HAND_UI_SELECTING_CARD_TO_DISCARD;

        Color shadowColor = Assets.I().shadowColor();
        Vector2 shadowOffset = UiUtility.calcShadowOffset(body.getCardCenter());
        Pencil.I().addDrawing(new TextureDrawing(
            jokerCardShadow,
            position.x + shadowOffset.x, position.y - 0.2f,
            bounds.width, bounds.height,
            scale, rotation,
            zIndex, new Color(shadowColor.r, shadowColor.g, shadowColor.b, Math.min(0.25f, alpha))
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            card.texture(),
            position.x, position.y, bounds.width, bounds.height,
            scale, rotation,
            zIndex, new Color(1f, 1f, 1f, alpha)
        ));
    }

    private void loadCards(List<? extends AbstractCard> newHand) {
        // Add new Cards
        for (AbstractCard card : newHand) {
            if (!cards.contains(card)) {

                // 1) Start at deck position
                Vector2 deckSpawn = DeckUi.I().getTopCardSpawnPos();

                Rectangle initialBounds = new Rectangle(
                    deckSpawn.x, deckSpawn.y,
                    getCardWidth(), getCardHeight()
                );

                card.addBody(initialBounds);
                card.getBody().getIdleFloatEffect().setStrength(0.02f, 0.75f);
//                card.getBody().getIdleSwayEffect().setStrength();
                cards.add(card);
            }
        }

        // Remove old Cards
        List<AbstractCard> cardsToRemove = new ArrayList<>();
        for (AbstractCard card : cards) {
            if (!newHand.contains(card)) {
                cardsToRemove.add(card);
            }
        }
        for (AbstractCard card : cardsToRemove) {
            cards.remove(card);
        }

        updateCardBounds();
    }

    private void updateCardBounds() {
        Seq.of(cards)
            .filter(card -> !applyingCards.contains(card))
            .forEach(card -> card.getBody().moveTo(new Vector2(calcCardX(card), Y + getHandYOffset(card))));
    }

    private float calcCardX(AbstractCard card) {
        return calcFistX() + calcCardIndex(card) * calcOffset();
    }

    private float calcFistX() {
        List<AbstractCard> layoutCards = getCardsForLayout();
        int n = layoutCards.size();
        if (n == 0) return 0;

        float screenWidth = GameContext.I().viewport.getWorldWidth();
        float handWidth = (n - 1) * calcOffset() + getCardWidth();
        return (screenWidth - handWidth) / 2f;
    }

    private int calcCardIndex(AbstractCard card) {
        List<AbstractCard> sorted = getEntriesSortedByX();
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) == card) return i;
        }
        return -1;
    }

    private float calcOffset() {
//        return cards.size() == 6 ? CARD_OFFSET - 0.325f :
//            cards.size() == 7 ? CARD_OFFSET - 0.425f : CARD_OFFSET;
        return CARD_OFFSET;
    }

    private List<AbstractCard> getEntriesSortedByX() {
        List<AbstractCard> sorted = new ArrayList<>(getCardsForLayout());
        Collections.sort(sorted, new Comparator<AbstractCard>() {
            @Override
            public int compare(AbstractCard a, AbstractCard b) {
                float ax = a.getBody().getRenderPos(new Vector2()).x;
                float bx = b.getBody().getRenderPos(new Vector2()).x;

                if (ax < bx) return -1;
                if (ax > bx) return 1;
                return 0;
            }
        });
        return sorted;
    }

    private float getHandRotation(AbstractCard card) {
        List<AbstractCard> layoutCards = getEntriesSortedByX();
        int n = layoutCards.size();
        if (n <= 1) return 0f;

        int i = layoutCards.indexOf(card);
        if (i < 0) return 0f;

        float t = (i / (float) (n - 1)) * 2f - 1f; // [-1..+1]

        float fanMaxDeg = 12;
        float jitterMaxDeg = 1.0f;

        float curve = t * t * t;

        int h = card.hashCode();
        float jitter01 = (h & 0xFFFF) / 65535f;
        float jitter = jitter01 * 2f - 1f;

        return (-curve * fanMaxDeg) + (jitter * jitterMaxDeg);
    }

    private float getHandYOffset(AbstractCard card) {
        List<AbstractCard> layoutCards = getEntriesSortedByX();
        int n = layoutCards.size();
        if (n <= 1) return 0f;

        int i = layoutCards.indexOf(card);
        if (i < 0) return 0f;

        float t = (i / (float) (n - 1)) * 2f - 1f;
        float arc = 0.3f;

        return arc * (1f - t * t);
    }

    private List<AbstractCard> getCardsForLayout() {
        return Seq.of(cards)
            .filter(card -> !applyingCards.contains(card))
            .toList();
    }

    public void deselectCard(boolean killTooltip) {
        if (selectedCard == null) return;
//        selectedCard.getBody().moveTo(new Vector2(renderPos.x, renderPos.y - 0.75f));
        selectedCard.getBody().endDrag(0);
        selectedCard.getBody().targetScale = 1f;
        selectedCard.getBody().setIdleEffectsEnabled(true);
        selectedCard = null;
        if (killTooltip) {
            PopupManager.I().killTooltip(tooltipPopup);
            tooltipPopup = null;
        }
    }

    public void applySelectedCard() {
        applyCard(selectedCard);
    }

    public void applyCard(AbstractCard card) {
        selectedCard = null;
        card.apply();
        applyingCards.add(card);

        DragableBody body = card.getBody();
        body.pulse();
        Vector2 pos = body.getRenderPos(new Vector2());

        ParticleManager.I().create(pos.x + getCardWidth() / 2, pos.y + getCardHeight() / 2, ParticleType.RAINBOW, 0.03f, ZIndex.CARD_APPLY_PARTICLES);

        pos.x += 1.3f;
        pos.y += 1.8f;
        card.createPopupRunnable(pos).run();
        PopupManager.I().killTooltip(tooltipPopup);
        updateCardBounds();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                body.startApplyAnimation(0.6f, () -> {
                    Hand.I().removeCardFromHand(card);
                    applyingCards.remove(card);
                });
            }
        }, 0.5f);

        RunManager.I().getRoundsManager().onCardPlayed(card);
    }

    public void selectCardToDiscard() {
        selectingCardToDiscard = true;
        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.HAND_UI_SELECTING_CARD_TO_DISCARD);
    }

    public void discardCard(AbstractCard card) {
        if (selectingCardToDiscard) {
            selectingCardToDiscard = false;
            Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.HAND_UI_SELECTING_CARD_TO_DISCARD);
        }
        card.getBody().startApplyAnimation(0.6f, () -> {
            Hand.I().discardCard(card);
        });
    }

    private float getCardWidth() {
        return AbstractCard.WIDTH / CARD_SIZE_DIVISOR;
    }

    private float getCardHeight() {
        return AbstractCard.HEIGHT / CARD_SIZE_DIVISOR;
    }

    public boolean cardIsSelected() {
        return selectedCard != null;
    }
}
