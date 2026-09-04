package com.avaricious.components.slot;

import com.avaricious.components.ButtonBoard;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/** Owns the persistent chest stack and the focused chest interaction. */
public final class ChestManager {

    public static final String DROP_CHANCE = "chestDropChance";
    public static final int BASE_DROP_CHANCE_PERCENT = 15;
    public static final int DROP_CHANCE_STEP = 5;
    public static final int MAX_DROP_CHANCE_PERCENT = 100;

    private static ChestManager instance;

    public static ChestManager I() {
        return instance == null ? instance = new ChestManager() : instance;
    }

    /** One roll per completed slot-machine spin. */
    private static final float STACK_X_OFFSET = 0.15f;
    private static final float STACK_BUTTON_GAP = 0.25f;
    private static final float CHEST_SIZE = 0.96f;

    private final List<ChestCollectible> chests = new ArrayList<>();
    private final PropertyChangeSupport changeSupport =
        new PropertyChangeSupport(this);
    private ChestCollectible focusedChest;
    private int dropChancePercent = BASE_DROP_CHANCE_PERCENT;
    private float backdropAmount;
    private boolean spaceCaptured;

    private ChestManager() {
    }

    public void rollForChest() {
        if (
            SeededRandomizer.get().nextFloat() * 100f >= dropChancePercent
        ) return;

        float machineHeight = SlotMachine.rowCount * SlotMachine.CELL_H
            + (SlotMachine.rowCount - 1) * SlotMachine.spacingY;
        float spawnX = GameplayLayout.SLOT_CENTER;
        float spawnY = SlotMachine.originY + machineHeight * 0.52f;
        ChestCollectible chest = new ChestCollectible(spawnX, spawnY);
        chests.add(chest);

        ParticleManager.I().create(
            spawnX,
            spawnY,
            ParticleType.COMP_CHIP,
            0.030f,
            68f,
            ZIndex.SYMBOL_HIT_PARTICLES
        );
        ParticleManager.I().create(
            spawnX,
            spawnY,
            ParticleType.WHITE,
            0.014f,
            32f,
            ZIndex.SLOT_MACHINE_FOREGROUND
        );
    }

    public void update(float delta) {
        layoutStack();

        for (ChestCollectible chest : chests) {
            chest.update(delta);
        }

        for (int i = chests.size() - 1; i >= 0; i--) {
            if (chests.get(i).isFinished()) {
                if (focusedChest == chests.get(i)) focusedChest = null;
                chests.remove(i);
            }
        }

        backdropAmount = MathUtils.lerp(
            backdropAmount,
            focusedChest == null ? 0f : 1f,
            Math.min(1f, delta * 8f)
        );
    }

    /**
     * Returns true while the chest modal owns input, or when this press
     * selected a chest from the stack.
     */
    public boolean handleInput(
        Vector2 mouse,
        boolean touching,
        boolean wasTouching
    ) {
        boolean justPressed = touching && !wasTouching;
        boolean spacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        boolean spaceJustPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean consumeCapturedSpace = spaceCaptured;

        if (!spacePressed) spaceCaptured = false;

        if (focusedChest != null) {
            for (ChestCollectible chest : chests) chest.setHovered(false);

            if (spaceJustPressed) spaceCaptured = true;

            if (
                (justPressed || spaceJustPressed) &&
                    focusedChest.fastForwardActiveAnimation()
            ) {
                return true;
            }

            if (
                (justPressed || spaceJustPressed) &&
                    focusedChest.getState() == ChestCollectible.State.FOCUSED &&
                    (spaceJustPressed || focusedChest.contains(mouse))
            ) {
                focusedChest.beginOpening();
            }
            return true;
        }

        ChestCollectible hoveredChest = null;
        ChestCollectible keyboardChest = null;
        for (int i = chests.size() - 1; i >= 0; i--) {
            ChestCollectible chest = chests.get(i);
            if (
                keyboardChest == null &&
                    chest.getState() == ChestCollectible.State.STACKED
            ) {
                keyboardChest = chest;
            }
            if (
                hoveredChest == null &&
                    chest.getState() == ChestCollectible.State.STACKED &&
                    chest.contains(mouse)
            ) {
                hoveredChest = chest;
            }
        }

        for (ChestCollectible chest : chests) {
            chest.setHovered(chest == hoveredChest);
        }

        if (justPressed && hoveredChest != null) {
            focusedChest = hoveredChest;
            focusedChest.beginFocus();
            return true;
        }

        if (spaceJustPressed && keyboardChest != null) {
            spaceCaptured = true;
            focusedChest = keyboardChest;
            focusedChest.beginFocus();
            return true;
        }

        return consumeCapturedSpace;
    }

    public void draw() {
        for (ChestCollectible chest : chests) {
            if (chest != focusedChest) chest.draw(false);
        }

        if (backdropAmount > 0.01f) {
            float left = GameContext.I().viewport.getCamera().position.x
                - GameContext.I().viewport.getWorldWidth() / 2f;
            float bottom = GameContext.I().viewport.getCamera().position.y
                - GameContext.I().viewport.getWorldHeight() / 2f;
            Pencil.I().addDrawing(new TextureDrawing(
                Assets.I().get(AssetKey.CHARCOAL_PIXEL),
                left - 1f,
                bottom - 1f,
                GameContext.I().viewport.getWorldWidth() + 2f,
                GameContext.I().viewport.getWorldHeight() + 2f,
                ZIndex.CHEST_MODAL_BACKDROP,
                new Color(0.03f, 0.025f, 0.02f, 0.66f * backdropAmount)
            ));
        }

        if (focusedChest != null) focusedChest.draw(true);
    }

    public void reset() {
        chests.clear();
        focusedChest = null;
        backdropAmount = 0f;
        spaceCaptured = false;
    }

    public int getDropChancePercent() {
        return dropChancePercent;
    }

    public int getNextDropChancePercent() {
        return getNextDropChancePercent(DROP_CHANCE_STEP);
    }

    public int getNextDropChancePercent(int amount) {
        return Math.min(
            MAX_DROP_CHANCE_PERCENT,
            dropChancePercent + Math.max(0, amount)
        );
    }

    public void increaseDropChance() {
        increaseDropChance(DROP_CHANCE_STEP);
    }

    public void increaseDropChance(int amount) {
        int oldChance = dropChancePercent;
        dropChancePercent = getNextDropChancePercent(amount);
        changeSupport.firePropertyChange(
            DROP_CHANCE,
            oldChance,
            dropChancePercent
        );
    }

    public void addDropChanceChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    private void layoutStack() {
        Rectangle button = ButtonBoard.I().getSpinButtonBounds();
        float baseX = button == null
            ? GameplayLayout.SLOT_CENTER + 0.65f
            : button.x - CHEST_SIZE * 0.58f - STACK_BUTTON_GAP;
        float baseY = button == null
            ? 1.02f
            : button.y + button.height / 2f;

        int stackIndex = 0;
        for (ChestCollectible chest : chests) {
            if (chest == focusedChest || chest.isFinished()) continue;
            chest.setStackTarget(
                baseX - stackIndex * STACK_X_OFFSET,
                baseY
            );
            stackIndex++;
        }
    }
}
