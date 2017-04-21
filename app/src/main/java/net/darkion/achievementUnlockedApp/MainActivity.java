package net.darkion.achievementUnlockedApp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;


public class MainActivity extends Activity {
    String running = "Service is running";
    String runningNot = "Service is not running";
    Toast hint;
    boolean isLarge = false, isTop = true;
    boolean isRounded = false;
    int initialSize;
    LogDecelerateInterpolator interpolator = new LogDecelerateInterpolator(30, 0);
    boolean needToChangeRadius = false, needToChangeHeight = false, needToChangeLocation = false;

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public void androidM() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);

        }

    }


    @Override
    protected void onDestroy() {
        if (donationProcessor != null) donationProcessor.destroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!donationProcessor.isOfAnInterest(requestCode, resultCode, data))
            if (requestCode == 123) {
                if (Build.VERSION.SDK_INT >= 23)
                    if (!android.provider.Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "Couldn't get the permission, terminating process", Toast.LENGTH_LONG).show();
                        finish();
                    }
            }
    }

    public boolean checkNotificationEnabled() {
        return !NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext()).contains(getPackageName());
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRequirements() {
        if (VERSION.SDK_INT >= 23) {

            if (!Settings.canDrawOverlays(getApplicationContext())) {
                new Builder(this)
                        .setMessage("Starting from Android 6, " + getResources().getString(R.string.app_name) + " needs permission to display pop-ups on top of other apps. Tap on \"enable\" to fix the problem")
                        .setPositiveButton("Enable", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                androidM();
                            }
                        })
                        .setCancelable(false)
                        .show();
                return false;
            }
        }


        if (checkNotificationEnabled()) {
            new Builder(this)

                    .setMessage("Please give this app the permission to read notifications")
                    .setPositiveButton("Enable", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        }
                    })
                    .setCancelable(false)
                    .show();
            return false;

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        checkRequirements();
        checkService();
//
//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
        findViewById(R.id.container).requestFocus();

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.preview).setClipToOutline(true);
        } else findViewById(R.id.test).setVisibility(View.GONE);

        ImageView wallpaper = (ImageView) findViewById(R.id.wallpaper);

        final View v = (View) wallpaper.getParent();

        v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init();


            }
        });

        Switch master = (Switch) findViewById(R.id.master);
        //yes onClick not onChecked
        master.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkRequirements()) {
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    if (hint != null) hint.cancel();
                    hint = Toast.makeText(getApplicationContext(), "Toggle AchievementUnlocked!", Toast.LENGTH_LONG);
                    hint.show();
                }
            }
        });

        final BeatingImageView heart = (BeatingImageView) findViewById(R.id.heart);
        heart.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_fg));

        final ImageView wow = (ImageView) findViewById(R.id.wow);
        wow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_fg));
        final ScrollView scrollView = ((ScrollView) findViewById(R.id.scrollView));
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                heart.beatTheHeart(scrollY);
                wow.setRotation(scrollY / 10f);
            }
        });

        updateBlackList();

