# ProgressViewRhythm
[![](https://jitpack.io/v/markizdeviler/ProgressViewRhythm.svg)](https://jitpack.io/#markizdeviler/ProgressViewRhythm)

 Demo progress library for "Rhythm" progress
	
![giphy](https://raw.githubusercontent.com/markizdeviler/ProgressViewRhythm/master/screens/progress.gif)

## Usage	

### Step 1 
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    compile 'com.github.markizdeviler:ProgressViewRhythm:1.0.5'
}
``` 

### Step 2
``` xml
<com.example.rhythmprogressview.RhythmProgressView
    android:id="@+id/progressView"
    android:layout_centerInParent="true"
    android:layout_width="50dp"
    android:layout_height="50dp" />
```

``` xml 
//to change color in xml
app:animationDarkColor="your dark color"
app:animationLightColor="your light color"

```

``` kotlin 
//to change color in java
progressView.setStyle(R.color.your_light_color, R.color.your_dark_color)

```
--------
#### Any Issues & contributions appreciated
