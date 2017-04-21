package net.darkion.achievementUnlockedApp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import net.darkion.achievementUnlockedApp.AchievementIconView.AchievementIconViewStates;

import static android.text.TextUtils.isEmpty;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.View.GONE;
import static android.widget.LinearLayout.VERTICAL;
import static java.lang.Boolean.FALSE;

@SuppressWarnings("unused")
/**
 * Basically an animated toast notification with queue support.
 *
 * Doesn't work with power-saving mode on unless you implement your
 * own valueAnimator class.
 *
 * This is 'all-in-one' library. You have to copy the class file
 * to your package folder, otherwise you won't have access to inner
 * classes such as AchievementData and listener.
 *
 * Don't forget to grant 'draw over apps' permission (SYSTEM_ALERT_WINDOW)
 *
 *
 * GPL
 * By DarkionAvey @ http://darkion.net/
 */

public class AchievementUnlocked {
    //dimens
    private int smallSize, largeSize, elevation, paddingLarge, paddingSmall, translationY, margin;
    //for debugging purpose
    final static int animationMultiplier = 1;
    private int initialSize = -1;
    //indices of data iterator
    private int index = 0;
    private Context context;
    private boolean dismissible = false;
    private boolean added = false;
    //achievements data
    private AchievementData[] achievements;
    private final OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    //save previous width of popup just in case measureWidth returns 0 when using multiline popup
    private int readingDelay = 1500;

    private final TimeInterpolator interpolator = new DeceleratingInterpolator(50);
    private final AnticipateInterpolator anticipateInterpolator = new AnticipateInterpolator();
    private final TimeInterpolator accelerateInterpolator = new AccelerateInterpolator(50);
    private int matchParent;
    private boolean dismissed = false;
    private AchievementListenerAdapter listener;
    private boolean isPowerSavingModeOn = false;
    private boolean isLarge = true, alignTop = true, isRounded = true;
    private ViewGroup container;
    private AchievementIconView icon;
    private TextView titleTextView;
    private ScrollTextView subtitleTextView;
    private final int focusable = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    private final int nonFocusable = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    private ViewGroup achievementLayout;
    private WindowManager.LayoutParams mainViewLP;
    final boolean DEBUG = true;

    public AchievementUnlocked(Context context) {
        this.context = context;

        initGlobalFields();
    }

    public AchievementUnlocked setTopAligned(boolean alignTop) {
        this.alignTop = alignTop;
        return this;
    }

    public AchievementUnlocked setReadingDelay(int readingDelay) {
        this.readingDelay = readingDelay;
        return this;

    }

    public AchievementUnlocked setRounded(boolean rounded) {
        isRounded = rounded;
        return this;
    }

    public void setAchievementListener(AchievementListenerAdapter listener) {
        this.listener = listener;
    }

    public AchievementUnlocked setLarge(boolean large) {
        this.isLarge = large;
        return this;
    }

    public AchievementUnlocked isTopAligned(boolean alignTop) {
        this.alignTop = alignTop;
        return this;
    }

