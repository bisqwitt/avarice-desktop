package com.avaricious;

import com.avaricious.components.buttons.Button;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.TooltipPopup;
import com.avaricious.components.slot.DragableBody;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.items.upgrades.AbstractUpgrade;
import com.avaricious.items.upgrades.UpgradeRarity;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

public abstract class PackOpening {

    private final Rectangle bounds;
    private DragableBody body;
    private final CreditNumber price;

    protected int currentTextureIndex = 0;
    private float lastTextureChange = 0f;
    private boolean bought = false;

    private float whiten = 0f;
    private boolean whitening = false;

    private boolean charging = false;
    private float charge = 0f;
    private float chargeDuration = 3.5f;

    private float shakeTime = 0f;
    private final Vector2 shakeOffset = new Vector2();
    private float shakeRotDeg = 0f;

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private float beamTime = 0f;
    private float beamLength = 10f;      // in world units (relative to your card size)
    private float beamWidth = 0.22f;     // thickness
    private float beamSpinDeg = 0f;

    private boolean flashActive = false;
    private float flashTime = 0f;
    private float flashDuration = 0.45f;

    private float screenWhiteAlpha = 0f;
    private float coreGlowAlpha = 0f;
    private float coreGlowScale = 0f;

    private static final int BEAM_COUNT = 8;
    private final UpgradeRarity[] beamRarity = new UpgradeRarity[BEAM_COUNT];

    protected boolean selected = false;
    protected boolean ripped = false;
    private boolean dragging = false;
    protected boolean closing = false;
    private boolean closed = false;

    private AbstractUpgrade result;
    private final Vector2 mouseTouchdownLocation = new Vector2();
    protected TooltipPopup tooltipPopup = null;

    private final Button buyButton = new Button(this::buy,
        Assets.I().get(AssetKey.BUY_BUTTON),
        Assets.I().get(AssetKey.BUY_BUTTON_PRESSED),
        Assets.I().get(AssetKey.BUY_BUTTON),
        new Rectangle(0, 0, 79 / 30f, 25 / 30f),
        Input.Keys.ENTER, ZIndex.UNFOLDED_DECK_CARD);
    private final Button sellButton = getSellButton();
    private final Button claimButton = getClaimButton();

    public PackOpening(Rectangle bounds) {
        this.bounds = bounds;
        body = new DragableBody(bounds);

        price = new CreditNumber(getPackDescription().price(), new Rectangle(bounds.x + 0.5f, bounds.y, 7 / 20f, 11 / 20f), 0.4f);
    }

