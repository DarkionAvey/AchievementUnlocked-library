package net.darkion.achievementUnlockedApp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import net.darkion.AchievementUnlockedLib.AchievementUnlocked;

import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnClickListener {
    public void androidM() {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 123);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Couldn't get the permission, terminating process", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {

            if (!Settings.canDrawOverlays(getApplicationContext()))
                new AlertDialog.Builder(this)

                        .setMessage("Starting from Android 6, " + getResources().getString(R.string.app_name) + " needs permission to display notifications. Click enable to proceed")
                        .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                androidM();
                            }
                        })

                        .show();

        }


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        final ViewGroup mainView = (ViewGroup) findViewById(R.id.container);
        ArrayList<View> buttons = getButtons(mainView);
        for (
                int i = 0;
                i < buttons.size(); i++)

        {
            Button button = (Button) buttons.get(i);
            String title = "Click me";
            switch (i) {
                case 0:
                    title = "Top, small, custom color";
                    break;
                case 1:
                    title = "Top, large, custom font/bg, squared";
                    break;
                case 2:
                    title = "Bottom, small, square";
                    break;
                case 3:
                    title = "Bottom, large";
                    break;

                case 4:
                    title = "Text notification";
                    break;

                case 5:
                    title = "Facebook, different text colors";
                    break;


                case 6:
                    title = "Incoming call, persistent, tap to dismiss";
                    break;
                case 7:
                    title = "Music now playing";
                    break;
                case 8:
                    title = "Ordinary toast with icon";
                    break;

                case 9:
                    title = "Toast with timeout and custom animations";
                    break;

                case 10:
                    title = "Google Play Games log-in";
                    break;

                case 11:
                    title = "Swipe to dismiss";
                    break;
            }
            button.setText(title);
            button.setTag(i);
            button.setOnClickListener(this);
        }

    }


    private static ArrayList<View> getButtons(ViewGroup root) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof Button) {
                views.add(child);
            }


        }
        return views;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        int i = (int) button.getTag();
        switch (i) {
            case 0:
                new AchievementUnlocked(this).setTitle("Lilac and Gooseberries").setSubtitleColor(0x80ffffff).setSubTitle("Find the sorceress").setBackgroundColor(Color.parseColor("#C2185B")).setTitleColor(0xffffffff).setIcon(getDrawableFromRes(R.drawable.wand)).isLarge(false).build().show();
                break;
            case 1:
                new AchievementUnlocked(this).setTitle("Transhumanist").setSubTitle("Augmented").setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "font.TTF")).setIcon(getDrawableFromRes(R.drawable.mario)).setBackgroundDrawable(getDrawableFromRes(R.drawable.custom_bg)).isLarge(true).build().show();
                break;
            case 2:
                new AchievementUnlocked(getApplicationContext()).setTitle("Pacifist").setSubTitle("They don't deserve death").setIcon(getDrawableFromRes(R.drawable.monster)).isRounded(false).isLarge(false).alignTop(false).build().show();
                break;
            case 3:

                new AchievementUnlocked(this).setTitle("I bet mine is bigger").setSubTitle("Upgrade your guns").isLarge(true).alignTop(false).setIcon(getDrawableFromRes(R.drawable.mario)).build().show();
                break;
            case 4:
                new AchievementUnlocked(this).setTitle("The Illusive Man").setSubTitle("Shepard, Freedom's Progress won't investigate itself.").setSubtitleColor(0x80000000).setIcon(getDrawableFromRes(R.drawable.chat)).isRounded(false).setDuration(2500).isLarge(true).build().show();
                break;
            case 5:
                new AchievementUnlocked(this).setTitle("Eliza Cassan posted on your timeline").setSubtitleColor(0xffffffff).setBackgroundColor(Color.parseColor("#3b5998")).setSubTitle("Where can we meet?").setIcon(getDrawableFromRes(R.drawable.fb)).setDuration(2500).setTitleColor(0x80ffffff).isLarge(false).build().show();
                break;
            case 6:
                final AchievementUnlocked achievementUnlocked = new AchievementUnlocked(this).setTitle("Faridah Malik is calling...").setTitleColor(0xff000000).setSubTitle("Tap to reject").setSubtitleColor(0x80000000).setIcon(getDrawableFromRes(R.drawable.calling)).isPersistent(true).build();

                View iconIV = achievementUnlocked.getIconView();
                ObjectAnimator outX = ObjectAnimator.ofFloat(iconIV, "scaleX", 1f, 0.7f);
                ObjectAnimator outY = ObjectAnimator.ofFloat(iconIV, "scaleY", 1f, 0.7f);
                ObjectAnimator inX = ObjectAnimator.ofFloat(iconIV, "scaleX", 0.7f, 1f);
                ObjectAnimator inY = ObjectAnimator.ofFloat(iconIV, "scaleY", 0.7f, 1f);
                final AnimatorSet Outset = new AnimatorSet();
                final AnimatorSet Ani = new AnimatorSet();
                final AnimatorSet Inset = new AnimatorSet();
                outX.setDuration(1000);
                outY.setDuration(1000);
                inX.setDuration(1000);
                inY.setDuration(1000);
                Ani.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Ani.start();
                    }
                });
                Outset.playTogether(outX, outY);
                Inset.playTogether(inX, inY);
                (achievementUnlocked.getAchievementView()).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        achievementUnlocked.dismiss();

                    }
                });
                Ani.play(Outset).before(Inset);
                achievementUnlocked.setAchievementListener(new AchievementUnlocked.achievementListener() {
                    @Override
                    public void onAchievementBeingCreated(AchievementUnlocked achievement, boolean created) {

                    }

                    @Override
                    public void onAchievementExpanding(AchievementUnlocked achievement, boolean expanded) {
                        if (expanded) Ani.start();

                    }

                    @Override
                    public void onAchievementShrinking(AchievementUnlocked achievement, boolean shrunken) {
                        if (!shrunken) {
                            if (Ani.isRunning())
                                Ani.cancel();
                        }


                    }

                    @Override
                    public void onAchievementBeingDestroyed(AchievementUnlocked achievement, boolean destroyed) {

                    }
                });
                achievementUnlocked.show();
                break;
            case 7:

                new AchievementUnlocked(this).setTitle("Ghosttown").setIconBG(Color.parseColor("#E67E22")).setBackgroundColor(Color.parseColor("#D35400")).setTitleColor(0xffffffff).setSubTitle("Madonna").setSubtitleColor(0x80ffffff).setIcon(getDrawableFromRes(R.drawable.music)).setDuration(1500).isLarge(false).build().show();


                break;


            case 8:

                AchievementUnlocked toast = new AchievementUnlocked(this).setTitle("Picture was sent").setBackgroundColor(Color.parseColor("#333333")).setTitleColor(0xffffffff).setIcon(getDrawableFromRes(R.drawable.tick)).setDuration(1000).alignTop(false).isLarge(false).build();
                toast.show();
                break;


            case 9:
                int bg = 0xffffffff;
                boolean rounded = false;
                if (Build.VERSION.SDK_INT >= 21) {
                    rounded = true;
                }

                final AchievementUnlocked toastCool =
                        new AchievementUnlocked(this).isRounded(rounded).setBackgroundColor(bg).isPersistent(true).setTitle("Message is about to be sent").setSubTitle("Tap to cancel, or wait 3 sec until it's sent ").setIcon(getDrawableFromRes(R.drawable.delete)).isPersistent(true).build();


                final View popUp = toastCool.getAchievementView();

                final View icon = toastCool.getIconView();
                int delta = toastCool.height;
                final ObjectAnimator transout = ObjectAnimator.ofFloat(icon, "translationY", 0, delta);
                final ObjectAnimator transin = ObjectAnimator.ofFloat(icon, "translationY", -delta, 0);
                transin.setDuration(250);
                transout.setDuration(250);
                final ValueAnimator blue = ValueAnimator.ofObject(new ArgbEvaluator(), bg, 0xff2196F3);
                blue.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            popUp.getBackground().setTint((Integer) animator.getAnimatedValue());
                        } else {
                            popUp.setBackgroundColor((Integer) animator.getAnimatedValue());
                        }
                    }

                });

                final ValueAnimator red = ValueAnimator.ofObject(new ArgbEvaluator(), bg, 0xffF44336);
                red.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            popUp.getBackground().setTint((Integer) animator.getAnimatedValue());
                        } else {
                            popUp.setBackgroundColor((Integer) animator.getAnimatedValue());
                        }
                    }

                });
                popUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (toastCool.expanded && !blue.isRunning()) {
                            toastCool.dismiss();
                            red.start();
                            transout.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (Build.VERSION.SDK_INT >= 16) {
                                        icon.setBackground(getDrawableFromRes(R.drawable.no));
                                    } else {
                                        icon.setBackgroundDrawable(getDrawableFromRes(R.drawable.no));
                                    }
                                    transin.start();

                                }
                            });
                            transout.start();
                        }

                    }
                });

                toastCool.setAchievementListener(new AchievementUnlocked.achievementListener() {
                    @Override
                    public void onAchievementBeingCreated(AchievementUnlocked achievement, boolean created) {

                    }

                    @Override
                    public void onAchievementExpanding(AchievementUnlocked achievement, boolean expanded) {
                        if (expanded)

                            (new android.os.Handler()).postDelayed(new Runnable() {
                                public void run() {
                                    if (toastCool.expanded && !red.isRunning()) {
                                        popUp.setOnClickListener(null);
                                        toastCool.dismiss();
                                        blue.start();

                                        transout.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                if (Build.VERSION.SDK_INT >= 16) {
                                                    icon.setBackground(getDrawableFromRes(R.drawable.send));
                                                } else {
                                                    icon.setBackgroundDrawable(getDrawableFromRes(R.drawable.send));
                                                }

                                                transin.start();
                                            }
                                        });
                                        transout.start();
                                    }
                                }
                            }, 3000);
                    }

                    @Override
                    public void onAchievementShrinking(AchievementUnlocked achievement, boolean shrunken) {
                        achievement.getTitleTextView().setVisibility(View.INVISIBLE);

                        achievement.getSubtitleTextView().setVisibility(View.INVISIBLE);

                    }

                    @Override
                    public void onAchievementBeingDestroyed(AchievementUnlocked achievement, boolean destroyed) {

                    }
                });


                toastCool.show();

                break;

            case 10:

                AchievementUnlocked gpg = new AchievementUnlocked(this).setTitle("Welcome").setTitleColor(0x70444444).setSubTitle("Jane Valderamma").setSubtitleColor(0xff444444).setIcon(getDrawableFromRes(R.drawable.gpg)).setDuration(1500).isLarge(false).build();
                final View iconView = gpg.getIconView();
                final Drawable iconViewDefaultBackground = gpg.getIconView().getBackground();
                final ObjectAnimator out = ObjectAnimator.ofFloat(iconView, "alpha", 1f, 0f);
                final ObjectAnimator in = ObjectAnimator.ofFloat(iconView, "alpha", 0f, 1f);

                gpg.setAchievementListener(new AchievementUnlocked.achievementListener() {
                    @Override
                    public void onAchievementBeingCreated(AchievementUnlocked achievement, boolean created) {

                    }

                    @Override
                    public void onAchievementExpanding(AchievementUnlocked achievement, boolean expanded) {
                        if (expanded) {

                            out.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (Build.VERSION.SDK_INT >= 16) {
                                        iconView.setBackground(getDrawableFromRes(R.drawable.jane));
                                    } else {
                                        iconView.setBackgroundDrawable(getDrawableFromRes(R.drawable.jane));

                                    }
                                    in.start();
                                }
                            });
                            out.start();

                        }
                    }

                    @Override
                    public void onAchievementShrinking(AchievementUnlocked achievement, boolean shrunken) {

                        if (!shrunken) {

                            out.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (Build.VERSION.SDK_INT >= 16) {
                                        iconView.setBackground(iconViewDefaultBackground);
                                    } else
                                        iconView.setBackgroundDrawable(iconViewDefaultBackground);

                                    in.start();
                                }
                            });
                            out.start();
                        }

                    }

                    @Override
                    public void onAchievementBeingDestroyed(AchievementUnlocked achievement, boolean destroyed) {

                    }
                });
                gpg.show();

                break;

            case 11:

                final AchievementUnlocked swipeToDismissPopUp = new AchievementUnlocked(this).setTitle("You can swipe to dismiss this pop-up").
                        setSubTitle("Swipe to left or right").setIcon(getDrawableFromRes(R.drawable.monster)).isRounded(false).
                        isPersistent(true).alignTop(false).
                        isLarge(false).build();


                final ViewGroup viewGroup = (ViewGroup) swipeToDismissPopUp.getAchievementView().getRootView();
                viewGroup.setOnTouchListener(new SwipeDismissTouchListener(swipeToDismissPopUp.getAchievementView(), null, new SwipeDismissTouchListener.OnDismissCallback() {
                    @Override
                    public void onDismiss(View view, Object token) {
                        swipeToDismissPopUp.dismissWithoutAnimation();


                    }
                }));


                swipeToDismissPopUp.show();


                break;


        }
    }


    public void howTo(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://darkion.net/news/achievementunlocked_library_documentation/2015-07-28-86"));
        startActivity(i);
    }

    public void download(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bitbucket.org/DarkionAvey/achievementunlocked-library"));
        startActivity(i);
    }

    private Drawable getDrawableFromRes(int ResID) {
        if (Build.VERSION.SDK_INT >= 21) getDrawable(ResID);
        return getResources().getDrawable((ResID));
    }
}


