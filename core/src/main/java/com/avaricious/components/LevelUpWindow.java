package com.avaricious.components;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.automations.Luck;
import com.avaricious.components.automations.SlotMachineSpeed;
import com.avaricious.components.slot.ChestManager;
import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.components.texts.*;
import com.avaricious.items.upgrades.UpgradeRarity;
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
    private static final float SELECTION_DURATION = 0.72f;
    private static final Color GOLD = new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color MUTED = new Color(0.63f, 0.71f, 0.76f, 1f);

    private final TextureRegion whitePixel =
        Assets.I().get(AssetKey.WHITE_PIXEL);

    private final GeneratedFabledText title = new GeneratedFabledText(
        "ROUND REWARD",
        21f,
        0.04f,
        0.20f,
        ZIndex.SHOP_CARD,
        true
    );
    private final GeneratedFabledText prompt = new GeneratedFabledText(
        "CHOOSE AN UPGRADE",
        39f,
        0.025f,
        0.13f,
        ZIndex.SHOP_CARD,
        true
    );

    private final List<LevelUpChoice> choices =
        new ArrayList<>();
    private final GeneratedFabledText controls = label("CLICK A CARD OR PRESS ITS NUMBER", 62f, MUTED);
    private float windowOpacity = 1f;
    private Runnable onRewardSelected;

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
    private final Color selectionColor = new Color(Color.WHITE);

    private static final Color[] CELEBRATION_COLORS = {
        GOLD,
        new Color(0.48f, 0.83f, 0.86f, 1f),
        new Color(1f, 0.94f, 0.75f, 1f)
    };

    /*
     * The title gets shifted during selection, so remember
     * where it originally lived.
     */
    private float titleStartY = Float.NaN;

    public LevelUpWindow() {
        prompt.fitWithinWidth(4.25f);
        prompt.setAbsoluteX(
            WORLD_WIDTH / 2f -
                Math.min(prompt.getNaturalWidth(), 4.25f) / 2f
        );
        prompt.setY(6.67f);
        prompt.setFloatEffects(0f, 0f);
        prompt.getWords().forEach(word ->
            word.setColor(MUTED)
        );
        title.fitWithinWidth(5.5f);
        title.setAbsoluteX((WORLD_WIDTH - title.getRenderedWidth()) / 2f);
        title.setY(7.31f);
        title.getWords().forEach(word -> word.setColor(GOLD));
    }

    private static GeneratedFabledText label(String text, float size, Color color) {
        GeneratedFabledText result = new GeneratedFabledText(
            text, size, 0.018f, 0.10f, ZIndex.SHOP_CARD
        );
        result.setFloatEffects(0f, 0f);
        result.getWords().forEach(word -> word.setColor(color));
        return result;
    }

    public void show() {
        show(null);
    }

    public void show(Runnable onRewardSelected) {
        if (showing) return;

        this.onRewardSelected = onRewardSelected;
        showing = true;
        selecting = false;
        selectedChoice = null;

        pressReleased = false;

        selectionTimer = 0f;
        screenFlash = 0f;
        revealTimer = 0f;
        windowOpacity = 1f;
        selectionColor.set(Color.WHITE);

        burstParticles.clear();

        /*
         * Capture the original position only once.
         */
        if (Float.isNaN(titleStartY)) {
            titleStartY = title.getY();
        }

        title.setY(titleStartY);
        title.setFloatEffects(0.018f, 0.85f);

        AudioManager.I().stopPayout();
        AudioManager.I().playLevelUp();

        /*
         * Freeze the result scheduler.
         */
        if (SlotMachineResultRunner.I().getScheduler() != null) {
            SlotMachineResultRunner.I()
                .getScheduler()
                .pause();
        }

        generateChoices();
        if (choices.isEmpty()) {
            completeReward();
            return;
        }
        spawnBurst(WORLD_WIDTH / 2f, 7.55f, 18, 0.58f);
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
        onRewardSelected = null;

        /*
         * Restore title position so repeated level-ups
         * always start in the same place.
         */
        if (!Float.isNaN(titleStartY)) {
            title.setY(titleStartY);
        }

        if (SlotMachineResultRunner.I().getScheduler() != null) {
            SlotMachineResultRunner.I()
                .getScheduler()
                .resume();
        }
    }

    private void generateChoices() {
        choices.clear();

        List<LevelUpChoice> possibleChoices =
            new ArrayList<>();

        Automations stats = Automations.I();

        SlotMachineSpeed speed = stats.getSlotMachineSpeed();
        if (!speed.isMaxSpeedReached()) {
            possibleChoices.add(createSpeedChoice(speed));
        }

        if (
            CollectibleValues.I().getExtraCollectibleSpawnChance() <
                CollectibleValues.MAX_EXTRA_COLLECTIBLE_SPAWN_CHANCE
        ) {
            possibleChoices.add(createCollectibleChoice());
        }

        if (
            CollectibleValues.I().getExtraSpadeSpawnChance() <
                CollectibleValues.MAX_EXTRA_SPADE_SPAWN_CHANCE
        ) {
            UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
            int increaseAmount = rarity.scaleLevelUpAmount(
                CollectibleValues.EXTRA_SPADE_CHANCE_STEP
            );
            TextureRegion spade = Assets.I().get(AssetKey.SPADE);
            possibleChoices.add(
                new LevelUpChoice(
                    new ExtraSpadeChanceText(),
                    new ExtraSpadeChanceDescription(increaseAmount),
                    () -> CollectibleValues.I()
                        .increaseExtraSpadeSpawnChance(increaseAmount),
                    spade,
                    spade,
                    spade,
                    rarity
                )
            );
        }

        if (
            CriticalHitValues.I().getCriticalHitChance() <
                CriticalHitValues.MAX_CRITICAL_HIT_CHANCE
        ) {
            UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
            int increaseAmount = rarity.scaleLevelUpAmount(
                CriticalHitValues.CRITICAL_HIT_CHANCE_STEP
            );
            TextureRegion criticalHit = Assets.I().get(AssetKey.CRITICAL_HIT);
            TextureRegion criticalHitShadow =
                Assets.I().get(AssetKey.CRITICAL_HIT_SHADOW);
            possibleChoices.add(
                new LevelUpChoice(
                    new CriticalHitChanceText(),
                    new CriticalHitChanceDescription(increaseAmount),
                    () -> CriticalHitValues.I()
                        .increaseCriticalHitChance(increaseAmount),
                    criticalHit,
                    criticalHitShadow,
                    criticalHit,
                    rarity
                )
            );
        }

        if (
            DoubleHitValues.I().getDoubleHitChance() <
                DoubleHitValues.MAX_DOUBLE_HIT_CHANCE
        ) {
            possibleChoices.add(createDoubleHitChoice());
        }

        if (
            CollectibleValues.I().getCashChipSpawnChance() <
                CollectibleValues.MAX_CASH_CHIP_SPAWN_CHANCE
        ) {
            possibleChoices.add(createCashChipChoice());
        }

        if (
            ChestManager.I().getDropChancePercent() <
                ChestManager.MAX_DROP_CHANCE_PERCENT
        ) {
            possibleChoices.add(createChestDropChoice());
        }

        Luck luck = stats.getLuck();
        if (!luck.isMaxBonusReached()) {
            possibleChoices.add(createLuckChoice(luck));
        }

        if (
            CriticalHitValues.I().getCriticalHitChance() > 0 &&
                !CriticalHitValues.I().isCriticalDamageMaxed()
        ) {
            UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
            int increaseAmount = rarity.scaleLevelUpAmount(
                CriticalHitValues.CRITICAL_DAMAGE_STEP
            );
            TextureRegion multiplier = Assets.I().get(AssetKey.MULTI);
            TextureRegion multiplierShadow = Assets.I().get(AssetKey.MULTI_SHADOW);
            possibleChoices.add(
                new LevelUpChoice(
                    new CriticalDamageText(),
                    new CriticalDamageDescription(increaseAmount),
                    () -> CriticalHitValues.I()
                        .increaseCriticalDamage(increaseAmount),
                    multiplier,
                    multiplierShadow,
                    multiplier,
                    rarity
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
                0.13f + i * 0.10f
            );
            choice.setShortcutNumber(i + 1);

            choices.add(choice);
        }

        updateChoiceBounds();
    }

    private LevelUpChoice createSpeedChoice(SlotMachineSpeed speed) {
        UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
        int upgradeCount = rarity.scaleLevelUpAmount(1);
        TextureRegion retrigger = Assets.I().get(AssetKey.RETRIGGER);
        return new LevelUpChoice(
            new SlotMachineSpeedText(),
            new SlotMachineSpeedDescriptionText(upgradeCount),
            () -> speed.increaseSpeed(upgradeCount),
            retrigger,
            Assets.I().get(AssetKey.RETRIGGER_SHADOW),
            retrigger,
            rarity
        );
    }

    private LevelUpChoice createCollectibleChoice() {
        UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
        int increaseAmount = rarity.scaleLevelUpAmount(
            CollectibleValues.EXTRA_COLLECTIBLE_CHANCE_STEP
        );
        TextureRegion retrigger = Assets.I().get(AssetKey.RETRIGGER);
        return new LevelUpChoice(
            new ExtraLemonCollectibleChanceText(),
            new ExtraCollectibleChanceDescription(increaseAmount),
            () -> CollectibleValues.I()
                .increaseExtraCollectibleSpawnChance(increaseAmount),
            retrigger,
            Assets.I().get(AssetKey.RETRIGGER_SHADOW),
            retrigger,
            rarity
        );
    }

    private LevelUpChoice createDoubleHitChoice() {
        UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
        int increaseAmount = rarity.scaleLevelUpAmount(
            DoubleHitValues.DOUBLE_HIT_CHANCE_STEP
        );
        TextureRegion retrigger = Assets.I().get(AssetKey.RETRIGGER);
        return new LevelUpChoice(
            createChoiceTitle("DOUBLE HIT CHANCE"),
            new DoubleHitChanceDescription(increaseAmount),
            () -> DoubleHitValues.I().increaseDoubleHitChance(increaseAmount),
            retrigger,
            Assets.I().get(AssetKey.RETRIGGER_SHADOW),
            retrigger,
            rarity
        );
    }

    private LevelUpChoice createCashChipChoice() {
        UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
        int increaseAmount = rarity.scaleLevelUpAmount(
            CollectibleValues.CASH_CHIP_CHANCE_STEP
        );
        TextureRegion cashChip = Assets.I().get(AssetKey.POKER_CHIP);
        return new LevelUpChoice(
            createChoiceTitle("CASH CHIP DROP"),
            new CashChipChanceDescription(increaseAmount),
            () -> CollectibleValues.I()
                .increaseCashChipSpawnChance(increaseAmount),
            cashChip,
            Assets.I().get(AssetKey.POKER_CHIP_SHADOW),
            cashChip,
            rarity
        );
    }

    private LevelUpChoice createChestDropChoice() {
        UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
        int increaseAmount = rarity.scaleLevelUpAmount(
            ChestManager.DROP_CHANCE_STEP
        );
        TextureRegion chest = Assets.I().get(AssetKey.CHEST_CLOSED);
        return new LevelUpChoice(
            createChoiceTitle("CHEST DROP CHANCE"),
            new ChestDropChanceDescriptionText(increaseAmount),
            () -> ChestManager.I().increaseDropChance(increaseAmount),
            chest,
            chest,
            chest,
            rarity
        );
    }

    private LevelUpChoice createLuckChoice(Luck luck) {
        UpgradeRarity rarity = UpgradeRarity.rollLevelUpRarity();
        int increaseAmount = rarity.scaleLevelUpAmount(
            Luck.BONUS_PER_UPGRADE
        );
        TextureRegion luckIcon = Assets.I().get(AssetKey.LUCK);
        return new LevelUpChoice(
            new LuckText(),
            new LuckDescriptionText(increaseAmount),
            () -> luck.increaseBonusPercent(increaseAmount),
            luckIcon,
            Assets.I().get(AssetKey.LUCK_SHADOW),
            luckIcon,
            rarity
        );
    }

    private GeneratedFabledText createChoiceTitle(String text) {
        return new GeneratedFabledText(
            text,
            22f,
            0.05f,
            0.22f,
            ZIndex.SHOP_CARD
        );
    }

    private void updateChoiceBounds() {
        float width = 3.72f;
        float height = 4.42f;

        float gap = 0.36f;

        float totalWidth =
            width * choices.size() +
                gap * Math.max(0, choices.size() - 1);

        float startX =
            (WORLD_WIDTH - totalWidth) / 2f;

        float y = 1.80f;

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
        if (selecting || !choice.isReady()) return;

        selecting = true;
        selectedChoice = choice;
        selectionColor.set(choice.getRarityColor());

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
            .addTrauma(
                0.25f +
                    choice.getRarityTier() * 0.025f
            );

        /*
         * Reward explosion around the lower symbol
         * portion of the selected card.
         */
        spawnBurst(choice.getCenterX(), choice.getIconCenterY(), 14, 0.58f, selectionColor);
    }

    private void spawnBurst(
        float x,
        float y,
        int particleCount,
        float power
    ) {
        spawnBurst(x, y, particleCount, power, null);
    }

    private void spawnBurst(
        float x,
        float y,
        int particleCount,
        float power,
        Color accentColor
    ) {
        for (int i = 0; i < particleCount; i++) {
            burstParticles.add(
                new BurstParticle(
                    x,
                    y,
                    power,
                    accentColor
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
                titleProgress * 0.06f
        );

        if (
            selectionTimer >=
                SELECTION_DURATION
        ) {
            completeReward();
        }
    }

    private void completeReward() {
        Runnable next = onRewardSelected;
        hide();
        if (next != null) next.run();
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
        if (!showing) return;
        updateBurstParticles(delta);
        windowOpacity = selecting
            ? 1f - smoothStep(MathUtils.clamp((selectionTimer - 0.57f) / 0.15f, 0f, 1f))
            : 1f;

        float reveal = smoothStep(MathUtils.clamp(revealTimer / 0.25f, 0f, 1f));
        drawBackdrop(reveal);
        if (screenFlash > 0f) {
            rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT,
                selectionColor, screenFlash * 0.06f, ZIndex.SHOP);
        }

        float titleReveal = smoothStep(MathUtils.clamp(revealTimer / 0.38f, 0f, 1f));
        title.setOpacity(titleReveal * windowOpacity);
        title.setAnimationScale(0.94f + 0.06f * titleReveal);
        title.setAbsoluteX((WORLD_WIDTH - title.getRenderedWidth()) / 2f);
        title.draw(delta);
        if (!selecting) {
            drawCentered(prompt, 6.67f, reveal, delta);
            drawCentered(controls, 0.82f, reveal * 0.66f, delta);
        }

        for (LevelUpChoice choice : choices) {
            choice.setWindowOpacity(windowOpacity);
            if (choice != selectedChoice) choice.draw(delta);
        }
        if (selectedChoice != null) selectedChoice.draw(delta);
        for (BurstParticle particle : burstParticles) particle.draw();
    }

    public boolean isShowing() {
        return showing;
    }

    private void drawBackdrop(float reveal) {
        rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT,
            new Color(0.018f, 0.030f, 0.041f, 1f), 0.92f * reveal, ZIndex.SHOP);
        rect(2.05f, 6.39f, 11.90f, 0.012f, GOLD, 0.16f * reveal, ZIndex.SHOP);
    }

    private void drawCentered(FabledText text, float y, float alpha, float delta) {
        text.setAbsoluteX((WORLD_WIDTH - text.getRenderedWidth()) / 2f);
        text.setY(y);
        text.setOpacity(alpha * windowOpacity);
        text.draw(delta);
    }

    private void rect(float x, float y, float width, float height, Color color, float alpha, ZIndex layer) {
        Pencil.I().addDrawing(new TextureDrawing(whitePixel, x, y, width, height, layer,
            new Color(color.r, color.g, color.b, alpha * windowOpacity)));
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
            float power,
            Color accentColor
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

            color = accentColor == null
                ? new Color(
                    CELEBRATION_COLORS[
                        MathUtils.random(CELEBRATION_COLORS.length - 1)
                    ]
                )
                : new Color(accentColor).lerp(
                    Color.WHITE,
                    MathUtils.random(0.12f, 0.58f)
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
                    ZIndex.SHOP_CARD_TOUCHING,
                    new Color(
                        color.r,
                        color.g,
                        color.b,
                        alpha * windowOpacity
                    )
                )
            );
        }

        public boolean finished() {
            return age >= lifetime;
        }
    }
}
