package com.avaricious.components.popups;

import com.avaricious.effects.PulseEffect;
import com.avaricious.utility.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class RedCrossPopup implements IPopup {
    private final TextureRegion redCross = Assets.I().get(AssetKey.RED_CROSS);
    private final Vector2 spawnPos;
    private final PulseEffect pulseEffect = new PulseEffect();
    private boolean isDead = false;

    public RedCrossPopup(Vector2 spawnPos) {
        this.spawnPos = new Vector2(spawnPos);
        pulseEffect.setStrength(3.5f);
        pulseEffect.pulse();
    }

    @Override
    public void update(float delta) {
        pulseEffect.update(delta);
    }

    @Override
    public void draw(float delta) {
        Pencil.I().addDrawing(new TextureDrawing(redCross,
            spawnPos.x, spawnPos.y,
            0.4f, 0.4f,
            pulseEffect.getScale(),
            0,
            ZIndex.SLOT_MACHINE
            ));

        if(pulseEffect.isFinished()) isDead = true;
    }

    @Override
    public boolean isFinished() {
        return isDead;
    }
}