    public void handleInput(Vector2 mouse, boolean touching, boolean wasTouching, float delta) {
        if (closed) return;
        update(delta);
        body.update(delta);

        if (ripped) {
            sellButton.handleInput(mouse, touching, wasTouching);
            claimButton.handleInput(mouse, touching, wasTouching);
        }

        if (bought) return;

        if ((selected || dragging) && buyButton.getBounds().contains(mouse)) {
            buyButton.handleInput(mouse, touching, wasTouching);
            return;
        }

        if (touching && !wasTouching) {
            if (body.getBounds().contains(mouse)) {
                this.dragging = true;
                body.targetScale = 1.3f;
                body.setIdleEffectsEnabled(false);
                body.beginDrag(mouse.x, mouse.y, 0);

                mouseTouchdownLocation.set(mouse);
                tooltipPopup = PopupManager.I().createTooltip(getPackDescription(), body.getRenderPos(new Vector2()));
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

        if (dragging || selected) {
            Vector2 cardRenderPos = body.getRenderPos(new Vector2());
            PopupManager.I().updateTooltip(
                new Vector2(cardRenderPos.x - 2f, cardRenderPos.y + getPackDescription().getTooltipYOffset()),
                true
            );
        }
    }

    private void deselect(boolean killTooltip) {
        body.targetScale = 1f;
        body.setIdleEffectsEnabled(true);
        selected = false;
        if (killTooltip) {
            PopupManager.I().killTooltip(tooltipPopup);
            tooltipPopup = null;
        }
    }

    public void draw(float delta) {
        if (closed) return;
        Rectangle bounds = body.getBounds();

        final Vector2 position = body.getRenderPos(new Vector2());
        final float alpha = body.getAlpha();
        final float scale = body.getScale();
        final float rotation = body.getRotation();
        final Vector2 drawPos = new Vector2(position).add(shakeOffset);
        final float drawRot = rotation + shakeRotDeg;
        final ZIndex layer = dragging || charging || ripped ? ZIndex.PACK_OPENING_SELECTED : ZIndex.SHOP;

        price.setZIndex(layer);
        price.getFirstDigitBounds().y = position.y - 1f;

        final Color shadowColor = Assets.I().shadowColor();
        Pencil.I().addDrawing(new TextureDrawing(
            getShadowTexture(),
            drawPos.x, drawPos.y - 0.2f * body.targetScale,
            bounds.width, bounds.height
            , scale, drawRot,
            layer, new Color(shadowColor.r, shadowColor.g, shadowColor.b, Math.min(shadowColor.a, alpha))
        ));

        if (charging || ripped)
            drawBeamsOrdered(getCenter(), bounds, scale, drawRot, alpha, layer);

        Pencil.I().addDrawing(new TextureDrawing(
            getTexture(),
            drawPos.x, drawPos.y, bounds.width, bounds.height,
            scale, drawRot,
            layer, new Color(1f, 1f, 1f, alpha)
        ));
        if (!dragging && !charging && !ripped)
            price.draw(delta, 1f, rotation);

        if (whiten > 0f && !ripped) {
            float easedWhiten = whiten * whiten * (3f - 2f * whiten);
            Pencil.I().addDrawing(new TextureDrawing(
                getWhiteTexture(),
                drawPos.x, drawPos.y, bounds.width, bounds.height,
                scale, drawRot,
                layer, new Color(1f, 1f, 1f, alpha * easedWhiten)
            ));
        }

        if ((selected || dragging) && !bought) {
            Vector2 renderPos = body.getRenderPos(new Vector2());
            buyButton.getBounds().x = renderPos.x - 0.5f;
            buyButton.getBounds().y = renderPos.y - 1.5f;
            buyButton.draw(delta);
        }

        if (flashActive) {
            drawCoreGlow(layer);
            drawScreenFlash(layer);
        }

        if (ripped && !closing) {
            sellButton.draw(delta);
            claimButton.draw(delta);
        }
    }

    private void drawBeamsOrdered(Vector2 drawPos, Rectangle bounds, float scale, float drawRot, float alpha, ZIndex layer) {
        float beamsDoneAt = 0.875f;
        float t = Math.min(1f, charge / beamsDoneAt);

        float aBase = alpha * 0.9f;

        for (int i = 0; i < BEAM_COUNT; i++) {
            float beamProgress = t * BEAM_COUNT - i;
            if (beamProgress <= 0f) continue;

            beamProgress = Math.min(1f, beamProgress);

            float angle = (90f + i * 135f + beamSpinDeg) % 360f;

            float flicker = 0.85f + 0.15f * (float) Math.sin(beamTime * 10f + i * 2.3f);
            float a = aBase * flicker;

            float length = beamLength * beamProgress;

//            Color color = Assets.I().getRarityColor(beamRarity[ripped ? beamRarity.length - 1 : i]);
            Color color = new Color(1f, 1f, 1f, a);
            Pencil.I().addDrawing(new TextureDrawing(
                whitePixel,
                drawPos.x - beamWidth / 2f, drawPos.y, beamWidth, length,
                scale, angle, layer,
                new Color(color.r, color.g, color.b, a)
            ));
        }
    }

    private void drawCoreGlow(ZIndex layer) {
        Vector2 center = body.getCardCenter();
        float width = bounds.width * coreGlowScale;
        float height = bounds.height * coreGlowScale;

        Pencil.I().addDrawing(new TextureDrawing(
            getWhiteTexture(),
            center.x - width / 2f,
            center.y - height / 2f,
            width, height
            , 1f, 0f, layer,
            new Color(1f, 1f, 1f, coreGlowAlpha)
        ).setBeforeDrawing(() -> GameContext.I().batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE))
            .setAfterDrawing(() -> GameContext.I().batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)));
    }

