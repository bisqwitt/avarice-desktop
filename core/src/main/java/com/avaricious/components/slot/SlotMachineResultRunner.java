package com.avaricious.components.slot;

import com.avaricious.TaskScheduler;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.NumberPopup;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.pattern.PatternMatch;
import com.avaricious.effects.EffectManager;
import com.avaricious.effects.TextureEcho;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.avaricious.utility.Assets;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlotMachineResultRunner {

    private static SlotMachineResultRunner instance;

    public static SlotMachineResultRunner I() {
        return instance == null ? instance = new SlotMachineResultRunner() : instance;
    }

    private static final float DEFAULT_RESULT_STEP_DELAY = 0.40f;
    private static final float MIN_RESULT_STEP_DELAY = 0.16f;
    private static final float RESULT_SPEED_STEP = 0.05f;
    private static final float ANTICIPATION_DELAY = 0.30f;
    private static final float PATTERN_FOCUS_HOLD = 0.16f;
    private static final float FIRST_REVEAL_DELAY =
        ANTICIPATION_DELAY + PATTERN_FOCUS_HOLD;

    private TaskScheduler scheduler;
    private final SlotMachine slotMachine = SlotMachine.I();
    private float resultStepDelay = DEFAULT_RESULT_STEP_DELAY;

    private SlotMachineResultRunner() {
    }

    public void runResult(PatternMatch match) {
        runResult(Arrays.asList(match));
    }

    public void runResult(List<PatternMatch> matches) {
        if (matches.isEmpty()) {
            scheduler = new TaskScheduler(resultStepDelay);
            scheduler.schedule(() -> {
                AudioManager.I().stopPayout();
//                buttonBoard.setVisible(true);
                slotMachine.playEmptySpinSweep(() -> {
                    slotMachine.setStale(true);
                    if (Automations.I().getAutoSpin().isActive()) {
                        ScreenManager.I().getScreen(SlotScreen.class).onSpinButtonPressed();
                    }
                });
            }, 0f);
            scheduler.runTasks(FIRST_REVEAL_DELAY);
            return;
        }

        scheduler = new TaskScheduler(resultStepDelay);
        scheduler.schedule(AudioManager.I()::startPayout, 0f);
        scheduler.schedule(() -> slotMachine.setRunningResults(true), 0f);

        for (int matchIndex = 0; matchIndex < matches.size(); matchIndex++) {
            PatternMatch patternMatch = matches.get(matchIndex);
            List<Body> slots = new ArrayList<>(patternMatch.getSlots());
            Body middleBody = slots.get(slots.size() / 2 - (slots.size() % 2 == 0 ? 1 : 0));

            Runnable focusPattern = () -> {
                for (Body body : slots) {
                    body.beginPatternHit();
                }
            };

            if (matchIndex == 0) {
                scheduler.schedule(focusPattern, PATTERN_FOCUS_HOLD);
            } else {
                scheduler.scheduleNoDelay(focusPattern);
            }

            triggerSeparateSlots(matches, patternMatch, slots, scheduler);

            scheduler.schedule(() -> {
                PopupManager.I().releaseHoldingNumbers();

                for (Body body : slots) {
                    slotMachine.flashSymbol(body, 1f);
                    body.pulse(1.2f);

                    EffectManager.create(Assets.I().getSymbol(patternMatch.getSymbol()),
                        new Rectangle(body.getPos().x, body.getPos().y, SlotMachine.CELL_W, SlotMachine.CELL_H),
                        TextureEcho.Type.SLOT);

                    BouncingSymbolManager.I().createFallingSymbol(
                        patternMatch.getSymbol(),
                        body.getPos().x,
                        body.getPos().y,
                        1.18f
                    );


                }

                ParticleManager.I().create(
                    middleBody.getPos().x,
                    middleBody.getPos().y,
                    ParticleType.RAINBOW,
                    0.045f,
                    80f + slots.size() * 5f,
                    ZIndex.SYMBOL_HIT_PARTICLES
                );

                ScreenShake.I().addTrauma(
                    0.31f + Math.min(0.12f, slots.size() * 0.012f)
                );

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
            AudioManager.I().stopPayout();
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

        scheduler.runTasks(ANTICIPATION_DELAY);
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

                slotMachine.flashSymbol(body, 1f);
                body.pulse(1.75f);
                ScreenShake.I().addTrauma(
                    0.18f + Math.min(0.10f, EffectManager.streak * 0.025f)
                );

                int points = SymbolValues.I().getValue(match.getSymbol());

                PopupManager.I().spawnNumber(new NumberPopup(
                    points,
                    Assets.I().getSymbolColor(match.getSymbol()),
                    body.getPos().x + SlotMachine.CELL_W * 0.72f + 0.2f,
                    body.getPos().y + SlotMachine.CELL_H * 0.62f + 0.2f,
                    false,
                    false
                ));

                ScoreDisplay.I().addToScore(points);

                EffectManager.create(Assets.I().getSymbol(match.getSymbol()),
                    new Rectangle(body.getPos().x, body.getPos().y, SlotMachine.CELL_W, SlotMachine.CELL_H),
                    TextureEcho.Type.SLOT);

                BouncingSymbolManager.I().createSymbolDrop(
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

    public TaskScheduler getScheduler() {
        return scheduler;
    }

    public void increaseRevealSpeed() {
        resultStepDelay = Math.max(
            MIN_RESULT_STEP_DELAY,
            resultStepDelay - RESULT_SPEED_STEP
        );
    }
}
