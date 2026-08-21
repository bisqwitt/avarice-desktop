package com.avaricious.components.slot;

import com.avaricious.audio.AudioManager;
import com.avaricious.components.ScreenShake;
import com.avaricious.effects.TextureEcho;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.components.slot.rework.ReelStripBuilder;
import com.avaricious.components.slot.rework.SpinResult;
import com.avaricious.components.slot.rework.SpinResultGenerator;
import com.avaricious.components.slot.rework.SpinResultManipulator;
import com.avaricious.components.slot.rework.SpinResultPolicy;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlotMachine {

    private static SlotMachine instance;

    public static SlotMachine I() {
        return instance == null ? instance = new SlotMachine() : instance;
    }

    // --- Layout ---
    public static final int colCount = 5;
    public static final int rowCount = 5;
    public static final float CELL_W = 1f;
    public static final float CELL_H = 1f;
    public static final float spacingX = 0.35f;
    public static final float spacingY = 0.15f;

    public static final float originX = 5f;
    public static final float originY = 2f;

    private float reelStartStagger = 0.1f;
    private float reelStopStagger = 0.5f;
    private float spinHoldDuration = 1f;
    private float emptySpinSweepTimeScale = 1f;
    private boolean instantSpin = false;

    private final List<Reel> reels = new ArrayList<>();
    private final DragableBody[][] grid = new DragableBody[colCount][rowCount];
    private ZIndex zIndex = ZIndex.SLOT_MACHINE;
    private final GlyphLayout bossDescription = new GlyphLayout();

    private boolean runningResults = false;
    private float alpha = 1f;
    private float desiredAlpha = 1f;

    private Runnable onLastReelFinished;
    private int spinningReels = 0;
    private boolean stale = true;

    private final SpinResultGenerator spinResultGenerator = new SpinResultGenerator();
    private final SpinResultPolicy spinResultPolicy = new SpinResultPolicy();
    private final ReelStripBuilder reelStripBuilder = new ReelStripBuilder();
    private final List<SpinResultManipulator> spinResultManipulators = new ArrayList<>();
    private SpinResult currentSpinResult;
    private SpinResult nextSpinResult;

    /*
     * A short white silhouette on each reel stop turns the mechanical
     * settle into a readable five-beat reward cadence.
     */
    private final float[] reelStopFlash = new float[colCount];
    private final float[] reelStartFlash = new float[colCount];
    private final float[][] symbolWhiteFlash = new float[colCount][rowCount];

    private static final float EMPTY_SPIN_SWEEP_STEP_DELAY = 0.017f;
    private static final float EMPTY_SPIN_SWEEP_COMPLETION_DELAY = 0.20f;

    private boolean shiftingSymbol = false;
    private DragableBody draggingBody = null;
    private Vector2 draggingBodyGridPos = null;
    private Vector2 draggingNeighbourGridPos = null;
    private Vector2 draggingBodyTouchdownLocation = null;
    private DragDirection dragDirection = DragDirection.NONE;
    private final float dragLockThreshold = 0.1f;
    private final float maxDraggingDistance = 1.4f + 0.25f;

    private SlotMachine() {
        // build visual cells
        for (int c = 0; c < colCount; c++) {
            for (int r = 0; r < rowCount; r++) {
                if (r == 0) grid[c][r] = new DragableBody(new Rectangle(
                    originX + c * (CELL_W + spacingX),
                    originY + 4 * (CELL_H + spacingY),
                    CELL_W, CELL_H
                ));
                if (r == 1) grid[c][r] = new DragableBody(new Rectangle(
                    originX + c * (CELL_W + spacingX),
                    originY + 4 * (CELL_H + spacingY),
                    CELL_W, CELL_H
                ));
                if (r == 2) grid[c][r] = new DragableBody(new Rectangle(
                    originX + c * (CELL_W + spacingX),
                    originY + 4 * (CELL_H + spacingY),
                    CELL_W, CELL_H
                ));
                if (r == 3) grid[c][r] = new DragableBody(new Rectangle(
                    originX + c * (CELL_W + spacingX),
                    originY + 4 * (CELL_H + spacingY),
                    CELL_W, CELL_H
                ));
                if (r == 4) grid[c][r] = new DragableBody(new Rectangle(
                    originX + c * (CELL_W + spacingX),
                    originY + 4 * (CELL_H + spacingY),
                    CELL_W, CELL_H
                ));
            }
        }

        for (int c = 0; c < colCount; c++) {
            final int reelIndex = c;
            reels.add(new Reel(rowCount, () -> {
                verifyReelLanding(reelIndex);
                onReelStopped(reelIndex);
                spinningReels--;
                if (spinningReels == 0 && onLastReelFinished != null) onLastReelFinished.run();
            }));
        }
        buildStrip();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != null) {
                    grid[i][j].idleSwayEffect.setStrength(3f, 0.8f);
                }
            }
        }

        Seq.of(reels).forEach(reel -> reel.setSpeed(16));
    }

    public void handleInput(Vector2 mouse, boolean touching, boolean wasTouching, float delta) {
        if (shiftingSymbol) {
            if (touching && !wasTouching) {
                for (int r = 0; r < grid.length; r++) {
                    for (int c = 0; c < grid[r].length; c++) {
                        DragableBody body = grid[r][c];
                        if (body.getBounds().contains(mouse)) {
                            body.targetScale = 1.15f;
                            body.beginDrag(mouse.x, mouse.y, 0);
                            draggingBody = body;
                            draggingBodyTouchdownLocation = new Vector2(mouse);
                            draggingBodyGridPos = new Vector2(r, c);
                        }
                    }
                }
            }

            if (touching && draggingBody != null) {
                float dx = mouse.x - draggingBodyTouchdownLocation.x;
                float dy = mouse.y - draggingBodyTouchdownLocation.y;

                if (dragDirection == DragDirection.NONE) {
                    if (Math.abs(dx) > dragLockThreshold || Math.abs(dy) > dragLockThreshold) {
                        if (Math.abs(dx) > Math.abs(dy)) {
                            dragDirection = DragDirection.HORIZONTAL;
                        } else {
                            dragDirection = DragDirection.VERTICAL;
                        }
                    }
                }

                if (dragDirection == DragDirection.HORIZONTAL) {
                    float clampedDx = MathUtils.clamp(dx, -maxDraggingDistance, maxDraggingDistance);
                    draggingBody.dragTo(draggingBodyTouchdownLocation.x + clampedDx, draggingBodyTouchdownLocation.y, 0);

                    draggingNeighbourGridPos = new Vector2(draggingBodyGridPos.x + (dx > 0 ? 1 : -1), draggingBodyGridPos.y);
                    DragableBody neighbour = grid[(int) draggingNeighbourGridPos.x][(int) draggingNeighbourGridPos.y];
                    Vector2 neighbourDragPos = new Vector2(neighbour.getBounds().x - clampedDx, neighbour.getBounds().y);
                    if (!neighbour.isDragging())
                        neighbour.beginDrag(neighbourDragPos.x, neighbourDragPos.y, 0);
                    else neighbour.dragTo(neighbourDragPos.x, neighbourDragPos.y, 0);
                } else if (dragDirection == DragDirection.VERTICAL) {
                    float clampedDy = MathUtils.clamp(dy, -maxDraggingDistance, maxDraggingDistance);
                    draggingBody.dragTo(draggingBodyTouchdownLocation.x, draggingBodyTouchdownLocation.y + clampedDy, 0);

                    draggingNeighbourGridPos = new Vector2(draggingBodyGridPos.x, draggingBodyGridPos.y + (dy > 0 ? -1 : 1));
                    DragableBody neighbour = grid[(int) draggingNeighbourGridPos.x][(int) draggingNeighbourGridPos.y];
                    Vector2 neighbourDragPos = new Vector2(neighbour.getBounds().x, neighbour.getBounds().y - clampedDy);
                    if (!neighbour.isDragging())
                        neighbour.beginDrag(neighbourDragPos.x, neighbourDragPos.y, 0);
                    else
                        neighbour.dragTo(neighbour.getBounds().x, neighbour.getBounds().y - clampedDy, 0);
                }
            }

            if (!touching && wasTouching) {
                shiftSymbol(draggingBodyGridPos.x, draggingBodyGridPos.y, draggingNeighbourGridPos.x, draggingNeighbourGridPos.y);

                draggingBody.targetScale = 1;
                draggingBody.endDrag(0);
                grid[(int) draggingNeighbourGridPos.x][(int) draggingNeighbourGridPos.y].endDrag(0);

                dragDirection = DragDirection.NONE;
                draggingBody = null;
                draggingBodyGridPos = null;
                draggingNeighbourGridPos = null;
            }
        }
    }

    public void update(float delta) {
        for (int c = 0; c < colCount; c++) {
            reels.get(c).update(delta);
            reelStopFlash[c] = Math.max(0f, reelStopFlash[c] - delta * 4.8f);
            reelStartFlash[c] = Math.max(0f, reelStartFlash[c] - delta * 6.5f);

            for (int row = 0; row < rowCount; row++) {
                symbolWhiteFlash[c][row] = Math.max(
                    0f,
                    symbolWhiteFlash[c][row] - delta * 5.8f
                );
            }
        }

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != null) {
                    grid[i][j].update(delta);
                }
            }
        }

        if (desiredAlpha != alpha) {
            float speed = 10f; // higher = faster convergence
            alpha = MathUtils.lerp(alpha, desiredAlpha, speed * delta);
        }
    }

    public void draw(float delta) {
        SpriteBatch batch = GameContext.I().batch;

        Rectangle area = getBounds(); // world-space
        area.setX(area.x - 0.3f);
        area.setY(area.y - 0.1f);
        area.setWidth(area.width + 0.3f);
        area.setHeight(area.height - 0.15f);

        Camera cam = GameContext.I().viewport.getCamera();
        cam.update();
        Pencil.I().startScissors(cam, batch.getTransformMatrix(), area);
        drawSymbols();
        Pencil.I().endScissors();

        TextureEcho.draw(delta);
    }

    public void drawSymbolsInPatternHit() {
        List<Vector2> symbolsInPatternHit = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j].isInPatternHit()) symbolsInPatternHit.add(new Vector2(i, j));
            }
        }
        Seq.of(symbolsInPatternHit).forEach(this::drawSymbol);
    }

    private List<Vector2> drawSymbols() {
        List<Vector2> symbolsInPatternHit = new ArrayList<>();
        Vector2 draggingSymbol = null;
        for (int c = 0; c < colCount; c++) {
            int drawFrom = -1;
            int drawTo = rowCount - 1;

            for (int k = drawFrom; k <= drawTo; k++) {
                Vector2 pos = new Vector2(c, k);
                if (isInGrid(pos) && grid[c][k].isInPatternHit()) {
                    symbolsInPatternHit.add(pos);
                    continue;
                }
                if (isInGrid(pos) && draggingBody == grid[c][k]) {
                    draggingSymbol = pos;
                    continue;
                }
                drawSymbol(pos);
            }
        }
        if (draggingSymbol != null) drawSymbol(draggingSymbol);
        return symbolsInPatternHit;
    }

    private void drawSymbol(Vector2 gridPos) {
        Reel reel = reels.get((int) gridPos.x);
        Symbol symbol = reel.symbolAtRow((int) gridPos.y);

        final float stepX = CELL_W + spacingX;
        float colX = originX + gridPos.x * stepX;

        boolean isInGrid = isInGrid(gridPos);

        final float stepY = (CELL_H + spacingY);
        final float topY = originY + (rowCount - 1) * stepY;
        float drawY = topY - (gridPos.y + reel.frac()) * stepY;

        float drawW = CELL_W;
        float drawH = CELL_H;
        float adjX = colX - (drawW - CELL_W) / 2f;
        float adjY = drawY - 0.08f - (drawH - CELL_H) / 2f;
        float alpha = this.alpha;

        Vector2 renderPos = new Vector2(adjX, adjY);
        if (isInGrid) {
            DragableBody body = grid[(int) gridPos.x][(int) gridPos.y];
            body.getPos().set(adjX, adjY);
            body.getRenderPos(renderPos);
            if (runningResults && !body.isInPatternHit()) alpha = 0.5f;
        }

        float scale = isInGrid ? grid[(int) gridPos.x][(int) gridPos.y].getScale() : 1f;
        float rotation = isInGrid ? grid[(int) gridPos.x][(int) gridPos.y].getRotation() : 0f;
        ZIndex finalZIndex = isInGrid && grid[(int) gridPos.x][(int) gridPos.y].isInPatternHit() ?
            ZIndex.SLOT_MACHINE_FOREGROUND : ZIndex.SLOT_MACHINE;

        Color shadowColor = Assets.I().shadowColor();
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(symbol.shadowKey()),
            renderPos.x, renderPos.y - 0.1f, drawW, drawH,
            scale, rotation, finalZIndex, new Color(shadowColor.r, shadowColor.g, shadowColor.b, Math.min(shadowColor.a, alpha))
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().getSymbol(symbol),
            renderPos.x, renderPos.y, drawW, drawH,
            scale, rotation, finalZIndex, new Color(1f, 1f, 1f, alpha)
        ));

        if (isInGrid) {
            int column = (int) gridPos.x;
            int row = (int) gridPos.y;
            float stopFlash = reelStopFlash[column];
            float startFlash = reelStartFlash[column];
            float symbolFlash = symbolWhiteFlash[column][row];
            float flash = Math.max(
                symbolFlash,
                Math.max(stopFlash, startFlash * 0.42f)
            );

            if (flash > 0f) {
                float flashScale = scale * (
                    1f + Math.max(stopFlash, symbolFlash) * 0.24f
                );
                float flashAlpha = Math.min(alpha, flash * flash * 0.88f);

                Pencil.I().addDrawing(new TextureDrawing(
                    Assets.I().get(symbol.whiteKey()),
                    renderPos.x, renderPos.y, drawW, drawH,
                    flashScale, rotation, finalZIndex,
                    new Color(1f, 1f, 1f, flashAlpha)
                ));
            }
        }
    }

    public void spin() {
        if (spinningReels > 0) return;

        resetResultPresentation();

        /*
         * Decide and finalize the outcome before scheduling a single frame
         * of reel movement. Animation is now only a presentation of data.
         */
        currentSpinResult = prepareSpinResult();
        buildStrip();

        spinningReels = colCount;
        stale = false;

        Arrays.fill(reelStopFlash, 0f);
        Arrays.fill(reelStartFlash, 0f);

        for (float[] columnFlashes : symbolWhiteFlash) {
            Arrays.fill(columnFlashes, 0f);
        }

        AudioManager.I().playSpinStart();
        ScreenShake.I().addTrauma(0.075f);

        if (instantSpin) {
            for (int c = 0; c < colCount; c++) {
                reels.get(c).landImmediately(currentSpinResult.column(c));
            }
            return;
        }

        for (int c = 0; c < colCount; c++) {
            final int col = c;
            float startDelay = c * reelStartStagger;
            float stopDelay = colCount * reelStartStagger + c * reelStopStagger;

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    onReelStarted(col);
                    reels.get(col).start(reels.get(col).getSpeed());
                }
            }, startDelay);

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    reels.get(col).stopOn(currentSpinResult.column(col));
                }
            }, spinHoldDuration + stopDelay);
        }
    }

    private void resetResultPresentation() {
        runningResults = false;

        for (Body[] column : grid) {
            for (Body body : column) {
                body.resetResultPresentation();
            }
        }
    }

    private SpinResult prepareSpinResult() {
        SpinResult queuedResult = nextSpinResult;
        SpinResult result = queuedResult == null
            ? spinResultPolicy.adjust(spinResultGenerator.generate(colCount, rowCount))
            : queuedResult.copy();

        result.requireDimensions(colCount, rowCount);

        /* A copy allows a manipulator to register/unregister itself safely. */
        for (SpinResultManipulator manipulator : new ArrayList<>(spinResultManipulators)) {
            manipulator.manipulate(result);
            result.requireDimensions(colCount, rowCount);
        }

        spinResultPolicy.record(result);
        if (queuedResult != null) nextSpinResult = null;

        /* Do not let a manipulator retain a reference to the live landing. */
        return result.copy();
    }

    private void verifyReelLanding(int column) {
        if (currentSpinResult == null) {
            throw new IllegalStateException("A reel stopped without a planned spin result");
        }

        Reel reel = reels.get(column);
        for (int row = 0; row < rowCount; row++) {
            Symbol expected = currentSpinResult.get(column, row);
            Symbol actual = reel.symbolAtRow(row);
            if (actual != expected) {
                throw new IllegalStateException(
                    "Reel " + column + " landed incorrectly at row " + row +
                        ": expected " + expected + ", got " + actual
                );
            }
        }
    }

    private void onReelStarted(int column) {
        reelStartFlash[column] = 1f;

        for (int row = 0; row < rowCount; row++) {
            grid[column][row].pulse(0.28f + column * 0.025f);
        }

        float x = originX + column * (CELL_W + spacingX);
        float y = originY + 2f * (CELL_H + spacingY);

        ParticleManager.I().create(
            x, y, ParticleType.WHITE, 0.012f, 12f,
            ZIndex.SYMBOL_HIT_PARTICLES
        );
    }

    private void onReelStopped(int column) {
        boolean finalReel = column == colCount - 1;
        reelStopFlash[column] = 1f;

        if (instantSpin) {
            for (int row = 0; row < rowCount; row++) {
                grid[column][row].pulse(0.38f);
            }

            if (finalReel) {
                for (int targetColumn = 0; targetColumn < colCount; targetColumn++) {
                    Body middleBody = grid[targetColumn][rowCount / 2];
                    ParticleManager.I().create(
                        middleBody.getPos().x,
                        middleBody.getPos().y,
                        ParticleType.WHITE,
                        0.014f,
                        16f,
                        ZIndex.SYMBOL_HIT_PARTICLES
                    );
                }
                ScreenShake.I().addTrauma(0.11f);
                AudioManager.I().playReelStop(column, true);
            }
            return;
        }

        for (int row = 0; row < rowCount; row++) {
            Body body = grid[column][row];
            body.pulse(0.56f + column * 0.07f);

            ParticleManager.I().create(
                body.getPos().x,
                body.getPos().y,
                finalReel ? ParticleType.RAINBOW : ParticleType.WHITE,
                finalReel ? 0.021f : 0.012f,
                finalReel ? 28f : 13f,
                ZIndex.SYMBOL_HIT_PARTICLES
            );
        }

        ScreenShake.I().addTrauma(0.055f + column * 0.018f);
        AudioManager.I().playReelStop(column, finalReel);
    }

    public void flashSymbol(Body target, float strength) {
        for (int column = 0; column < colCount; column++) {
            for (int row = 0; row < rowCount; row++) {
                if (grid[column][row] == target) {
                    symbolWhiteFlash[column][row] = Math.max(
                        symbolWhiteFlash[column][row],
                        MathUtils.clamp(strength, 0f, 1f)
                    );
                    return;
                }
            }
        }
    }

    /**
     * A soft consolation wave for an empty spin. The rows scan from
     * top to bottom, with a tiny alternating horizontal stagger so the
     * flash reads as motion rather than one full-screen blink.
     */
    public void playEmptySpinSweep(Runnable onComplete) {
        if (emptySpinSweepTimeScale <= 0f) {
            if (onComplete != null) onComplete.run();
            return;
        }
        playEmptySpinSweepStep(0, onComplete);
    }

    private void playEmptySpinSweepStep(
        int sweepStep,
        Runnable onComplete
    ) {
        int totalSteps = rowCount * colCount;

        if (sweepStep >= totalSteps) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if (onComplete != null) onComplete.run();
                }
            }, EMPTY_SPIN_SWEEP_COMPLETION_DELAY * emptySpinSweepTimeScale);
            return;
        }

        int targetRow = sweepStep / colCount;
        int stepInRow = sweepStep % colCount;
        int targetColumn = targetRow % 2 == 0
            ? stepInRow
            : colCount - 1 - stepInRow;

        symbolWhiteFlash[targetColumn][targetRow] = 0.72f;
        grid[targetColumn][targetRow].pulse(0.18f);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                playEmptySpinSweepStep(
                    sweepStep + 1,
                    onComplete
                );
            }
        }, EMPTY_SPIN_SWEEP_STEP_DELAY * emptySpinSweepTimeScale);
    }

    public void shiftSymbol() {
        shiftingSymbol = true;
        zIndex = ZIndex.HAND_UI_SELECTING_CARD_TO_DISCARD;
        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.HAND_UI_SELECTING_CARD_TO_DISCARD);
        for (Body[] row : grid) {
            for (Body body : row) {
                body.pulse();
            }
        }
    }

    private void shiftSymbol(float col, float row, float neighbourCol, float neighbourRow) {
        swapBetweenReels((int) col, (int) row, (int) neighbourCol, (int) neighbourRow);

        Pencil.I().toggleDarkenEverythingBehindLayer(ZIndex.HAND_UI_SELECTING_CARD_TO_DISCARD);
        zIndex = ZIndex.SLOT_MACHINE;
        shiftingSymbol = false;
        SlotMachineResultRunner.I().runResult(Seq.of(SlotMachineMatchFinder.I().findMatches())
            .filter(patternHit -> patternHit.getPositions()
                .contains(new Vector2(neighbourCol, neighbourRow))
                || patternHit.getPositions()
                .contains(new Vector2(col, row)))
            .toList());
    }

    /**
     * Swaps the symbols at two arbitrary grid positions (may span different reels).
     */
    private void swapBetweenReels(int colA, int rowA, int colB, int rowB) {
        Reel reelA = reels.get(colA);
        Reel reelB = reels.get(colB);
        Symbol symA = reelA.symbolAtRow(rowA);
        Symbol symB = reelB.symbolAtRow(rowB);
        reelA.setSymbolAtRow(rowA, symB);
        reelB.setSymbolAtRow(rowB, symA);
        currentSpinResult = new SpinResult(getSymbolMap());
    }

    public static Rectangle getBounds() {
        return new Rectangle(
            originX,
            originY,
            colCount * (CELL_W + spacingX),
            rowCount * (CELL_H + spacingY)
        );
    }

    public void buildStrip() {
        /* A weight upgrade during a spin takes effect on the next spin. */
        if (spinningReels > 0) return;

        List<Symbol> baseStrip = reelStripBuilder.buildBaseStrip();

        for (Reel reel : reels) {
            List<Symbol> reelStrip = reelStripBuilder.buildShuffledStrip(baseStrip);
            if (reel.stripSize() == 0) {
                reel.setStrip(reelStrip);
            } else {
                reel.setStripPreservingVisible(reelStrip);
            }
        }
    }

    private boolean isInGrid(Vector2 pos) {
        return pos.y >= 0 && pos.y < rowCount;
    }

    public void setAlpha(float value) {
        desiredAlpha = value;
    }

    public void setRunningResults(boolean runningResults) {
        this.runningResults = runningResults;
    }

    public void setStale(boolean stale) {
        this.stale = stale;
    }

    public Symbol[][] getSymbolMap() {
        Symbol[][] symbolMap = new Symbol[colCount][rowCount];
        for (int c = 0; c < reels.size(); c++) {
            for (int row = 0; row < rowCount; row++) {
                symbolMap[c][row] = reels.get(c).symbolAtRow(row);
            }
        }
        return symbolMap;
    }

    /**
     * Returns the immutable snapshot being presented by the current spin.
     */
    public SpinResult getCurrentSpinResult() {
        return currentSpinResult == null
            ? new SpinResult(getSymbolMap())
            : currentSpinResult.copy();
    }

    /**
     * Forces one exact result. Built-in pity and near-miss rules are skipped,
     * while registered manipulators still get their normal final pass.
     */
    public void setNextSpinResult(SpinResult result) {
        if (result == null) throw new IllegalArgumentException("Spin result cannot be null");
        result.requireDimensions(colCount, rowCount);
        nextSpinResult = result.copy();
    }

    public void setNextSpinResult(Symbol[][] symbols) {
        setNextSpinResult(new SpinResult(symbols));
    }

    public void addSpinResultManipulator(SpinResultManipulator manipulator) {
        if (manipulator == null) {
            throw new IllegalArgumentException("Spin-result manipulator cannot be null");
        }
        if (!spinResultManipulators.contains(manipulator)) {
            spinResultManipulators.add(manipulator);
        }
    }

    public void removeSpinResultManipulator(SpinResultManipulator manipulator) {
        spinResultManipulators.remove(manipulator);
    }

    public void clearSpinResultManipulators() {
        spinResultManipulators.clear();
    }

    public void setLuckBonus(float bonusChance) {
        spinResultPolicy.setRescueChanceBonus(bonusChance);
    }

    public void setSpeedProfile(
        float reelSpeed,
        float reelStartStagger,
        float spinHoldDuration,
        float reelStopStagger,
        float reelStopDuration,
        float emptySpinSweepTimeScale,
        boolean instantSpin
    ) {
        this.reelStartStagger = Math.max(0f, reelStartStagger);
        this.spinHoldDuration = Math.max(0f, spinHoldDuration);
        this.reelStopStagger = Math.max(0f, reelStopStagger);
        this.emptySpinSweepTimeScale = Math.max(0f, emptySpinSweepTimeScale);
        this.instantSpin = instantSpin;

        for (Reel reel : reels) {
            reel.setSpeed(reelSpeed);
            reel.setStopDuration(reelStopDuration);
        }
    }

    public void setOnLastReelFinished(Runnable onLastReelFinished) {
        this.onLastReelFinished = onLastReelFinished;
    }

    public boolean isStale() {
        return stale;
    }

    private enum DragDirection {
        NONE, HORIZONTAL, VERTICAL
    }

    public List<Reel> getReels() {
        return reels;
    }

    public DragableBody[][] getGrid() {
        return grid;
    }

    public float getReelStartStagger() {
        return reelStartStagger;
    }

    public void setReelStartStagger(float reelStartStagger) {
        this.reelStartStagger = reelStartStagger;
    }

    public float getReelStopStagger() {
        return reelStopStagger;
    }

    public void setReelStopStagger(float reelStopStagger) {
        this.reelStopStagger = reelStopStagger;
    }
}
