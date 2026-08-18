package com.avaricious.components.texts;

import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class GoalWord extends FabledWord {
    protected GoalWord(List<TextureRegion> letterTextures, List<TextureRegion> letterShadowTextures, Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(letterTextures, letterShadowTextures, startingPos, sizeRatio, spacing, zIndex);
    }
//    protected GoalText(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
//        super(
//            Arrays.asList(
//                Assets.I().get(G)
//            ), letterShadowTextures, startingPos, sizeRatio, spacing, zIndex);
//    }
}
