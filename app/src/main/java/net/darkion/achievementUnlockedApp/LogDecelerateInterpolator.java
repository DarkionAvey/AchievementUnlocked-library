package net.darkion.achievementUnlockedApp;

import android.animation.TimeInterpolator;


/**
 * Copied from launcher3
 */
public class LogDecelerateInterpolator implements TimeInterpolator {

    private final float mLogScale;
    private int mBase;
    private int mDrift;

    LogDecelerateInterpolator(int base, int drift) {
        mBase = base;
        mDrift = drift;

        mLogScale = 1f / computeLog(1, mBase, mDrift);
    }

    private float computeLog(float t, int base, int drift) {
        return (float) -Math.pow(base, -t) + 1 + (drift * t);
    }

    @Override
    public float getInterpolation(float t) {
        return computeLog(t, mBase, mDrift) * mLogScale;
    }
}