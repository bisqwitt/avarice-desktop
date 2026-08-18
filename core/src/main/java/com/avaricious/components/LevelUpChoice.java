package com.avaricious.components;

import com.avaricious.components.texts.FabledText;
import com.avaricious.utility.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class LevelUpChoice {

    private final String title;
    private final String description;

    private final Runnable upgrade;

    private Rectangle bounds;

    private final TextureRegion background =
        Assets.I().get(AssetKey.BLACK_PIXEL);

    public LevelUpChoice(
        String title,
        String description,
        Runnable upgrade
    ) {
        this.title = title;
        this.description = description;
        this.upgrade = upgrade;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public boolean contains(Vector2 position) {
        return bounds != null &&
            bounds.contains(position);
    }

    public void select() {
        upgrade.run();
    }

    public void draw(float delta) {
        if (bounds == null) return;

        Pencil.I().addDrawing(new TextureDrawing(
            background,
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            ZIndex.SHOP
        ));

        // Replace these with however your FabledText
        // constructor actually works.
        //
        // new FabledText(title, ...)
        // new FabledText(description, ...)
    }
}
