package com.avaricious.components.slot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Draggable slot/card behavior (Balatro-like):
 * - smooth follow
 * - grip offset (no snapping)
 * - tilt based on velocity
 * - lerp back to origin on release
 * <p>
 * Rendering should use getRenderPos() instead of getPos().
 */
public class DragableBody extends Body {

    // --- Size for hit-testing (set to your card sprite size in world units) ---
    private float width;
    private float height;

    // --- Drag state ---
    private boolean dragging = false;
    private int draggingPointer = -1;

    // Offset from base position (Slot.pos) to where we actually render
    private final Vector2 offset = new Vector2();
    private final Vector2 targetOffset = new Vector2();

    // Cursor grip: (cursor - renderPos) at the moment we started dragging
    private final Vector2 grip = new Vector2();

    // Velocity for tilt
    private final Vector2 lastRenderPos = new Vector2();
    private final Vector2 vel = new Vector2();

    private final Vector2 lastMouse = new Vector2();
    private float dragVelX = 0f;

    private float dragFollowSpeed = 14f;     // higher = snappier to cursor
    private float returnSpeed = 14f;         // higher = snappier return to origin
    private float maxTiltDeg = 12f;          // max drag tilt
    private float tiltResponsiveness = 10f;  // smoothing for tilt
    private float dragScaleMul = 1.10f;      // scale while dragging
    private float dropWobble = 1f;           // call wobble() on release if you like
    private float moveToSpeed = 10f;

    // Smoothed tilt
    private float tiltDeg = 0f;

    private boolean applying = false;
    private float applyTime = 0f;
    private float applyDuration = 0.35f;
    private float applyStartTilt = 0f;

    private float alpha = 1f;
    private float extraScaleMul = 1f;

    private Vector2 targetPosition = null;

    // Optional: callback on finish
    private Runnable onApplyFinished = null;

    // Temp to avoid allocations
    private final Vector2 tmp = new Vector2();
    private final Vector2 renderPos = new Vector2();

    public DragableBody(Rectangle bounds) {
        super(new Vector2(bounds.x, bounds.y));
        this.width = bounds.width;
        this.height = bounds.height;

        // initialize lastRenderPos
        renderPos.set(super.getPos());
        lastRenderPos.set(renderPos);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (applying) {
            applyTime += delta;
            float t = Math.min(1f, applyTime / applyDuration);

            // Easing (Balatro-ish): fast start, smooth end
            float ease = 1f - (float) Math.pow(1f - t, 3); // cubic out

            // Shrink + fade out near the end
            extraScaleMul = 1f - 0.35f * ease;   // ends at 0.65
            alpha = 1f - ease;            // ends at 0

            // Reduce tilt to zero
            tiltDeg = applyStartTilt * (1f - ease);

            if (t >= 1f) {
                applying = false;
                extraScaleMul = 1f;
                tiltDeg = 0f;

                if (onApplyFinished != null) {
                    Runnable r = onApplyFinished;
                    onApplyFinished = null;
                    r.run();
                }
            }
            return;
        }
        // Compute current render pos (base + offset)
        getRenderPos(renderPos);

        // Velocity (for tilt)
        vel.set(renderPos).sub(lastRenderPos).scl(delta > 0 ? (1f / delta) : 0f);
        lastRenderPos.set(renderPos);

        // Offset smoothing: follow cursor if dragging, otherwise return to zero
        float speed = dragging ? dragFollowSpeed : returnSpeed;
        offset.x += (targetOffset.x - offset.x) * Math.min(1f, speed * delta);
        offset.y += (targetOffset.y - offset.y) * Math.min(1f, speed * delta);

        if (dragging) {
            // tune 0.08..0.25 depending on your world scale
            float tiltStrength = 0.5f;
            float targetTilt = MathUtils.clamp(-dragVelX * tiltStrength, -maxTiltDeg, maxTiltDeg);
            tiltDeg += (targetTilt - tiltDeg) * Math.min(1f, tiltResponsiveness * delta);
        } else {
            // decay tilt back to 0 when not dragging
            tiltDeg += (0f - tiltDeg) * Math.min(1f, tiltResponsiveness * delta);
        }

        if (targetPosition != null) {
            Vector2 pos = getPos();
            float t = 1f - (float) Math.exp(-moveToSpeed * delta);

            pos.lerp(targetPosition, t);

            // snap when close
            if (pos.epsilonEquals(targetPosition, 0.01f)) {
                pos.set(targetPosition);
                targetPosition = null; // optional: stop moving
            }
        }
    }

