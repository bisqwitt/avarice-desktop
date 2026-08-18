package com.avaricious.components.shop;

import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.BellValueText;
import com.avaricious.components.texts.CherryValueText;
import com.avaricious.components.texts.CloverValueText;
import com.avaricious.components.texts.DiamondValueText;
import com.avaricious.components.texts.IronValueText;
import com.avaricious.components.texts.LemonValueText;
import com.avaricious.components.texts.SevenValueText;
import com.badlogic.gdx.math.Rectangle;

public class SymbolShopList extends ShopList {

    public SymbolShopList(Rectangle bounds) {
        super(bounds);
        items.add(new ShopItem(
            new LemonValueText(),
            Symbol.LEMON
        ));
        items.add(new ShopItem(
            new CherryValueText(),
            Symbol.CHERRY
        ));
        items.add(new ShopItem(
            new CloverValueText(),
            Symbol.CLOVER
        ));
        items.add(new ShopItem(
            new BellValueText(),
            Symbol.BELL
        ));
        items.add(new ShopItem(
            new IronValueText(),
            Symbol.IRON
        ));
        items.add(new ShopItem(
            new DiamondValueText(),
            Symbol.DIAMOND
        ));
        items.add(new ShopItem(
            new SevenValueText(),
            Symbol.SEVEN
        ));

        updateItemPositions();
    }

}
