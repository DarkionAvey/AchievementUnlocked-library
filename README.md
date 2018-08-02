<img src="/preview.gif" width="300" align="right" alt="AchievementUnlocked app demo" hspace="20">
<h1>AchievementUnlocked</h1>
<p>Animated pop-ups inspired by XBOX. </p>
<a href="https://github.com/DarkionAvey/AchievementUnlocked-library/blob/master/app/app-release.apk?raw=true">Download APK (for Android KitKat+)</a>

<h3>License</h3>
<p>GPL (for the companion App)</p>
<p>Library (MIT)</p>

<h3>How to add to my app</h3>
<p>Copy <a href="https://raw.githubusercontent.com/DarkionAvey/AchievementUnlocked-library/master/app/src/main/java/net/darkion/achievementUnlockedApp/AchievementUnlocked.java">AchievementUnlocked.Java</a> class to your app. That's it!</p> <br>
<h3>How to show popups</h3>
<ol>
  <li>Initialize achievementUnlocked object
 
```java
AchievementUnlocked achievementUnlocked = new AchievementUnlocked(getApplicationContext());
```
</li>
  <li>Customize appearance
  
```java
  achievementUnlocked.setRounded(rounded).setLarge(large).setTopAligned(top).setDismissible(dismiss)
```   
</li>
<li>Supply the aforementioned object with (array of) AcheievementData

```java

AchievementData data0 = new AchievementData();
data0.setTitle(title); 
data0.setSubTitle(title);
data0.setIcon(iconFinal);  
data0.setTextColor(textColor);
data0.setIconBackgroundColor(iconBG);
data0.setBackgroundColor(bg);
data0.setPopUpOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 
                }
            });

```
</li>
<li>Pop your data!

```java
achievementUnlocked.show(data0, data1, data2, data3);
```
</li>
</ol> 

<h2>Phones with large status bars and/or Android Oreo+</h2>

```java
achievementUnlocked.setNotchMode(Integer statusBarHeight)
```

Use the above method to vertically shift top-aligned popups. 'statusBarHeight' will be the vertical shift of the popup. If you supply a null value, 'status_bar_height' dimen from 'android' package will be used instead. You don't need to use the ViewCompat.setOnApplyWindowInsetsListener to get the statusbar height since the popup will be drawn over all apps and using the hardcoded status bar height is sufficient. This is the default behaviour on devices running Android Oreo+ since it prevents apps from drawing over the status bar. 

<h2>New: control subtitle scrolling speed</h2>
In the latest version, new method to control scrolling speed has been added. To set speed rate, use the code below:

```java
achievementUnlocked.setScrollingPxPerSeconds(float);
```

Higher values will result in faster scrolling. This method should be used alongside setReadingDelay method to assign rates that best fit your application nature

