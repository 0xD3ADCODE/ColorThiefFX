# ColorThiefFX

Calculates **Dominant color** or representative **Color palette** from an ```JavaFX Image```

This is ```JavaFX``` port of an [AWT version](https://github.com/SvenWoltmann/color-thief-java) that is a very fast
Java port of [Lokesh Dhakar's JavaScript version](http://lokeshdhakar.com/projects/color-thief/)

This port exist for one reason: use only ```JavaFX Image``` implementation for calculations without touching
```AWT's BufferedImage```. It will be helpfull in cases where ```AWT``` may not be available
(for example, in ```GraalVM Native Image``` mode on **Windows**) or if you want to use only native ```JavaFX```
implementations

## Dependency

Add new repository into your `build.gradle`:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add dependency into your `build.gradle`:
```groovy
dependencies {
    implementation 'com.github.0xD3ADCODE:ColorThiefFX:{version or commit hash}'
}
```

## Examples

```java
Color dominantColor = ColorThief.getDominantColor(image); // Calculate dominant color
Color dominantBorderColor = ColorThief.getDominantBorderColor(image, borderSize); // Calculate dominant color for border
```

## Speed comparisons
 - [Lokesh Dhakar's JavaScript version](http://lokeshdhakar.com/projects/color-thief/): 29.84 ms
 - [AWT version](https://github.com/SvenWoltmann/color-thief-java): 0.712 ms 
 - This version: 0.717 ms + less **RAM** consumption on ```JavaFX Image``` > ```AWT BufferedImage``` conversions via ```Swing```

## Thanks
* Lokesh Dhakar - for the original [Color Thief JavaScript version](http://lokeshdhakar.com/projects/color-thief/)
* SvenWoltmann - for [AWT version](https://github.com/SvenWoltmann/color-thief-java)