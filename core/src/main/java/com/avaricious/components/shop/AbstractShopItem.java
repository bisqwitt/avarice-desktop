package com.avaricious.components.shop;

import com.avaricious.CreditManager;
import com.avaricious.CreditNumber;
import com.avaricious.components.buttons.Button;
import com.avaricious.components.popups.BoughtPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.TooltipPopup;
import com.avaricious.components.slot.DragableBody;
import com.avaricious.items.upgrades.AbstractUpgrade;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

public abstract class AbstractShopItem {

    protected AbstractUpgrade upgrade;
    protected CreditNumber priceTag;

    private boolean dragging;
    private boolean selected;
    private boolean bought;
    private boolean dead = false;

    private final Vector2 mouseTouchdownLocation = new Vector2();
    private TooltipPopup tooltipPopup = null;

    private final Button buyButton = new Button(this::buyUpgrade,
        Assets.I().get(AssetKey.BUY_BUTTON),
        Assets.I().get(AssetKey.BUY_BUTTON_PRESSED),
        Assets.I().get(AssetKey.BUY_BUTTON),
        new Rectangle(0, 0, 79 / 30f, 25 / 30f),
        Input.Keys.ENTER, ZIndex.UNFOLDED_DECK_CARD);

    public AbstractShopItem(Vector2 initialPos) {
        load(initialPos);
    }

    public void handleInput(Vector2 mouse, boolean touching, boolean wasTouching, float delta) {
        if (dead) return;
        upgrade.getBody().update(delta);

        if ((selected || dragging) && buyButton.getBounds().contains(mouse)) {
            buyButton.handleInput(mouse, touching, wasTouching);
            return;
        }

        if (touching && !wasTouching) {
            if (upgrade.getBody().getBounds().contains(mouse)) {
                dragging = true;
                upgrade.getBody().targetScale = 1.3f;
                upgrade.getBody().setIdleEffectsEnabled(false);
                upgrade.getBody().beginDrag(mouse.x, mouse.y, 0);

                mouseTouchdownLocation.set(mouse);
                tooltipPopup = PopupManager.I().createTooltip(upgrade, upgrade.getBody().getRenderPos(new Vector2()));
            } else deselectCard(true);
        }

        if (touching && dragging) {
            upgrade.getBody().dragTo(mouse.x, mouse.y, 0);
        }

        if (!touching && wasTouching && dragging) {
            upgrade.getBody().endDrag(0);
            boolean isClick = mouseTouchdownLocation.dst(mouse) <= 0.2f * 0.2f;
            if (isClick) {
                if (selected) deselectCard(true);
                else selected = true;
            } else {
                selected = true;
                deselectCard(true);
            }
            dragging = false;
        }

        if (selected) {
            Vector2 renderPos = upgrade.getBody().getRenderPos(new Vector2());
            PopupManager.I().updateTooltip(
                new Vector2(renderPos.x - 2f, renderPos.y + upgrade.getTooltipYOffset()), true
            );
        }
    }

    public void draw(float delta) {
        if (dead) return;
        DragableBody body = upgrade.getBody();

        Rectangle bounds = body.getBounds();
        Vector2 position = body.getRenderPos(new Vector2());
        float alpha = body.getAlpha();
        float scale = body.getScale();
        float rotation = body.getRotation();
        ZIndex zIndex = dragging ? ZIndex.SHOP_CARD_TOUCHING : ZIndex.SHOP_CARD;

        Color shadowColor = Assets.I().shadowColor();
        shadowColor.a = Math.min(shadowColor.a, alpha);
        Pencil.I().addDrawing(new TextureDrawing(
            upgrade.shadowTexture(),
            position.x, position.y - (dragging ? 0.3f : 0.2f),
            bounds.width, bounds.height,
            scale, rotation, zIndex, shadowColor
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            upgrade.texture(),
            position.x, position.y, bounds.width, bounds.height,
            scale, rotation, zIndex, new Color(1f, 1f, 1f, alpha)
        ));

        if (!dragging && !selected && !bought) {
            priceTag.setZIndex(zIndex);
            priceTag.getFirstDigitBounds().y = position.y - 1f;
            priceTag.draw(delta, 1f, rotation);
        }

        if (selected || dragging) {
            Vector2 renderPos = body.getRenderPos(new Vector2());
            buyButton.getBounds().x = renderPos.x - 0.5f;
            buyButton.getBounds().y = renderPos.y - 1.5f;
            buyButton.draw(delta);
        }
    }

    private void buyUpgrade() {
        if (!CreditManager.I().enoughCredit(upgrade.price())) {
            CreditManager.I().pulse();
            return;
        }
        acquireItem();
        CreditManager.I().pay(upgrade.price());
        PopupManager.I().killTooltip(tooltipPopup);
        bought = true;
        priceTag = null;

        upgrade.getBody().pulse();

        Vector2 renderPos = upgrade.getBody().getRenderPos(new Vector2());
        PopupManager.I().spawnTextPopup(new BoughtPopup(new Vector2(
            (renderPos.x + upgrade.getBody().getBounds().width / 2f) - BoughtPopup.WIDTH / 2f,
            renderPos.y + 3f), ZIndex.SHOP_CARD_TOUCHING));

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                dead = true;
            }
        }, 1);
    }

    private void deselectCard(boolean killTooltip) {
        upgrade.getBody().targetScale = 1f;
        upgrade.getBody().setIdleEffectsEnabled(true);
        selected = false;
        if (killTooltip) PopupManager.I().killTooltip(tooltipPopup);
    }

    protected abstract void load(Vector2 pos);

    protected abstract void acquireItem();

    protected abstract float getPriceTagYOffset();

    public void setY(float y) {
        upgrade.getBody().getPos().y = y;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isSelected() {
        return selected;
    }
}
