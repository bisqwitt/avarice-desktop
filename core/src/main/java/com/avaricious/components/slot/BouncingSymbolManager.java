package com.avaricious.components.slot;

import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.components.automations.Automations;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.CollectibleValues;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class BouncingSymbolManager {

    private static final float CURSOR_LINE_THICKNESS = 0.014f;
    private static final float CURSOR_LINE_START_INSET = 0.25f;
    private static final float CURSOR_LINE_TARGET_GAP = 0.06f;

    private static BouncingSymbolManager instance;

    public static BouncingSymbolManager I() {
        return instance == null ? instance = new BouncingSymbolManager() : instance;
    }

    private final List<BouncingSymbol> bouncingSymbols = new ArrayList<>();
    private final List<CashChipCollectible> cashChips = new ArrayList<>();
    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private float cursorLineTime;

    private BouncingSymbolManager() {
    }

    public void createFallingSymbol(Symbol symbol, float x, float y) {
        createFallingSymbol(symbol, x, y, 1f);
    }

    public void createSymbolDrop(Symbol symbol, float x, float y) {
        createFallingSymbol(symbol, x, y);

        int extraSpawnChance =
            CollectibleValues.I().getExtraCollectibleSpawnChance();

        if (extraSpawnChance <= 0) {
            return;
        }

        if (
            extraSpawnChance >= 100 ||
                SeededRandomizer.get().nextFloat() * 100f < extraSpawnChance
        ) {
            createFallingSymbol(symbol, x, y);
        }
    }

    public void createFallingSymbol(
        Symbol symbol,
        float x,
        float y,
        float launchPower
    ) {
        bouncingSymbols.add(
            new BouncingSymbol(symbol, x, y, launchPower)
        );

        ParticleManager.I().create(
            x,
            y,
            ParticleType.RAINBOW,
            0.012f * launchPower,
            16f * launchPower,
            ZIndex.SYMBOL_HIT_PARTICLES
        );
    }

    public void createCashChip(float reward, float x, float y) {
        cashChips.add(new CashChipCollectible(reward, x, y));

        ParticleManager.I().create(
            x,
            y,
            ParticleType.COMP_CHIP,
            0.025f,
            55f,
            ZIndex.SYMBOL_HIT_PARTICLES
        );
    }

    public void reset() {
        bouncingSymbols.clear();
        cashChips.clear();
        cursorLineTime = 0f;
    }

    public boolean hasUnclaimedCollectibles() {
        for (BouncingSymbol symbol : bouncingSymbols) {
            if (symbol.isUnclaimed()) return true;
        }
        for (CashChipCollectible cashChip : cashChips) {
            if (cashChip.isUnclaimed()) return true;
        }
        return false;
    }

    public void handleInput(
        Vector2 mouse,
        boolean touching,
        boolean wasTouching,
        float delta
    ) {
        float targetRadius = Automations.I().getCursorTargetRadius().getRadius();
        BouncingSymbol clickedSymbol = touching && !wasTouching
            ? findSymbolUnderCursor(mouse)
            : null;

        if (clickedSymbol != null) {
            clickedSymbol.collectImmediately();
        }

        for (BouncingSymbol symbol : bouncingSymbols) {
            float distance = Vector2.dst(
                mouse.x,
                mouse.y,
                symbol.getCenterX(),
                symbol.getCenterY()
            );
            float hoverStrength = symbol.isUnclaimed() && distance <= targetRadius
                ? 1f - MathUtils.clamp(distance / targetRadius, 0f, 1f)
                : -1f;
            symbol.updateHoverHealth(hoverStrength, delta);
        }
        for (CashChipCollectible cashChip : cashChips) {
            cashChip.handleInput(mouse, touching);
        }
    }

    private BouncingSymbol findSymbolUnderCursor(Vector2 mouse) {
        BouncingSymbol closest = null;
        float closestDistanceSquared = Float.MAX_VALUE;

        for (BouncingSymbol symbol : bouncingSymbols) {
            if (!symbol.isUnclaimed()) continue;

            float deltaX = mouse.x - symbol.getCenterX();
            float deltaY = mouse.y - symbol.getCenterY();
            float distanceSquared = deltaX * deltaX + deltaY * deltaY;
            float radius = symbol.getRadius();
            if (distanceSquared > radius * radius ||
                distanceSquared >= closestDistanceSquared) {
                continue;
            }

            closest = symbol;
            closestDistanceSquared = distanceSquared;
        }

        return closest;
    }

    public void drawFallingSymbols(float delta) {
        drawFallingSymbols(delta, null);
    }

    public void drawFallingSymbols(float delta, Vector2 cursor) {
        cursorLineTime += delta;
        drawCursorLines(cursor);

        for (BouncingSymbol symbol : bouncingSymbols) {
            symbol.draw();
        }
        for (CashChipCollectible cashChip : cashChips) {
            cashChip.draw();
        }
    }

    private void drawCursorLines(Vector2 cursor) {
        if (cursor == null) return;

        float targetRadius = Automations.I().getCursorTargetRadius().getRadius();
        for (BouncingSymbol symbol : bouncingSymbols) {
            if (!symbol.isUnclaimed()) continue;

            float distance = Vector2.dst(
                cursor.x,
                cursor.y,
                symbol.getCenterX(),
                symbol.getCenterY()
            );
            if (distance > targetRadius) continue;

            drawCursorLine(cursor, symbol, distance, targetRadius);
        }
    }

    private void drawCursorLine(
        Vector2 cursor,
        BouncingSymbol symbol,
        float distance,
        float targetRadius
    ) {
        if (distance <= 0.000001f) return;

        float targetInset = symbol.getRadius() + CURSOR_LINE_TARGET_GAP;
        float lineLength = distance - CURSOR_LINE_START_INSET - targetInset;
        if (lineLength <= 0f) return;

        float directionX = (symbol.getCenterX() - cursor.x) / distance;
        float directionY = (symbol.getCenterY() - cursor.y) / distance;
        float startX = cursor.x + directionX * CURSOR_LINE_START_INSET;
        float startY = cursor.y + directionY * CURSOR_LINE_START_INSET;
        float endX = symbol.getCenterX() - directionX * targetInset;
        float endY = symbol.getCenterY() - directionY * targetInset;
        float midpointX = (startX + endX) * 0.5f;
        float midpointY = (startY + endY) * 0.5f;
        float angle = MathUtils.atan2(directionY, directionX)
            * MathUtils.radiansToDegrees;
        float proximity = 1f - MathUtils.clamp(
            distance / targetRadius,
            0f,
            1f
        );
        float pulse = 0.5f + 0.5f * MathUtils.sin(cursorLineTime * 5.5f);
        float alpha = 0.34f
            + proximity * 0.38f
            + pulse * (0.12f + proximity * 0.10f);

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            midpointX - lineLength * 0.5f,
            midpointY - CURSOR_LINE_THICKNESS * 0.5f,
            lineLength,
            CURSOR_LINE_THICKNESS,
            1f,
            angle,
            ZIndex.SLOT_MACHINE,
            new Color(
                0.78f + proximity * 0.22f,
                0.86f + proximity * 0.12f,
                0.92f + proximity * 0.08f,
                Math.min(1f, alpha)
            )
        ));
    }

    public void updateFallingSymbols(float delta) {
        updateFallingSymbols(delta, new Rectangle[0]);
    }

    public void updateFallingSymbols(
        float delta,
        Rectangle... obstacleBounds
    ) {
        for (BouncingSymbol symbol : bouncingSymbols) {
            symbol.update(delta);
        }
        for (CashChipCollectible cashChip : cashChips) {
            cashChip.update(delta);
        }

        /*
         * Dynamic symbols against each other.
         */
        handleSymbolCollisions();

        /*
         * Dynamic symbols against the currently
         * highlighted/hitting slot-machine symbols.
         */
        handlePatternHitCollisions();

        if (obstacleBounds != null) {
            for (Rectangle obstacle : obstacleBounds) {
                if (obstacle != null) {
                    handleObstacleCollisions(obstacle);
                }
            }
        }

        for (int i = bouncingSymbols.size() - 1; i >= 0; i--) {
            if (bouncingSymbols.get(i).isFinished()) {
                bouncingSymbols.remove(i);
            }
        }
        for (int i = cashChips.size() - 1; i >= 0; i--) {
            if (cashChips.get(i).isFinished()) {
                cashChips.remove(i);
            }
        }
    }

    List<CollectorTarget> getCollectorTargets() {
        List<CollectorTarget> targets = new ArrayList<>(
            bouncingSymbols.size() + cashChips.size()
        );
        targets.addAll(bouncingSymbols);
        targets.addAll(cashChips);
        return targets;
    }

    /*
     * ---------------------------------------------------------
     * BOUNCING SYMBOL <-> BOUNCING SYMBOL
     * ---------------------------------------------------------
     */

    private void handleSymbolCollisions() {
        for (int i = 0; i < bouncingSymbols.size(); i++) {
            BouncingSymbol a = bouncingSymbols.get(i);

            for (int j = i + 1; j < bouncingSymbols.size(); j++) {
                BouncingSymbol b =
                    bouncingSymbols.get(j);

                resolveCollision(a, b);
            }
        }
    }

    private void resolveCollision(
        BouncingSymbol a,
        BouncingSymbol b
    ) {
        float dx =
            b.getCenterX() - a.getCenterX();

        float dy =
            b.getCenterY() - a.getCenterY();

        float distanceSquared =
            dx * dx + dy * dy;

        float minDistance =
            a.getRadius() + b.getRadius();

        if (
            distanceSquared
                >= minDistance * minDistance
        ) {
            return;
        }

        float distance =
            (float) Math.sqrt(distanceSquared);

        /*
         * Exact same position.
         */
        if (distance < 0.001f) {
            dx = MathUtils.random(-1f, 1f);
            dy = MathUtils.random(-1f, 1f);

            distance =
                (float) Math.sqrt(
                    dx * dx + dy * dy
                );
        }

        /*
         * Collision normal.
         */
        float normalX = dx / distance;
        float normalY = dy / distance;

        /*
         * Separate overlapping symbols.
         */
        float overlap =
            minDistance - distance;

        float separation =
            overlap * 0.5f;

        a.move(
            -normalX * separation,
            -normalY * separation
        );

        b.move(
            normalX * separation,
            normalY * separation
        );

        /*
         * Relative velocity.
         */
        float relativeVelocityX =
            b.getVelocityX()
                - a.getVelocityX();

        float relativeVelocityY =
            b.getVelocityY()
                - a.getVelocityY();

        float velocityAlongNormal =
            relativeVelocityX * normalX
                + relativeVelocityY * normalY;

        /*
         * Already moving apart.
         */
        if (velocityAlongNormal > 0f) {
            return;
        }

        float restitution =
            MathUtils.random(0.55f, 0.8f);

        /*
         * Equal mass collision.
         */
        float impulse =
            -(1f + restitution)
                * velocityAlongNormal
                / 2f;

        float impulseX =
            impulse * normalX;

        float impulseY =
            impulse * normalY;

        a.setVelocityX(
            a.getVelocityX() - impulseX
        );

        a.setVelocityY(
            a.getVelocityY() - impulseY
        );

        b.setVelocityX(
            b.getVelocityX() + impulseX
        );

        b.setVelocityY(
            b.getVelocityY() + impulseY
        );

        /*
         * Tangential collision -> tumbling.
         */
        float tangentX = -normalY;
        float tangentY = normalX;

        float tangentVelocity =
            relativeVelocityX * tangentX
                + relativeVelocityY * tangentY;

        float spin =
            tangentVelocity
                * MathUtils.random(8f, 16f);

        a.addRotationVelocity(-spin);
        b.addRotationVelocity(spin);

        a.addRotationVelocity(
            MathUtils.random(-25f, 25f)
        );

        b.addRotationVelocity(
            MathUtils.random(-25f, 25f)
        );

        float impactForce = Math.abs(velocityAlongNormal);
        a.triggerImpact(impactForce);
        b.triggerImpact(impactForce);
    }

    /*
     * ---------------------------------------------------------
     * BOUNCING SYMBOL <-> SLOT MACHINE HIT BODY
     * ---------------------------------------------------------
     */

    private void handlePatternHitCollisions() {

        Seq.of(SlotMachine.I().getGrid())
            .filter(Body::isInPatternHit)
            .forEach(body -> {

                for (
                    BouncingSymbol symbol :
                    bouncingSymbols
                ) {
                    resolveRectangleCollision(
                        symbol,
                        body.getPos().x,
                        body.getPos().y,
                        SlotMachine.CELL_W,
                        SlotMachine.CELL_H
                    );
                }
            });
    }

    private void handleObstacleCollisions(Rectangle obstacle) {
        for (BouncingSymbol symbol : bouncingSymbols) {
            resolveRectangleCollision(
                symbol,
                obstacle.x,
                obstacle.y,
                obstacle.width,
                obstacle.height
            );
        }
    }

    /* Treat the symbol as a circle and each obstacle as a rectangle. */
    private void resolveRectangleCollision(
        BouncingSymbol symbol,
        float obstacleLeft,
        float obstacleBottom,
        float obstacleWidth,
        float obstacleHeight
    ) {

        float circleX =
            symbol.getCenterX();

        float circleY =
            symbol.getCenterY();

        float radius =
            symbol.getRadius();

        float obstacleRight =
            obstacleLeft + obstacleWidth;

        float obstacleTop =
            obstacleBottom + obstacleHeight;

        /*
         * Find the closest point on the rectangle
         * to the bouncing symbol.
         */
        float closestX =
            MathUtils.clamp(
                circleX,
                obstacleLeft,
                obstacleRight
            );

        float closestY =
            MathUtils.clamp(
                circleY,
                obstacleBottom,
                obstacleTop
            );

        float dx =
            circleX - closestX;

        float dy =
            circleY - closestY;

        float distanceSquared =
            dx * dx + dy * dy;

        /*
         * No collision.
         */
        if (
            distanceSquared
                >= radius * radius
        ) {
            return;
        }

        float normalX;
        float normalY;
        float penetration;

        /*
         * Normal case:
         *
         * Circle center is outside the rectangle,
         * so closest point gives us the collision normal.
         */
        if (distanceSquared > 0.000001f) {

            float distance =
                (float) Math.sqrt(
                    distanceSquared
                );

            normalX =
                dx / distance;

            normalY =
                dy / distance;

            penetration =
                radius - distance;
        }

        /*
         * Special case:
         *
         * The circle center itself is inside the obstacle.
         *
         * Push it towards the nearest edge.
         */
        else {

            float distanceLeft =
                circleX - obstacleLeft;

            float distanceRight =
                obstacleRight - circleX;

            float distanceBottom =
                circleY - obstacleBottom;

            float distanceTop =
                obstacleTop - circleY;

            float minimum =
                Math.min(
                    Math.min(
                        distanceLeft,
                        distanceRight
                    ),
                    Math.min(
                        distanceBottom,
                        distanceTop
                    )
                );

            if (minimum == distanceLeft) {

                normalX = -1f;
                normalY = 0f;

                penetration =
                    radius + distanceLeft;

            } else if (minimum == distanceRight) {

                normalX = 1f;
                normalY = 0f;

                penetration =
                    radius + distanceRight;

            } else if (minimum == distanceBottom) {

                normalX = 0f;
                normalY = -1f;

                penetration =
                    radius + distanceBottom;

            } else {

                normalX = 0f;
                normalY = 1f;

                penetration =
                    radius + distanceTop;
            }
        }

        /*
         * First move the bouncing symbol outside the
         * obstacle so it doesn't remain overlapping.
         */
        symbol.move(
            normalX * penetration,
            normalY * penetration
        );

        /*
         * Current velocity projected onto
         * the collision normal.
         */
        float velocityAlongNormal =
            symbol.getVelocityX() * normalX
                + symbol.getVelocityY() * normalY;

        /*
         * If it's already traveling away from the
         * obstacle, separation was enough.
         */
        if (velocityAlongNormal >= 0f) {
            return;
        }

        /*
         * Slightly stronger bounce than symbol-to-symbol
         * collisions because the obstacle is static.
         */
        float restitution =
            MathUtils.random(0.8f, 0.95f);

        /*
         * Reflect velocity along the surface normal.
         *
         * v' = v - (1 + e)(v · n)n
         */
        float impulse =
            -(1f + restitution)
                * velocityAlongNormal;

        symbol.setVelocityX(
            symbol.getVelocityX()
                + impulse * normalX
        );

        symbol.setVelocityY(
            symbol.getVelocityY()
                + impulse * normalY
        );

        /*
         * A little extra spin depending on the surface
         * direction makes the collision look less rigid.
         */
        float tangentX =
            -normalY;

        float tangentY =
            normalX;

        float tangentialVelocity =
            symbol.getVelocityX() * tangentX
                + symbol.getVelocityY() * tangentY;

        symbol.addRotationVelocity(
            tangentialVelocity
                * MathUtils.random(5f, 12f)
        );

        symbol.addRotationVelocity(
            MathUtils.random(-35f, 35f)
        );

        symbol.triggerImpact(
            Math.abs(velocityAlongNormal)
        );
    }
}
