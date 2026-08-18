package com.avaricious;

import com.avaricious.components.ItemBag;
import com.avaricious.components.roundInfoPanel.RoundTimer;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.items.upgrades.quests.AbstractQuest;
import com.avaricious.items.upgrades.quests.PlaySevenCardsInOneSpinQuest;
import com.avaricious.utility.Observable;
import com.avaricious.utility.Seq;

import java.util.ArrayList;
import java.util.List;

public class RoundsManager extends Observable<Integer> {

    private final RoundTimer roundTimer = new RoundTimer();
    private Integer currentRound = 0;

    private final List<AbstractCard> playedCardsThisRound = new ArrayList<>();
    private boolean defenceTypeCardsDisabled = false;

    public void nextRound() {
        setCurrentRound(currentRound + 1);
        CreditManager.I().roundEnd();
//        Hand.I().drawCard();

        roundTimer.startTimer();
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    private void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;

        playedCardsThisRound.clear();
        defenceTypeCardsDisabled = false;
        notifyChanged(snapshot());
    }

    public void onCardPlayed(AbstractCard card) {
        playedCardsThisRound.add(card);

        if (ItemBag.I().containsItem(PlaySevenCardsInOneSpinQuest.class) && playedCardsThisRound.size() == 7) {
            Seq.of(ItemBag.I().getItemOfType(PlaySevenCardsInOneSpinQuest.class))
                .filter(quest -> !quest.isCompleted())
                .forEach(AbstractQuest::complete);
        }
    }

    public List<AbstractCard> getPlayedCardsThisRound() {
        return playedCardsThisRound;
    }

    public void disableDefenceTypeCards() {
        defenceTypeCardsDisabled = true;
    }

    public boolean defenceTypeCardsDisabled() {
        return defenceTypeCardsDisabled;
    }

    public RoundTimer getRoundTimer() {
        return roundTimer;
    }

    @Override
    protected Integer snapshot() {
        return currentRound;
    }
}
