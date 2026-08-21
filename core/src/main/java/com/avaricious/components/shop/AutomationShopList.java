package com.avaricious.components.shop;

import com.avaricious.components.automations.Automations;
import com.avaricious.components.texts.AutoSpinCapacityDescriptionText;
import com.avaricious.components.texts.AutoSpinCapacityText;
import com.avaricious.components.texts.AutoSpinText;
import com.avaricious.components.texts.HandCapacityDescriptionText;
import com.avaricious.components.texts.HandCapacityText;
import com.avaricious.components.texts.LuckDescriptionText;
import com.avaricious.components.texts.LuckText;
import com.avaricious.components.texts.SlotMachineSpeedDescriptionText;
import com.avaricious.components.texts.SlotMachineSpeedText;
import com.badlogic.gdx.math.Rectangle;

public class AutomationShopList extends ShopList {

    public AutomationShopList(Rectangle bounds) {
        super(bounds);
        items.add(new ShopItem(
            new AutoSpinText(),
            Automations.I().getAutoSpin()));
        items.add(new ShopItem(
            new AutoSpinCapacityText(),
            new AutoSpinCapacityDescriptionText(),
            Automations.I().getAutoSpinCapacity()));
//        items.add(new ShopItem(
//            new SpinBuyerText(),
//            Automations.I().getSpinBuyer()
//        ));
//        items.add(new ShopItem(
//            new SpinBuyerSpeedText(),
//            Automations.I().getSpinBuyerSpeed()
//        ));

        items.add(new ShopItem(
            new SlotMachineSpeedText(),
            new SlotMachineSpeedDescriptionText(),
            Automations.I().getSlotMachineSpeed()
        ));

        items.add(new ShopItem(
            new LuckText(),
            new LuckDescriptionText(),
            Automations.I().getLuck()
        ));

        items.add(new ShopItem(
            new HandCapacityText(),
            new HandCapacityDescriptionText(),
            Automations.I().getHandCapacity()));

        updateItemPositions();
    }


}
