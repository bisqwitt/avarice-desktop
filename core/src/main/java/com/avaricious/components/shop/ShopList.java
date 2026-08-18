package com.avaricious.components.shop;

import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.Seq;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class ShopList {

    protected final List<ShopItem> items = new ArrayList<>();
    private final Rectangle bounds;

    private final float itemSpacing = 0.5f;
    private float scrollOffset = 0f;
    private boolean dragging = false;
    private float lastTouchY;

    public ShopList(Rectangle bounds) {
        this.bounds = bounds;
    }

    public void draw(float delta) {
        Pencil.I().startScissors(GameContext.I().viewport.getCamera(), GameContext.I().batch.getTransformMatrix(), bounds);
        Seq.of(items).forEach(item -> item.draw(delta));
        Pencil.I().endScissors();
    }

    public void handleInput(
        Vector2 mouse,
        boolean touching,
        boolean wasTouching
    ) {
        boolean justTouched = touching && !wasTouching;
        boolean justReleased = !touching && wasTouching;

        if (!bounds.contains(mouse) && !dragging) {
            return;
        }

        if (justTouched) {
            dragging = true;
            lastTouchY = mouse.y;
        }

        if (dragging && touching) {
            float deltaY = mouse.y - lastTouchY;

            scrollOffset += deltaY;

            clampScroll();
            updateItemPositions();

            lastTouchY = mouse.y;
        }

        if (justReleased) {
            dragging = false;
        }

        Seq.of(items).forEach(item ->
            item.handleInput(mouse, touching, wasTouching)
        );
    }

    protected void updateItemPositions() {
        float y = bounds.y + bounds.height + scrollOffset;

        for (ShopItem item : items) {
            y -= item.getHeight();
            item.setY(y);
            y -= itemSpacing;
        }
    }

    private float getContentHeight() {
        float height = 0f;
        for (ShopItem item : items) {
            height += item.getHeight();
        }
        height += Math.max(0, items.size() - 1) * itemSpacing;
        return height;
    }

    private void clampScroll() {
        float maxScroll = Math.max(0f, getContentHeight() - bounds.height);
        scrollOffset = Math.max(0f, Math.min(scrollOffset, maxScroll));
    }

    public void setY(float y) {
        bounds.y = y;
        updateItemPositions();
    }

    public Rectangle getBounds() {
        return bounds;
    }


}
