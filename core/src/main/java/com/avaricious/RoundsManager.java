package com.avaricious;

import com.avaricious.components.ItemBag;
import com.avaricious.components.roundInfoPanel.RoundTimer;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.avaricious.items.upgrades.quests.AbstractQuest;
import com.avaricious.items.upgrades.quests.PlaySevenCardsInOneSpinQuest;
import com.avaricious.utility.EconomyScaling;
import com.avaricious.utility.Observable;
import com.avaricious.utility.Seq;

import java.util.ArrayList;
import java.util.List;

public class RoundsManager extends Observable<Integer> {

    private static final float FIRST_ROUND_TARGET = 100f;
    private static final float ROUND_TARGET_GROWTH = 1.50f;

    public enum RoundOutcome {
        IN_PROGRESS,
        CLEARED,
        FAILED
    }

    private final RoundTimer roundTimer = new RoundTimer();
    private Integer currentRound = 0;
    private float roundTarget;
    private RoundOutcome outcome = RoundOutcome.IN_PROGRESS;

    private final List<AbstractCard> playedCardsThisRound = new ArrayList<>();
    private boolean defenceTypeCardsDisabled = false;

    public void startNewRun() {
        currentRound = 0;
        nextRound();
    }

    public void nextRound() {
        int nextRound = currentRound + 1;
        RoundStats.I().reset();
        roundTarget = calculateTarget(nextRound);
        outcome = RoundOutcome.IN_PROGRESS;
        roundTimer.startTimer();
        setCurrentRound(nextRound);
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public boolean tryStartSpin() {
        if (!canSpin()) return false;
        RoundStats.I().recordSpin();
        return true;
    }

    public boolean updateTimer(float delta) {
        return outcome == RoundOutcome.IN_PROGRESS
            && roundTimer.update(delta);
    }

    public RoundOutcome resolveRound(float availableCash) {
        if (outcome != RoundOutcome.IN_PROGRESS) {
            return outcome;
        }

        if (roundTimer.timerEnded()) {
            outcome = availableCash >= roundTarget
                ? RoundOutcome.CLEARED
                : RoundOutcome.FAILED;
        }
        return outcome;
    }

    public boolean canSpin() {
        return outcome == RoundOutcome.IN_PROGRESS
            && !roundTimer.timerEnded();
    }

    public int getSecondsRemaining() {
        return roundTimer.getSecondsRemaining();
    }

    public float getRoundTarget() {
        return roundTarget;
    }

    public RoundOutcome getOutcome() {
        return outcome;
    }

    private float calculateTarget(int round) {
        double rawTarget = FIRST_ROUND_TARGET * Math.pow(
            ROUND_TARGET_GROWTH,
            Math.max(0, round - 1)
        );
        return EconomyScaling.roundPrice(rawTarget);
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
