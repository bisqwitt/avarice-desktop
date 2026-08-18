package com.avaricious.utility;

import com.avaricious.components.slot.Symbol;
import com.avaricious.items.upgrades.UpgradeRarity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class Assets {

    private static Assets instance;

    public static Assets I() {
        return instance == null ? instance = new Assets() : instance;
    }

    private Assets() {
    }

    private final AssetManager manager = new AssetManager();

    private boolean queued = false;
    private boolean cached = false;

    private final Color blue = new Color(0.1647f, 0.5412f, 0.7843f, 1f);
    private final Color red = new Color(0.7922f, 0.3765f, 0.3333f, 1f);
    private final Color green = new Color(0.2980f, 0.7098f, 0.4470f, 1f);
    private final Color yellow = new Color(218f / 255f, 172f / 255f, 83f / 255f, 1f);
    private final Color silver = new Color(191f / 255f, 197f / 255f, 204f / 255f, 1f);
    private final Color light = new Color(0.992156f, 0.992156f, 0.992156f, 1f);
    private final Color shadow = new Color(1f, 1f, 1f, 0.25f);
    private final Color healthRed = new Color(229 / 255f, 57 / 255f, 53 / 255f, 1f);
    private final Color applyColor = new Color(255 / 255f, 216 / 255f, 77 / 255f, 1f);
    private final Color common = new Color(1f, 1f, 1f, 1f);
    private final Color uncommon = new Color(0.30f, 0.69f, 0.31f, 1f);
    private final Color rare = new Color(0.13f, 0.59f, 0.95f, 1f);
    private final Color epic = new Color(0.61f, 0.15f, 0.69f, 1f);
    private final Color legendary = new Color(1f, 0.60f, 0.00f, 1f);

    private final Color lemonColor = new Color(1.000f, 0.914f, 0.290f, 1f);
    private final Color cherryColor = new Color(1.000f, 0.090f, 0.267f, 1f);
    private final Color cloverColor = new Color(0.263f, 0.788f, 0.000f, 1f);
    private final Color bellColor = new Color(1.000f, 0.702f, 0.000f, 1f);
    private final Color diamondColor = new Color(0.161f, 0.714f, 0.965f, 1f);
    private final Color ironColor = new Color(1.000f, 0.839f, 0.000f, 1f);
    private final Color sevenColor = new Color(1.000f, 0.125f, 0.125f, 1f);

    private final Map<AssetKey, TextureRegion> cachedTextures = new HashMap<>();
    private BitmapFont fabledFont;
    private BitmapFont titleFont;
    private BitmapFont bigFont;
    private BitmapFont mediumFont;
    private BitmapFont smallFont;

    public void queueLoading() {
        if (queued) return;

        manager.load("atlases.atlas", TextureAtlas.class);

        // Fonts are currently generated synchronously.
        // This is okay if they are fast enough for your project.
        loadFonts();

        queued = true;
    }

    public boolean update() {
        if (!queued) {
            queueLoading();
        }

        if (!manager.update()) {
            return false;
        }

        if (!cached) {
            TextureAtlas atlas = manager.get("atlases.atlas", TextureAtlas.class);
            cache(atlas);
            cached = true;
        }

        return true;
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public boolean isReady() {
        return cached;
    }

    private void loadFonts() {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/m6x11plus.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 120;
        titleFont = generator.generateFont(param);
        titleFont.setUseIntegerPositions(false);
        titleFont.getData().markupEnabled = true;

        param.size = 80;
        bigFont = generator.generateFont(param);
        bigFont.setUseIntegerPositions(false);
        bigFont.getData().markupEnabled = true;

        param.size = 56;
        mediumFont = generator.generateFont(param);
        mediumFont.setUseIntegerPositions(false);
        mediumFont.getData().markupEnabled = true;

        param.size = 40;
        smallFont = generator.generateFont(param);
        smallFont.setUseIntegerPositions(false);
        smallFont.getData().markupEnabled = true;

        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Fabled_Font.ttf"));

        param.size = 108;
        fabledFont = generator.generateFont(param);
        fabledFont.setUseIntegerPositions(false);
        fabledFont.getData().markupEnabled = true;

        generator.dispose();
    }

    private void cache(TextureAtlas atlas) {
        for (AssetKey key : AssetKey.values()) {
            TextureRegion r = atlas.findRegion(key.path());
            if (r == null) {
                throw new IllegalStateException(
                    "Missing region in atlas: key=" + key + " regionName=" + key.path()
                );
            }
            cachedTextures.put(key, r);
        }
    }

    public TextureRegion get(AssetKey key) {
        if (!cached) {
            throw new IllegalStateException("Assets are not ready yet. Did you wait for Assets.I().update()?");
        }

        return cachedTextures.get(key);
    }

    public Animation<TextureRegion> getAnimation(AssetAnimationKey key, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>();
        for (AssetKey textureKey : key.getFrameAssetKeys()) {
            frames.add(get(textureKey));
        }
        return new Animation<>(frameDuration, frames, playMode);
    }

    public TextureRegion getSymbol(Symbol symbol) {
        return get(symbol.textureKey());
    }

    public Color getSymbolColor(Symbol symbol) {
        switch (symbol) {
            case LEMON: return lemonColor;
            case CHERRY: return cherryColor;
            case CLOVER: return cloverColor;
            case BELL: return bellColor;
            case IRON: return ironColor;
            case DIAMOND: return diamondColor;
        }
        return sevenColor;
    }

    private static final AssetKey[] DIGITS = {
        AssetKey.ZERO_NUMBER, AssetKey.ONE_NUMBER, AssetKey.TWO_NUMBER, AssetKey.THREE_NUMBER, AssetKey.FOUR_NUMBER,
        AssetKey.FIVE_NUMBER, AssetKey.SIX_NUMBER, AssetKey.SEVEN_NUMBER, AssetKey.EIGHT_NUMBER, AssetKey.NINE_NUMBER
    };

    private static final AssetKey[] DIGIT_SHADOWS = {
        AssetKey.ZERO_NUMBER_SHADOW, AssetKey.ONE_NUMBER_SHADOW, AssetKey.TWO_NUMBER_SHADOW, AssetKey.THREE_NUMBER_SHADOW, AssetKey.FOUR_NUMBER_SHADOW,
        AssetKey.FIVE_NUMBER_SHADOW, AssetKey.SIX_NUMBER_SHADOW, AssetKey.SEVEN_NUMBER_SHADOW, AssetKey.EIGHT_NUMBER_SHADOW, AssetKey.NINE_NUMBER_SHADOW
    };

    public TextureRegion getDigitalNumber(int number) {
        if (number < 0 || number > 9) number = 0;
        return get(DIGITS[number]);
    }

    public TextureRegion getDigitalNumberShadow(int number) {
        if (number < 0 || number > 9) number = 0;
        return get(DIGIT_SHADOWS[number]);
    }

    public BitmapFont getFabledFont() {
        return fabledFont;
    }

    public BitmapFont getTitleFont() {
        return titleFont;
    }

    public BitmapFont getBigFont() {
        return bigFont;
    }

    public BitmapFont getMediumFont() {
        return mediumFont;
    }

    public BitmapFont getSmallFont() {
        return smallFont;
    }

    public Color blue() {
        return blue;
    }

    public String blueText(String txt) {
        return "[#2A8AC8]" + txt + "[]";
    }

    public Color red() {
        return red;
    }

    public String redText(String txt) {
        return "[#CA6055]" + txt + "[]";
    }

    public Color green() {
        return green;
    }

    public String greenText(String txt) {
        return "[#4CB572]" + txt + "[]";
    }

    public Color yellow() {
        return yellow;
    }

    public String yellowText(String txt) {
        return "[#daad53]" + txt + "[]";
    }

    public Color lightColor() {
        return light;
    }

    public Color shadowColor() {
        return shadow;
    }

    public Color silver() {
        return silver;
    }

    public String silverText(String txt) {
        return "[#bfc5cc]" + txt + "[]";
    }

    public Color healthRedColor() {
        return healthRed;
    }

    public String healthRedText(String txt) {
        return "[#E53935]" + txt + "[]";
    }

    public Color applyColor() {
        return applyColor;
    }

    public Color getRarityColor(UpgradeRarity rarity) {
        if (rarity == UpgradeRarity.COMMON) return common;
        else if (rarity == UpgradeRarity.UNCOMMON) return uncommon;
        else if (rarity == UpgradeRarity.RARE) return rare;
        else if (rarity == UpgradeRarity.EPIC) return epic;
        else return legendary;
    }
}

