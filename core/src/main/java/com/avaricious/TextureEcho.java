package com.avaricious;

import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TextureEcho {

    private final TextureRegion textureEcho;
    private final Color color;

    private final Rectangle currentPos;
    private final float streak;
    private float alpha = 1f;

    private static final List<TextureEcho> echos = new ArrayList<>();

    public static void create(TextureRegion texture, Rectangle spawnPoint, Color color, float streak) {
        echos.add(new TextureEcho(texture, spawnPoint, color, streak));
    }

    public static void draw(SpriteBatch batch, float delta) {
        List<TextureEcho> dump = new ArrayList<>();
        for (TextureEcho echo : echos) {
            echo._draw(batch, delta);
            if (!echo.isAlive()) dump.add(echo);
        }
        echos.removeAll(dump);
    }

    private TextureEcho(TextureRegion textureEcho, Rectangle spawnPoint, Color color, float streak) {
        this.textureEcho = textureEcho;
        this.color = color;
        currentPos = new Rectangle(spawnPoint);
        this.streak = streak;
    }

    private void _draw(SpriteBatch batch, float delta) {
        Pencil.I().addDrawing(new TextureDrawing(
            textureEcho,
            currentPos.x, currentPos.y, currentPos.width, currentPos.height,
            ZIndex.TEXTURE_ECHO, new Color(color.r, color.g, color.b, alpha)));

        float echoSpeed = 18f;
        float fadeSpeed = 0.2f;

        float additionalSize = delta * echoSpeed;
        float aspectRatio = currentPos.width / currentPos.height;

        currentPos.width += additionalSize;
        currentPos.height = currentPos.width / aspectRatio;

        currentPos.x -= currentPos.width / 2;
        currentPos.y -= currentPos.height / 2;

//        alpha -= delta * fadeSpeed;
    }

    public boolean isAlive() {
        return alpha > 0f;
    }
}

