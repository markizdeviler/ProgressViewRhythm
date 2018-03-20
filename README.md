# ProgressViewRhythm
[![](https://jitpack.io/v/markizdeviler/ProgressViewRhythm.svg)](https://jitpack.io/#markizdeviler/ProgressViewRhythm)

 Demo progress library for "Rhythm" progress
	
![giphy](https://user-images.githubusercontent.com/22816503/37585433-dff58d62-2b7a-11e8-9c3e-f8d2e94bbce2.gif)


## Usage	

### Step 1 
```android
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	compile 'com.github.markizdeviler:ProgressViewRhythm:1.0.3'
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

``` java 
//to change color in java
animationView.setStyle(R.color.your_light_color, R.color.your_dark_color)

```


``` java
//to start animation
animationView.show();

//to start animation smoothly
animationView.smoothToShow();

//to hide animation smoothly
animationView.smoothToHide();

//to hide animation
animationView.hide();

```

--------
#### Any Issues & contributions appreciated
