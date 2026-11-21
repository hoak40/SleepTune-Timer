# SleepTune Timer

An offline Android sleep timer that pauses music apps playing in the background. No screen locking or forcibly closing apps.

## Features

- Preset durations: 15, 30, 45, 60, 90 or 120 minutes; custom duration from 1 to 720 minutes.
- Pauses active media sessions after the timer expires.
- Optional mute-media fallback: lowers media volume to zero, without automatic restoration.
- Uses exact alarms when allowed; falls back to approximate alarms.
- Automatically re-schedules an active timer after a device restart.

## Permissions

Enable Notification access for SleepTune Timer to let it see and pause active media sessions. Enable Alarms and reminders for more accurate timing on Android 12+. Notification permission is optional for the timer completion alert.

Some apps might ignore the Android pause command. On some devices, aggressive battery optimizations can delay alarms.

## Build APK

Open the **Actions** tab and choose **Build Android APK**. Select **Run workflow** (or push to the main branch). After successful completion, download the **SleepTune-Timer-debug-apk** artifact and install app-debug.apk on Android.

The workflow uses Java 17, Android SDK 36, Gradle 8.13 and Android Gradle Plugin 8.13.2.

## Build locally

Open the project with Android Studio, using JDK 17, or run **gradle :app:assembleDebug** with the Android SDK installed.

## Privacy

No Internet permission, advertising, accounts or tracking services. Settings are stored on-device. Android 8.0+ supported. Device testing still required.