    public View getAchievementView() {
        return container;
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    public TextView getSubtitleTextView() {
        return subtitleTextView;
    }

    public View getIconView() {
        return icon;
    }

    public ViewGroup getAchievementParent() {
        return achievementLayout;
    }

    private int convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    private boolean initiatedGlobalFields = false;

    private void initGlobalFields() {
        if (!initiatedGlobalFields) {
            initiatedGlobalFields = true;
            margin = convertDpToPixel(16);
            elevation = convertDpToPixel(10);
            paddingLarge = convertDpToPixel(10);
            paddingSmall = convertDpToPixel(5);
            smallSize = convertDpToPixel(50);
            largeSize = convertDpToPixel(65);
            translationY = convertDpToPixel(20);

            achievementLayout = new RelativeLayout(context);
            achievementLayout.setClipToPadding(FALSE);
            LayoutParams motherLayoutLP = new LayoutParams(-2, -2);
            achievementLayout.setLayoutParams(motherLayoutLP);
            achievementLayout.setTag("motherLayout");
            LinearLayout textContainerFake = new LinearLayout(context);
            textContainerFake.setOrientation(VERTICAL);
            textContainerFake.setPadding(convertDpToPixel(10), 0, convertDpToPixel(20), 0);
            textContainerFake.setVisibility(View.INVISIBLE);
            LayoutParams textContainerFakeLP = new LayoutParams(-2, -2);
            textContainerFakeLP.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            textContainerFake.setLayoutParams(textContainerFakeLP);
            textContainerFake.setTag("textContainerFake");
            TextView titleFake = new TextView(context);
            titleFake.setText("Title");
            LayoutParams titleFakeLP = new LayoutParams(-2, -2);
            titleFake.setLayoutParams(titleFakeLP);
            titleFake.setTag("titleFake");
            titleFake.setMaxLines(1);
            ScrollTextView subtitleFake = new ScrollTextView(context);
            subtitleFake.setText("Subtitle");
            subtitleFake.setVisibility(GONE);
            subtitleFake.setMaxLines(1);
            LayoutParams subtitleFakeLP = new LayoutParams(-2, -2);
            subtitleFake.setLayoutParams(subtitleFakeLP);
            subtitleFake.setTag("subtitleFake");
            textContainerFake.addView(titleFake);
            textContainerFake.addView(subtitleFake);
            achievementLayout.addView(textContainerFake);
            container = new RelativeLayout(context);
            container.setClipToPadding(false);
            container.setClipChildren(false);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                achievementLayout.setClipToOutline(true);
            }

            LayoutParams achievementBodyLP = new LayoutParams(-2, largeSize);
            achievementBodyLP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            achievementBodyLP.addRule(CENTER_HORIZONTAL, RelativeLayout.TRUE);
            achievementBodyLP.bottomMargin = achievementBodyLP.topMargin = convertDpToPixel(10);
            container.setLayoutParams(achievementBodyLP);
            container.setTag("achievementBody");
            LinearLayout achievementIconBg = new LinearLayout(context);
            LayoutParams achievementIconBgLP = new LayoutParams(largeSize, largeSize);
            achievementIconBg.setLayoutParams(achievementIconBgLP);
            achievementIconBg.setTag("achievementIconBg");
            container.addView(achievementIconBg);
            icon = new AchievementIconView(context);
            icon.setPadding(convertDpToPixel(7), convertDpToPixel(7), convertDpToPixel(7), convertDpToPixel(7));
            LayoutParams achievementIconLP = new LayoutParams(largeSize, largeSize);
            icon.setMaxWidth(largeSize);
            icon.setLayoutParams(achievementIconLP);
            icon.setTag("achievementIcon");
            achievementIconBg.addView(icon);
            LinearLayout textContainer = new LinearLayout(context);
            textContainer.setClipToPadding(false);
            textContainer.setClipChildren(false);
            textContainer.setOrientation(VERTICAL);
            textContainer.setTag("textContainer");
            LayoutParams textContainerLP = new LayoutParams(-2, -2);
            textContainer.setLayoutParams(textContainerLP);
            textContainerLP.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            container.addView(textContainer);

            container.setTag("achievementBody");
            titleTextView = new TextView(context);
            titleTextView.setText("Title");
            titleTextView.setMaxLines(1);
            LayoutParams titleLP = new LayoutParams(-2, -2);
            titleTextView.setLayoutParams(titleLP);
            titleTextView.setTag("title");
            subtitleTextView = new ScrollTextView(context);
            subtitleTextView.setText("Subtitle");
            subtitleTextView.setVisibility(GONE);
            subtitleTextView.setLayoutParams(titleLP);
            subtitleTextView.setMaxLines(1);
            subtitleTextView.setTag("subtitle");
            textContainer.addView(titleTextView);
            textContainer.addView(subtitleTextView);
            achievementLayout.addView(container);


            if (mainViewLP == null) {
                mainViewLP = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, focusable
                        ,
                        PixelFormat.TRANSLUCENT);
            }

            if (titleTextView == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isLarge) {
                    titleTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                }
                titleTextView.setSingleLine(true);
                titleTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        ((TextView) achievementLayout.findViewWithTag("titleFake")).setText(titleTextView.getText());
                    }
                });
            }
            if (subtitleTextView == null) {

                subtitleTextView.setSingleLine(true);
                subtitleTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        ((TextView) achievementLayout.findViewWithTag("subtitleFake")).setText(subtitleTextView.getText());
                    }
                });
            }

        }
    }


    void setDismissible(boolean dismissible) {
        this.dismissible = dismissible;
        if (dismissible) {
            achievementLayout.setOnTouchListener(new SwipeDismissTouchListener());
            container.setOnTouchListener(new SwipeDismissTouchListener());
        } else {
            achievementLayout.setOnTouchListener(null);
            container.setOnTouchListener(null);
        }
    }

    private int getTargetWidth(AchievementData data) {

        View textContainerFake = achievementLayout.findViewWithTag("textContainerFake");
        ((TextView) textContainerFake.findViewWithTag("titleFake")).setText(data.getTitle());
        ((TextView) textContainerFake.findViewWithTag("subtitleFake")).setText(data.getSubtitle());
        textContainerFake.measure(MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return textContainerFake.getMeasuredWidth();
    }

    private void buildAchievement() {
        initGlobalFields();
        int padding;
        if (isLarge) {
            initialSize = largeSize;
            padding = paddingLarge;
        } else {
            initialSize = smallSize;
            padding = paddingSmall;
        }
        ((View) icon.getParent()).invalidate();
        icon.setPadding(padding, padding, padding, padding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            container.setElevation(elevation);
        }
        titleTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0) {
                    titleTextView.setVisibility(GONE);
                } else {
                    titleTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        final TextView fakeTitle = ((TextView) achievementLayout.findViewWithTag("titleFake"));
        fakeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0) {
                    fakeTitle.setVisibility(GONE);
                } else {
                    fakeTitle.setVisibility(View.VISIBLE);
                }
            }
        });
        final TextView fakeSubTitle = ((TextView) achievementLayout.findViewWithTag("subtitleFake"));
        fakeSubTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0) {
                    fakeSubTitle.setVisibility(GONE);
                } else {
                    fakeSubTitle.setVisibility(View.VISIBLE);
                }
            }
        });
        subtitleTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0) {
                    subtitleTextView.setVisibility(GONE);
                } else subtitleTextView.setVisibility(View.VISIBLE);
            }
        });
        titleTextView.setAlpha(0f);
        titleTextView.setTranslationY(translationY);
        subtitleTextView.setTranslationY(translationY);
        subtitleTextView.setAlpha(0f);
        container.setScaleY(0f);
        container.setScaleX(0f);
        container.setVisibility(GONE);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        matchParent = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) - margin;
        //stretched = 900;
        //  textContainer.setVisibility(View.GONE);

        View textContainer = achievementLayout.findViewWithTag("textContainer");
        if (textContainer != null) {
            textContainer.setPadding(convertDpToPixel(10) + (initialSize), 0, convertDpToPixel(20), 0);
            achievementLayout.findViewWithTag("textContainerFake").setPadding(textContainer.getPaddingLeft(), textContainer.getPaddingTop(), textContainer.getPaddingRight(), textContainer.getPaddingBottom());
        }
        icon.setMaxWidth(initialSize);
        container.getLayoutParams().width = container.getLayoutParams().height = icon.getLayoutParams().height = icon.getLayoutParams().width = ((View) icon.getParent()).getLayoutParams().height = ((View) icon.getParent()).getLayoutParams().width = initialSize;
        container.requestLayout();


        if (alignTop) {
            mainViewLP.gravity = Gravity.TOP;
        } else {
            mainViewLP.gravity = Gravity.BOTTOM;
        }
        // mainViewLP.width = stretched + (int) elevation;
        if (alignTop && (achievementLayout.getBackground() == null || !(achievementLayout.getBackground() instanceof GradientDrawable))) {
            GradientDrawable scrim = new GradientDrawable();
            scrim.setShape(GradientDrawable.RECTANGLE);
            scrim.setColors(new int[]{0x40000000, 0});
            scrim.setAlpha(0);
            achievementLayout.setBackground(scrim);
            achievementLayout.setClipToPadding(false);
        } else if (!alignTop) {
            achievementLayout.setBackground(null);
        }
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).addView(achievementLayout, mainViewLP);
        added = true;
    }

    private void setTextColor(int textColor) {
        subtitleTextView.setTextColor(Color.rgb(Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
        titleTextView.setTextColor(Color.rgb(Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
    }

    private void setBackground(View v, Drawable d) {
        if (Build.VERSION.SDK_INT >= 16) {
            v.setBackground(d);
        } else v.setBackgroundDrawable(d);
    }

    public AchievementUnlocked createViews() {
        buildAchievement();
        return this;
    }

    private AchievementData[] concat(AchievementData[] a, AchievementData[] b) {
        int aLen = a.length;
        int bLen = b.length;
        AchievementData[] c = new AchievementData[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private final String TAG = "AU";

    public void show(AchievementData... data) {
//Check permission first
        if (VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) {
            Log.e(TAG, "'canDrawOverlays' permission is not granted");
            return;

        }
        //Don't bother if powersaving is on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            isPowerSavingModeOn = powerManager.isPowerSaveMode();
            if (isPowerSavingModeOn) {
                Log.w(TAG, "Power saving is on, AU was canceled");
                return;
            }
        }
        if (data == null || data.length == 0) {
            Log.e(TAG, "Nothing to show");
            return;
        }
        if (added) {
            if (achievements != null) {
                achievements = concat(achievements, data);
            } else
                achievements = data;
            return;
        }
        dismissWithoutAnimation();
        this.achievements = data;
        buildAchievement();
        setContainerBg(achievements[0].getBackgroundColor());
        if (listener != null)
            listener.onViewCreated(this, data);
        prepareMorphism();
    }

    public void dismissWithoutAnimation() {
        removeView();
        if (listener != null)
            listener.onAchievementDismissed(this);
    }

    private void removeListeners(Animator animatorSet) {
        if (animatorSet == null) return;
        if (animatorSet instanceof AnimatorSet && !((AnimatorSet) animatorSet).getChildAnimations().isEmpty())
            for (Animator animator : ((AnimatorSet) animatorSet).getChildAnimations()) {
                removeListeners(animator);
            }
        else {
            if (animatorSet instanceof ValueAnimator) {
                ((ValueAnimator) animatorSet).removeAllUpdateListeners();
            }
            animatorSet.removeAllListeners();
            animatorSet.end();
            animatorSet.cancel();
        }
    }

    private void removeView() {
        if (!added) return;
        index = 0;
        setSwipeEffect(0);

        hasBeenDismissed = false;
        isPowerSavingModeOn = false;
        icon.setVisibility(View.VISIBLE);
        setBackground(((View) icon.getParent()), null);
        setBackground(container, null);
        setBackground(icon, null);
        isLarge = true;
        alignTop = true;
        isRounded = true;
        icon.setOnClickListener(null);
        container.setOnClickListener(null);
        achievementLayout.setOnClickListener(null);
        achievementLayout.setVisibility(View.VISIBLE);
        //    container.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        container.setOnTouchListener(null);
        container.setVisibility(View.VISIBLE);
        container.setTranslationX(0f);
        container.setAlpha(1f);
        achievementLayout.setAlpha(1f);
        setDismissible(false);
        listener = null;
        ((View) icon.getParent()).setBackground(null);
        dismissed = false;
        try {
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).removeView(achievementLayout);
            added = false;
        } catch (Exception e) {
            e.printStackTrace();
            // *shrug emoji*
            //there's no way to check if view is already added to windowManager or not, probably the exception is nullPointerException where achievementLayout is null
            //best thing we could do is to check added boolean
        }
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private int getStartValue(int start) {
        return clamp(start, initialSize, matchParent);
    }

    private int getEndValue(int end) {
        return Math.min(end, matchParent);
    }

    int currentContainerWidth;

    private ValueAnimator getContainerStretchAnimation(int start, int end) {
        final ValueAnimator containerStretch = ValueAnimator.ofInt(getStartValue(start), getEndValue(end));
        containerStretch.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!dismissed) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
                    layoutParams.width = val;
                    currentContainerWidth = val;
                    container.setLayoutParams(layoutParams);
                }
            }
        });
        containerStretch.setInterpolator(interpolator);
        containerStretch.setDuration(animationMultiplier * 300);
        return containerStretch;
    }

    private GradientDrawableWithColors getContainerBg() {
        if ((container.getBackground()) instanceof GradientDrawableWithColors)
            return (GradientDrawableWithColors) (container.getBackground());
        GradientDrawableWithColors iconBackground = new GradientDrawableWithColors();
        if (isRounded)
            iconBackground.setCornerRadius(initialSize / 2);
        else iconBackground.setCornerRadius(convertDpToPixel(2));
        return iconBackground;
    }

    private void setContainerBg(int color) {
        Drawable bgDrawable = container.getBackground();
        if (bgDrawable != null && bgDrawable instanceof GradientDrawable)
            ((GradientDrawableWithColors) bgDrawable).setColor(color);
        else {
            GradientDrawableWithColors iconBackground = getContainerBg();
            iconBackground.setColor(color);
            setBackground(container, iconBackground);
        }
    }

    private int getIconBgColor(int defaultColor) {
        Drawable bgDrawable = ((View) icon.getParent()).getBackground();
        if (bgDrawable != null && bgDrawable instanceof GradientDrawable)
            return ((GradientDrawableWithColors) bgDrawable).getGradientColor();
        return defaultColor;
    }

    private int getContainerBgColor(int defaultColor) {
        Drawable bgDrawable = container.getBackground();
        if (bgDrawable != null && bgDrawable instanceof GradientDrawable)
            return ((GradientDrawableWithColors) bgDrawable).getGradientColor();
        return defaultColor;
    }

    private GradientDrawableWithColors getIconBg() {
        if ((((View) icon.getParent()).getBackground()) instanceof GradientDrawable)
            return (GradientDrawableWithColors) (((View) icon.getParent()).getBackground());
        GradientDrawableWithColors iconBackground = new GradientDrawableWithColors();
        if (isRounded)
            iconBackground.setShape(GradientDrawable.OVAL);
        else iconBackground.setCornerRadius(convertDpToPixel(2));
        return iconBackground;
    }

    private void setIconBg(int color) {
        Drawable bgDrawable = (((View) icon.getParent()).getBackground());
        if (bgDrawable != null && bgDrawable instanceof GradientDrawable)
            bgDrawable.setColorFilter(Color.argb(bgDrawable.getAlpha(), Color.red(color), Color.green(color), Color.blue(color)), PorterDuff.Mode.SRC_IN);
        else {
            GradientDrawableWithColors iconBackground = getIconBg();
            iconBackground.setColor(color);
            setBackground(((View) icon.getParent()), iconBackground);
        }
    }

    private AnimatorSet getExitAnimation() {
        final ObjectAnimator containerScale = ObjectAnimator.ofFloat(container, View.SCALE_X, 1f, 0f);
        containerScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!dismissed)
                    container.setScaleY((float) animation.getAnimatedValue());
            }
        });
        containerScale.setDuration(animationMultiplier * 250);
        containerScale.setStartDelay(100);
        containerScale.setInterpolator(anticipateInterpolator);
        boolean scrimIsAvailable = alignTop && achievementLayout.getBackground() != null;
        ObjectAnimator scrim = null;
        if (scrimIsAvailable) {
            scrim = ObjectAnimator.ofInt(achievementLayout.getBackground(), "alpha", 255, 0);
        }
        AnimatorSet out = new AnimatorSet();
        if (scrim != null)
            out.playTogether(containerScale, scrim);
        else out.play(containerScale);
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(getContainerStretchAnimation(Math.min(container.getMeasuredWidth(), matchParent), initialSize), out);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dismissWithoutAnimation();
            }
        });
        return set;
    }


    private int getContainerBackgroundColor() {
        if ((container).getBackground() != null)
            if ((container).getBackground() instanceof GradientDrawableWithColors)
                return ((GradientDrawableWithColors) (container).getBackground()).getGradientColor();
        return 0xffffffff;
    }

    //  String subtitles[] = data.getSubtitle().split("\\r\\n|\\r|\\n");

    private static int countMatches(final String str, final String sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    private int getSubtitleLines(String subtitleRaw) {
        if (subtitleRaw.contains("\n"))
            return countMatches(subtitleRaw, "\n") + 1;
        return 1;
    }

    private boolean isBlank(final String cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    private boolean allClear(AnimatorSet[] sets) {
        for (AnimatorSet set : sets) {
            if (set == null) return false;
        }
        return true;
    }

    private AnimatorSet morphData() {
        AnimatorSet sets = new AnimatorSet();
        AchievementData data = achievements[index];
//        String subtitleRaw = data.getSubtitle();
//        String subtitles[] = null;
//        int lines = getSubtitleLines(subtitleRaw);
//        if (lines > 1)
//            subtitles = subtitleRaw.split("\\r\\n|\\r|\\n");
//
//        AnimatorSet[] animatorSetArrayList = null;
//        if (subtitles != null) {
//            animatorSetArrayList = new AnimatorSet[lines];
//            final boolean multiLine = data.isMultiLine();
//            for (int i = 0; i < subtitles.length; i++) {
//
//                String s = subtitles[i];
//                if (isBlank(s)) continue;
//                AchievementData freshData = AchievementData.copyFrom(data);
//                freshData.setMultiLine(true);
//                freshData.setSubtitle(s);
//                if (freshData.getCurrentMorphingLine() == null)
//                    freshData.setCurrentMorphingLine(MorphingLine.FIRST);
//                else if (i == subtitles.length - 1)
//                    freshData.setCurrentMorphingLine(MorphingLine.LAST);
//                else freshData.setCurrentMorphingLine(INTERMEDIATE);
//
//
//                animatorSetArrayList[i] = animateData(freshData);
//
//            }
//        }
//        if (lines > 1 && animatorSetArrayList != null && allClear(animatorSetArrayList)) {
//            sets.playSequentially((Animator[]) animatorSetArrayList);
//        } else sets.play(animateData(achievements[index]));

        sets.play(animateData(achievements[index]));
        sets.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (AchievementUnlocked.this.achievements != null && !hasBeenDismissed && AchievementUnlocked.this.achievements.length > 0 && index + 1 < AchievementUnlocked.this.achievements.length) {
                    index++;
                    morphData().start();
                } else
                    getExitAnimation().start();
            }
        });
        return sets;

    }


    private AnimatorSet animateData(final AchievementData data) {
        final AnimatorSet backgroundAnimators = new AnimatorSet();
        final AnimatorSet inAnimation = new AnimatorSet();
        final AnimatorSet outAnimation = new AnimatorSet();
        final AnimatorSet result = new AnimatorSet();

        ObjectAnimator titleIn = null, subtitleIn = null, titleOut = null, subtitleOut = null;
        if ((container.getTag() != null && container.getTag() != data)) {
            int previousBgColor = 0xffffffff;
            int previousIconBgColor = 0x30ffffff;
            if (index == 0) {
                previousBgColor = data.getBackgroundColor();
                previousIconBgColor = data.getIconBackgroundColor();
            } else if (index > 0 && index < achievements.length) {
                previousBgColor = achievements[index - 1].getBackgroundColor();
                previousIconBgColor = achievements[index - 1].getIconBackgroundColor();
            }
            ValueAnimator iconBgColor = ValueAnimator.ofInt(getIconBgColor(previousIconBgColor), data.getIconBackgroundColor());
            iconBgColor.setEvaluator(new ArgbEvaluator());
            iconBgColor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (!dismissed)
                        setIconBg((int) animation.getAnimatedValue());
                }
            });
            ValueAnimator bgColor = ValueAnimator.ofInt(getContainerBgColor(previousBgColor), data.getBackgroundColor());
            bgColor.setEvaluator(new ArgbEvaluator());
            bgColor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (!dismissed)
                        setContainerBg((int) animation.getAnimatedValue());
                }
            });
            bgColor.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (index > 0) setIcon(data);
                }
            });

            backgroundAnimators.play(iconBgColor).with(bgColor);
            backgroundAnimators.setInterpolator(interpolator);
            backgroundAnimators.setDuration(animationMultiplier * 300);


        }
        titleIn = ObjectAnimator.ofFloat(titleTextView, View.TRANSLATION_Y, translationY, 0);
        titleIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (dismissed) return;
                titleTextView.setAlpha(animation.getAnimatedFraction());
            }
        });
        titleIn.setDuration(animationMultiplier * 300);
        titleIn.setInterpolator(interpolator);

        titleOut = ObjectAnimator.ofFloat(titleTextView, View.TRANSLATION_Y, 0, translationY);
        titleOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (dismissed) return;
                titleTextView.setAlpha(1f - animation.getAnimatedFraction());
            }
        });
        titleOut.setInterpolator(accelerateInterpolator);


        final int startScrollingDelay = 800;
        ValueAnimator stretch = getContainerStretchAnimation(container.getMeasuredWidth(), getTargetWidth(data));

        if (data.getSubtitle() != null) {
            subtitleIn = ObjectAnimator.ofFloat(subtitleTextView, View.TRANSLATION_Y, translationY, 0);
            subtitleIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (dismissed) return;
                    subtitleTextView.setAlpha(animation.getAnimatedFraction());
                }
            });
            subtitleIn.setInterpolator(interpolator);
            subtitleIn.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    subtitleTextView.stopScrolling();

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    subtitleTextView.stopScrolling();
                    if ((matchParent - initialSize) < getTargetWidth(data))
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                subtitleTextView.startScrolling();

                            }
                        }, startScrollingDelay);


                }
            });
            subtitleIn.setStartDelay(150);
            subtitleIn.setInterpolator(interpolator);
            subtitleIn.setDuration(animationMultiplier * 300);
        }
        //use previousWidth better than real-time measuring to increase performance

        if (data.getSubtitle() != null) {
            AnimatorSet textViews = new AnimatorSet();
            if (titleIn != null)
                textViews.playTogether(titleIn, subtitleIn);
            else textViews.playTogether(subtitleIn);

            inAnimation.play(stretch).with(backgroundAnimators).before(textViews);
        } else {
            if (titleIn != null)
                inAnimation.play(stretch).with(backgroundAnimators).before(titleIn);
            else inAnimation.playTogether(backgroundAnimators, stretch);

        }
        // inAnimation.setInterpolator(interpolator);
        if (data.getSubtitle() != null) {
            subtitleOut = ObjectAnimator.ofFloat(subtitleTextView, View.TRANSLATION_Y, 0, translationY);
            subtitleOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (dismissed) return;
                    subtitleTextView.setAlpha(1f - animation.getAnimatedFraction());
                }
            });
            subtitleOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    subtitleTextView.stopScrolling();

                }
            });
            subtitleOut.setInterpolator(accelerateInterpolator);

        }

        if (data.getSubtitle() != null) {
            if (titleOut != null) {
                titleOut.setStartDelay(150 * animationMultiplier);
                outAnimation.playTogether(subtitleOut, titleOut);
            } else outAnimation.play(subtitleOut);
        } else {
            if (titleOut != null)
                outAnimation.play(titleOut);
        }
        final String title = data.getTitle(), subtitle = data.getSubtitle();
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (listener != null)
                    listener.onAchievementMorphed(AchievementUnlocked.this, data);
                if (data.getPopUpOnClickListener() != null || dismissible) {
                    mainViewLP.flags = focusable;
                } else {
                    mainViewLP.flags = nonFocusable;
                }
                container.setOnClickListener(data.getPopUpOnClickListener());
                if (added)
                    ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(achievementLayout, mainViewLP);


                subtitleTextView.setText(subtitle);
                titleTextView.setText(title);
                setTextColor(data.getTextColor());
            }


        });


        boolean isBig = getEndValue(container.getMeasuredWidth()) >= matchParent;
        ScrollTextView fake = ((ScrollTextView) achievementLayout.findViewWithTag("subtitleFake"));
        fake.setText(data.getSubtitle());
        int duration = readingDelay;
        if ((matchParent) < getTargetWidth(data))
            duration = fake.getDuration() + startScrollingDelay;
        outAnimation.setStartDelay(duration);
        outAnimation.setDuration(animationMultiplier * 300);
        result.playSequentially(inAnimation, outAnimation);
        result.setInterpolator(interpolator);
        container.setTag(data);
        return result;
    }

    private void prepareMorphism() {
        if (achievements == null || achievements.length == 0)
            return;
        index = 0;
        AnimatorSet scene = new AnimatorSet();
        scene.playSequentially(getEntranceAnimation(achievements[0]), morphData());

        scene.start();
    }

    private AnimatorSet getEntranceAnimation(final AchievementData data) {
        final int iconBG = data.getIconBackgroundColor();
        // final Drawable iconDrawable = data.getIcon();
        // final int bg = data.getBackgroundColor();
        //ValueAnimator stretch = getContainerStretchAnimation(initialSize, getTargetWidth(data));
        ObjectAnimator containerScale = ObjectAnimator.ofFloat(container, View.SCALE_X, 0f, 1f);
        containerScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (dismissed) return;
                container.setScaleY((float) animation.getAnimatedValue());
            }
        });
        containerScale.setDuration(animationMultiplier * 250);
        containerScale.setInterpolator(overshootInterpolator);
        boolean scrimIsAvailable = alignTop && achievementLayout.getBackground() != null;
        ObjectAnimator scrim = null;
        if (scrimIsAvailable) {
            scrim = ObjectAnimator.ofInt(achievementLayout.getBackground(), "alpha", 0, 255);
        }
        AnimatorSet set = new AnimatorSet();
        if (scrim != null)
            set.playTogether(containerScale, scrim);
        else set.play(containerScale);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (Color.alpha(iconBG) > 0) {
                    setIconBg(iconBG);
                } else {
                    View textContainer = (View) titleTextView.getParent();
                    textContainer.setPadding((isLarge ? largeSize : smallSize), textContainer.getPaddingTop(), textContainer.getPaddingRight(), textContainer.getPaddingBottom());
                    achievementLayout.findViewWithTag("textContainerFake").setPadding(textContainer.getPaddingLeft(), textContainer.getPaddingTop(), textContainer.getPaddingRight(), textContainer.getPaddingBottom());
                }
                container.setVisibility(View.VISIBLE);
                setIcon(data);
            }
        });
        return set;
    }

    private boolean hasBeenDismissed = false;

    private void setIcon(AchievementData data) {
        if (data == null) {
            //  icon.setDrawable(null);
            return;
        }
        if (data.getState() == AchievementIconViewStates.SAME_DRAWABLE)
            return;
        Drawable d = data.getIcon();
        if (d != null) {

            if (data.getState() == AchievementIconViewStates.FADE_DRAWABLE)
                icon.fadeDrawable(d);
            else icon.setDrawable(d);

        } else icon.setDrawable(null);
    }

    private class SwipeDismissTouchListener implements View.OnTouchListener {
        private int mSlop;
        private int mMinFlingVelocity;
        private int mMaxFlingVelocity;
        private long mAnimationTime;
        private float mDownX;
        private boolean mSwiping;
        private float mTranslationX;
        private Runnable end;

        SwipeDismissTouchListener() {
            ViewConfiguration vc = ViewConfiguration.get(container.getContext());
            mSlop = vc.getScaledTouchSlop();
            mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
            mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
            mAnimationTime = container.getContext().getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
            end = new Runnable() {
                @Override
                public void run() {
                    hasBeenDismissed = true;
                    if (alignTop)
                        achievementLayout.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                achievementLayout.setVisibility(GONE);



                            }
                        }).start();
                        //no scrim in bottom-aligned achievements
                    else achievementLayout.setVisibility(GONE);
                }
            };
        }


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            motionEvent.offsetLocation(mTranslationX, 0);
            float deltaX = (motionEvent.getRawX() - mDownX);

            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    mDownX = motionEvent.getRawX();
                    view.onTouchEvent(motionEvent);
                    return false;
                }
                case MotionEvent.ACTION_UP: {

                    if (container.getAlpha() == 0) {
                        dismissWithoutAnimation();
                        return true;
                    }

                    boolean dismiss = false;
                    boolean dismissRight = false;

                    int spaceToEdge = ((achievementLayout.getWidth() - container.getWidth()) / 2);
                    float swipePercentage = Math.abs(mTranslationX / spaceToEdge);


                    if (swipePercentage>=0.5f) {
                        dismiss = true;
                        dismissRight = deltaX > 0;
                    }
                    if (dismiss) {
                        ObjectAnimator translation = ObjectAnimator.ofFloat(container, View.TRANSLATION_X, container.getTranslationX(), dismissRight ? container.getMeasuredWidth() : -container.getMeasuredWidth());
                        translation.addUpdateListener(new AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                setSwipeEffect((float) animation.getAnimatedValue());

                            }
                        });
                        translation.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (end != null) end.run();
                            }
                        });
                        translation.setInterpolator(interpolator);
                        translation.setDuration(mAnimationTime);
                        translation.start();
                        dismissed = true;
                    } else {
                        ObjectAnimator translation = ObjectAnimator.ofFloat(container, View.TRANSLATION_X, container.getTranslationX(), 0);
                        translation.addUpdateListener(new AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                setSwipeEffect((float) animation.getAnimatedValue());
                            }
                        });
                        translation.setDuration(mAnimationTime);
                        translation.setInterpolator(interpolator);
                        translation.start();

                        dismissed = false;
                    }
                    mTranslationX = 0;
                    mDownX = 0;
                    mSwiping = false;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {

                    if (Math.abs(deltaX) > mSlop) {
                        mSwiping = true;
                        container.getParent().requestDisallowInterceptTouchEvent(true);
                        MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                                (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        container.onTouchEvent(cancelEvent);
                    }
                    if (mSwiping) {
                        mTranslationX = deltaX;
                        setSwipeEffect(mTranslationX);
                        return true;
                    }
                    break;
                }
            }
            return false;
        }
    }


    private void setSwipeEffect(float amount) {

        container.setTranslationX(amount);
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return;
//        tmpRect.set(Math.abs(Math.round(amount)), 0, container.getWidth() - Math.abs(Math.round(amount)), initialSize);
//        //if (Math.abs(amount) > container.getWidth()) return;
//
//        tmpRect.offset(Math.round(amount), 0);
//        if (amount > 0 && tmpRect.left < (container.getWidth() - icon.getWidth())) {
//            ((View) icon.getParent()).setTranslationX(tmpRect.left);
//            ((View) titleTextView.getParent()).setTranslationX(tmpRect.left);
//
//        }
//
//        if (tmpRect.width() < initialSize) return;
//
//        container.setOutlineProvider(viewOutlineProvider);
//        container.setClipToOutline(true);
//        if (amount == 0) {
//            container.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
//            ((View) icon.getParent()).setTranslationX(0);
//            ((View) titleTextView.getParent()).setTranslationX(0);
//
//        }

    }

}

