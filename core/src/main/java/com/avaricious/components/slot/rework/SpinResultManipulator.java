package com.avaricious.components.slot.rework;

/**
 * Changes a generated result before any reel begins to animate.
 */
@FunctionalInterface
public interface SpinResultManipulator {

    void manipulate(SpinResult result);
}
