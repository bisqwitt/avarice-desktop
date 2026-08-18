package com.avaricious.components;

import com.avaricious.components.buttons.Button;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.TooltipPopup;
import com.avaricious.components.slot.DragableBody;
import com.avaricious.components.texts.ItemsWord;
import com.avaricious.effects.IdleFloatEffect;
import com.avaricious.effects.IdleSwayEffect;
import com.avaricious.items.AbstractItem;
import com.avaricious.items.upgrades.quests.AbstractQuest;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Observable;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemBag extends Observable<List<? extends AbstractItem>> {

    private static ItemBag instance;

    public static ItemBag I() {
        return instance == null ? instance = new ItemBag() : instance;
    }

    private ItemBag() {
        notifyChanged(snapshot());
    }

    private final Rectangle bagBounds = new Rectangle(7.05f, 5.8f, 42 / 26f, 48 / 26f);
    private final TextureRegion bagTexture = Assets.I().get(AssetKey.BAG);
    private final TextureRegion bagShadowTexture = Assets.I().get(AssetKey.BAG_SHADOW);
    private final TextureRegion checkmark = Assets.I().get(AssetKey.CHECKMARK);

    private final List<AbstractItem> items = new ArrayList<>();

    private boolean showingItems = false;
    private final Vector2 mouseTouchdownLocation = new Vector2();
    private AbstractItem touchingItem = null;
    private AbstractItem selectedItem = null;
    private TooltipPopup tooltipPopup = null;

    private final ItemsWord itemsText = new ItemsWord(new Vector2(3.25f, 17f), 10f, 0.15f, ZIndex.UNFOLDED_DECK_CARD);
    private final Button claimButton = new Button(() -> {
        AbstractQuest quest = (AbstractQuest) selectedItem;
        quest.claim();
        deselectItem(true);
    },
        Assets.I().get(AssetKey.CLAIM_BUTTON),
        Assets.I().get(AssetKey.CLAIM_BUTTON_PRESSED),
        Assets.I().get(AssetKey.CLAIM_BUTTON),
        new Rectangle(0f, 0f, 79 / 30f, 25 / 30f),
        Input.Keys.ENTER, ZIndex.UNFOLDED_DECK_CARD);

    private final IdleFloatEffect bagFloatEffect = new IdleFloatEffect();
    private final IdleSwayEffect bagSwayEffect = new IdleSwayEffect();

    public void handleInput(Vector2 mouse, boolean leftClickPressed, boolean leftClickWasPressed, float delta) {
        Seq.of(items).forEach(item -> item.getBody().update(delta));
        bagFloatEffect.update(delta);
        bagSwayEffect.update(delta);

        if (leftClickPressed && !leftClickWasPressed) {
            mouseTouchdownLocation.set(mouse.x, mouse.y);
        }

        if (!leftClickPressed && leftClickWasPressed && bagBounds.contains(mouseTouchdownLocation) && bagBounds.contains(mouse) && !showingItems) {
            toggleShowItems();
        }

        if (showingItems) {
            if (selectedItem != null
                && selectedItem instanceof AbstractQuest
                && ((AbstractQuest) selectedItem).isCompleted()
                && (claimButton.getBounds().contains(mouse) || claimButton.getBounds().contains(mouseTouchdownLocation))) {
                claimButton.handleInput(mouse, leftClickPressed, leftClickWasPressed);
                return;
            }

            if (leftClickPressed && !leftClickWasPressed) {
                for (AbstractItem item : items) {
                    if (item.getBody().getBounds().contains(mouse)) {
                        onCardTouchDown(item, mouse);
                        return;
                    }
                }

                deselectItem(true);
                toggleShowItems();
            }

            if (leftClickPressed && touchingItem != null) {
                onCardTouching(touchingItem, mouse);
            }

            if (!leftClickPressed && leftClickWasPressed && touchingItem != null) {
                onCardTouchReleased(touchingItem, mouse);
            }

            if (touchingItem != null || selectedItem != null) {
                AbstractItem item = touchingItem == null ? selectedItem : touchingItem;
                Vector2 itemRenderPos = item.getBody().getRenderPos(new Vector2());
                PopupManager.I().updateTooltip(
                    new Vector2(itemRenderPos.x - 2f, itemRenderPos.y + item.getTooltipYOffset()),
                    true
                );
            }
        }
    }

    private void onCardTouchDown(AbstractItem item, Vector2 mouse) {
        if (selectedItem != null && item != selectedItem) deselectItem(false);
        touchingItem = item;
        item.getBody().targetScale = 1.3f;
        item.getBody().beginDrag(mouse.x, mouse.y, 0);

        if (selectedItem == null)
            tooltipPopup = PopupManager.I().createTooltip(item, item.getBody().getRenderPos(new Vector2()), ZIndex.UNFOLDED_DECK_CARD);
    }

    private void onCardTouching(AbstractItem item, Vector2 mouse) {
        item.getBody().dragTo(mouse.x, mouse.y, 0);
    }

    private void onCardTouchReleased(AbstractItem item, Vector2 mouse) {
        DragableBody body = item.getBody();
        body.endDrag(0);
        boolean isClick = mouseTouchdownLocation.dst2(mouse) <= 0.2f * 0.2f;
        if (isClick) {
            if (selectedItem == item) deselectItem(true);
            else {
                if (selectedItem != null) deselectItem(false);
                selectedItem = item;
            }
        } else selectedItem = item;
        touchingItem = null;
    }

    public void deselectItem(boolean killTooltip) {
        if (selectedItem == null) return;
        selectedItem.getBody().targetScale = 1f;
        selectedItem.getBody().setIdleEffectsEnabled(true);
        selectedItem = null;
        if (killTooltip) {
            PopupManager.I().killTooltip(tooltipPopup);
            tooltipPopup = null;
        }
    }

    public void draw(float delta) {
        Pencil.I().addDrawing(new TextureDrawing(bagShadowTexture,
            bagBounds.x, bagBounds.y - 0.1f + bagFloatEffect.getValue(), bagBounds.width, bagBounds.height,
            1f, bagSwayEffect.getValue(), ZIndex.RELIC_BAG, Assets.I().shadowColor()));
        Pencil.I().addDrawing(new TextureDrawing(bagTexture,
            bagBounds.x, bagBounds.y + bagFloatEffect.getValue(), bagBounds.width, bagBounds.height,
            1f, bagSwayEffect.getValue(), ZIndex.RELIC_BAG
        ));

        if (showingItems) {
            itemsText.draw(delta);
            for (AbstractItem item : items) {
                drawItem(item);
            }
        }

        if (selectedItem != null || touchingItem != null) {
            AbstractItem item = selectedItem == null ? touchingItem : selectedItem;
            if (item instanceof AbstractQuest && ((AbstractQuest) item).isCompleted()) {
                Vector2 renderPos = item.getBody().getRenderPos(new Vector2());
                claimButton.getBounds().x = renderPos.x - 0.5f;
                claimButton.getBounds().y = renderPos.y - 1.5f;
                claimButton.draw(delta);
            }
        }
    }

    public void drawItem(AbstractItem item) {
        DragableBody body = item.getBody();
        Vector2 pos = body.getRenderPos(new Vector2());
        float scale = body.getScale();
        float rotation = body.getRotation();

        Pencil.I().addDrawing(new TextureDrawing(
            item.shadowTexture(),
            pos.x, pos.y - 0.2f, getTextureWidth(item), getTextureHeight(item),
            scale, rotation, ZIndex.UNFOLDED_DECK_CARD, Assets.I().shadowColor())
        );
        Pencil.I().addDrawing(new TextureDrawing(
            item.texture(),
            pos.x, pos.y, getTextureWidth(item), getTextureHeight(item),
            scale, rotation, ZIndex.UNFOLDED_DECK_CARD
        ));

        if (item instanceof AbstractQuest && ((AbstractQuest) item).isCompleted()) {
            Pencil.I().addDrawing(new TextureDrawing(
                checkmark,
                pos.x + 0.9f, pos.y + 1f, 15 / 15f, 12 / 15f,
                scale, rotation, ZIndex.UNFOLDED_DECK_CARD
            ));
        }
    }

    public void toggleShowItems() {
        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.UNFOLDED_DECK_BACKGROUND);
        if (!showingItems) {
            List<Vector2> positions = new ArrayList<>();
            for (int col = 5; col > 0; col--) {
                for (int row = 0; row < 4; row++) {
                    positions.add(new Vector2(0.7f + row * 2f, 1f + col * 2.5f));
                }
            }

            int index = 0;
            for (AbstractItem item : items) {
                int finalIndex = index;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        item.getBody().moveTo(positions.get(finalIndex));
                    }
                }, finalIndex * 0.025f);
                index++;
            }

            showingItems = true;
        } else {
            List<AbstractItem> reversed = new ArrayList<>(items);
            Collections.reverse(reversed);
            int index = reversed.size() - 1;
            for (AbstractItem item : reversed) {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        item.getBody().moveTo(new Vector2(
                            bagBounds.x, bagBounds.y
                        ));
                    }
                }, index * 0.025f);
                index--;
            }
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    showingItems = false;
                }
            }, items.size() * 0.025f + 0.25f);
        }
    }

    public <T extends AbstractItem> T getItemByClass(Class<T> ringClass) {
        return Seq.of(items)
            .filter(ringClass::isInstance)
            .map(ringClass::cast)
            .findFirstOrNull();
    }


    public <T> List<T> getItemOfType(Class<T> type) {
        return Seq.of(items)
            .filter(type::isInstance)
            .map(type::cast)
            .toList();
    }

    public boolean containsItem(Class<? extends AbstractItem> itemClass) {
        return Seq.of(items).anyMatch(itemClass::isInstance);
    }

    public void addItem(AbstractItem item) {
        Rectangle bounds = new Rectangle(bagBounds.x, bagBounds.y, getTextureWidth(item), getTextureHeight(item));
        item.addBody(bounds);
        items.add(item);

        notifyChanged(snapshot());
    }

    public void removeItem(AbstractItem item) {
        items.remove(item);
    }

    public List<AbstractItem> getItems() {
        return items;
    }

    private float getTextureWidth(AbstractItem item) {
        return item instanceof AbstractQuest ? AbstractQuest.WIDTH / 30
            : item.getTextureWidth() / 15;
    }

    private float getTextureHeight(AbstractItem item) {
        return item instanceof AbstractQuest ? AbstractQuest.HEIGHT / 30
            : item.getTextureHeight() / 15;
    }

    public void setItems(List<? extends AbstractItem> items) {
        this.items.clear();
        Seq.of(items).forEach(this::addItem);
    }

    @Override
    protected List<? extends AbstractItem> snapshot() {
        return items;
    }
}