/**
 * Class that holds the data to be displayed
 * Created by Darkion Avey
 */
class AchievementData {
    private String title = "", subtitle;
    private Drawable icon;
    private int textColor = 0xff000000, backgroundColor = 0xffffffff, iconBackgroundColor = 0x0;
    private View.OnClickListener onClickListener;
    AchievementIconViewStates state = null;

    AchievementData setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public static AchievementData copyFrom(AchievementData data) {
        AchievementData result = new AchievementData();
        result.setTitle(data.getTitle());
        result.setSubtitle(data.getSubtitle());
        result.setIcon(data.getIcon());
        result.setState(data.getState());
        result.setBackgroundColor(data.getBackgroundColor());
        result.setIconBackgroundColor(data.getIconBackgroundColor());
        result.setTextColor(data.getTextColor());
        result.setPopUpOnClickListener(data.getPopUpOnClickListener());

        return result;
    }

    View.OnClickListener getPopUpOnClickListener() {
        return onClickListener;
    }

    AchievementData setPopUpOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    int getTextColor() {
        return textColor;
    }

    AchievementData setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    String getTitle() {
        return title;
    }

    AchievementData setTitle(String title) {
        this.title = title;
        return this;
    }

    String getSubtitle() {
        return subtitle;
    }

    public AchievementIconViewStates getState() {
        return state;
    }

