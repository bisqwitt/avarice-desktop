package com.avaricious.effects.particle;

import com.avaricious.components.slot.SlotMachine;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.RunnableDrawing;
import com.avaricious.utility.Seq;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParticleManager {

    private static ParticleManager instance;

    public static ParticleManager I() {
        return instance == null ? instance = new ParticleManager() : instance;
    }

    private ParticleManager() {
    }

    private final Map<ParticleEffect, ZIndex> particleEffects = new HashMap<>();

    /*
     * Touch trail
     */
    private ParticleEffect touchTrail;
    private ZIndex touchTrailLayer;

    public void draw(SpriteBatch batch, float delta) {

        /*
         * Normal particle effects
         */
        for (Map.Entry<ParticleEffect, ZIndex> entry : particleEffects.entrySet()) {
            ParticleEffect particleEffect = entry.getKey();

            particleEffect.update(delta);

            Pencil.I().addDrawing(
                new RunnableDrawing(
                    () -> particleEffect.draw(batch),
                    entry.getValue()
                )
            );
        }

        /*
         * Remove completed normal effects
         */
        Set<ParticleEffect> dump = new HashSet<>();

        for (ParticleEffect particleEffect : particleEffects.keySet()) {
            if (particleEffect.isComplete()) {
                dump.add(particleEffect);
            }
        }

        for (ParticleEffect particleEffect : dump) {
            particleEffects.remove(particleEffect);
            particleEffect.dispose();
        }

        /*
         * Touch trail
         */
        if (touchTrail != null) {
            touchTrail.update(delta);

            Pencil.I().addDrawing(
                new RunnableDrawing(
                    () -> {
                        if (touchTrail != null) touchTrail.draw(batch);
                    },
                    touchTrailLayer
                )
            );

            /*
             * If it was stopped and all remaining particles
             * have disappeared, clean it up.
             */
            if (touchTrail.isComplete()) {
                touchTrail.dispose();
                touchTrail = null;
                touchTrailLayer = null;
            }
        }
    }

    public void create(
        float x,
        float y,
        ParticleType type,
        float scale,
        float emissionHigh,
        ZIndex layer
    ) {
        ParticleEffect particle = new ParticleEffect();

        particle.load(
            type.getFile(),
            Gdx.files.internal("particles/pngs")
        );

        particle.scaleEffect(scale);

        for (ParticleEmitter emitter : particle.getEmitters()) {
            emitter.getEmission().setHigh(emissionHigh);
        }

        particle.setPosition(
            x + SlotMachine.CELL_W / 2f,
            y + SlotMachine.CELL_H / 2f
        );

        particle.start();

        particleEffects.put(
            particle,
            layer
        );
    }

    public void create(
        float x,
        float y,
        ParticleType type,
        float scale,
        ZIndex layer
    ) {
        ParticleEffect particle = new ParticleEffect();

        particle.load(
            type.getFile(),
            Gdx.files.internal("particles/pngs")
        );

        particle.scaleEffect(scale);

        particle.setPosition(
            x + SlotMachine.CELL_W / 2f,
            y + SlotMachine.CELL_H / 2f
        );

        particle.start();

        particleEffects.put(
            particle,
            layer
        );
    }

    /*
     * Starts a continuous particle trail.
     *
     * x/y are already actual world coordinates,
     * so there is NO SlotMachine.CELL_W offset here.
     */
    public void startTrail(
        float x,
        float y,
        ParticleType type,
        float scale,
        ZIndex layer
    ) {

        /*
         * Remove an old trail if one somehow still exists.
         */
        if (touchTrail != null) {
            touchTrail.dispose();
        }

        touchTrail = new ParticleEffect();

        touchTrail.load(
            type.getFile(),
            Gdx.files.internal("particles/pngs")
        );

        touchTrail.scaleEffect(scale);
//        Seq.of(touchTrail.getEmitters())
//            .forEach(emitter -> emitter.getEmission().setHigh(75));

        touchTrail.setPosition(x, y);

        /*
         * Important:
         * start() makes all emitters start emitting.
         */
        touchTrail.start();

        touchTrailLayer = layer;
    }

    /*
     * Moves the emitter while already-created particles
     * remain behind at their previous positions.
     */
    public void moveTrail(float x, float y) {
        if (touchTrail == null) {
            return;
        }

        touchTrail.setPosition(x, y);
    }

    /*
     * Stop generating new particles.
     *
     * Existing particles continue to live/fade naturally.
     */
    public void stopTrail() {
        if (touchTrail == null) {
            return;
        }

        for (ParticleEmitter emitter : touchTrail.getEmitters()) {
            emitter.allowCompletion();
        }
    }

    public boolean isTrailActive() {
        return touchTrail != null;
    }
}
