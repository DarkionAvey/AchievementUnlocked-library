package net.darkion.achievementUnlockedApp;

import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;

/**
 * Drawable used for preview animations
 */
@SuppressWarnings("unused")
public class TranslationDrawable extends GradientDrawable {
    private float translationY, translationX;

    public float getTranslationX() {
        return translationX;
    }

    public void setTranslationX(float translationX) {
        this.translationX = translationX;
    }

    public float getTranslationY() {
        return translationY;
    }

    public void setTranslationY(float translationY) {
        this.translationY = translationY;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.translate(translationX, translationY);
        super.draw(canvas);
    }
}
