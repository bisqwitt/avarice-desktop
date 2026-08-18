package com.avaricious.components.buttons;

import com.avaricious.components.texts.FabledWord;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class AbstractToggleButton {

    private final Rectangle bounds;

    private final Runnable onToggled;
    private final Runnable onUntoggled;

    protected boolean toggled = false;

    private boolean pressDownIsOnButton = false;

    public AbstractToggleButton(Rectangle bounds, Runnable onToggled, Runnable onUntoggled) {
        this.bounds = bounds;
        this.onToggled = onToggled;
        this.onUntoggled = onUntoggled;
    }

    public void draw(float delta) {
        Pencil.I().addDrawing(new TextureDrawing(
            toggled ? toggledTexture() : untoggledTexture(),
            bounds.x, bounds.y, bounds.width, bounds.height,
            ZIndex.SHOP_CARD
        ));
        title().draw(delta);
        if (!toggled)
            Pencil.I().addDrawing(new TextureDrawing(
                Assets.I().get(AssetKey.BLACK_PIXEL),
                bounds.x, bounds.y, bounds.width, bounds.height,
                ZIndex.SHOP_CARD, Assets.I().shadowColor()
            ));
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (pressed && !wasPressed) {
            pressDownIsOnButton = bounds.contains(mouse);
        } else if (!pressed && wasPressed && pressDownIsOnButton && bounds.contains(mouse)) {
            onButtonPressed();
        }
    }

    public void onButtonPressed() {
        if (toggled) {
            onUntoggled.run();
            toggled = false;
        } else {
            onToggled.run();
            toggled = true;
        }
    }

    abstract FabledWord title();

    abstract TextureRegion toggledTexture();

    abstract TextureRegion untoggledTexture();

    public void setY(float y) {
        bounds.setY(y);
        title().getStartingPos().y = y + 0.25f;
    }

    public boolean isToggled() {
        return toggled;
    }
}
