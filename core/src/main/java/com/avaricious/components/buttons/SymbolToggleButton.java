package com.avaricious.components.buttons;

import com.avaricious.components.texts.FabledWord;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class SymbolToggleButton extends AbstractToggleButton {

    private final FabledWord title = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.S_BIG),
            Assets.I().get(AssetKey.Y),
            Assets.I().get(AssetKey.M),
            Assets.I().get(AssetKey.B),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.L),
            Assets.I().get(AssetKey.S)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.S_BIG_SHADOW),
            Assets.I().get(AssetKey.Y_SHADOW),
            Assets.I().get(AssetKey.M_SHADOW),
            Assets.I().get(AssetKey.B_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.L_SHADOW),
            Assets.I().get(AssetKey.S_SHADOW)
        ), new Vector2(1.75f, 15.75f), 30f, 0.05f, ZIndex.SHOP_CARD
    );

    public SymbolToggleButton(Runnable onToggled, Runnable onUntoggled) {
        super(new Rectangle(1f, 15.5f, 3.25f, 0.9f), onToggled, onUntoggled);
        toggled = true;
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
