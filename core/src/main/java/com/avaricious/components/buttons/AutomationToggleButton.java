package com.avaricious.components.buttons;

import com.avaricious.components.texts.FabledWord;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class AutomationToggleButton extends AbstractToggleButton {

    private final FabledWord title = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.A_BIG),
            Assets.I().get(AssetKey.U),
            Assets.I().get(AssetKey.T),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.M),
            Assets.I().get(AssetKey.A),
            Assets.I().get(AssetKey.T),
            Assets.I().get(AssetKey.I),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.S)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.A_BIG_SHADOW),
            Assets.I().get(AssetKey.U_SHADOW),
            Assets.I().get(AssetKey.T_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.M_SHADOW),
            Assets.I().get(AssetKey.A_SHADOW),
            Assets.I().get(AssetKey.T_SHADOW),
            Assets.I().get(AssetKey.I_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.S_SHADOW)
        ), new Vector2(4.75f, 15.75f), 30f, 0.05f, ZIndex.SHOP_CARD);

    public AutomationToggleButton(Runnable onToggled, Runnable onUntoggled) {
        super(new Rectangle(4.5f, 15.5f, 3.25f, 0.9f), onToggled, onUntoggled);
    }

    @Override
    FabledWord title() {
        return title;
    }

    @Override
    TextureRegion toggledTexture() {
        return Assets.I().get(AssetKey.LIGHT_GREY_PIXEL);
    }

    @Override
    TextureRegion untoggledTexture() {
        return Assets.I().get(AssetKey.LIGHT_GREY_PIXEL);
    }

}
