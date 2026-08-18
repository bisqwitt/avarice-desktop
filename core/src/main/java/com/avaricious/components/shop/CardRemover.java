package com.avaricious.components.shop;

import com.avaricious.CreditManager;
import com.avaricious.CreditNumber;
import com.avaricious.components.RemoveCardWindow;
import com.avaricious.components.buttons.Button;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.TooltipPopup;
import com.avaricious.components.slot.DragableBody;
import com.avaricious.items.upgrades.AbstractUpgrade;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.items.upgrades.quests.QuestType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CardRemover {

    private final RemoveCardWindow removeCardWindow = new RemoveCardWindow();

    private final TextureRegion texture = Assets.I().get(AssetKey.REMOVE_CARD);
    private final TextureRegion shadowTexture = Assets.I().get(AssetKey.REMOVE_CARD_SHADOW);
    private final AbstractUpgrade upgrade = new AbstractUpgrade() {
        @Override
        public String title() {
            return "Card Remover";
        }

        @Override
        public String description() {
            return "Permanently remove a Card from your Deck";
        }

        @Override
        public int price() {
            return 5;
        }

        @Override
        public TextureRegion texture() {
            return texture;
        }

        @Override
        public TextureRegion shadowTexture() {
            return null;
        }

        @Override
        public float getTextureWidth() {
            return 0;
        }

        @Override
        public float getTextureHeight() {
            return 0;
        }

        @Override
        public float getTooltipYOffset() {
            return 1.5f;
        }

        @Override
        public IUpgradeType type() {
            return QuestType.DEFAULT;
        }
    };
    private final DragableBody body;
    private final CreditNumber priceTag;

    private final Vector2 mouseTouchdownLocation = new Vector2();
    private TooltipPopup tooltipPopup = null;

    private boolean selected;
    private boolean dragging;
    private boolean bought = false;

    private final Button buyButton = new Button(this::buy,
        Assets.I().get(AssetKey.BUY_BUTTON),
        Assets.I().get(AssetKey.BUY_BUTTON_PRESSED),
        Assets.I().get(AssetKey.BUY_BUTTON),
        new Rectangle(0, 0, 79 / 30f, 25 / 30f),
        Input.Keys.ENTER, ZIndex.SHOP_CARD);

    public CardRemover(Vector2 initialPos) {
        Rectangle bounds = new Rectangle(
            initialPos.x, initialPos.y, 228 / 100f, 239 / 100f
        );
        body = new DragableBody(bounds);
        body.getIdleScaleEffect().setAllowed(false);
        priceTag = new CreditNumber(upgrade.price(), new Rectangle(initialPos.x + 0.5f, initialPos.y, 7 / 20f, 11 / 20f), 0.4f);
    }

    public void handleInput(Vector2 mouse, boolean touching, boolean wasTouching, float delta) {
        if (removeCardWindow.isOpen()) {
            removeCardWindow.handleInput(mouse, touching, wasTouching);
            return;
        }

        if (bought) return;
        body.update(delta);

        if ((selected || dragging) && buyButton.getBounds().contains(mouse)) {
            buyButton.handleInput(mouse, touching, wasTouching);
            return;
        }

        if (touching && !wasTouching) {
            if (body.getBounds().contains(mouse)) {
                dragging = true;
                body.targetScale = 1.3f;
                body.setIdleEffectsEnabled(false);
                body.beginDrag(mouse.x, mouse.y, 0);

                mouseTouchdownLocation.set(mouse);
                tooltipPopup = PopupManager.I().createTooltip(upgrade, body.getRenderPos(new Vector2()));
            } else deselect(true);
        }

        if (touching && dragging) {
            body.dragTo(mouse.x, mouse.y, 0);
        }

        if (!touching && wasTouching && dragging) {
            body.endDrag(0);
            boolean isClick = mouseTouchdownLocation.dst(mouse) <= 0.2f * 0.2f;
            if (isClick) {
                if (selected) deselect(true);
                else selected = true;
            } else {
                selected = true;
                deselect(true);
            }
            dragging = false;
        }

        if (selected) {
            Vector2 renderPos = body.getRenderPos(new Vector2());
            PopupManager.I().updateTooltip(
                new Vector2(renderPos.x - 1.65f, renderPos.y + 3.25f), true
            );
        }
    }

    public void draw(float delta) {
        removeCardWindow.draw(delta);
        if (bought) return;

        Rectangle bounds = body.getBounds();
        Vector2 pos = body.getRenderPos(new Vector2());
        float alpha = body.getAlpha();
        float scale = body.getScale();
        float rotation = body.getRotation();
        ZIndex zIndex = dragging ? ZIndex.SHOP_CARD_TOUCHING : ZIndex.SHOP_CARD;

        Color shadowColor = Assets.I().shadowColor();
        shadowColor.a = Math.min(shadowColor.a, alpha);
        Pencil.I().addDrawing(new TextureDrawing(
            shadowTexture,
            pos.x, pos.y - 0.2f, bounds.width, bounds.height,
            scale, rotation, zIndex, shadowColor
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            texture,
            pos.x, pos.y, bounds.width, bounds.height,
            scale, rotation, zIndex, new Color(1f, 1f, 1f, alpha)
        ));

        if (!dragging && !selected) {
            priceTag.setZIndex(zIndex);
            priceTag.getFirstDigitBounds().x = pos.x + 0.75f;
            priceTag.getFirstDigitBounds().y = pos.y - 1f;
            priceTag.draw(delta, 1f, rotation);
        }

        if ((selected || dragging) && !bought) {
            buyButton.getBounds().x = pos.x - 0.35f;
            buyButton.getBounds().y = pos.y - 1.5f;
            buyButton.draw(delta);
        }
    }

    private void deselect(boolean killTooltip) {
        body.targetScale = 1f;
        body.setIdleEffectsEnabled(true);
        selected = false;
        if (killTooltip) PopupManager.I().killTooltip(tooltipPopup);
    }

    private void buy() {
        if (!CreditManager.I().enoughCredit(upgrade.price())) {
            CreditManager.I().pulse();
            return;
        }
        CreditManager.I().pay(upgrade.price());
        deselect(true);

        bought = true;
        removeCardWindow.open();
    }

    public DragableBody getBody() {
        return body;
    }

    public RemoveCardWindow getRemoveCardWindow() {
        return removeCardWindow;
    }
}
