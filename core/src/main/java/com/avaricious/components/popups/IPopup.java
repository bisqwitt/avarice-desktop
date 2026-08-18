package com.avaricious.components.popups;

public interface IPopup {

    void update(float delta);

    void draw(float delta);

    boolean isFinished();
}
