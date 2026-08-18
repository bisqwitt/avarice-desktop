package com.avaricious.components.popups;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class TextBoxPopup {

    private final float boxWidth = 173 / 50f;
    private final float boxHeight = 65 / 50f;

    private final TextureRegion textBox = Assets.I().get(AssetKey.TXT_BOX);
    private final TextureRegion textBoxShadow = Assets.I().get(AssetKey.TXT_BOX_SHADOW);

    protected Vector2 pos = new Vector2();
    protected float alpha = 0f;

    private boolean killed = false;
    private Runnable onDead = null;

    public TextBoxPopup(Vector2 pos) {
        this.pos.set(pos);
    }

    public void update(Vector2 pos) {
        this.pos.set(pos);
    }

    public void draw(float delta) {
        updateAlpha(delta);

        float boxX = calcBoxX();
        Pencil.I().addDrawing(new TextureDrawing(textBoxShadow,
            boxX, pos.y - 0.1f, boxWidth, boxHeight,
            ZIndex.POPUP_DEFAULT, new Color(1f, 1f, 1f, Math.min(0.25f, alpha))));

        Pencil.I().addDrawing(new TextureDrawing(getTextBox(),
            boxX, pos.y, boxWidth, boxHeight,
            ZIndex.POPUP_DEFAULT, new Color(1f, 1f, 1f, alpha)));
    }

    private void updateAlpha(float delta) {
        float targetAlpha = killed ? 0 : 1;
        float responsiveness = 12f; // higher = snappier
        alpha += (targetAlpha - alpha) * (1f - (float) Math.exp(-responsiveness * delta));
        // optional: snap when extremely close
        if (Math.abs(targetAlpha - alpha) < 0.001f) {
            alpha = targetAlpha;
            if (onDead != null) onDead.run();
        }
    }

    public void kill(Runnable onDead) {
        this.onDead = onDead;
        killed = true;
    }

    public boolean isKilled() {
        return killed;
    }

    protected float calcBoxX() {
        float worldWidth = GameContext.I().viewport.getWorldWidth();
        return pos.x < 0.25f
            ? 0.25f : pos.x + boxWidth > worldWidth - 0.25f
            ? worldWidth - boxWidth - 0.25f : pos.x;
    }

    protected TextureRegion getTextBox() {
        return textBox;
    }

}
