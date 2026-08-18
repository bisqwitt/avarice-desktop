package com.avaricious.items.upgrades.quests;

import com.avaricious.CreditManager;
import com.avaricious.components.popups.CreditNumberPopup;
import com.avaricious.components.popups.NumberPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class PlaySevenCardsInOneSpinQuest extends AbstractQuest {

    @Override
    public String description() {
        return "Play 7 cards in one round\n"
            + "Reward: " + Assets.I().yellowText("10$");
    }

    @Override
    public int price() {
        return 5;
    }

    @Override
    public void onClaim() {
        Vector2 renderPos = body.getRenderPos(new Vector2());
        PopupManager.I().spawnNumber(new CreditNumberPopup(10, new Rectangle(
            renderPos.x + 0.9f, renderPos.y + 1f,
            NumberPopup.defaultWidth + 0.1f,
            NumberPopup.defaultHeight + 0.1f
        ),
            false, false).setZIndex(ZIndex.UNFOLDED_DECK_CARD));
        CreditManager.I().gain(10);
    }
}