    public boolean beginDrag(float worldX, float worldY, int pointer) {
        if (dragging) return false;
        dragging = true;
        draggingPointer = pointer;

        lastMouse.set(worldX, worldY);
        dragVelX = 0f;

        // Grip = cursor - currentRenderPos (so we keep relative grab point)
        getRenderPos(renderPos);
        grip.set(worldX - renderPos.x, worldY - renderPos.y);

        // Immediately set target so it feels responsive on pickup
        dragTo(worldX, worldY, pointer);
        return true;
    }

    /**
     * Continue dragging.
     */
    public void dragTo(float worldX, float worldY, int pointer) {
        if (!dragging || pointer != draggingPointer) return;

        // Desired render position = cursor - grip
        float desiredX = worldX - grip.x;
        float desiredY = worldY - grip.y;

        // basePos is Slot.getPos() (DO NOT mutate it)
        Vector2 base = super.getPos();

        // targetOffset = desiredRenderPos - basePos
        targetOffset.set(desiredX - base.x, desiredY - base.y);

        float dx = worldX - lastMouse.x;
        lastMouse.set(worldX, worldY);

        // dx is world-units per frame, convert to "per second"
        dragVelX = dx / Math.max(1e-6f, Gdx.graphics.getDeltaTime());
    }

    /**
     * End dragging; card lerps back (or you can add drop logic here).
     */
    public void endDrag(int pointer) {
        if (!dragging || pointer != draggingPointer) return;

        targetOffset.setZero();
        dragging = false;
        draggingPointer = -1;

        if (dropWobble > 0f) {
//            wobble();
        }
    }

    public void startApplyAnimation(float duration, Runnable onFinished) {
        this.applying = true;
        this.applyTime = 0f;
        this.applyDuration = Math.max(0.01f, duration);
        this.onApplyFinished = onFinished;

        this.applyStartTilt = this.tiltDeg;

        // Make it feel snappy
        this.dragging = false;
        this.draggingPointer = -1;
    }

    public float getAlpha() {
        return alpha;
    }

    // --- Rendering helpers ---

    /**
     * Render position (base + offset) into out (WORLD coords).
     * Use this instead of getPos() when drawing.
     */
    public Vector2 getRenderPos(Vector2 out) {
        Vector2 base = super.getPos();
        return out.set(base.x + offset.x, base.y + offset.y + idleFloatEffect.getValue());
    }

    /**
     * Additional rotation from drag. Combine with Slot.wobbleAngleDeg().
     */
    public float getDragTiltDeg() {
        return dragging ? tiltDeg : tiltDeg * 0.25f; // keep a tiny residual if you like
    }

    public DragableBody setMoveToSpeed(float moveToSpeed) {
        this.moveToSpeed = moveToSpeed;
        return this;
    }

    public Vector2 getCardCenter() {
        return new Vector2(renderPos.x + width / 2, renderPos.y + height / 2);
    }

    public void moveTo(Vector2 targetPosition) {
        this.targetPosition = targetPosition;
    }

    public DragableBody setTilt(float maxTiltDeg, float tiltResponsiveness) {
        this.maxTiltDeg = maxTiltDeg;
        this.tiltResponsiveness = tiltResponsiveness;
        return this;
    }

    @Override
    public DragableBody setTargetScaleSpeed(float value) {
        super.setTargetScaleSpeed(value);
        return this;
    }

    public Rectangle getBounds() {
        return new Rectangle(pos.x, pos.y, width, height);
    }

    @Override
    public float getRotation() {
        return super.getRotation() + getDragTiltDeg();
    }

    public boolean isDragging() {
        return dragging;
    }
}