    public void setState(AchievementIconViewStates state) {
        this.state = state;
    }

    Drawable getIcon() {

        return icon;
    }

    AchievementData setIcon(Drawable icon) {
        this.icon = icon;
        return this;
    }

    int getBackgroundColor() {
        return backgroundColor;
    }

    AchievementData setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    int getIconBackgroundColor() {
        return iconBackgroundColor;
    }

    AchievementData setIconBackgroundColor(int iconBackgroundColor) {
        this.iconBackgroundColor = iconBackgroundColor;
        return this;
    }
}

/**
 * Adapter for listener
 */
abstract class AchievementListenerAdapter implements AchievementListener {
    @Override
    public void onAchievementDismissed(AchievementUnlocked achievement) {
    }

    @Override
    public void onViewCreated(AchievementUnlocked achievement, AchievementData[] data) {
    }

    @Override
    public void onAchievementMorphed(AchievementUnlocked achievement, AchievementData data) {
    }
}

/*
Ticker textView
 */
@SuppressLint("AppCompatCustomView")
class ScrollTextView extends TextView {
    public ScrollTextView(Context context) {
        super(context);
        init();
    }

    public ScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(3);
        setHorizontalFadingEdgeEnabled(true);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);


    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != VISIBLE) {
            setSelected(false);
        } else setSelected(true);
    }

    /**
     * Get duration of marquee, roughly.
     *
     * @return assumed duration
     */
    public int getDuration() {
        if (getLayout() == null) measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        final float density = getContext().getResources().getDisplayMetrics().density;
        float dpPerSec = 30 * density;
        float textWidth = getLayout().getLineWidth(0);
        final float gap = textWidth / 3.0f;
        int result = Math.round(
                (textWidth - gap) / dpPerSec);
        return result > 0 ? result * 1000 : 2000;
    }


    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (alpha <= 0.1f) {
            stopScrolling();
        }
    }

    public void stopScrolling() {
        ((View) getParent()).requestFocus();
        setSelected(false);
    }

    public void startScrolling() {
        requestFocus();
        setSelected(true);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopScrolling();
    }
}

