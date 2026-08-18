package com.avaricious.components.popups;

import com.avaricious.items.AbstractItem;
import com.avaricious.items.IItemWithRarity;
import com.avaricious.items.IItemWithType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.FontDrawing;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;
import java.util.Map;

public class TooltipPopup {
    private final Map<Integer, TextureRegion> boxes = new HashMap<>();
    private final TextureRegion boxShadow;

    private final BitmapFont bigFont;
    private final BitmapFont mediumFont;
    private final BitmapFont smallFont;

    private final GlyphLayout titleTxt = new GlyphLayout();
    private final GlyphLayout descriptionTxt = new GlyphLayout();
    private final GlyphLayout rarityTxt = new GlyphLayout();
    private final GlyphLayout typeTxt = new GlyphLayout();

    private final float BOX_WRAP_WIDTH = 500f;
    private final float TYPE_BOX_WRAP_WIDTH = 150f;

    private final AbstractItem item;
    private final ZIndex layer;

    private Vector2 pos;

    private float alpha = 0f;
    private boolean visible = false;

    public TooltipPopup(AbstractItem item, Vector2 pos) {
        this(item, pos, ZIndex.POPUP_DEFAULT);
    }

    public TooltipPopup(AbstractItem item, Vector2 pos, ZIndex layer) {
        this.item = item;
        this.pos = new Vector2(pos);
        this.layer = layer;

        boxes.put(1, Assets.I().get(AssetKey.TOOLTIP_BOX_S));
        boxes.put(2, Assets.I().get(AssetKey.TOOLTIP_BOX_M));
        boxes.put(3, Assets.I().get(AssetKey.TOOLTIP_BOX_L));
        boxes.put(4, Assets.I().get(AssetKey.TOOLTIP_BOX_XL));
        boxShadow = Assets.I().get(AssetKey.TOOLTIP_BOX_SHADOW);

        bigFont = Assets.I().getBigFont();
        mediumFont = Assets.I().getMediumFont();
        smallFont = Assets.I().getSmallFont();

        titleTxt.setText(bigFont, item.title(), Color.WHITE, BOX_WRAP_WIDTH, Align.top | Align.center, true);
        setDescription(item.description());
        if (item instanceof IItemWithType)
            typeTxt.setText(smallFont, ((IItemWithType) item).type().toString(), Color.WHITE, TYPE_BOX_WRAP_WIDTH, Align.top | Align.center, true);
        if (item instanceof IItemWithRarity)
            rarityTxt.setText(smallFont, ((IItemWithRarity) item).rarity().toString(), Color.WHITE, TYPE_BOX_WRAP_WIDTH, Align.top | Align.center, true);
    }

    public void update(Vector2 pos, boolean visible) {
        this.pos = new Vector2(pos);
        this.visible = visible;
        setDescription(item.description());
    }

    public void draw(float delta) {
        updateAlpha(delta);
        TextureRegion box = boxes.get(calcDescriptionLineAmount());
        float worldWidth = GameContext.I().viewport.getWorldWidth();
        float worldHeight = GameContext.I().viewport.getWorldHeight();

        float boxWidth = 82 / 15f;
        float boxHeight = box.getRegionHeight() / 15f;
        final float boxX = pos.x < 0.25f
            ? 0.25f : pos.x + boxWidth > worldWidth - 0.25f
            ? worldWidth - boxWidth - 0.25f : pos.x;
        float boxY = pos.y + boxHeight > worldHeight - 0.25f
            ? worldHeight - boxHeight - 0.25f : pos.y;

        Color shadowColor = Assets.I().shadowColor();
        Pencil.I().addDrawing(new TextureDrawing(
            boxShadow,
            boxX, boxY - 0.2f, boxWidth, boxHeight,
            layer, new Color(shadowColor.r, shadowColor.g, shadowColor.b, Math.min(shadowColor.a, alpha))
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            box,
            boxX, boxY, boxWidth, boxHeight,
            layer, new Color(1f, 1f, 1f, alpha)
        ));

        float typeBoxWidth = 79 / 45f;
        float typeBoxHeight = 23 / 45f;
        if (item instanceof IItemWithType)
            Pencil.I().addDrawing(new TextureDrawing(
                ((IItemWithType) item).type().getTypeBox(),
                1.2f + (boxX + boxWidth / 2f) - typeBoxWidth / 2f, boxY + 0.22f, typeBoxWidth, typeBoxHeight,
                layer, new Color(1f, 1f, 1f, alpha)
            ));
        if (item instanceof IItemWithRarity)
            Pencil.I().addDrawing(new TextureDrawing(
                ((IItemWithRarity) item).rarity().getRarityBoxTexture(),
                -1.25f + (boxX + boxWidth / 2f) - typeBoxWidth / 2f, boxY + 0.22f, typeBoxWidth, typeBoxHeight,
                layer, new Color(1f, 1f, 1f, alpha)
            ));

        Vector2 center = new Vector2(boxX + boxWidth / 2f, boxY + boxHeight / 2f);
        center.set(center.x * 100, center.y * 100); // Convert to UIViewport

        if (alpha > 0.5f) {
            float offset = calcTextOffset();

            float titleX = center.x - BOX_WRAP_WIDTH / 2f;
            float titleY = center.y + 140f + offset;

            float descriptionX = center.x - BOX_WRAP_WIDTH / 2f;
            float descriptionY = center.y + 35f + offset;

            float typeX = (center.x - TYPE_BOX_WRAP_WIDTH / 2f) + 120f;
            float typeY = (boxY * 100f) + 60f;
            float rarityX = (center.x - TYPE_BOX_WRAP_WIDTH / 2f) - 127f;


            Pencil.I().addDrawing(new FontDrawing(
                bigFont, titleTxt, new Vector2(titleX, titleY), layer
            ));
            Pencil.I().addDrawing(new FontDrawing(
                mediumFont, descriptionTxt, new Vector2(descriptionX, descriptionY), layer
            ));
            Pencil.I().addDrawing(new FontDrawing(
                smallFont, typeTxt, new Vector2(typeX, typeY), layer
            ));
            Pencil.I().addDrawing(new FontDrawing(
                smallFont, rarityTxt, new Vector2(rarityX, typeY), layer
            ));
        }
    }

    private float getTargetAlpha() {
        return visible ? 1f : 0f;
    }

    private void updateAlpha(float delta) {
        float targetAlpha = getTargetAlpha();
        float responsiveness = 12f; // higher = snappier
        alpha += (targetAlpha - alpha) * (1f - (float) Math.exp(-responsiveness * delta));
        // optional: snap when extremely close
        if (Math.abs(targetAlpha - alpha) < 0.001f) {
            alpha = targetAlpha;
        }
    }

    private int calcDescriptionLineAmount() {
        if (descriptionTxt.height > 146) return 4;
        else if (descriptionTxt.height > 90) return 3;
        else if (descriptionTxt.height > 34) return 2;
        else return 1;
    }

    private float calcTextOffset() {
        int numberOfLines = calcDescriptionLineAmount();
        return -25 + ((numberOfLines - 1) * 29);
    }

    private void setDescription(String txt) {
        descriptionTxt.setText(mediumFont, "[WHITE]" + txt + "[]", Color.WHITE, BOX_WRAP_WIDTH, Align.top | Align.center, true);
    }

    public TooltipPopup setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public AbstractItem getItem() {
        return item;
    }
}
