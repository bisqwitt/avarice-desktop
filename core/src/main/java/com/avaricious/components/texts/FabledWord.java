package com.avaricious.components.texts;

import com.avaricious.effects.IdleFloatEffect;
import com.avaricious.effects.IdleSwayEffect;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FabledWord {

    private final List<TextureRegion> letterTextures = new ArrayList<>();
    private final List<TextureRegion> letterShadowTextures = new ArrayList<>();
    private final List<Float> lettersX = new ArrayList<>();

    private final Vector2 startingPos;
    private final float sizeRatio;
    private final float spacing;
    private final ZIndex zIndex;
    private Color color;

    protected final List<IdleFloatEffect> floatEffects = new ArrayList<>();
    protected final List<IdleSwayEffect> swayEffects = new ArrayList<>();

    public FabledWord(List<TextureRegion> letterTextures, List<TextureRegion> letterShadowTextures,
                      Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        this.letterTextures.addAll(letterTextures);
        this.letterShadowTextures.addAll(letterShadowTextures);
        this.startingPos = startingPos;
        this.sizeRatio = sizeRatio;
        this.spacing = spacing;
        this.zIndex = zIndex;

        Seq.of(letterTextures).forEach(letter -> {
            floatEffects.add(new IdleFloatEffect());
            swayEffects.add(new IdleSwayEffect());
        });

        calcLetterX();
    }

    public void draw(float delta) {
        Seq.of(floatEffects).forEach(effect -> effect.update(delta));
        Seq.of(swayEffects).forEach(effect -> effect.update(delta));

        for (int i = 0; i < letterTextures.size(); i++) {
            TextureRegion letter = letterTextures.get(i);
            float x = startingPos.x + lettersX.get(i);
            float y = startingPos.y + floatEffects.get(i).getValue();
            float width = letter.getRegionWidth() / sizeRatio;
            float height = letter.getRegionHeight() / sizeRatio;
            float rotation = swayEffects.get(i).getValue();
            Color color = this.color == null ? new Color(1f, 1f, 1f, 1f) : this.color;

            if (extendsBelowBaseline(letter)) y -= height / 3.5f;

            Pencil.I().addDrawing(new TextureDrawing(letterShadowTextures.get(i),
                x, y - 0.1f, width, height,
                1f, rotation, zIndex, Assets.I().shadowColor()));

            Pencil.I().addDrawing(new TextureDrawing(letter,
                x, y, width, height, 1f, rotation, zIndex, color));
        }
    }

    private void calcLetterX() {
        lettersX.add(0f);

        for (int i = 1; i < letterTextures.size(); i++) {
            TextureRegion letterBefore = letterTextures.get(i - 1);
            float letterBeforeWidth = letterBefore.getRegionWidth() / sizeRatio;

            float x =
                lettersX.get(i - 1)
                    + letterBeforeWidth
                    + spacing;

            lettersX.add(x);
        }
    }

    private boolean extendsBelowBaseline(TextureRegion letterTexture) {
        return Arrays.asList(
            Assets.I().get(AssetKey.P),
            Assets.I().get(AssetKey.P_SHADOW),
            Assets.I().get(AssetKey.Y),
            Assets.I().get(AssetKey.Y_SHADOW),
            Assets.I().get(AssetKey.G),
            Assets.I().get(AssetKey.G_SHADOW),
            Assets.I().get(AssetKey.Q),
            Assets.I().get(AssetKey.Q_SHADOW)
        ).contains(letterTexture);
    }

    public FabledWord setColor(Color color) {
        this.color = color;
        return this;
    }

    public Vector2 getStartingPos() {
        return startingPos;
    }

    public float getWidth() {
        if (letterTextures.isEmpty()) {
            return 0f;
        }

        int lastIndex = letterTextures.size() - 1;
        TextureRegion lastLetter = letterTextures.get(lastIndex);

        float lastLetterX = lettersX.get(lastIndex);
        float lastLetterWidth = lastLetter.getRegionWidth() / sizeRatio;

        return lastLetterX + lastLetterWidth;
    }
}
