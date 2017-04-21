<img src="/preview.gif" width="300" align="right" alt="AchievementUnlocked app demo" hspace="20">
<h1>AchievementUnlocked</h1>
<p>Animated pop-ups inspired by XBOX. </p>
<a href="https://github.com/DarkionAvey/AchievementUnlocked-library/blob/master/app/app-release.apk?raw=true">Download APK (for Android KitKat+)</a>

<h3>License</h3>
<p>GPL (App)</p>
<p>Library (MIT)</p>
<h3>How to add to my app</h3>
<p>Copy <a href="https://raw.githubusercontent.com/DarkionAvey/AchievementUnlocked-library/master/app/src/main/java/net/darkion/achievementUnlockedApp/AchievementUnlocked.java">AchievementUnlocked.Java</a> class to your app. That's it!</p> <br>
<h3>How to pop pop-ups</h3>
<ol>
  <li>Initialise achievementUnlocked object
 
```java
AchievementUnlocked achievementUnlocked = new AchievementUnlocked(getApplicationContext());
```
</li>
  <li>Customise appearance
  
```java
  achievementUnlocked.setRounded(rounded).setLarge(large).setTopAligned(top).setDismissible(dismiss)
```   
</li>
<li>Supply the aforementioned object with AcheievementData

```java

AchievementData data = new AchievementData();
data.setTitle(title); 
data.setSubTitle(title);
data.setIcon(iconFinal);  
data.setTextColor(textColor);
data.setIconBackgroundColor(iconBG);
data.setBackgroundColor(bg);
data.setPopUpOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 
                }
            });

```
</li>
<li>Pop your data!

```java
achievementUnlocked.show(data,data1,data2,data3);
```
</li>
</ol> 