    private void drawScreenFlash(ZIndex layer) {
        float w = GameContext.I().viewport.getWorldWidth();
        float h = GameContext.I().viewport.getWorldHeight();

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            0f, 0f, w, h,
            1f, 0f, layer,
            new Color(1f, 1f, 1f, screenWhiteAlpha)
        ));
    }

    private void update(float delta) {
        lastTextureChange += delta;

        beamTime += delta;
        float targetSpeed = 75 * charge * charge;
        beamSpinDeg += delta * targetSpeed;
        beamSpinDeg %= 360f;

        float textureChangeSpeed = 0.1f;
        if (lastTextureChange > textureChangeSpeed) {
            currentTextureIndex = currentTextureIndex == getTextureAmount() - 1 ? 0 : currentTextureIndex + 1;
            lastTextureChange = 0f;
        }

        if (whitening) {
            float speed = 0.35f;
            whiten = Math.min(1f, whiten + delta * speed);
            if (whiten >= 1f) whitening = false;
        }

        shakeTime += delta;

        if (charging && !ripped) {
            charge = Math.min(1f, charge + delta / chargeDuration);

            float intensity = charge * charge;
            float freq = 40f + 60f * intensity;
            float ampPos = 0.02f + 0.18f * intensity;
            float ampRot = 0.4f + 6f * intensity;

            shakeOffset.set(
                (float) Math.sin(shakeTime * freq * 1.7f) * ampPos,
                (float) Math.cos(shakeTime * freq * 2.1f) * ampPos
            );
            shakeRotDeg = (float) Math.sin(shakeTime * freq * 1.3f) * ampRot;

            if (charge >= 1f && !flashActive) {
                startFlash();
            }
        } else {
            shakeOffset.setZero();
            shakeRotDeg = 0f;
        }

        updateFlash(delta);
    }

    private void updateFlash(float delta) {
        if (!flashActive) return;

        flashTime += delta;
        float t = Math.min(1f, flashTime / flashDuration);

        // Smooth growth
        float eased = t * t * (3f - 2f * t); // smoothstep

        // Core glow starts strong and expands quickly
        coreGlowAlpha = Math.min(1f, eased * 1.2f);
        coreGlowScale = 0.6f + eased * 5.0f;

        // Fullscreen white stays modest, then ramps hard near the end
        float late = Math.max(0f, (t - 0.45f) / 0.55f);
        late = late * late * late; // aggressive end burst
        screenWhiteAlpha = Math.min(1f, late * 1.15f);

        if (t >= 1f) {
            flashActive = false;
            screenWhiteAlpha = 1f; // optional short white hold
            ripOpen();
        }
    }

    private void buy() {
        CreditManager.I().pay(3);
        bought = true;
        PopupManager.I().killTooltip(tooltipPopup);

        Vector2 pos = body.getRenderPos(new Vector2());
        Rectangle initialBounds = new Rectangle(pos.x, pos.y, bounds.width, bounds.height);
        body = new DragableBody(initialBounds).setMoveToSpeed(3).setTargetScaleSpeed(5);

        float screenWidth = GameContext.I().viewport.getWorldWidth();
        float screenHeight = GameContext.I().viewport.getWorldHeight();
        body.moveTo(new Vector2(screenWidth / 2f - (bounds.width / 2f), screenHeight / 2f - (bounds.height / 2f)));
        body.targetScale = 2f;

        whitening = true;
        whiten = 0f;

        ripped = false;
        charging = true;
        shakeTime = 0f;

        float rarityUpgradeChance = 0.50f; // 5%
        for (int i = 0; i < beamRarity.length; i++) {
            boolean upgrade = Math.random() < rarityUpgradeChance;
            UpgradeRarity rarity;
            if (i == 0) rarity = upgrade ? UpgradeRarity.UNCOMMON : UpgradeRarity.COMMON;
            else rarity = upgrade ? beamRarity[i - 1].next() : beamRarity[i - 1];
            beamRarity[i] = rarity;
        }

        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.PACK_OPENING_BACKGROUND);
    }

    private void startFlash() {
        flashActive = true;
        flashTime = 0f;
    }

    protected void ripOpen() {
        ripped = true;
        result = getResult();
        Vector2 centerPos = body.getCardCenter().sub(1, 1);
        ParticleManager.I().create(centerPos.x, centerPos.y, ParticleType.RAINBOW, 0.09f, ZIndex.PACK_OPENING);
//        ParticleManager.I().create(centerPos.x, centerPos.y, ParticleType.WHITE, 0.05f, 17);

        body.pulse();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Vector2 renderPos = body.getRenderPos(new Vector2());
                tooltipPopup = PopupManager.I().createTooltip(result, new Vector2(
                        renderPos.x - 2f, renderPos.y + getPackDescription().getTooltipYOffset() + 0.85f),
                    ZIndex.PACK_OPENING_SELECTED).setVisible(true);
            }
        }, 0.2f);
    }

    protected void close() {
        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.PACK_OPENING_BACKGROUND);
        closed = true;
    }

    public DragableBody getBody() {
        return body;
    }

    protected abstract TextureRegion getTexture();

    protected abstract TextureRegion getShadowTexture();

    protected abstract TextureRegion getWhiteTexture();

    protected abstract int getTextureAmount();

    protected abstract AbstractUpgrade getPackDescription();

    protected abstract AbstractUpgrade getResult();

    protected abstract Button getSellButton();

    protected abstract Button getClaimButton();

    public boolean isDragging() {
        return dragging;
    }

    public boolean isSelected() {
        return selected;
    }

    public Vector2 getCenter() {
        return body.getCardCenter();
    }

}
