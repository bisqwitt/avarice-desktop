package com.avaricious.items.upgrades;

import static com.avaricious.items.upgrades.cards.AbstractCard.allCardClasses;
import static com.avaricious.items.upgrades.cards.AbstractCard.instantiateItem;

import com.avaricious.DevTools;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol.PointsOnBellCard;
import com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol.PointsOnCherryCard;
import com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol.PointsOnCloverCard;
import com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol.PointsOnLemonCard;
import com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol.PointsOnSevenCard;
import com.avaricious.utility.Observable;
import com.avaricious.utility.Seq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck extends Observable<List<? extends AbstractCard>> {

    private static Deck instance;

    public static Deck I() {
        return instance == null ? instance = new Deck() : instance;
    }

    private Deck() {
        notifyChanged(snapshot());
        if (DevTools.allCardsInDeck()) {
            for (Class<? extends AbstractCard> cardClass : allCardClasses) {
                addCardToDeck(instantiateItem(cardClass));
            }
        } else {
            addCardToDeck(instantiateItem(PointsOnLemonCard.class));
            addCardToDeck(instantiateItem(PointsOnLemonCard.class));
            addCardToDeck(instantiateItem(PointsOnCherryCard.class));
            addCardToDeck(instantiateItem(PointsOnCherryCard.class));
            addCardToDeck(instantiateItem(PointsOnCloverCard.class));
            addCardToDeck(instantiateItem(PointsOnBellCard.class));
            addCardToDeck(instantiateItem(PointsOnSevenCard.class));
        }
    }

    private final List<AbstractCard> deck = new ArrayList<>();

    public AbstractCard drawRandomCard() {
        return removeCard((int) (Math.random() * deck.size()));
    }

    public AbstractCard drawCard(Class<? extends AbstractCard> cardClass) {
        return removeCard(deck.indexOf(Seq.of(deck)
            .filter(cardClass::isInstance)
            .findFirstOrNull()));
    }

    public void addCardToDeck(AbstractCard upgrade) {
        deck.add(upgrade);
        notifyChanged(snapshot());
    }

    public AbstractCard removeCard(int index) {
        AbstractCard card = deck.remove(index);
        notifyChanged(snapshot());
        return card;
    }

    public void removeCard(AbstractCard card) {
        deck.remove(card);
        notifyChanged(snapshot());
    }

    @Override
    protected List<? extends AbstractCard> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(deck));
    }

    public void setDeck(List<? extends AbstractCard> cardsInDeck) {
        deck.clear();
        Seq.of(cardsInDeck).forEach(this::addCardToDeck);
    }

    public List<AbstractCard> getDeck() {
        return deck;
    }
}
