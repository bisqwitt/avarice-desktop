package com.avaricious.components;

import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.TooltipPopup;
import com.avaricious.components.slot.DragableBody;
import com.avaricious.items.upgrades.Deck;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.FontDrawing;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckUi {

    private static DeckUi instance;

    public static DeckUi I() {
        return instance == null ? instance = new DeckUi() : instance;
    }

    private DeckUi() {
        Deck.I().onChange(newDeck -> pendingCards = newDeck);
        cardsInDeckTxt.setText(Assets.I().getTitleFont(), "Cards in Deck");
    }

    private final float CARD_WIDTH = AbstractCard.WIDTH / 90f;
    private final float CARD_HEIGHT = AbstractCard.HEIGHT / 90f;
    private final Rectangle firstCardBounds = new Rectangle(7.1f, 2.5f, CARD_WIDTH, CARD_HEIGHT);

    private final TextureRegion jokerCardBack = Assets.I().get(AssetKey.JOKER_CARD_BACK);
    private final TextureRegion jokerCardShadow = Assets.I().get(AssetKey.JOKER_CARD_SHADOW);

    private final List<AbstractCard> cards = new ArrayList<>();
    private List<? extends AbstractCard> pendingCards;

    private final GlyphLayout cardsInDeckTxt = new GlyphLayout();
    private final GlyphLayout cardsInDeckCountTxt = new GlyphLayout();

    private final Vector2 touchDownLocation = new Vector2();
    private boolean unfolded = false;
    private AbstractCard touchingCard = null;
    private AbstractCard selectedCard = null;
    private TooltipPopup tooltipPopup = null;

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed, float delta) {
        update(delta);

        if (pressed && !wasPressed) {
            touchDownLocation.set(mouse.x, mouse.y);
        }

        if (!pressed && wasPressed) {
            if (!unfolded && firstCardBounds.contains(touchDownLocation) && firstCardBounds.contains(mouse)) {
                toggleShowDeck();
            }
        }

        if (unfolded) {
            if (pressed && !wasPressed) {
                for (AbstractCard card : cards) {
                    if (card.getBody().getBounds().contains(mouse)) {
                        onCardTouchDown(card, mouse);
                        return;
                    }
                }
                deselectCard(true);
                toggleShowDeck();
            }

            if (pressed && touchingCard != null) {
                onCardTouching(touchingCard, mouse);
            }

            if (!pressed && wasPressed && touchingCard != null) {
                onCardTouchReleased(touchingCard, mouse);
            }

            if (touchingCard != null || selectedCard != null) {
                AbstractCard card = touchingCard == null ? selectedCard : touchingCard;
                Vector2 cardRenderPos = card.getBody().getRenderPos(new Vector2());
                PopupManager.I().updateTooltip(
                    new Vector2(cardRenderPos.x - 2f, cardRenderPos.y + 2.85f),
                    true
                );
            }
        }
    }

    private void onCardTouchDown(AbstractCard card, Vector2 mouse) {
        if (selectedCard != null && card != selectedCard) deselectCard(false);
        touchingCard = card;
        card.getBody().targetScale = 1.3f;
        card.getBody().beginDrag(mouse.x, mouse.y, 0);

        if (selectedCard == null)
            tooltipPopup = PopupManager.I().createTooltip(card, card.getBody().getRenderPos(new Vector2()), ZIndex.UNFOLDED_DECK_CARD);
    }

    private void onCardTouching(AbstractCard card, Vector2 mouse) {
        card.getBody().dragTo(mouse.x, mouse.y, 0);
    }

    private void onCardTouchReleased(AbstractCard card, Vector2 mouse) {
        DragableBody dragableBody = card.getBody();
        dragableBody.endDrag(0);
        boolean isClick = touchDownLocation.dst2(mouse) <= 0.2f * 0.2f;
        if (isClick) {
            if (selectedCard == card) deselectCard(true);
            else {
                if (selectedCard != null) deselectCard(false);
                selectedCard = card;
            }
        } else selectedCard = card;
        touchingCard = null;
    }

    private void update(float delta) {
        if (pendingCards != null) {
            loadPendingCards();
            pendingCards = null;
        }
        Seq.of(cards).forEach(card -> card.getBody().update(delta));
    }

    public void draw() {
        Vector2 cardsInDeckPos = new Vector2(7.85f * 100, 2.2f * 100f);
        cardsInDeckCountTxt.setText(Assets.I().getSmallFont(), cards.size() + " / " + (cards.size() + Hand.I().cardsHeldInHand()));
        Pencil.I().addDrawing(new FontDrawing(Assets.I().getSmallFont(), cardsInDeckCountTxt, cardsInDeckPos, ZIndex.DECK_UI_CARD));

        Pencil.I().addDrawing(new TextureDrawing(
            jokerCardShadow,
            firstCardBounds.x - 0.2f, firstCardBounds.y - 0.2f, CARD_WIDTH + 0.4f, CARD_HEIGHT + 0.4f,
            ZIndex.DECK_UI_BOX, Assets.I().shadowColor()));

        for (AbstractCard card : cards) {
            if (card != touchingCard) drawCard(card);
        }
        if (touchingCard != null) drawCard(touchingCard);

        if (unfolded) {
            Pencil.I().addDrawing(new FontDrawing(Assets.I().getTitleFont(), cardsInDeckTxt,
                new Vector2(1.75f * 100, 17f * 100), ZIndex.UNFOLDED_DECK_CARD));
        }
    }

    public void drawCard(AbstractCard card) {
        DragableBody slot = card.getBody();
        Vector2 pos = slot.getRenderPos(new Vector2());
        final float scale = slot.getScale();
        final float rotation = slot.getRotation();

        if (unfolded) {
            Pencil.I().addDrawing(new TextureDrawing(
                jokerCardShadow,
                pos.x, pos.y - 0.2f, firstCardBounds.width, firstCardBounds.height,
                scale, rotation, ZIndex.UNFOLDED_DECK_CARD, Assets.I().shadowColor()
            ));
        }
        Pencil.I().addDrawing(new TextureDrawing(
            unfolded ? card.texture() : jokerCardBack,
            pos.x, pos.y, firstCardBounds.width, firstCardBounds.height,
            scale, rotation, unfolded ? ZIndex.UNFOLDED_DECK_CARD : ZIndex.DECK_UI_CARD
        ));
    }

    private void loadPendingCards() {
        cards.clear();
        for (int i = 0; i < pendingCards.size(); i++) {
            Rectangle bounds = new Rectangle(
                firstCardBounds.x + 0.015f * i, firstCardBounds.y + 0.025f * i,
                firstCardBounds.width, firstCardBounds.height
            );
            pendingCards.get(i).addBody(bounds);
            pendingCards.get(i).getBody().setIdleEffectsEnabled(false);
            cards.add(pendingCards.get(i));
        }
    }

    private void toggleShowDeck() {
        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.UNFOLDED_DECK_BACKGROUND);
        if (!unfolded) {
            List<Vector2> positions = new ArrayList<>();
            for (int col = 5; col > 0; col--) {
                for (int row = 0; row < 4; row++) {
                    positions.add(new Vector2(0.7f + row * 2f, 1f + col * 2.5f));
                }
            }

            int index = 0;
            List<AbstractCard> reversed = new ArrayList<>(cards);
            Collections.reverse(reversed);
            for (AbstractCard card : reversed) {
                int finalIndex = index;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        card.getBody().moveTo(positions.get(finalIndex));
                    }
                }, index * 0.025f);
                index++;
            }
            unfolded = true;
            Seq.of(cards).forEach(card -> card.getBody().setIdleEffectsEnabled(true));
        } else {
            List<AbstractCard> reversed = new ArrayList<>(cards);
            Collections.reverse(reversed);
            int index = reversed.size() - 1;
            for (AbstractCard card : reversed) {
                int finalIndex = index;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        card.getBody().moveTo(new Vector2(firstCardBounds.x + 0.015f * finalIndex, firstCardBounds.y + 0.025f * finalIndex));
                    }
                }, index * 0.025f);
                index--;
            }
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    unfolded = false;
                    Seq.of(cards).forEach(card -> card.getBody().setIdleEffectsEnabled(false));
                }
            }, reversed.size() * 0.025f + 0.25f);
        }
    }

    public void deselectCard(boolean killTooltip) {
        if (selectedCard == null) return;
        selectedCard.getBody().targetScale = 1f;
        selectedCard.getBody().setIdleEffectsEnabled(true);
        selectedCard = null;
        if (killTooltip) {
            PopupManager.I().killTooltip(tooltipPopup);
            tooltipPopup = null;
        }
    }

    public Vector2 getTopCardSpawnPos() {
        return new Vector2(firstCardBounds.x, firstCardBounds.y);
    }

    public Vector2 getTopCardCenter(Vector2 out) {
        return out.set(
            firstCardBounds.x + firstCardBounds.width / 2f,
            firstCardBounds.y + firstCardBounds.height / 2f
        );
    }

    public Rectangle getFirstCardBounds() {
        return firstCardBounds;
    }

    public Rectangle getHitBox() {
        return new Rectangle(firstCardBounds.x - 0.1f, firstCardBounds.y - 0.1f, firstCardBounds.width + 0.2f, firstCardBounds.height + 0.2f);
    }
}
