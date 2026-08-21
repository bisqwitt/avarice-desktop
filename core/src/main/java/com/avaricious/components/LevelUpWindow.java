package com.avaricious.components;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.slot.pattern.PatternUnlocks;
import com.avaricious.components.slot.pattern.UnlockablePattern;
import com.avaricious.components.texts.*;
import com.avaricious.utility.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelUpWindow {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;

    /*
     * How long the entire selection payoff lasts before
     * normal gameplay resumes.
     */
    private static final float SELECTION_DURATION = 0.58f;

    private final TextureRegion background =
        Assets.I().get(AssetKey.CHARCOAL_PIXEL);

    private final TextureRegion whitePixel =
        Assets.I().get(AssetKey.WHITE_PIXEL);

    private final LevelUpText title = new LevelUpText();

    private final List<LevelUpChoice> choices =
        new ArrayList<>();

    /*
     * These are completely separate from ParticleManager.
     *
     * That is intentional:
     *
     * gameplay particles are paused while the level-up
     * window is active, but these particles should still
     * animate.
     */
    private final List<BurstParticle> burstParticles =
        new ArrayList<>();

    private boolean showing = false;

    /*
     * Prevents the click that caused the level-up from
     * immediately selecting a reward.
     */
    private boolean pressReleased = false;

    /*
     * Once a choice has been clicked, input is locked
     * while the reward animation finishes.
     */
    private boolean selecting = false;

    private LevelUpChoice selectedChoice;

    private float selectionTimer = 0f;

    /*
     * Full-screen micro flash.
     */
    private float screenFlash = 0f;
    private float revealTimer = 0f;

    private static final Color[] CELEBRATION_COLORS = {
        new Color(1f, 0.32f, 0.72f, 1f),
        new Color(0.42f, 0.82f, 1f, 1f),
        new Color(0.72f, 0.46f, 1f, 1f),
        new Color(1f, 0.82f, 0.24f, 1f),
        new Color(0.50f, 1f, 0.66f, 1f),
        Color.WHITE
    };

    /*
     * The title gets shifted during selection, so remember
     * where it originally lived.
     */
    private float titleStartY = Float.NaN;

    public void show() {
        if (showing) return;

        showing = true;
        selecting = false;
        selectedChoice = null;

        pressReleased = false;

        selectionTimer = 0f;
        screenFlash = 0f;
        revealTimer = 0f;

        burstParticles.clear();

        /*
         * Capture the original position only once.
         */
        if (Float.isNaN(titleStartY)) {
            titleStartY = title.getY();
        }

        title.setY(titleStartY);
        title.setFloatEffects(0.10f, 1.15f);

        AudioManager.I().stopPayout();
        AudioManager.I().playLevelUp();

        /*
         * Freeze the result scheduler.
         */
        SlotMachineResultRunner.I()
            .getScheduler()
            .pause();

        generateChoices();
        spawnBurst(WORLD_WIDTH / 2f, 7.05f, 52, 1.12f);
    }

    public void hide() {
        if (!showing) return;

        showing = false;
        selecting = false;

        selectedChoice = null;

        choices.clear();
        burstParticles.clear();

        selectionTimer = 0f;
        screenFlash = 0f;
        revealTimer = 0f;

        /*
         * Restore title position so repeated level-ups
         * always start in the same place.
         */
        if (!Float.isNaN(titleStartY)) {
            title.setY(titleStartY);
        }

        SlotMachineResultRunner.I()
            .getScheduler()
            .resume();
    }

    private void generateChoices() {
        choices.clear();

        List<LevelUpChoice> possibleChoices =
            new ArrayList<>();

        Symbol valueSymbol = randomSymbol();
        possibleChoices.add(createValueChoice(valueSymbol));

        Symbol collectibleSymbol = randomSymbol();
        possibleChoices.add(createCollectibleChoice(collectibleSymbol));

        if (
            CollectibleValues.I().getExtraSpadeSpawnChance() <
                CollectibleValues.MAX_EXTRA_SPADE_SPAWN_CHANCE
        ) {
            TextureRegion spade = Assets.I().get(AssetKey.SPADE);
            possibleChoices.add(
                new LevelUpChoice(
                    new ExtraSpadeChanceText(),
                    new ExtraSpadeChanceDescription(),
                    () -> CollectibleValues.I().increaseExtraSpadeSpawnChance(),
                    spade,
                    spade,
                    spade
                )
            );
        }

        if (!Automations.I().getLuck().isMaxBonusReached()) {
            TextureRegion luck = Assets.I().get(AssetKey.LUCK);
            TextureRegion luckShadow = Assets.I().get(AssetKey.LUCK_SHADOW);
            possibleChoices.add(
                new LevelUpChoice(
                    new LuckText(),
                    new LuckDescriptionText(),
                    () -> Automations.I().getLuck().upgrade(),
                    luck,
                    luckShadow,
                    luck
                )
            );
        }

        if (!Automations.I().getSlotMachineSpeed().isMaxSpeedReached()) {
            TextureRegion speed = Assets.I().get(AssetKey.RETRIGGER);
            TextureRegion speedShadow = Assets.I().get(AssetKey.RETRIGGER_SHADOW);
            possibleChoices.add(
                new LevelUpChoice(
                    new SlotMachineSpeedText(),
                    new SlotMachineSpeedDescriptionText(),
                    () -> Automations.I().getSlotMachineSpeed().upgrade(),
                    speed,
                    speedShadow,
                    speed
                )
            );
        }

        List<UnlockablePattern> lockedPatterns =
            PatternUnlocks.I().getLockedPatterns();
        if (!lockedPatterns.isEmpty()) {
            UnlockablePattern pattern = lockedPatterns.get(
                SeededRandomizer.nextInt(0, lockedPatterns.size() - 1)
            );
            possibleChoices.add(
                new LevelUpChoice(
                    new GeneratedFabledText(
                        pattern.displayName(),
                        22f,
                        0.05f,
                        0.22f,
                        ZIndex.SHOP_CARD
                    ),
                    new GeneratedFabledText(
                        "UNLOCK SHAPE",
                        27f,
                        0.04f,
                        0.20f,
                        ZIndex.SHOP_CARD
                    ),
                    () -> PatternUnlocks.I().unlock(pattern),
                    pattern.mask()
                )
            );
        }

        if (
            CriticalHitValues.I().getCriticalHitChance() <
                CriticalHitValues.MAX_CRITICAL_HIT_CHANCE
        ) {
            TextureRegion criticalHit = Assets.I().get(AssetKey.CRITICAL_HIT);
            TextureRegion criticalHitShadow =
                Assets.I().get(AssetKey.CRITICAL_HIT_SHADOW);
            possibleChoices.add(
                new LevelUpChoice(
                    new CriticalHitChanceText(),
                    new CriticalHitChanceDescription(),
                    () -> CriticalHitValues.I().increaseCriticalHitChance(),
                    criticalHit,
                    criticalHitShadow,
                    criticalHit
                )
            );
        }

        if (
            CriticalHitValues.I().getCriticalHitChance() > 0 &&
                !CriticalHitValues.I().isCriticalDamageMaxed()
        ) {
            TextureRegion multiplier = Assets.I().get(AssetKey.MULTI);
            TextureRegion multiplierShadow = Assets.I().get(AssetKey.MULTI_SHADOW);
            possibleChoices.add(
                new LevelUpChoice(
                    new CriticalDamageText(),
                    new CriticalDamageDescription(),
                    () -> CriticalHitValues.I().increaseCriticalDamage(),
                    multiplier,
                    multiplierShadow,
                    multiplier
                )
            );
        }

        Collections.shuffle(possibleChoices, SeededRandomizer.get());

        int amount =
            Math.min(3, possibleChoices.size());

        for (int i = 0; i < amount; i++) {
            LevelUpChoice choice =
                possibleChoices.get(i);

            /*
             * LEFT
             * CENTER
             * RIGHT
             *
             * pop in one after another.
             */
            choice.setEntranceDelay(
                i * 0.075f
            );

            choices.add(choice);
        }

        updateChoiceBounds();
    }

    private Symbol randomSymbol() {
        Symbol[] symbols = Symbol.values();
        return symbols[SeededRandomizer.nextInt(0, symbols.length - 1)];
    }

    private LevelUpChoice createValueChoice(Symbol symbol) {
        return new LevelUpChoice(
            symbol,
            createValueTitle(symbol),
            new SymbolValueDescription(symbol),
            () -> SymbolValues.I().increaseValue(symbol)
        );
    }

    private FabledText createValueTitle(Symbol symbol) {
        switch (symbol) {
            case LEMON: return new LemonValueText();
            case CHERRY: return new CherryValueText();
            case CLOVER: return new CloverValueText();
            case BELL: return new BellValueText();
            case IRON: return new IronValueText();
            case DIAMOND: return new DiamondValueText();
            case SEVEN: return new SevenValueText();
            default: throw new IllegalArgumentException("Unsupported symbol: " + symbol);
        }
    }

    private LevelUpChoice createCollectibleChoice(Symbol symbol) {
        return new LevelUpChoice(
            symbol,
            new ExtraLemonCollectibleChanceText(),
            new ExtraCollectibleChanceDescription(symbol),
            () -> SymbolValues.I().increaseExtraCollectibleSpawnChance(symbol)
        );
    }

    private void updateChoiceBounds() {
        float width = 4f;
        float height = 4.5f;

        float gap = 0.5f;

        float totalWidth =
            width * choices.size() +
                gap * Math.max(0, choices.size() - 1);

        float startX =
            (WORLD_WIDTH - totalWidth) / 2f;

        float y = 2f;

        for (int i = 0; i < choices.size(); i++) {
            choices.get(i).setBounds(
                new Rectangle(
                    startX +
                        i * (width + gap),
                    y,
                    width,
                    height
                )
            );
        }
    }

    public void handleInput(
        Vector2 mouse,
        boolean pressed,
        boolean wasPressed
    ) {
        if (!showing) return;

        /*
         * Hover is still handled even when there is
         * currently no mouse click.
         */
        LevelUpChoice hoveredChoice = null;

        if (!selecting) {
            for (LevelUpChoice choice : choices) {
                if (choice.contains(mouse)) {
                    hoveredChoice = choice;
                    break;
                }
            }
        }

        for (LevelUpChoice choice : choices) {
            boolean hovered =
                choice == hoveredChoice;

            choice.setHovered(hovered);
            choice.setLightened(
                hoveredChoice != null &&
                    !hovered
            );
        }

        /*
         * Once selected, no further UI input is allowed.
         */
        if (selecting) {
            return;
        }

        /*
         * Wait until the input that opened the window
         * has been released.
         */
        if (!pressReleased) {
            if (!pressed) {
                pressReleased = true;
            } else {
                return;
            }
        }

        /*
         * Keyboard selection.
         *
         * 1 = left
         * 2 = middle
         * 3 = right
         */
        if (
            choices.size() > 0 &&
                (
                    Gdx.input.isKeyJustPressed(
                        Input.Keys.NUM_1
                    ) ||
                        Gdx.input.isKeyJustPressed(
                            Input.Keys.NUMPAD_1
                        )
                )
        ) {
            beginSelection(
                choices.get(0)
            );
            return;
        }

        if (
            choices.size() > 1 &&
                (
                    Gdx.input.isKeyJustPressed(
                        Input.Keys.NUM_2
                    ) ||
                        Gdx.input.isKeyJustPressed(
                            Input.Keys.NUMPAD_2
                        )
                )
        ) {
            beginSelection(
                choices.get(1)
            );
            return;
        }

        if (
            choices.size() > 2 &&
                (
                    Gdx.input.isKeyJustPressed(
                        Input.Keys.NUM_3
                    ) ||
                        Gdx.input.isKeyJustPressed(
                            Input.Keys.NUMPAD_3
                        )
                )
        ) {
            beginSelection(
                choices.get(2)
            );
            return;
        }

        /*
         * Only react to the beginning of a click.
         *
         * This prevents repeatedly triggering while
         * the button is held down.
         */
        if (!pressed || wasPressed) {
            return;
        }

        for (LevelUpChoice choice : choices) {
            if (choice.contains(mouse)) {
                beginSelection(choice);
                return;
            }
        }
    }

    private void beginSelection(
        LevelUpChoice choice
    ) {
        if (selecting) return;

        selecting = true;
        selectedChoice = choice;

        selectionTimer = 0f;

        float targetX =
            choice.getCenterX();

        /*
         * Apply the gameplay upgrade immediately.
         *
         * The game itself still stays paused until the
         * animation has finished.
         */
        choice.select();

        /*
         * Selected card gets the reward punch.
         */
        choice.beginSelectedAnimation();

        /*
         * All other cards collapse into it.
         */
        for (LevelUpChoice other : choices) {
            if (other == choice) continue;

            other.beginDismissAnimation(
                targetX
            );
        }

        /*
         * Fast white background hit.
         */
        screenFlash = 1f;

        /*
         * Stronger than a normal symbol hit but still
         * safely below maximum trauma.
         */
        ScreenShake.I()
            .addTrauma(0.28f);

        /*
         * Reward explosion around the lower symbol
         * portion of the selected card.
         */
        spawnBurst(
            choice.getCenterX(),
            choice.getCenterY() - 1f,
            64,
            1.25f
        );
    }

    private void spawnBurst(
        float x,
        float y,
        int particleCount,
        float power
    ) {
        for (int i = 0; i < particleCount; i++) {
            burstParticles.add(
                new BurstParticle(
                    x,
                    y,
                    power
                )
            );
        }
    }

    private void updateSelection(
        float delta
    ) {
        if (!selecting) return;

        selectionTimer += delta;

        /*
         * Short background flash.
         */
        screenFlash -= delta * 8f;

        if (screenFlash < 0f) {
            screenFlash = 0f;
        }

        /*
         * Title gets pushed upwards as the reward
         * claims the visual focus.
         */
        float titleProgress =
            MathUtils.clamp(
                selectionTimer / 0.32f,
                0f,
                1f
            );

        titleProgress =
            smoothStep(titleProgress);

        title.setY(
            titleStartY +
                titleProgress * 0.38f
        );

        if (
            selectionTimer >=
                SELECTION_DURATION
        ) {
            hide();
        }
    }

    private void updateBurstParticles(
        float delta
    ) {
        for (
            int i =
            burstParticles.size() - 1;
            i >= 0;
            i--
        ) {
            BurstParticle particle =
                burstParticles.get(i);

            particle.update(delta);

            if (particle.finished()) {
                burstParticles.remove(i);
            }
        }
    }

    public void draw(float delta) {
        if (!showing) return;

        revealTimer += delta;

        updateSelection(delta);

        /*
         * updateSelection may hide the window.
         */
        if (!showing) return;

        updateBurstParticles(delta);

        float backgroundAlpha =
            selecting
                ? 0.31f
                : 0.25f;

        Pencil.I().addDrawing(
            new TextureDrawing(
                background,
                0f,
                0f,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                ZIndex.SHOP,
                new Color(
                    1f,
                    1f,
                    1f,
                    backgroundAlpha
                )
            )
        );

//        drawEnergyRays();

        /*
         * Very fast full-screen white hit.
         *
         * Keep this subtle. The symbol/card itself carries
         * most of the actual flash.
         */
        if (screenFlash > 0f) {
            Pencil.I().addDrawing(
                new TextureDrawing(
                    whitePixel,
                    0f,
                    0f,
                    WORLD_WIDTH,
                    WORLD_HEIGHT,
                    ZIndex.SHOP,
                    new Color(
                        1f,
                        1f,
                        1f,
                        screenFlash * 0.11f
                    )
                )
            );
        }

        title.draw(delta);

        for (LevelUpChoice choice : choices) {
            choice.draw(delta);
        }

        /*
         * Reward burst is drawn after the cards so it
         * visually sprays out over them.
         */
        for (
            BurstParticle particle :
            burstParticles
        ) {
            particle.draw();
        }
    }

    public boolean isShowing() {
        return showing;
    }

    private void drawEnergyRays() {
        float reveal = MathUtils.clamp(revealTimer / 0.42f, 0f, 1f);
        reveal = smoothStep(reveal);

        float centerX = WORLD_WIDTH / 2f;
        float centerY = selecting && selectedChoice != null
            ? MathUtils.lerp(5.3f, selectedChoice.getCenterY() - 0.8f,
                smoothStep(MathUtils.clamp(selectionTimer / 0.28f, 0f, 1f)))
            : 5.3f;

        for (int i = 0; i < 18; i++) {
            float angle = i * (360f / 18f) + revealTimer * 6f;
            float shimmer = (MathUtils.sin(revealTimer * 4f + i * 1.7f) + 1f) * 0.5f;
            float length = MathUtils.lerp(2.7f, 5.8f, shimmer) * reveal;
            float thickness = MathUtils.lerp(0.018f, 0.055f, shimmer);
            Color base = CELEBRATION_COLORS[i % CELEBRATION_COLORS.length];

            Pencil.I().addDrawing(
                new TextureDrawing(
                    whitePixel,
                    centerX - length / 2f,
                    centerY - thickness / 2f,
                    length,
                    thickness,
                    1f,
                    angle,
                    ZIndex.SHOP,
                    new Color(base.r, base.g, base.b,
                        reveal * (0.035f + shimmer * 0.055f))
                )
            );
        }
    }

    private static float smoothStep(
        float value
    ) {
        value =
            MathUtils.clamp(
                value,
                0f,
                1f
            );

        return value *
            value *
            (3f - 2f * value);
    }

    /*
     * -------------------------------------------------
     * LEVEL-UP ONLY PARTICLE
     * -------------------------------------------------
     *
     * Intentionally local to the LevelUpWindow.
     *
     * It continues updating even while the rest of the
     * game's particle systems are frozen.
     */
    private class BurstParticle {

        private float x;
        private float y;

        private float velocityX;
        private float velocityY;

        private final float lifetime;

        private float age = 0f;

        private final float startSize;

        private final float rotation;

        private final float rotationVelocity;

        private final Color color;

        public BurstParticle(
            float x,
            float y,
            float power
        ) {
            this.x = x;
            this.y = y;

            float angle =
                MathUtils.random(
                    0f,
                    MathUtils.PI2
                );

            float speed =
                MathUtils.random(
                    3.5f,
                    8.5f
                ) * power;

            velocityX =
                MathUtils.cos(angle) *
                    speed;

            velocityY =
                MathUtils.sin(angle) *
                    speed;

            lifetime =
                MathUtils.random(
                    0.28f,
                    0.52f
                );

            startSize =
                MathUtils.random(
                    0.035f,
                    0.105f
                );

            rotation =
                MathUtils.random(
                    0f,
                    360f
                );

            rotationVelocity =
                MathUtils.random(
                    -360f,
                    360f
                );

            color = new Color(
                CELEBRATION_COLORS[
                    MathUtils.random(CELEBRATION_COLORS.length - 1)
                ]
            );
        }

        public void update(float delta) {
            age += delta;

            /*
             * Small air resistance.
             */
            float drag =
                (float) Math.pow(
                    0.90f,
                    delta * 60f
                );

            velocityX *= drag;
            velocityY *= drag;

            /*
             * Tiny gravity makes the burst feel physical
             * instead of like a perfectly symmetric
             * digital firework.
             */
            velocityY -=
                2.2f * delta;

            x +=
                velocityX * delta;

            y +=
                velocityY * delta;
        }

        public void draw() {
            float progress =
                MathUtils.clamp(
                    age / lifetime,
                    0f,
                    1f
                );

            float alpha =
                1f - progress;

            alpha *= alpha;

            float size =
                startSize *
                    MathUtils.lerp(
                        1.25f,
                        0.2f,
                        progress
                    );

            Pencil.I().addDrawing(
                new TextureDrawing(
                    whitePixel,
                    x - size / 2f,
                    y - size / 2f,
                    size,
                    size,
                    1f,
                    rotation +
                        rotationVelocity * age,
                    ZIndex.SHOP,
                    new Color(
                        color.r,
                        color.g,
                        color.b,
                        alpha
                    )
                )
            );
        }

        public boolean finished() {
            return age >= lifetime;
        }
    }
}
