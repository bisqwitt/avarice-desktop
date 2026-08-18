package com.avaricious.components.buttons;

import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

public class ExitShopButton extends Button {

    public ExitShopButton(Rectangle buttonRectangle) {
        super(
            () -> ScreenManager.I().getScreen(SlotScreen.class).getShop().exit(),
            Assets.I().get(AssetKey.EXIT_SHOP_BUTTON),
            Assets.I().get(AssetKey.EXIT_SHOP_BUTTON_PRESSED),
            Assets.I().get(AssetKey.EXIT_SHOP_BUTTON),
            buttonRectangle, Input.Keys.ENTER, ZIndex.SHOP);
    }

}
