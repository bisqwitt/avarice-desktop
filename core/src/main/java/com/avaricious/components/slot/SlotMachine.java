package com.avaricious.components.slot;

import com.avaricious.effects.TextureEcho;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SeededRandomizer;
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
import java.util.Collections;
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
            reels.add(new Reel(rowCount, () -> {
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

        Seq.of(reels).forEach(reel -> reel.setSpeed(8));
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

    private void update(float delta) {
        for (int c = 0; c < colCount; c++) {
            reels.get(c).update(delta);
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
        // update reel motion
        update(delta);

        Rectangle area = getBounds(); // world-space
        area.setX(area.x - 0.3f);
        area.setY(area.y - 0.1f);
        area.setWidth(area.width + 0.3f);
        area.setHeight(area.height - 0.15f);

        Camera cam = GameContext.I().viewport.getCamera();
        cam.update();
        Pencil.I().startScissors(cam, batch.getTransformMatrix(), area);
        List<Vector2> symbolsInPatternHit = drawSymbols();
        Pencil.I().endScissors();

        Seq.of(symbolsInPatternHit).forEach(this::drawSymbol);

        TextureEcho.draw(delta);
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
    }

    public void spin() {
        spinningReels = colCount;
        stale = false;

        float startSpeed = 16f;

        for (int c = 0; c < colCount; c++) {
            final int col = c;
            float startDelay = c * reelStartStagger;
            float stopDelay = colCount * reelStartStagger + c * reelStopStagger;

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    reels.get(col).start(startSpeed + MathUtils.random(-0.7f, 0.7f)); // tiny per-reel variation
                }
            }, startDelay);

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    reels.get(col).requestStopAlignCenter(0); // at least 2 full strip loops after stop requested
                }
            }, 1f + stopDelay);
        }
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
        reelA.setSymbolAtRow(rowA, symB);   // ← musst du noch public machen (s.u.)
        reelB.setSymbolAtRow(rowB, symA);
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
        List<Symbol> baseStrip = new ArrayList<>();

        Seq.of(Arrays.asList(Symbol.values()))
            .forEach(symbol -> {
                for (int i = 0; i < symbol.poolCount() / 2; i++) {
                    baseStrip.add(symbol);
                }
            });

        for (Reel reel : reels) {
            List<Symbol> reelStrip = new ArrayList<>(baseStrip);
            Collections.shuffle(reelStrip, SeededRandomizer.get());
            reel.setStrip(reelStrip);
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
