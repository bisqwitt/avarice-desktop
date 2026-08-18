package com.avaricious.items;

import com.avaricious.components.slot.DragableBody;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractItem {

    protected DragableBody body = null;

    public abstract String title();

    public abstract String description();

    public abstract int price();

    public abstract TextureRegion texture();

    public abstract TextureRegion shadowTexture();

    public abstract float getTextureWidth();

    public abstract float getTextureHeight();

    public void addBody(Rectangle initialBounds) {
        body = new DragableBody(initialBounds).setTilt(200f, 20f);
    }

    public DragableBody getBody() {
        return body;
    }

    public abstract float getTooltipYOffset();

    public static <T> T instantiateItem(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T instantiateItem(String className, Class<T> expectedType) {
        try {
            return expectedType.cast(instantiateItem(Class.forName(className)));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getId() {
        return getClass().getName();
    }

}
