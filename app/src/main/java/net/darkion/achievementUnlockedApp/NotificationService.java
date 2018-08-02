package net.darkion.achievementUnlockedApp;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import net.darkion.achievementUnlockedApp.AchievementIconView.AchievementIconViewStates;

import java.util.ArrayList;
import java.util.Collections;

import static android.support.v4.app.NotificationCompat.CATEGORY_STATUS;
import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;
import static net.darkion.achievementUnlockedApp.R.drawable.notification;

public class NotificationService extends NotificationListenerService {
    private ArrayList<String> shownNotificationsIDs = new ArrayList<>();
    private ArrayList<ArrayList<String>> shownNotificationsText = new ArrayList<>();
    private ArrayList<String> shownNotificationsTitle = new ArrayList<>();
    private ArrayList<String> blackList = new ArrayList<>();
    private String currentIconId = "system";
    private int currentTextColor = -1;
    private AchievementUnlocked achievementUnlocked;
    public static String pendingIntentExtra = "fromPersistentNotification";
    private final static String TAG = "DA";
    private final static boolean DEBUG = false;

    /**
     * Initialization
     */
    @Override
    public void onCreate() {
        super.onCreate();
        boolean persistent = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("persistent", true);


        MainActivity.createNotificationChannels(this);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, MainActivity.serviceChannelId);

        if (persistent) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.putExtra(pendingIntentExtra, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, Notification.FLAG_ONGOING_EVENT, notificationIntent, 0);


            Notification mNotification = notificationBuilder
                    .setOngoing(true).setPriority(PRIORITY_MIN)
                    .setSmallIcon(notification)
                    .setColor(getResources().getColor(R.color.accent))
                    .setCategory(CATEGORY_STATUS)
                    .setContentTitle("Service is running")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Some ROMs such as MIUI and EMUI have aggressive RAM management to conserve battery life. Showing a persisting notification will prevent that. It is only recommended that you disable this if your phone is running vanilla Android. You can disable this from AU settings."))
                    .setContentText("This notification prevents the system from killing AU service")
                    .setContentIntent(pendingIntent)
                    .build();

            mNotification.flags |= Notification.FLAG_ONGOING_EVENT;

