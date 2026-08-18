package com.avaricious.components.background;

//import box2dLight.ConeLight;
//import box2dLight.RayHandler;

public class BackgroundLights {

//    private final RayHandler rayHandler;
//
//    private final ConeLight[] topLights = new ConeLight[5];
//    private float[] baseLightX;
//    private float[] baseLightY;
//
//    private boolean lightIsShaking = false;
//    private float lightShakeTime = 0f;
//    private float lightShakeStrength;
//
//    public BackgroundLights(RayHandler rayHandler) {
//        this.rayHandler = rayHandler;
//
//        lightIsShaking = false;
//        lightShakeTime = 0f;
//
//        float startX = 2f;
//        float stepX  = 3f;
//        float y      = 11f;
//        float distance = 14f;
//        float direction = 270f;
//        float coneDegrees = 20f;
//
//        baseLightX = new float[topLights.length];
//        baseLightY = new float[topLights.length];
//
//        for (int i = 0; i < topLights.length; i++) {
//            float x = startX + i * stepX;
//            baseLightX[i] = x;
//            baseLightY[i] = y;
//
////            topLights[i] = new ConeLight(
////                rayHandler,
////                120,
////                Assets.I().lightColor(),
////                distance,
////                x, y,
////                direction,
////                coneDegrees
////            );
//        }
//    }
//
//    public void triggerLightShake(float strength) {
//        lightIsShaking = true;
//        lightShakeStrength = strength;
//    }
//    public void render(float delta) {
//        if (!lightIsShaking) return;
//
//        lightShakeTime += delta;
//
//        float baseDirection = 270f;
//
//        // Max angle swing at full lightShakeStrength
//        float maxAngle = 10f;
//
//        // How quickly “energy” decays per second
//        float strengthDecayPerSecond = 0.25f; // smaller = longer tail
//
//        // Oscillation speed (how fast it swings left/right)
//        float frequency = 6f;
//
//        // 1) Decay shake strength over time
//        lightShakeStrength -= strengthDecayPerSecond * delta;
//        if (lightShakeStrength <= 0f) {
//            lightShakeStrength = 0f;
//            lightIsShaking = false;
//            // Reset lights to their default pose
//            for (int i = 0; i < topLights.length; i++) {
//                ConeLight light = topLights[i];
//                if (light == null) continue;
//
//                float baseX = baseLightX[i];
//                float baseY = baseLightY[i];
//
//                light.setPosition(baseX, baseY);
//                light.setDirection(baseDirection);
//            }
//            return;
//        }
//
//        // 2) Compute current angle amplitude based on remaining strength
//        float amplitude = maxAngle * lightShakeStrength;
//
//        // 3) Animate each light
//        for (int i = 0; i < topLights.length; i++) {
//            ConeLight light = topLights[i];
//            if (light == null) continue;
//
//            float phaseOffset = i * 0.5f;
//
//            float angleOffset =
//                (float) Math.sin(lightShakeTime * frequency + phaseOffset) * amplitude;
//
//            // Horizontal sway
//            float swayRadius = 0.3f;
//            float offsetX =
//                (float) Math.sin(lightShakeTime * frequency + phaseOffset) * swayRadius * lightShakeStrength;
//
//            // Small vertical bob
//            float swayY = 0.1f;
//            float offsetY =
//                (float) Math.cos(lightShakeTime * frequency + phaseOffset) * swayY * lightShakeStrength;
//
//            float baseX = baseLightX[i];
//            float baseY = baseLightY[i];
//
//            light.setPosition(baseX + offsetX, baseY + offsetY);
//            light.setDirection(baseDirection + angleOffset);
//        }
//    }

}