/**
 * GradientDrawable that saves the drawable colors; used for AU background
 */
class GradientDrawableWithColors extends GradientDrawable {
    private int color;

    int getGradientColor() {
        return color;
    }

    @Override
    public void setColor(int argb) {
        super.setColors(new int[]{argb, argb});
        color = argb;
    }

    @Override
    public void setColors(int[] colors) {
        super.setColors(colors);
        color = colors[0];
    }
}

/**
 * ImageView that animates drawable change. It also scales
 * according to the drawable size to make sure it doesn't clip
 */
@SuppressLint("AppCompatCustomView")
class AchievementIconView extends ImageView {
    public AchievementIconView(Context context) {
        super(context);
    }

    public enum AchievementIconViewStates {
        FADE_DRAWABLE, SAME_DRAWABLE
    }


    public void setDrawable(final Drawable drawable) {
        if (drawable == null) {
            setImageDrawable(null);
            return;
        }
        if (getScaleType() != ScaleType.CENTER_CROP) setScaleType(ScaleType.CENTER_CROP);

        final float scaleX = 3.5f / (getMaxWidth() / drawable.getIntrinsicWidth());
        final float scaleY = 3.5f / (getMaxWidth() / drawable.getIntrinsicHeight());

        if (getDrawable() == null) {
            setImageDrawable(drawable);
            setScaleX(1 / Math.max(scaleX, scaleY));
            setScaleY(1 / Math.max(scaleX, scaleY));
        } else {
            if (drawable.getAlpha() < 255)
                drawable.setAlpha(255);

            animate().scaleX(0f).setDuration(200 * AchievementUnlocked.animationMultiplier).scaleY(0f).alpha(0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animate().setDuration(200 * AchievementUnlocked.animationMultiplier).scaleX(1 / Math.max(scaleX, scaleY)).scaleY(1 / Math.max(scaleX, scaleY)).alpha(1f).withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            setImageDrawable(drawable);
                        }
                    }).start();
                }
            }).start();
        }
    }

    public void fadeDrawable(final Drawable drawable) {
        if (drawable == null) {
            setImageDrawable(null);
            return;
        }
        if (getScaleType() != ScaleType.CENTER_CROP) setScaleType(ScaleType.CENTER_CROP);

        final float scaleX = 3.5f / (getMaxWidth() / drawable.getIntrinsicWidth());
        final float scaleY = 3.5f / (getMaxWidth() / drawable.getIntrinsicHeight());

        if (getDrawable() == null) {
            setImageDrawable(drawable);
            setScaleX(1 / Math.max(scaleX, scaleY));
            setScaleY(1 / Math.max(scaleX, scaleY));
        } else {
            if (drawable.getAlpha() < 255)
                drawable.setAlpha(255);

            animate().setDuration(50 * AchievementUnlocked.animationMultiplier).alpha(0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animate().setDuration(50 * AchievementUnlocked.animationMultiplier).alpha(1f).withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            setImageDrawable(drawable);
                        }
                    }).start();
                }
            }).start();
        }
    }

}

/* used by the abstract class adapter */
interface AchievementListener {
    void onViewCreated(AchievementUnlocked achievement, AchievementData[] data);

    void onAchievementMorphed(AchievementUnlocked achievement, AchievementData data);

    void onAchievementDismissed(AchievementUnlocked achievement);
}

enum MorphingLine {
    FIRST, LAST, INTERMEDIATE
}

class DeceleratingInterpolator implements TimeInterpolator {

    private int mBase;
    private final float mLogScale;

    DeceleratingInterpolator(int base) {
        mBase = base;
        mLogScale = 1f / computeLog(1, mBase);
    }

    private static float computeLog(float t, int base) {
        return (float) -Math.pow(base, -t) + 1;
    }

    @Override
    public float getInterpolation(float t) {
        return computeLog(t, mBase) * mLogScale;
    }
}