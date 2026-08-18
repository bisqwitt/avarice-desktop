package com.avaricious.components;

import com.avaricious.components.buttons.Button;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.TooltipPopup;
import com.avaricious.components.slot.DragableBody;
import com.avaricious.components.texts.PermanentlyRemoveACardText;
import com.avaricious.items.upgrades.Deck;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class RemoveCardWindow {

    private final PermanentlyRemoveACardText title = new PermanentlyRemoveACardText();
    private final List<RemovableCard> cards = new ArrayList<>();

    private boolean open = false;

    private AbstractCard selectedCard = null;
    private AbstractCard touchingCard = null;

    private final Vector2 mouseTouchdownLocation = new Vector2();
    private TooltipPopup tooltipPopup;

    private final Button removeButton = new Button(this::onRemoveButtonPressed,
        Assets.I().get(AssetKey.REMOVE_BUTTON),
        Assets.I().get(AssetKey.REMOVE_BUTTON_PRESSED),
        Assets.I().get(AssetKey.REMOVE_BUTTON),
        new Rectangle(0, 0, 79 / 30f, 25 / 30f),
        Input.Keys.ENTER, ZIndex.PACK_OPENING);

    public void handleInput(Vector2 mouse, boolean touching, boolean wasTouching) {
        if (!open) return;

        if ((selectedCard != null || touchingCard != null) && removeButton.getBounds().contains(mouse)) {
            removeButton.handleInput(mouse, touching, wasTouching);
            return;
        }

        if (touching && !wasTouching) {
            mouseTouchdownLocation.set(mouse.x, mouse.y);
            for (RemovableCard card : cards) {
                if (card.card.getBody().getBounds().contains(mouse)) {
                    onCardTouchdown(card.card, mouse);
                    return;
                }
            }
            deselectCard(true);
        }

        if (touching && touchingCard != null) {
            onCardTouching(touchingCard, mouse);
        }

        if (!touching && wasTouching && touchingCard != null) {
            onCardTouchReleased(touchingCard, mouse);
        }

        if (touchingCard != null || selectedCard != null) {
            AbstractCard card = touchingCard == null ? selectedCard : touchingCard;
            Vector2 renderPos = card.getBody().getRenderPos(new Vector2());
            PopupManager.I().updateTooltip(
                new Vector2(renderPos.x - 2f, renderPos.y + 2.85f), true
            );
        }
    }

    private void onCardTouchdown(AbstractCard card, Vector2 mouse) {
        if (selectedCard != null && card != selectedCard) deselectCard(false);
        touchingCard = card;
        card.getBody().targetScale = 1.3f;
        card.getBody().beginDrag(mouse.x, mouse.y, 0);

        if (selectedCard == null)
            tooltipPopup = PopupManager.I().createTooltip(card, card.getBody().getRenderPos(new Vector2()), ZIndex.PACK_OPENING);
    }

    private void onCardTouching(AbstractCard card, Vector2 mouse) {
        card.getBody().dragTo(mouse.x, mouse.y, 0);
    }

    private void onCardTouchReleased(AbstractCard card, Vector2 mouse) {
        DragableBody body = card.getBody();
        body.endDrag(0);
        boolean isClick = mouseTouchdownLocation.dst(mouse) <= 0.2f * 0.2f;
        if (isClick) {
            if (selectedCard == card) deselectCard(true);
            else {
                if (selectedCard != null) deselectCard(false);
                selectedCard = card;
            }
        } else selectedCard = card;
        touchingCard = null;
    }

    public void draw(float delta) {
        if (!open) return;

        title.draw(delta);
        Seq.of(cards).forEach(this::drawCard);

        if (selectedCard != null || touchingCard != null) {
            AbstractCard card = selectedCard == null ? touchingCard : selectedCard;
            Vector2 renderPos = card.getBody().getRenderPos(new Vector2());
            removeButton.getBounds().x = renderPos.x - 0.5f;
            removeButton.getBounds().y = renderPos.y - 1.5f;
            removeButton.draw(delta);
        }
    }

    private void drawCard(RemovableCard card) {
        DragableBody body = card.card.getBody();
        Vector2 pos = body.getRenderPos(new Vector2());
        float scale = body.getScale();
        float rotation = body.getRotation();
        float width = AbstractCard.WIDTH / 90f;
        float height = AbstractCard.HEIGHT / 90f;
        ZIndex zIndex = card.card == touchingCard || card.card == selectedCard ? ZIndex.PACK_OPENING_SELECTED : ZIndex.PACK_OPENING;

        Pencil.I().addDrawing(new TextureDrawing(
            card.card.shadowTexture(),
            pos.x, pos.y - 0.2f, width, height,
            scale, rotation, zIndex, Assets.I().shadowColor()
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            card.card.texture(),
            pos.x, pos.y, width, height,
            scale, rotation, zIndex
        ));
    }

    private void deselectCard(boolean killTooltip) {
        if (selectedCard == null) return;
        selectedCard.getBody().targetScale = 1f;
        selectedCard.getBody().setIdleEffectsEnabled(true);
        selectedCard = null;
        if (killTooltip) {
            PopupManager.I().killTooltip(tooltipPopup);
            tooltipPopup = null;
        }
    }

    public void open() {
        open = true;

        cards.clear();

        Seq.of(Hand.I().getHand()).forEach(card -> cards.add(
            new RemovableCard(card, CardSource.HAND, card.getBody().getPos())));
        Seq.of(Deck.I().getDeck()).forEach(card -> cards.add(
            new RemovableCard(card, CardSource.DECK, card.getBody().getPos())));

        List<Vector2> positions = new ArrayList<>();
        for (int col = 5; col > 0; col--) {
            for (int row = 0; row < 4; row++) {
                positions.add(new Vector2(0.7f + row * 2f, 1f + col * 2.5f));
            }
        }

        List<RemovableCard> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, new Comparator<RemovableCard>() {
            @Override
            public int compare(RemovableCard a, RemovableCard b) {
                String aName = a.card.getClass().getSimpleName();
                String bName = b.card.getClass().getSimpleName();

                return aName.compareTo(bName);
            }
        });

        int i = 0;
        for (RemovableCard card : sortedCards) {
            card.card.getBody().getPos().set(positions.get(i));
            i++;
        }

        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.PACK_OPENING);
    }

    private void onRemoveButtonPressed() {
        if (Objects.requireNonNull(Seq.of(cards)
            .filter(removableCard -> removableCard.card == selectedCard)
            .findAnyOrNull()).source == CardSource.HAND) {
            Hand.I().deleteCard(selectedCard);
        } else {
            Deck.I().removeCard(selectedCard);
        }

        Seq.of(cards).forEach(removableCard -> removableCard.card.getBody().getPos().set(removableCard.position));

        deselectCard(true);
        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.PACK_OPENING);
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    private static class RemovableCard {
        public AbstractCard card;
        public CardSource source;
        public Vector2 position;

        public RemovableCard(AbstractCard card, CardSource source, Vector2 position) {
            this.card = card;
            this.source = source;
            this.position = position;
        }
    }

    private enum CardSource {
        HAND, DECK;
    }
}
