package net.darkion.AchievementUnlockedLib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressWarnings("unused")

public class AchievementUnlocked {


    private achievementListener listener;

    Context context;
    private String titleString = "Achievement unlocked!", subtitle;
    private int duration = 2000, delay = 100, backgroundColor = 0xffffffff, iconBG, titleColor = 0xaa000000, subtitleColor = 0, yOffset = 0;
    private Drawable iconDrawable;
    private boolean large = true, alignTop = true, isRounded = true, persistent = false;
    private Typeface typeface;
    private float elevation = 40;
    private WindowManager windowManager;
    private ViewGroup container;
    private SquaredView icon;
    private TextView title, subtitleText;
    private int initialSize;
    private Drawable backgroundDrawable;
    public boolean expanded = false;
    public int height;
    public int width;
    private AchievementUnlocked achievementUnlocked;

    public AchievementUnlocked(Context context) {
        this.context = context;
    }


    public AchievementUnlocked setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;

    }


    public interface achievementListener {


        void onAchievementBeingCreated(AchievementUnlocked achievement, boolean created);

        void onAchievementExpanding(AchievementUnlocked achievement, boolean expanded);

        void onAchievementShrinking(AchievementUnlocked achievement, boolean shrunken);

        void onAchievementBeingDestroyed(AchievementUnlocked achievement, boolean destroyed);
    }


    public void setAchievementListener(achievementListener listener) {
        this.listener = listener;
    }


    public String getTitleString() {
        return titleString;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getIconBG() {
        return iconBG;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getSubtitleColor() {
        return subtitleColor;
    }

    public int getyOffset() {
        return yOffset;
    }

    public boolean isLarge() {
        return large;
    }

    public boolean isAlignTop() {
        return alignTop;
    }


    public boolean isRounded() {
        return isRounded;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public TextView getSubtitleText() {
        return subtitleText;
    }

    public int getDuration() {
        return duration;
    }

    public int getDelay() {
        return delay;
    }

    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public AchievementUnlocked setSubtitleColor(int subtitleColor) {
        this.subtitleColor = subtitleColor;
        return this;

    }

    public AchievementUnlocked setBackgroundDrawable(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        return this;

    }


    public AchievementUnlocked setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;

    }

    @TargetApi(21)
    public AchievementUnlocked setElevation(float elevation) {
        this.elevation = elevation;
        return this;

    }


    public AchievementUnlocked isLarge(boolean large) {
        this.large = large;
        return this;

    }


    public AchievementUnlocked alignTop(boolean alignTop) {
        this.alignTop = alignTop;
        return this;

    }

    public AchievementUnlocked setIconBG(int iconBG) {
        this.iconBG = iconBG;
        return this;

    }


    public AchievementUnlocked setIcon(Drawable icon) {
        this.iconDrawable = icon;
        return this;

    }

    public Drawable getIcon() {
        return iconDrawable;
    }

    public String getTitle() {
        return titleString;
    }

    public String getSubtitle() {
        return subtitle;
    }


    public AchievementUnlocked setTitle(String title) {
        this.titleString = title;
        return this;

    }

    public AchievementUnlocked setSubTitle(String subtitle) {
        this.subtitle = subtitle;
        return this;

    }

    public AchievementUnlocked setTypeface(Typeface typeface) {
        this.typeface = typeface;
        return this;

    }

    public AchievementUnlocked setTitle(int ResID) {
        this.titleString = context.getResources().getText(ResID).toString();
        return this;

    }

    public AchievementUnlocked setSubTitle(int ResID) {
        this.subtitle = context.getResources().getText(ResID).toString();
        return this;
    }


    public AchievementUnlocked setTitle(CharSequence title) {
        this.titleString = title.toString();
        return this;

    }

    public AchievementUnlocked setSubTitle(CharSequence subTitle) {
        this.subtitle = subTitle.toString();
        return this;
    }


    public AchievementUnlocked isPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }


    public AchievementUnlocked isRounded(boolean isRounded) {
        this.isRounded = isRounded;
        return this;
    }

    public AchievementUnlocked setDuration(int timeInMilliseconds) {
        this.duration = timeInMilliseconds;
        return this;

    }

    public AchievementUnlocked setStartDelay(int timeInMilliseconds) {
        this.delay = timeInMilliseconds;
        return this;

    }

    public AchievementUnlocked setYOffset(int yOffset) {
        this.delay = yOffset;
        return this;

    }

    public View getAchievementView() {
        return container;

    }


    public TextView getTitleTextView() {
        return title;

    }

    public TextView getSubtitleTextView() {
        return subtitleText;

    }

    public View getIconView() {
        return icon;

    }

    public AchievementUnlocked build() {
        buildAchievement();
        return this;
    }


    private void buildAchievement() {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.achievement_layout, null);
        float dimen;

        if (large)
            initialSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, context.getResources().getDisplayMetrics());
        else
            initialSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics());

        height = initialSize;
        width = initialSize;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, initialSize + (int) elevation * 2,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        if (alignTop)
            params.gravity = Gravity.TOP;
        else params.gravity = Gravity.BOTTOM;


        //  int translationY;

        windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);

        container = (ViewGroup) view.findViewById(R.id.achievement_body);
        icon = (SquaredView) view.findViewById(R.id.achievement_icon);
        title = new TextView(context);
        subtitleText = new TextView(context);

        RelativeLayout.LayoutParams viewRLP = new RelativeLayout.LayoutParams(initialSize, initialSize);
        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        GradientDrawable iconBackground = new GradientDrawable();
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(initialSize, initialSize);
        RelativeLayout.LayoutParams subtitleTextlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams containerLP = new RelativeLayout.LayoutParams(initialSize, initialSize);


        view.setLayoutParams(viewRLP);


        title.setAlpha(0f);
        title.setTranslationY(10);
        subtitleText.setTranslationY(10);
        subtitleText.setAlpha(0f);
        container.setScaleY(0f);
        container.setScaleX(0f);
        icon.setScaleY(0f);
        icon.setScaleY(0f);


        iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (backgroundDrawable == null) {

            GradientDrawable background = new GradientDrawable();
            background.setColor(backgroundColor);
            background.setShape(GradientDrawable.RECTANGLE);
            if (isRounded)
                background.setCornerRadius(initialSize);
            else background.setCornerRadius(10);

            if (Build.VERSION.SDK_INT >= 16) {
                container.setBackground(background);
            } else container.setBackgroundDrawable(background);


        } else {
            if (Build.VERSION.SDK_INT >= 16) {
                container.setBackground(backgroundDrawable);
            } else container.setBackgroundDrawable(backgroundDrawable);

        }


        titleLP.addRule(RelativeLayout.RIGHT_OF, icon.getId());


        if (large)
            titleLP.setMargins(16, 25, 16, 0);
        else titleLP.setMargins(16, 12, 16, 0);

        if (subtitle == null) {
            titleLP.addRule(RelativeLayout.CENTER_VERTICAL);

        }

        title.setText(titleString);
        if (large)
            title.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Medium);
        else title.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);

        title.setTextColor(titleColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && large) {
            title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        }

        title.setLayoutParams(titleLP);

        title.setId(R.id.achievement_title);


        if (large)
            subtitleTextlp.setMargins(16, 0, 16, 0);

        else subtitleTextlp.setMargins(16, 0, 16, 0);
        subtitleTextlp.addRule(RelativeLayout.BELOW, title.getId());
        subtitleTextlp.addRule(RelativeLayout.RIGHT_OF, icon.getId());
        subtitleText.setText(subtitle);
        subtitleText.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);
        if (subtitleColor == 0)
            subtitleColor = titleColor;
        subtitleText.setTextColor(subtitleColor);


        subtitleText.setLayoutParams(subtitleTextlp);
        subtitleText.setId(R.id.achievement_subtitle);


        if (Build.VERSION.SDK_INT >= 16 && typeface == null) {
            title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
            subtitleText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        } else {
            subtitleText.setTypeface(typeface);
            title.setTypeface(typeface);
        }
        subtitleText.setSingleLine(true);
        subtitleText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        subtitleText.setMarqueeRepeatLimit(-1);
        subtitleText.setFocusableInTouchMode(true);
        subtitleText.setFocusable(true);
        subtitleText.setSelected(true);
        subtitleText.setHorizontallyScrolling(true);
        subtitleText.setHorizontalFadingEdgeEnabled(true);

        title.setSingleLine(true);


        icon.setLayoutParams(iconParams);

        iconBackground.setShape(GradientDrawable.OVAL);
        iconBackground.setColor(iconBG);

        if (iconDrawable != null) {
            Drawable[] layers = {iconBackground, iconDrawable};

            LayerDrawable layerDrawable = new LayerDrawable(layers);
            if (Build.VERSION.SDK_INT >= 16) {
                icon.setBackground(layerDrawable);
            } else icon.setBackgroundDrawable(layerDrawable);


        } else {
            if (Build.VERSION.SDK_INT >= 16) {
                icon.setBackground(iconBackground);
            } else icon.setBackgroundDrawable(iconBackground);

        }


        containerLP.addRule(RelativeLayout.CENTER_IN_PARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            container.setElevation(elevation);
        }
        container.addView(title);
        container.addView(subtitleText);

        container.setLayoutParams(containerLP);
        windowManager.addView(view, params);

        achievementUnlocked = this;
    }


    public void show() {
        setOrientation(true);
        if (listener != null)
            listener.onAchievementBeingCreated(achievementUnlocked, false);
        icon.animate().scaleX(1f).setDuration(250).scaleY(1f).setInterpolator(new OvershootInterpolator()).setStartDelay(delay + 200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

            }
        }).start();
        container.animate().scaleX(1f).setDuration(250).setStartDelay(delay).scaleY(1f).setInterpolator(new OvershootInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onAchievementBeingCreated(achievementUnlocked, true);
                super.onAnimationEnd(animation);
                expandAchievement(true);
            }
        }).start();
    }

    private void setOrientation(boolean toggle) {
        Activity activity = (Activity) context;
        int currentOrientation = context.getResources().getConfiguration().orientation;
        if (toggle)
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }

    public AchievementUnlocked dismiss() {
        shrinkAchievement(true);
        return this;
    }


    private void shrinkAchievement(final boolean continuous) {
        expanded = false;

        if (listener != null)
            listener.onAchievementShrinking(achievementUnlocked, false);

        subtitleText.animate().alpha(0f).setDuration(300).translationY(10).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                title.animate().setDuration(300).alpha(0f).translationY(10).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);


                        ValueAnimator animBack = ValueAnimator.ofInt(container.getMeasuredWidth(), initialSize);
                        animBack.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                int val = (Integer) valueAnimator.getAnimatedValue();

                                ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
                                layoutParams.width = val;
                                container.setLayoutParams(layoutParams);


                            }
                        });
                        animBack.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (container.getWidth() != initialSize) {
                                    ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
                                    layoutParams.width = initialSize;
                                    container.setLayoutParams(layoutParams);
                                }

                                if (listener != null)
                                    listener.onAchievementShrinking(achievementUnlocked, true);

                                if (continuous) {
                                    if (listener != null)
                                        listener.onAchievementBeingDestroyed(achievementUnlocked, false);
                                    container.animate().scaleX(0f).setStartDelay(100).setDuration(250).setInterpolator(new AnticipateInterpolator()).scaleY(0f).setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            if (listener != null)
                                                listener.onAchievementBeingDestroyed(achievementUnlocked, true);

                                            container.clearAnimation();
                                            if (container.getRootView() != null)
                                                try {
                                                    windowManager.removeView(container.getRootView());

                                                } catch (java.lang.IllegalArgumentException e) {
// *shrug emoji*
                                                }

                                            setOrientation(false);
                                        }
                                    }).start();
                                }
                            }
                        });


                        animBack.setDuration(300);

                        animBack.start();


                    }
                }).start();

            }
        }).start();

    }




    private void expandAchievement(final boolean continuous) {
        if (listener != null)
            listener.onAchievementExpanding(achievementUnlocked, false);
        title.setVisibility(View.VISIBLE);
        subtitleText.setVisibility(View.VISIBLE);
        int stretched;
        if (subtitle != null)
            stretched = Math.max(title.getWidth(), subtitleText.getWidth()) + initialSize + 70;
        else stretched = title.getWidth() + initialSize + 70;


        stretched = Math.min(stretched, context.getResources().getDisplayMetrics().widthPixels - 50);

        width = stretched;


        final ValueAnimator anim = ValueAnimator.ofInt(initialSize, stretched);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
                layoutParams.width = val;
                container.setLayoutParams(layoutParams);

            }
        });

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                subtitleText.animate().alpha(1f).translationY(0).setStartDelay(150).setInterpolator(new DecelerateInterpolator()).setDuration(300).start();
                title.animate().alpha(1f).translationY(0).setDuration(300).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        expanded = true;

                        if (listener != null)
                            listener.onAchievementExpanding(achievementUnlocked, true);

                        if (continuous && !persistent) {

                            new android.os.Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    shrinkAchievement(true);
                                }
                            }, duration);

                        }

                    }
                });

            }
        });
        anim.setDuration(300);

        anim.start();


    }


}

class SquaredView extends View {

    public SquaredView(Context context) {
        super(context);
    }

    public SquaredView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquaredView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}



