package com.avaricious.components.buttons;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

public class OpenShopButton extends DisablableButton {

    public OpenShopButton(
        Runnable openShop,
        Rectangle buttonRectangle,
        int key
    ) {
        super(openShop,
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
