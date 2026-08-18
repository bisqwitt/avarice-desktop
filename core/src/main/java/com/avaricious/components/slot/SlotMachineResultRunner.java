package com.avaricious.components.slot;

import com.avaricious.TaskScheduler;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.pattern.PatternMatch;
import com.avaricious.effects.EffectManager;
import com.avaricious.effects.TextureEcho;
import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.avaricious.utility.Assets;
import com.avaricious.utility.SymbolValues;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlotMachineResultRunner {

    private static SlotMachineResultRunner instance;

    public static SlotMachineResultRunner I() {
        return instance == null ? instance = new SlotMachineResultRunner() : instance;
    }

    private final SlotMachine slotMachine = SlotMachine.I();
    private float defaultDelay = 0.4f;

    private SlotMachineResultRunner() {
    }

    public void runResult(PatternMatch match) {
        runResult(Arrays.asList(match));
    }

    public void runResult(List<PatternMatch> matches) {
        if (matches.isEmpty()) {
//            buttonBoard.setVisible(true);
            slotMachine.setStale(true);
            if (Automations.I().getAutoSpin().isActive())
                ScreenManager.I().getScreen(SlotScreen.class).onSpinButtonPressed();
            return;
        }

        TaskScheduler scheduler = new TaskScheduler(defaultDelay);
        scheduler.schedule(() -> slotMachine.setRunningResults(true), 0f);

        for (PatternMatch patternMatch : matches) {
            List<Body> slots = new ArrayList<>(patternMatch.getSlots());
            Body middleBody = slots.get(slots.size() / 2 - (slots.size() % 2 == 0 ? 1 : 0));

            scheduler.scheduleNoDelay(() -> {
                for (Body body : slots) {
                    body.beginPatternHit();
                }
            });

            triggerSeparateSlots(matches, patternMatch, slots, scheduler);

            scheduler.schedule(() -> {
                PopupManager.I().releaseHoldingNumbers();

                for (Body body : slots) {
                    body.pulse(1.2f);

                    EffectManager.create(Assets.I().getSymbol(patternMatch.getSymbol()),
                        new Rectangle(body.getPos().x, body.getPos().y, SlotMachine.CELL_W, SlotMachine.CELL_H),
                        TextureEcho.Type.SLOT);

                    BouncingSymbolManager.I().createFallingSymbol(
                        patternMatch.getSymbol(),
                        body.getPos().x,
                        body.getPos().y
                    );
                }
                ScreenShake.I().addTrauma(0.3f);

                int multi = slots.size() * 10;

//                PopupManager.I().spawnNumber(multi, Assets.I().red(),
//                    middleBody.getPos().x + ((slots.size() % 2 == 0) ? 2f : 1.5f), middleBody.getPos().y + 1f,
//                    true);

//                ScoreDisplay.I().addToScore(multi);

                AudioManager.I().playHit(EffectManager.streak);
            });

            scheduler.schedule(() -> {
                if (matches.indexOf(patternMatch) != matches.size() - 1)
                    EffectManager.increaseStreak();
                for (Body body : patternMatch.getSlots()) {
                    body.endPatternHit();
                    PopupManager.I().releaseHoldingNumbers();
                }
            });
        }

        scheduler.schedule(() -> {
            slotMachine.setRunningResults(false);
            slotMachine.setStale(true);
            EffectManager.endStreak();
            if (ScoreDisplay.I().reachedRoundGoal())
                ScreenManager.I().getScreen(SlotScreen.class).onRoundEnd();
//            buttonBoard.setVisible(true);
//            ScoreDisplay.I().updateScoreNumber();
            if (Automations.I().getAutoSpin().isActive())
                ScreenManager.I().getScreen(SlotScreen.class).onSpinButtonPressed();
        });

        scheduler.runTasks();
    }

    private int nextCard = 3;
    private int hitCount = 0;

    private void onHit() {
        hitCount++;

        if (hitCount == nextCard) {
//            Hand.I().drawCard();
            hitCount = 0;
            nextCard = nextCard + 2;
        }
    }

    private void triggerSeparateSlots(List<PatternMatch> matches, PatternMatch match, List<Body> slots, TaskScheduler scheduler) {
        SlotScreen slotScreen = ScreenManager.I().getScreen(SlotScreen.class);
        slotScreen.setSymbolsHitLastSpin(0);
        for (Body body : slots) {
            scheduler.schedule(() -> {
                slotScreen.addSymbolsHitLastSpin();

                body.pulse(1.75f);
                ScreenShake.I().addTrauma(0.2f);

                int points = SymbolValues.I().getValue(match.getSymbol());

//                PopupManager.I().spawnNumber(points, Assets.I().blue(),
//                    body.getPos().x + 1.5f, body.getPos().y + 1f, true);
//                ScoreDisplay.I().addToScore(points);

                EffectManager.create(Assets.I().getSymbol(match.getSymbol()),
                    new Rectangle(body.getPos().x, body.getPos().y, SlotMachine.CELL_W, SlotMachine.CELL_H),
                    TextureEcho.Type.SLOT);

                BouncingSymbolManager.I().createFallingSymbol(
                    match.getSymbol(),
                    body.getPos().x,
                    body.getPos().y
                );

                AudioManager.I().playHit(EffectManager.streak);

//                Seq.of(Hand.I().getHand())
//                    .filter(card -> card instanceof AbstractQuestCard
//                        && ((AbstractQuestCard) card).condition(matches, match))
//                    .forEach(card -> ((AbstractQuestCard) card).complete());

                onHit();
            });

        }
    }

    public float getDefaultDelay() {
        return defaultDelay;
    }

    public void setDefaultDelay(float defaultDelay) {
        this.defaultDelay = defaultDelay;
    }
}
