package com.avaricious;

import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

public class CreditScore extends CreditNumber {

    public CreditScore(Rectangle rectangle, float offset) {
        super(CreditManager.I().getCredits(), rectangle, offset);

        CreditManager.I().onChange(this::setValue);
        setZIndex(ZIndex.CREDIT_SCORE);
    }

    @Override
    public CreditScore setZIndex(ZIndex zIndex) {
        return (CreditScore) super.setZIndex(zIndex);
    }
}