//show immersive, edge-to-edge ads
        final View mainView = findViewById(R.id.main);
        mainView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //adview supplies false width measurements on landscape orientation, so don't bother
                if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) return;

                float max = getResources().getDimension(R.dimen.max_width);
                float w = Math.min(max, mainView.getMeasuredWidth());
                View adView = findViewById(R.id.adView);
                float scale = w / adView.getMeasuredWidth() * 1f;

                if (scale < 1f || (scale * adView.getMeasuredWidth()) > max) return;
                adView.setScaleX(scale);
                adView.setScaleY(scale);

                View adCard = findViewById(R.id.adCard);
                adCard.getLayoutParams().height *= scale;

                adCard.requestLayout();

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkService();
        refreshPreview();
        if (donationProcessor != null) donationProcessor.onResume();
    }

    private void checkService() {

        Switch master = (Switch) findViewById(R.id.master);
        boolean runningService = isServiceRunning();
        boolean settingsToggle = NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext()).contains(getApplicationContext().getPackageName());
        if (!runningService && settingsToggle) {
            startService(new Intent(this, NotificationService.class));

        } else if (runningService && !settingsToggle) {
            stopService(new Intent(this, NotificationService.class));

        }
        runningService = isServiceRunning();

        if (runningService) {
            master.setText(running);

        } else {
            master.setText(runningNot);
        }
        master.setChecked(runningService);

    }

    private static int convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    private void init() {
        Switch top = ((Switch) findViewById(R.id.top));
        Switch rounded = ((Switch) findViewById(R.id.rounded));
        Switch large = ((Switch) findViewById(R.id.large));
        Switch dismiss = ((Switch) findViewById(R.id.swipeToDismiss));
        Switch clickable = ((Switch) findViewById(R.id.clickable));
        Switch persistent = ((Switch) findViewById(R.id.persistent));


        boolean topValue = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("top", isTop);
        boolean roundedValue = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("rounded", isRounded);
        boolean largeValue = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("large", isLarge);
        boolean dismissValue = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("dismiss", true);
        boolean clickableValue = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("clickable", true);
        boolean persistentValue = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("persistent", true);


        top.setChecked(topValue);
        rounded.setChecked(roundedValue);
        large.setChecked(largeValue);
        dismiss.setChecked(dismissValue);
        clickable.setChecked(clickableValue);
        toggleRounded(roundedValue);
        toggleLarge(largeValue);
        toggleTop(topValue);
        persistent.setChecked(persistentValue);
        large.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("large", isChecked).apply();
                toggleLarge(isChecked);

            }
        });
        rounded.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("rounded", isChecked).apply();
                toggleRounded(isChecked);
            }
        });

        top.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("top", isChecked).apply();
                toggleTop(isChecked);

            }
        });

        dismiss.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("dismiss", isChecked).apply();

            }
        });
        clickable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("clickable", isChecked).apply();

            }
        });
        persistent.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("persistent", isChecked).apply();

                stopService(new Intent(MainActivity.this, NotificationService.class));

                startService(new Intent(MainActivity.this, NotificationService.class));


            }
        });


        donationProcessor = new DonationProcessor().bindActivity(this);
        final ImageView addBlackList = (ImageView) findViewById(R.id.addBlackList);
        addBlackList.setTag(false);
        addBlackList.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.bottom_chin_fab_inactive), Mode.SRC_IN);
        addBlackList.setRotation(45f);
        addBlackList.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.bottom_chin_fab_icon), Mode.SRC_IN);
        final EditText blackListedPhrases = (EditText) findViewById(R.id.blackListedPhrases);
        blackListedPhrases.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int fromBg = -1, toBg = -1, fromIcon = -1, toIcon = -1;
                if (s != null && s.length() > 0 && s.toString().trim().length() > 0) {
                    if (addBlackList.getRotation() != 0) {
                        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                            addBlackList.animate().translationZ(getResources().getDimension(R.dimen.fab_elevation)).setInterpolator(interpolator).rotation(0f).start();
                        } else
                            addBlackList.animate().setInterpolator(interpolator).rotation(0f).start();

                        fromBg = ContextCompat.getColor(getApplicationContext(), R.color.bottom_chin_fab_inactive);
                        toBg = ContextCompat.getColor(getApplicationContext(), R.color.accent);
                        fromIcon = ContextCompat.getColor(getApplicationContext(), R.color.bottom_chin_fab_icon);
                        toIcon = ContextCompat.getColor(getApplicationContext(), R.color.accent_fg);
                    }
                } else {
                    if (addBlackList.getRotation() != 45) {
                        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                            addBlackList.animate().translationZ(0f).setInterpolator(interpolator).rotation(45f).start();
                        } else
                            addBlackList.animate().setInterpolator(interpolator).rotation(45f).start();
                        toBg = ContextCompat.getColor(getApplicationContext(), R.color.bottom_chin_fab_inactive);
                        fromBg = ContextCompat.getColor(getApplicationContext(), R.color.accent);
                        toIcon = ContextCompat.getColor(getApplicationContext(), R.color.bottom_chin_fab_icon);
                        fromIcon = ContextCompat.getColor(getApplicationContext(), R.color.accent_fg);
                    }
                }
                if ((fromBg != -1 || toBg != -1) && (fromIcon != -1 || toIcon != -1) && !(boolean) addBlackList.getTag()) {
                    ValueAnimator icon = ValueAnimator.ofInt(fromIcon, toIcon);
                    icon.setEvaluator(new ArgbEvaluator());
                    icon.setInterpolator(interpolator);
                    icon.addUpdateListener(new AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            addBlackList.setColorFilter((int) animation.getAnimatedValue(), Mode.SRC_IN);
                        }
                    });

                    ValueAnimator anim = ValueAnimator.ofInt(fromBg, toBg);
                    anim.setEvaluator(new ArgbEvaluator());
                    anim.setInterpolator(interpolator);
                    anim.addUpdateListener(new AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            addBlackList.getBackground().setColorFilter((int) animation.getAnimatedValue(), Mode.SRC_IN);
                        }
                    });
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            addBlackList.setTag(true);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            addBlackList.setTag(false);

                        }
                    });
                    anim.start();
                    icon.start();
                }
            }
        });
        addBlackList.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (addBlackList.getRotation() != 0) return false;
                String text = blackListedPhrases.getText().toString();
                if (text.length() > 0 && text.trim().length() > 0) {
                    text = "regex " + text;
                    SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
                    String key = "blackList";
                    String previousValues = preferences.getString(key, null);
                    if (previousValues != null && !previousValues.contains(text)) {

                        previousValues = previousValues + "\n" + text;
                    } else previousValues = text;

                    preferences.edit().putString(key, previousValues).apply();
                    blackListedPhrases.setText(null);
                    updateBlackList();
                    return true;
                }
                return false;
            }
        });
        addBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addBlackList.getRotation() != 0) return;
                String text = blackListedPhrases.getText().toString();
                if (text.length() > 0 && text.trim().length() > 0) {
                    SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
                    String key = "blackList";
                    String previousValues = preferences.getString(key, null);
                    if (previousValues != null && !previousValues.contains(text)) {

                        previousValues = previousValues + "\n" + text;
                    } else previousValues = text;

                    preferences.edit().putString(key, previousValues).apply();
                    blackListedPhrases.setText(null);
                    updateBlackList();
                }
            }
        });
    }


    private void updateBlackList() {
        final SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        final String previousValues = preferences.getString("blackList", null);


        LinearLayout blackListEntries = (LinearLayout) findViewById(R.id.blackListEntries);
        TransitionManager.beginDelayedTransition(blackListEntries);
        blackListEntries.setVisibility(View.GONE);
        if (previousValues == null) {
            blackListEntries.removeAllViews();

            return;
        }
        //tag all views with false
        for (int i = 0; i < blackListEntries.getChildCount(); i++) {
            blackListEntries.getChildAt(i).setTag(false);
        }
        if (previousValues.contains("\n")) {
            String[] split = previousValues.split("\n");
            for (int i = 0; i < blackListEntries.getChildCount(); i++) {
                for (int x = 0; x < split.length; x++) {
                    String s = split[x];
                    TextView view = (TextView) blackListEntries.getChildAt(i);
                    if (view.getText().toString().equalsIgnoreCase(s)) {
                        //tag view with true if it has a blacklist entry and null it from array
                        view.setTag(true);
                        split[x] = null;
                        break;
                    }
                }
            }
            for (String s : split) {
                //add new entries
                if (s != null)
                    blackListEntries.addView(getTextViewForBlackList(s));

            }


        } else {
            boolean hadAnAddedView = false;
            for (int i = 0; i < blackListEntries.getChildCount(); i++) {
                TextView view = (TextView) blackListEntries.getChildAt(i);
                if (view.getText().toString().equalsIgnoreCase(previousValues)) {
                    blackListEntries.getChildAt(i).setTag(true);
                    hadAnAddedView = true;
                }
            }
            if (!hadAnAddedView) {
                blackListEntries.addView(getTextViewForBlackList(previousValues));

            }

        }

        for (int i = 0; i < blackListEntries.getChildCount(); i++) {
            if (!(boolean) blackListEntries.getChildAt(i).getTag()) {
                blackListEntries.removeView(blackListEntries.getChildAt(i));
            }

        }
        blackListEntries.setVisibility(View.VISIBLE);

    }

    public static boolean isRegEx(String s) {
        return s.startsWith("regex ");
    }

    private TextView getTextViewForBlackList(final String text) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent_fg));
        textView.setText(text);

        if (isRegEx(text)) {
            textView.setText(text.substring("regex ".length()));
            Drawable regexDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.regex);
            regexDrawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent_fg), Mode.SRC_IN);
            textView.setCompoundDrawablesWithIntrinsicBounds(regexDrawable, null, null, null);
            textView.setCompoundDrawablePadding(convertDpToPixel(5));
        }


        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            textView.setElevation(convertDpToPixel(2));
        }
        textView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
                String previousValue = preferences.getString("blackList", null);
                if (previousValue == null)
                    throw new IllegalArgumentException("No views should be visible when blackList is empty");

                String newValue = null, text = ((TextView) v).getText().toString();
                if (previousValue.contains("\n") && ((ViewGroup) v.getParent()).getChildCount() > 1) {
                    if (((TextView) v).getCompoundDrawables()[0] != null) {
                        text = "regex " + text;
                    }
                    newValue = previousValue.replace("\n" + text, "");
                }
                preferences.edit().putString("blackList", newValue).apply();

                updateBlackList();
                return true;
            }
        });
        Drawable bg = ContextCompat.getDrawable(getApplicationContext(), R.drawable.chip);
        bg.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.accent), Mode.SRC);
        textView.setBackground(bg);
        textView.setPadding(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel(10));
        MarginLayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.leftMargin = convertDpToPixel(5);
        textView.setLayoutParams(layoutParams);
        textView.setTag(true);
        return textView;
    }

    //You can delete this part since it's for donation handling
    DonationProcessor donationProcessor;

    private void toggleRounded(boolean isChecked) {
        isRounded = isChecked;
        needToChangeRadius = true;
        refreshPreview();
    }

    private void toggleTop(boolean isChecked) {
        isTop = isChecked;
        needToChangeLocation = true;
        refreshPreview();
    }

    private void toggleLarge(boolean isChecked) {
        isLarge = isChecked;
        needToChangeHeight = true;
        refreshPreview();
    }

    private void setBackground(View v, Drawable d) {
        v.setBackground(d);
    }

    private void moveContainer(float y, boolean animated) {
        final View container = findViewById(R.id.container);
        y = clamp(y, getResources().getDimensionPixelOffset(R.dimen.margin), ((View) container.getParent()).getMeasuredHeight() - container.getMeasuredHeight() - getResources().getDimensionPixelOffset(R.dimen.margin));
        if (animated)
            container.animate().setInterpolator(interpolator).translationY(y);
        else container.setTranslationY(y);
    }

    public void test(View v) {

        if (checkRequirements()) {
            NotificationCompat.Builder allSet =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_mood).setColor(wallpaperColor)
                            .setContentTitle("You're all set; enjoy");

            NotificationCompat.Builder morph =
                    new NotificationCompat.Builder(this).setColor(ContextCompat.getColor(getApplicationContext(), R.color.accent))
                            .setSmallIcon(R.drawable.ic_mood)
                            .setContentTitle("Did you know?")
                            .setContentText("Popups can scroll if the text is too long");
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.cancel(0);
            notificationManager.cancel(1);
            notificationManager.notify(0, morph.build());
            notificationManager.notify(1, allSet.build());


        }
    }

    public void goToDarkion(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://darkion.net/index/0-3"));
        startActivity(browserIntent);
    }

    public void goToGitHub(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DarkionAvey/AchievementUnlocked-library"));
        startActivity(browserIntent);
    }

    private void refreshPreview() {

        if (isLarge) {
            initialSize = (int) getResources().getDimension(R.dimen.large);
        } else {
            initialSize = (int) getResources().getDimension(R.dimen.small);
        }
        animateLocation();

        animateCorner();
        animateHeight();
        Bitmap bitmap = ((BitmapDrawable) WallpaperManager.getInstance(this).getDrawable()).getBitmap();
        ImageView wallpaper = (ImageView) findViewById(R.id.wallpaper);
        wallpaper.setImageBitmap(bitmap);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                int textColor = Color.BLACK, iconBG;
                try {
                    if (palette.getVibrantSwatch() != null) {
                        textColor = palette.getVibrantSwatch().getTitleTextColor();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ImageView icon = (ImageView) findViewById(R.id.achievement_icon);

                iconBG = Color.argb(notificationIconBgAlpha, Color.red(textColor), Color.green(textColor), Color.blue(textColor));
                icon.setColorFilter(Color.rgb(Color.red(textColor), Color.green(textColor), Color.blue(textColor)), PorterDuff.Mode.SRC_IN);
                ((TextView) findViewById(R.id.title)).setTextColor(Color.rgb(Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
                ((TextView) findViewById(R.id.subtitle)).setTextColor(Color.argb(180, Color.red(textColor), Color.green(textColor), Color.blue(textColor)));


                setIconBg(iconBG);
                wallpaperColor = palette.getVibrantColor(ContextCompat.getColor(getApplicationContext(), R.color.bg));
                setContainerBg(wallpaperColor);


            }


        });
    }

    public static int notificationIconBgAlpha = 25;
    int wallpaperColor;

    private void animateHeight() {
        if (!needToChangeHeight) return;
        needToChangeHeight = false;

        int large = (int) getResources().getDimension(R.dimen.large);
        int small = (int) getResources().getDimension(R.dimen.small);

        final View container = findViewById(R.id.container);
        final ImageView icon = (ImageView) findViewById(R.id.achievement_icon);


        ValueAnimator valueAnimator = ValueAnimator.ofInt(container.getMeasuredHeight(), isLarge ? large : small);
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                container.getLayoutParams().height = (int) animation.getAnimatedValue();
                icon.getLayoutParams().height = icon.getLayoutParams().width = ((View) icon.getParent()).getLayoutParams().height = ((View) icon.getParent()).getLayoutParams().width = (int) animation.getAnimatedValue();

                container.requestLayout();
                if (!needToChangeLocation) {
                    if (!isTop) {
                        moveContainer(((View) container.getParent()).getMeasuredHeight() - container.getMeasuredHeight() - getResources().getDimensionPixelOffset(R.dimen.margin), false);
                    }
                }
            }
        });
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.start();

        float scale = 0.98f;
        if (isLarge) scale = 1.2f;
        icon.animate().setInterpolator(interpolator).scaleY(scale).scaleX(scale).start();

    }

    private void animateLocation() {
        if (!needToChangeLocation) return;
        needToChangeLocation = false;
        final View container = findViewById(R.id.container);

        if (isTop) {
            moveContainer(getResources().getDimensionPixelOffset(R.dimen.margin), true);
        } else {
            moveContainer(((View) container.getParent()).getMeasuredHeight() - container.getMeasuredHeight() - getResources().getDimensionPixelOffset(R.dimen.margin), true);
        }

    }

    private void animateCorner() {
        if (!needToChangeRadius) return;
        needToChangeRadius = false;
        int from, to;
        if (isRounded) {
            from = getResources().getDimensionPixelOffset(R.dimen.card_radius);
            to = initialSize / 2;
        } else {
            to = getResources().getDimensionPixelOffset(R.dimen.card_radius);
            from = initialSize / 2;
        }
        final View icon = findViewById(R.id.achievement_icon_bg);
        final View container = findViewById(R.id.container);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (icon.getBackground() instanceof GradientDrawable)
                    ((GradientDrawable) icon.getBackground()).setCornerRadius((int) animation.getAnimatedValue());
                if (container.getBackground() instanceof GradientDrawable)
                    ((GradientDrawable) container.getBackground()).setCornerRadius((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.start();
    }

    private GradientDrawable getIconBg() {
        GradientDrawable iconBackground = new GradientDrawable();
        if (isRounded)
            iconBackground.setCornerRadius(initialSize / 2);
        else
            iconBackground.setCornerRadius(getResources().getDimensionPixelOffset(R.dimen.card_radius));
        return iconBackground;
    }

    private void setIconBg(int color) {
        View icon = findViewById(R.id.achievement_icon_bg);
        GradientDrawable iconBackground = getIconBg();
        iconBackground.setColor(color);
        setBackground(icon, iconBackground);
    }

    private GradientDrawable getContainerBg() {
        GradientDrawable iconBackground = new GradientDrawable();
        if (isRounded)
            iconBackground.setCornerRadius(initialSize / 2);
        else
            iconBackground.setCornerRadius(getResources().getDimensionPixelOffset(R.dimen.card_radius));

        return iconBackground;
    }

    private void setContainerBg(int color) {
        View container = findViewById(R.id.container);

        GradientDrawable iconBackground = getContainerBg();
        iconBackground.setColor(color);
        setBackground(container, iconBackground);


        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            container.setElevation(getResources().getDimensionPixelOffset(R.dimen.elevation));
        }

    }

}


