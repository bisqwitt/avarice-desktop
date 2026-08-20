package com.avaricious.components.slot;

import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.Seq;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class BouncingSymbolManager {

    private static BouncingSymbolManager instance;

    public static BouncingSymbolManager I() {
        return instance == null ? instance = new BouncingSymbolManager() : instance;
    }

    private final List<BouncingSymbol> bouncingSymbols = new ArrayList<>();

    private BouncingSymbolManager() {
    }

    public void createFallingSymbol(Symbol symbol, float x, float y) {
        createFallingSymbol(symbol, x, y, 1f);
    }

    public void createSymbolDrop(Symbol symbol, float x, float y) {
        createFallingSymbol(symbol, x, y);

        int extraSpawnChance =
            SymbolValues.I().getExtraCollectibleSpawnChance(symbol);

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

    public void handleInput(
        Vector2 mouse,
        boolean touching,
        boolean wasTouching
    ) {
        for (BouncingSymbol symbol : bouncingSymbols) {
            symbol.handleInput(
                mouse,
                touching,
                wasTouching
            );
        }
    }

    public void drawFallingSymbols(float delta) {
        for (BouncingSymbol symbol : bouncingSymbols) {
            symbol.draw();
        }
    }

    public void updateFallingSymbols(float delta) {
        for (BouncingSymbol symbol : bouncingSymbols) {
            symbol.update(delta);
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

        for (int i = bouncingSymbols.size() - 1; i >= 0; i--) {
            if (bouncingSymbols.get(i).isFinished()) {
                bouncingSymbols.remove(i);
            }
        }
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
                    resolveBodyCollision(
                        symbol,
                        body
                    );
                }
            });
    }

    /*
     * Treat the BouncingSymbol as a circle and the Body
     * as an axis-aligned rectangle.
     */
    private void resolveBodyCollision(
        BouncingSymbol symbol,
        Body body
    ) {

        float circleX =
            symbol.getCenterX();

        float circleY =
            symbol.getCenterY();

        float radius =
            symbol.getRadius();

        /*
         * Slot body rectangle.
         */
        float bodyLeft =
            body.getPos().x;

        float bodyRight =
            bodyLeft + SlotMachine.CELL_W;

        float bodyBottom =
            body.getPos().y;

        float bodyTop =
            bodyBottom + SlotMachine.CELL_H;

        /*
         * Find the closest point on the rectangle
         * to the bouncing symbol.
         */
        float closestX =
            MathUtils.clamp(
                circleX,
                bodyLeft,
                bodyRight
            );

        float closestY =
            MathUtils.clamp(
                circleY,
                bodyBottom,
                bodyTop
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
         * The circle center itself is inside the Body.
         *
         * Push it towards the nearest edge.
         */
        else {

            float distanceLeft =
                circleX - bodyLeft;

            float distanceRight =
                bodyRight - circleX;

            float distanceBottom =
                circleY - bodyBottom;

            float distanceTop =
                bodyTop - circleY;

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
         * slot Body so it doesn't remain overlapping.
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
         * Body, separation was enough.
         */
        if (velocityAlongNormal >= 0f) {
            return;
        }

        /*
         * Slightly stronger bounce than symbol-to-symbol
         * collisions because the slot Body is static.
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
