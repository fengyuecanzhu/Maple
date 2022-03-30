# Maple

A Java hook framwork (Xposed style) for Android Runtime (ART) used [LSPlant](https://github.com/LSPosed/LSPlant).

## Features

* Support Android 5.0 - 13 (API level 21 - 33)
* Support armeabi-v7a, arm64-v8a, x86, x86-64
* Xposed style hook api

## Usage

#### 1、Before usage

Import method:
Add the maven repository in your build.gradle(Project) (new version Android Studio please in settings.gradle):

```groovy
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

Then import the framework in your build.gradle(app):

```groovy
dependencies {
    implementation "me.fycz.maple:maple:1.7"
}
```

#### 2、Usage in codes

All APIs are xposed style, you can use as simple as using xposed.

kotlin:

```kotlin
MapleUtils.findAndHookMethod(
    Activity::class.java,
    "onCreate",
    Bundle::class.java,
    object : MethodHook() {
        override fun beforeHookedMethod(param: MapleBridge.MethodHookParam) {
            //TODO: Hook before the method onCreate in the Activity is called.
        }
    }
)
```

java:

```java
MapleUtils.findAndHookMethod(
    Activity.class,
    "onCreate",
    Bundle.class,
    new MethodHook() {
        @Override
    	public void afterHookedMethod(MapleBridge.MethodHookParam param) throws Throwable {
            //TODO: Hook after the method onCreate in the Activity is called.
        }
    }
);
```

## Credits

Inspired by the following frameworks:

- [LSPlant](https://github.com/LSPosed/LSPlant)
- [Dobby](https://github.com/LSPosed/Dobby)
- [Pine](https://github.com/canyie/Pine)
- [XposedBridge](https://github.com/rovo89/XposedBridge) 

