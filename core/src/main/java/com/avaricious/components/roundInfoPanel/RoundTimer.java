package com.avaricious.components.roundInfoPanel;

import com.avaricious.components.DigitalNumber;
import com.avaricious.components.texts.TimeWord;
import com.avaricious.utility.Assets;
import com.avaricious.utility.RunManager;
import com.avaricious.utility.Seq;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;

public class RoundTimer {

    private final TimeWord timeText = new TimeWord(new Vector2(0.75f, 19.1f), 30f, 0.05f, ZIndex.PATTERN_DISPLAY);
    private final DigitalNumber roundTimer = new DigitalNumber(30, Assets.I().lightColor(), 2,
        new Rectangle(0f, 18.35f, 7 / 23f, 11 / 23f), 0.4f).setZIndex(ZIndex.PATTERN_DISPLAY);

    private boolean timerEnded = false;
    private long roundStartTime;

    public RoundTimer() {
        centerNumberToText();
    }

    public void draw(float delta) {
//        timeText.draw(delta);
//        roundTimer.draw(delta);
    }

    public void startTimer() {
        timerEnded = false;
        roundTimer.setValue(30);
        roundStartTime = TimeUtils.millis();

        tickTimer();
        setupOpponentsScoreChanges();
    }

    private void tickTimer() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                roundTimer.setValue(roundTimer.getValue() - 1);
                centerNumberToText();

                if (roundTimer.getValue() == 0) onTimerEnd();
                else tickTimer();
            }
        }, 1);
    }

    private void setupOpponentsScoreChanges() {
        Seq.of(RunManager.I().getOpponentsRun().scoreChangeData)
            .filter(scoreChangeData -> scoreChangeData.round == RunManager.I().getRoundsManager().getCurrentRound())
            .forEach(scoreChangeData -> {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        PlayerScores.I().setEnemyScoreNumber((int) scoreChangeData.newScore);
                    }
                }, scoreChangeData.msSinceRoundStart / 1000f);
            });
    }

    private void onTimerEnd() {
//        timerEnded = true;
//        if (SlotMachine.I().isStale()) ScreenManager.I().getScreen(SlotScreen.class).onRoundEnd();
    }

    public long msSinceRoundStart() {
        return TimeUtils.timeSinceMillis(roundStartTime);
    }

    public boolean timerEnded() {
        return timerEnded;
    }

    private void centerNumberToText() {
        float textX = timeText.getStartingPos().x;
        float textWidth = timeText.getWidth();
        float numberWidth = roundTimer.getWidth();

        roundTimer.getFirstDigitBounds().x = textX + (textWidth / 2f) - (numberWidth / 2f);
    }
}
