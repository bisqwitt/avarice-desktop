package com.avaricious.components.buttons;

import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

public class OpenShopButton extends DisablableButton {

    public OpenShopButton(Rectangle buttonRectangle, int key) {
        super(() -> ScreenManager.I().getScreen(SlotScreen.class).getShop().show(),
            Assets.I().get(AssetKey.SHOPPING_CART),
            Assets.I().get(AssetKey.SHOPPING_CART),
            Assets.I().get(AssetKey.SHOPPING_CART),
            buttonRectangle, key, ZIndex.BUTTON_BOARD);
    }

    @Override
    public boolean disabled() {
        return false;
    }
}
