package com.avaricious.items.upgrades.quests;

import com.avaricious.components.ItemBag;
import com.avaricious.components.popups.ClaimedPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.items.upgrades.AbstractUpgrade;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractQuest extends AbstractUpgrade {

    public static float WIDTH = 48;
    public static float HEIGHT = 44;

    private boolean completed = false;

    @Override
    public String title() {
        return completed ? "Quest (Completed)" : "Quest";
    }

    @Override
    public IUpgradeType type() {
        return QuestType.DEFAULT;
    }

    public TextureRegion texture() {
        return Assets.I().get(AssetKey.QUEST_SCROLL);
    }

    @Override
    public TextureRegion shadowTexture() {
        return Assets.I().get(AssetKey.QUEST_SCROLL_SHADOW);
    }

    public void claim() {
        body.pulse();
        Vector2 renderPos = body.getRenderPos(new Vector2());
        PopupManager.I().spawnTextPopup(new ClaimedPopup(new Vector2(
            (renderPos.x + body.getBounds().width / 2f) - ClaimedPopup.WIDTH / 2f,
            renderPos.y + 3f), ZIndex.UNFOLDED_DECK_CARD));
        onClaim();

        final AbstractQuest quest = this;
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                ItemBag.I().removeItem(quest);
            }
        }, 1);
    }

    @Override
    public float getTextureHeight() {
        return HEIGHT;
    }

    @Override
    public float getTextureWidth() {
        return WIDTH;
    }

    public static AbstractQuest randomQuest() {
        return instantiateItem(allQuestClasses.get((int) (Math.random() * allQuestClasses.size())));
    }

    public static final List<Class<? extends AbstractQuest>> allQuestClasses = Collections.unmodifiableList(Arrays.asList(
        PlaySevenCardsInOneSpinQuest.class

    ));

    public void complete() {
        completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public float getTooltipYOffset() {
        return 2.1f;
    }

    protected abstract void onClaim();
}
