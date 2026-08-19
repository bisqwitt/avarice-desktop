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

    public void update(float delta) {
        Seq.of(particleEffects.keySet()).forEach(particle -> particle.update(delta));
    }

    public void draw(SpriteBatch batch, float delta) {
        for (Map.Entry<ParticleEffect, ZIndex> entry : particleEffects.entrySet()) {
            ParticleEffect particleEffect = entry.getKey();

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
}
