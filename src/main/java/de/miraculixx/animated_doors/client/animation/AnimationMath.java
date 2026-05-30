package de.miraculixx.animated_doors.client.animation;

import org.joml.Matrix4f;

public final class AnimationMath {
    private AnimationMath() {
    }

    public static float smooth(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }

    public static Matrix4f rotateY(float pivotX, float pivotZ, float radians) {
        return new Matrix4f()
            .translate(pivotX, 0.0f, pivotZ)
            .rotateY(radians)
            .translate(-pivotX, 0.0f, -pivotZ);
    }

    public static Matrix4f rotateX(float pivotY, float pivotZ, float radians) {
        return new Matrix4f()
            .translate(0.0f, pivotY, pivotZ)
            .rotateX(radians)
            .translate(0.0f, -pivotY, -pivotZ);
    }

    public static Matrix4f rotateZ(float pivotX, float pivotY, float radians) {
        return new Matrix4f()
            .translate(pivotX, pivotY, 0.0f)
            .rotateZ(radians)
            .translate(-pivotX, -pivotY, 0.0f);
    }
}
