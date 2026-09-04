package com.avaricious.components.slot;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** One autonomous pointer that seeks and physically touches collectibles. */
final class Collector {

    private static final float WORLD_HEIGHT = 9f;
    private static final float WIDTH = 0.36f;
    private static final float HEIGHT = 0.54f;
    private static final float RADIUS = 0.16f;
    private static final float SEEK_SPEED = 6.4f;
    private static final float WANDER_SPEED = 3.0f;
    private static final float STEERING = 7.5f;
    private static final Color SHADOW_COLOR = new Color(1f, 1f, 1f, 0.70f);

    private final TextureRegion texture = Assets.I().get(AssetKey.COLLECTOR);
    private final TextureRegion shadow = Assets.I().get(AssetKey.COLLECTOR_SHADOW);
    private final float phase;

    private CollectorTarget target;
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float age;
    private float rotation;
    private float collectFlash;

    Collector(int index) {
        phase = index * 2.3999632f;
        x = GameplayLayout.SLOT_CENTER + MathUtils.cos(phase) * 0.75f;
        y = 4.5f + MathUtils.sin(phase) * 0.75f;
        velocityX = MathUtils.cos(phase + 0.8f) * WANDER_SPEED;
        velocityY = MathUtils.sin(phase + 0.8f) * WANDER_SPEED;
    }

    void update(float delta) {
        float step = Math.min(delta, 0.05f);
        age += step;
        collectFlash = Math.max(0f, collectFlash - step * 4.5f);

        if (target != null && !target.isAvailableForCollector()) {
            target = null;
        }

        float destinationX;
        float destinationY;
        float speed;
        if (target != null) {
            destinationX = target.getCollectorTargetX();
            destinationY = target.getCollectorTargetY();
            speed = SEEK_SPEED;
        } else {
            destinationX = GameplayLayout.WORLD_WIDTH / 2f
                + MathUtils.cos(age * 0.63f + phase) * 6.8f;
            destinationY = WORLD_HEIGHT / 2f
                + MathUtils.sin(age * 0.91f + phase * 1.7f) * 3.7f;
            speed = WANDER_SPEED;
        }

        steerTowards(destinationX, destinationY, speed, step);
        x += velocityX * step;
        y += velocityY * step;
        keepOnScreen();

        if (velocityX != 0f || velocityY != 0f) {
            rotation = MathUtils.atan2(velocityY, velocityX)
                * MathUtils.radiansToDegrees - 135f;
        }

        if (touchesTarget() && target.collectByCollector()) {
            target = null;
            collectFlash = 1f;
            velocityX *= -0.35f;
            velocityY *= -0.35f;
        }
    }

    private void steerTowards(
        float destinationX,
        float destinationY,
        float speed,
        float delta
    ) {
        float dx = destinationX - x;
        float dy = destinationY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001f) return;

        float desiredSpeed = speed;
        if (target != null && distance < 0.75f) {
            desiredSpeed = MathUtils.lerp(1.6f, speed, distance / 0.75f);
        }

        float desiredVelocityX = dx / distance * desiredSpeed;
        float desiredVelocityY = dy / distance * desiredSpeed;
        float follow = Math.min(1f, delta * STEERING);
        velocityX = MathUtils.lerp(velocityX, desiredVelocityX, follow);
        velocityY = MathUtils.lerp(velocityY, desiredVelocityY, follow);
    }

    private void keepOnScreen() {
        float halfWidth = WIDTH / 2f;
        float halfHeight = HEIGHT / 2f;
        if (x < halfWidth) {
            x = halfWidth;
            velocityX = Math.abs(velocityX);
        } else if (x > GameplayLayout.WORLD_WIDTH - halfWidth) {
            x = GameplayLayout.WORLD_WIDTH - halfWidth;
            velocityX = -Math.abs(velocityX);
        }
        if (y < halfHeight) {
            y = halfHeight;
            velocityY = Math.abs(velocityY);
        } else if (y > WORLD_HEIGHT - halfHeight) {
            y = WORLD_HEIGHT - halfHeight;
            velocityY = -Math.abs(velocityY);
        }
    }

    private boolean touchesTarget() {
        if (target == null || !target.isAvailableForCollector()) return false;
        float dx = target.getCollectorTargetX() - x;
        float dy = target.getCollectorTargetY() - y;
        float touchDistance = RADIUS + target.getCollectorTargetRadius();
        return dx * dx + dy * dy <= touchDistance * touchDistance;
    }

    void draw() {
        float pulse = 1f + MathUtils.sin(age * 8f + phase) * 0.045f;
        float scale = pulse + collectFlash * 0.32f;
        float drawX = x - WIDTH / 2f;
        float drawY = y - HEIGHT / 2f;

        Pencil.I().addDrawing(new TextureDrawing(
            shadow,
            drawX + 0.06f,
            drawY - 0.07f,
            WIDTH,
            HEIGHT,
            scale,
            rotation,
            ZIndex.SLOT_MACHINE_FOREGROUND,
            SHADOW_COLOR
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            texture,
            drawX,
            drawY,
            WIDTH,
            HEIGHT,
            scale,
            rotation,
            ZIndex.SLOT_MACHINE_FOREGROUND
        ));
    }

    CollectorTarget getTarget() {
        return target;
    }

    void setTarget(CollectorTarget target) {
        this.target = target;
    }

    float getX() {
        return x;
    }

    float getY() {
        return y;
    }
}