            startForeground(666, mNotification);
        } else stopForeground(true);
        initAchievement();
    }

    /**
     * Initialization
     */
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private void updateBlackList() {
        String blackListValues = getSharedPreferences("settings", Context.MODE_PRIVATE).getString("blackList", null);
        if (blackListValues != null) {
            blackList.clear();
            if (blackListValues.contains("\n"))
                Collections.addAll(blackList, blackListValues.split("\n"));
            else blackList.add(blackListValues);
        }
    }


    /**
     * Called when new notification is posted to status bar
     */
    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {

//        if (sbn.getNotification().flags == Notification.FLAG_ONGOING_EVENT)
//            return;
//
        updateBlackList();

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            PowerManager powerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
            //don't bother, pop-ups don't animate in power saving mode
            if (powerManager != null && powerManager.isPowerSaveMode()) return;

        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (StatusBarNotification notification : getActiveNotifications()) {
                    registerNotification(notification);
                }
            }
        });
    }

    final Handler mHandler = new Handler();

    //save toast for later use in future
    Toast toast;

    /**
     * Show a toast and cancel any current ones
     *
     * @param text toast
     */
    private void makeToast(String text) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public boolean stopService(Intent name) {
        makeToast("Stopped AchievementUnlocked");

        return super.stopService(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onCreate();
        return START_STICKY;
    }

    @Override
    public ComponentName startService(Intent service) {

        makeToast("Started AchievementUnlocked");
        return super.startService(service);
    }

    /**
     * Get a unique notification ID from a status bar notification
     *
     * @param notification the notification we want an ID from
     * @return Notification ID after app package name
     */
    private String getNotificationId(StatusBarNotification notification) {
        return notification.getPackageName();
    }

    private int regexLength = "regex ".length();

    private boolean isBlackListedWord(String word) {
        for (String s : blackList) {
            if (MainActivity.isRegEx(s) && word.matches(s.substring(regexLength)))
                return true;
            else if (word.toLowerCase().contains(s.toLowerCase())) return true;

        }
        return false;
    }

    /**
     * Called to check if notification has same text as before.
     *
     * @param notification the notification we want to compare its text contents with history
     * @return yes if same text
     */
    private boolean isSameText(StatusBarNotification notification) {
        //don't spam user with progress bar updates
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            if (notification.getNotification().extras.getInt(Notification.EXTRA_PROGRESS) > 0.0)
                return true;
        }

        String title = getTitle(notification), subtitle = getText(notification);
        if (title == null && subtitle == null)
            return true;


        int index = shownNotificationsIDs.indexOf(getNotificationId(notification));

        //index shouldn't be -1

        return (shownNotificationsTitle.get(index) != null || title == null) && (shownNotificationsTitle.get(index) == null || title != null) && (!shownNotificationsText.get(index).isEmpty() || subtitle == null) && shownNotificationsText.get(index).contains(subtitle) && (shownNotificationsTitle.get(index).equalsIgnoreCase(title) || shownNotificationsText.get(index).contains(subtitle));


    }

    /**
     * For debugging purposes, print all notification data
     *
     * @param sbn source notification
     * @return everything inside a notification
     */
    @SuppressWarnings("unused")
    private String printExtras(StatusBarNotification sbn) {
        StringBuilder builder = new StringBuilder();
        //the array extracted using regex, don't worry
        String[] names;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            names = new String[]{
                    Notification.EXTRA_TITLE, Notification.EXTRA_TITLE + ".big",
                    Notification.EXTRA_TEXT, Notification.EXTRA_SUB_TEXT,
                    Notification.EXTRA_INFO_TEXT, Notification.EXTRA_SUMMARY_TEXT,
                    Notification.EXTRA_BIG_TEXT, Notification.EXTRA_SMALL_ICON,
                    Notification.EXTRA_LARGE_ICON, Notification.EXTRA_LARGE_ICON + ".big",
                    Notification.EXTRA_PROGRESS, Notification.EXTRA_PROGRESS_MAX, Notification.EXTRA_PROGRESS_INDETERMINATE,
                    Notification.EXTRA_SHOW_CHRONOMETER, Notification.EXTRA_SHOW_WHEN, Notification.EXTRA_PICTURE,
                    Notification.EXTRA_TEXT_LINES, Notification.EXTRA_TEMPLATE, Notification.EXTRA_PEOPLE,
                    Notification.EXTRA_BACKGROUND_IMAGE_URI,
                    Notification.EXTRA_MEDIA_SESSION,
                    Notification.EXTRA_COMPACT_ACTIONS
            };
        } else {
            names = new String[]{
                    Notification.EXTRA_TITLE, Notification.EXTRA_TITLE + ".big",
                    Notification.EXTRA_TEXT, Notification.EXTRA_SUB_TEXT,
                    Notification.EXTRA_INFO_TEXT, Notification.EXTRA_SUMMARY_TEXT,
                    Notification.EXTRA_SMALL_ICON,
                    Notification.EXTRA_LARGE_ICON, Notification.EXTRA_LARGE_ICON + ".big",
                    Notification.EXTRA_PROGRESS, Notification.EXTRA_PROGRESS_MAX, Notification.EXTRA_PROGRESS_INDETERMINATE,
                    Notification.EXTRA_SHOW_CHRONOMETER, Notification.EXTRA_SHOW_WHEN, Notification.EXTRA_PICTURE,
                    Notification.EXTRA_TEXT_LINES, Notification.EXTRA_PEOPLE

            };
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            for (String name : names) {
                Object o = sbn.getNotification().extras.get(name);
                if (o != null) {
                    builder.append("\n");
                    builder.append(name);
                    builder.append(" ");
                    builder.append(o.toString());
                }
            }
        }
        try {
            builder.append(checkContentView(sbn));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();

    }

    /**
     * For manual text extraction
     *
     * @param sbn source notification
     * @return Every text in a notification
     * @throws Exception Just for backup, never know what exceptions this might rise up while looking for text
     */
    private StringBuilder checkContentView(StatusBarNotification sbn) throws Exception {
        RemoteViews rv = sbn.getNotification().contentView;
        Context context = createPackageContext(sbn.getPackageName(), CONTEXT_IGNORE_SECURITY);
        ViewGroup localView = (ViewGroup) LayoutInflater.from(context).inflate(rv.getLayoutId(), null);
        rv.reapply(getApplicationContext(), localView);
        return checkContentViewLoop(context, localView);
    }

    private StringBuilder checkContentViewLoop(Context context, ViewGroup localView) {
        StringBuilder everything = new StringBuilder();
        for (int i = 0; i < (localView).getChildCount(); ++i) {
            View nextChild = (localView).getChildAt(i);
            try {
                if (nextChild instanceof TextView) {
                    TextView textView = (TextView) nextChild;
                    if (textView.getText() != null || textView.getText().length() > 0) {
                        everything.append("\n");
                        everything.append(context.getResources().getResourceName(nextChild.getId()));
                        everything.append(" ");
                        everything.append(textView.getText().toString());
                    }
                } else if (nextChild instanceof ViewGroup) {
                    StringBuilder builder = checkContentViewLoop(context, (ViewGroup) nextChild);
                    if (builder != null)
                        everything.append(builder.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return everything;

    }

    /**
     * At this current stage we are desperate for a value,
     * so will search each notification's contentView for
     * a textView to extract the text from
     *
     * @param sbn   the notification of interest
     * @param names possible names of textView id's
     * @return the value of textView
     */

    private String getTextViewValue(StatusBarNotification sbn, String... names) {
        try {
            RemoteViews rv = sbn.getNotification().contentView;
            Context context = createPackageContext(sbn.getPackageName(), CONTEXT_IGNORE_SECURITY);
            if (rv != null) {
                ViewGroup localView = (ViewGroup) LayoutInflater.from(context).inflate(rv.getLayoutId(), null);
                rv.reapply(getApplicationContext(), localView);
                return getTextViewContent(context, localView, names);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getTextViewContent(Context context, ViewGroup localView, String... names) {


        for (int i = 0; i < (localView).getChildCount(); ++i) {
            View nextChild = (localView).getChildAt(i);
            try {
                if (nextChild instanceof TextView) {
                    TextView textView = (TextView) nextChild;
                    CharSequence text = textView.getText();
                    String resourceName = context.getResources().getResourceName(nextChild.getId());
                    for (String name : names) {
                        if (resourceName.contains(name))
                            if (text != null && text.length() > 0)
                                return text.toString();
                    }
                } else if (nextChild instanceof ViewGroup) {
                    String value = getTextViewContent(context, (ViewGroup) nextChild, names);
                    if (value != null)
                        return value;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    private boolean isInteresting(StatusBarNotification notification) {

        String title = getTitle(notification), subtitle = getText(notification);
        if (isBlackListedWord(title) || isBlackListedWord(subtitle))
            return false;

        boolean lowPriority =
                (notification.getNotification().priority <= Notification.PRIORITY_LOW);
        if (lowPriority) {
            if (DEBUG) Log.e(TAG, "low priority " + notification.getPackageName());
            return false;
        }

        boolean registered = shownNotificationsIDs.contains(getNotificationId(notification));
        if (!registered) {
            if (DEBUG) Log.e(TAG, "!registered " + notification.getPackageName());
            return true;
        }

        boolean sameText = isSameText(notification);
        if (!sameText) {
            if (DEBUG) Log.e(TAG, "!sameText " + notification.getPackageName());
            return true;
        }

        if (DEBUG) Log.e(TAG, "None of above " + notification.getPackageName());

        return false;

    }


    /**
     * Register the notification in history so that it won't show up again unless its text
     * contents change (aka isInteresting)
     *
     * @param notification the notification to be registered
     */
    private void registerNotification(StatusBarNotification notification) {
        boolean isInteresting = isInteresting(notification);

        if (!isInteresting) return;

        int index = -1;
        if (shownNotificationsIDs.contains(getNotificationId(notification))) {
            index = shownNotificationsIDs.indexOf(getNotificationId(notification));
        }
        if (index > -1) {
            shownNotificationsIDs.set(index, getNotificationId(notification));
            ArrayList<String> text = shownNotificationsText.get(index);
            if (!text.contains(getText(notification))) text.add(getText(notification));
            shownNotificationsText.set(index, text);
            shownNotificationsTitle.set(index, getTitle(notification));
        } else {
            shownNotificationsIDs.add(getNotificationId(notification));
            ArrayList<String> text = new ArrayList<>();
            text.add(getText(notification));
            shownNotificationsText.add(text);
            shownNotificationsTitle.add(getTitle(notification));
        }
        showNotification(notification);
    }

    /**
     * Convenience method to remove alpha from a color
     *
     * @param color input color
     * @return opaque color
     */
    private int getOpaqueColor(int color) {
        return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Build and queue a notification
     *
     * @param sbn notification to be shown
     */
    private void showNotification(final StatusBarNotification sbn) {
        initAchievement();
        final AchievementData data = new AchievementData();
        String subtitle = getText(sbn), title = getTitle(sbn);

        //Don't bother
        if ((subtitle == null || subtitle.isEmpty()) && (title == null || title.isEmpty())) return;

        if ((title == null || title.isEmpty())) {
            title = subtitle;
            subtitle = null;
        }


        data.setTitle(title);
        //Don't show same text
        if (subtitle != null && !title.equalsIgnoreCase(subtitle))
            data.setSubtitle(subtitle);
        Drawable[] drawables = getIcon(sbn);

        final Drawable iconFinal = drawables[0];
        final Drawable packageIcon = drawables[1];

        if (packageIcon == null) return;

        Bitmap bitmap = Bitmap.createBitmap(packageIcon.getIntrinsicWidth(), packageIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        packageIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        packageIcon.draw(canvas);
        int color = getColor(sbn);
        int textColor = 0xffffffff, iconBG, bg = 0xff333333;
        if (color != 0) {
            textColor = getTitleColor(color);
            bg = color;

        } else {
            Palette palette = Palette.from(bitmap).generate();
            try {
                if (palette.getVibrantSwatch() != null) {
                    textColor = palette.getVibrantSwatch().getTitleTextColor();

                }
                bg = palette.getVibrantColor(bg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("shadedIcon", true))
            iconBG = Color.argb(MainActivity.notificationIconBgAlpha, Color.red(textColor), Color.green(textColor), Color.blue(textColor));
        else iconBG = Color.TRANSPARENT;
        if (currentIconId.equals(getIconId(sbn))) {
            if (getOpaqueColor(textColor) != getOpaqueColor(currentTextColor))
                data.setState(AchievementIconViewStates.FADE_DRAWABLE);
            else {
                data.setState(AchievementIconViewStates.SAME_DRAWABLE);
            }
        }
        if (iconFinal != null)
            iconFinal.setColorFilter(getOpaqueColor(textColor), PorterDuff.Mode.SRC_IN);

        data.setIcon(iconFinal);
        data.setTextColor(textColor);
        data.setIconBackgroundColor(iconBG);
        data.setBackgroundColor(bg);
        if (getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("clickable", true))
            data.setPopUpOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (sbn.getNotification().contentIntent != null)
                            sbn.getNotification().contentIntent.send();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        currentIconId = getIconId(sbn);
        currentTextColor = data.getTextColor();
        achievementUnlocked.show(data);

    }

    /**
     * Remove a notification from notification history stack. Called when user removes a notification
     * from notifications area
     *
     * @param notification notification to be removed
     */
    private void removeNotification(StatusBarNotification notification) {
        if (shownNotificationsIDs.contains(getNotificationId(notification))) {
            int index = shownNotificationsIDs.indexOf(getNotificationId(notification));
            shownNotificationsIDs.remove(index);
            shownNotificationsText.remove(index);
            shownNotificationsTitle.remove(index);

        }
    }

    private String getIconId(StatusBarNotification sbn) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            return sbn.getPackageName() + sbn.getNotification().extras.getInt("android.icon");

        return "empty";
    }


    /**
     * Convienience method to return owner app of a notification
     *
     * @param sbn source notification
     * @return app package name
     */
    private String getPackageName(StatusBarNotification sbn) {
        return sbn.getPackageName();
    }

    private Drawable[] getIcon(StatusBarNotification sbn) {
        Drawable icon = null, packageIcon;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Bundle extras = sbn.getNotification().extras;

            //  Bitmap bmp = extras.getParcelable(Notification.EXTRA_LARGE_ICON);
            // this is to use any image supplied by the notification (e.g. profile pictures) but for now, use notification's mini icon
            Bitmap bmp = null;

            if (bmp == null) {
                int iconId = extras.getInt("android.icon");
                if (iconId != -1)
                    try {
                        icon = getApplicationContext().createPackageContext(getPackageName(sbn), CONTEXT_IGNORE_SECURITY).getResources().getDrawable(iconId);
                    } catch (Exception e) {

                        //ignore; possible exceptions:
                        //PackageManager.NameNotFound AND Resources$NotFoundException
                    }
            } else {
                icon = new BitmapDrawable(getApplicationContext().getResources(), bmp);
            }
        }

        try {
            packageIcon = getPackageManager().getApplicationIcon(getPackageName(sbn));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            packageIcon = getResources().getDrawable(R.mipmap.ic_launcher);
        }
        return new Drawable[]{icon, packageIcon};
    }

    private CharSequence getTicker(StatusBarNotification sbn) {
        return sbn.getNotification().tickerText;
    }

    private Object getExtra(StatusBarNotification sbn, String... names) {
        for (String name : names) {
            Bundle extras = sbn.getNotification().extras;
            Object o;
            o = extras.get(name);
            if (o == null) o = extras.getString(name);
            if (o != null)
                return o;
        }

        return null;
    }

    private String getTitle(StatusBarNotification sbn) {


        String title = "";
        Object value = getExtra(sbn, Notification.EXTRA_TITLE_BIG, Notification.EXTRA_TITLE, Notification.EXTRA_SUB_TEXT);
        if (value != null) title = value.toString();

        else {
            String result = getTextViewValue(sbn, "title", "artistalbum");
            if (result != null) title = result;
        }

        return title;
    }

    private String getText(StatusBarNotification sbn) {
        String text = "";


        Object o;
        if (Build.VERSION.SDK_INT >= 21) {
            o = getExtra(sbn, Notification.EXTRA_BIG_TEXT, Notification.EXTRA_TEXT, Notification.EXTRA_SUMMARY_TEXT, Notification.EXTRA_INFO_TEXT, Notification.EXTRA_TEXT_LINES);
        } else if (Build.VERSION.SDK_INT >= 19) {
            o = getExtra(sbn, Notification.EXTRA_TEXT, Notification.EXTRA_SUMMARY_TEXT, Notification.EXTRA_INFO_TEXT);
        } else o = sbn.getNotification().tickerText;
        if (o != null) {
            text = o.toString();
        } else {

            String result = getTextViewValue(sbn, "content", "trackname");
            if (result != null) text = result;
            else {

                if (getTicker(sbn) != null) {
                    text = getTicker(sbn).toString();
                }
            }
        }


        return text;
    }


    private int getColor(StatusBarNotification sbn) {
        if (sbn.getNotification().ledARGB > 0) {
            return sbn.getNotification().ledARGB;
        } else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return sbn.getNotification().color;
        }

        return 0;
    }


    private int getTitleColor(int mRgb) {
        float MIN_CONTRAST = 3.0f;
        // First check white, as most colors will be dark
        int lightBodyAlpha = ColorUtils.calculateMinimumAlpha(
                Color.WHITE, mRgb, MIN_CONTRAST);
        int lightTitleAlpha = ColorUtils.calculateMinimumAlpha(
                Color.WHITE, mRgb, MIN_CONTRAST);

        if (lightBodyAlpha != -1 && lightTitleAlpha != -1) {
            // If we found valid light values, use them and return
            return ColorUtils.setAlphaComponent(Color.WHITE, lightBodyAlpha);
        }

        int darkBodyAlpha = ColorUtils.calculateMinimumAlpha(
                Color.BLACK, mRgb, MIN_CONTRAST);


        if (darkBodyAlpha != -1) {
            // If we found valid dark values, use them and return

            return ColorUtils.setAlphaComponent(Color.BLACK, darkBodyAlpha);
        }

        // If we reach here then we can not find title and body values which use the same
        // lightness, we need to use mismatched values
        if (darkBodyAlpha < 0) darkBodyAlpha = 0;
        else if (darkBodyAlpha > 255) darkBodyAlpha = 255;
        if (lightBodyAlpha < 0) lightBodyAlpha = 0;
        else if (lightBodyAlpha > 255) lightBodyAlpha = 255;
        return lightBodyAlpha != -1
                ? ColorUtils.setAlphaComponent(Color.WHITE, lightBodyAlpha)
                : ColorUtils.setAlphaComponent(Color.BLACK, darkBodyAlpha);


    }


    private void initAchievement() {
        if (achievementUnlocked == null) {
            achievementUnlocked = new AchievementUnlocked(getApplicationContext());
        }

        SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean top = settings.getBoolean("top", true);
        boolean rounded = settings.getBoolean("rounded", false);
        boolean large = settings.getBoolean("large", false);
        boolean dismiss = settings.getBoolean("dismiss", true);

        achievementUnlocked.setReadingDelay(1000);
        achievementUnlocked.setScrollingPxPerSeconds(70);
        achievementUnlocked.setRounded(rounded).setLarge(large).setTopAligned(top).setDismissible(dismiss);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        removeNotification(sbn);
    }


}