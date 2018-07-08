package net.darkion.achievementUnlockedApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class BeatingImageView extends ImageView {
    public BeatingImageView(Context context) {
        super(context);
    }

    public BeatingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BeatingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //how many pixels to fully beat a heart
    final int heartCycle = 200;
    final float maxHeartScale = 1.3f;
    final float heartBeatIncrement = maxHeartScale / heartCycle;
    boolean scaleUp = true;


    public void beatTheHeart() {
        final ImageView heart = findViewById(R.id.heart);
        float direction = 1;
        if (!scaleUp || heart.getScaleY() >= maxHeartScale) {
            direction = -1;
            scaleUp = false;
        }
        if (heart.getScaleX() == 1f) {
            scaleUp = true;
        }
        float scale = heart.getScaleY() + (heartBeatIncrement * direction);
        heart.setScaleY(scale);
        heart.setScaleX(scale);


    }
}
