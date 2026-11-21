# SleepTune Timer

A simple **offline Android sleep timer** that pauses background music and optionally locks the screen when the countdown ends, useful for video apps such as TikTok.

## Features

- Preset timers: 15, 30, 45, 60, 90 and 120 minutes; custom 1–720 minutes.
- Pauses playing media sessions and sends a general media-pause command when needed.
- **Optional screen lock at timeout** (for TikTok or other foreground video apps). Screen turns off and the phone is locked when the timer expires. Unlock using your normal screen lock.
- Optional mute fallback: sets media volume to zero. You need to manually raise media volume afterwards.
- Precise alarm when the permission is enabled, approximate alarm otherwise.
- Reschedules active alarms when the phone restarts.

## Permissions

**Notification access:** Required to pause media sessions from other apps reliably. The app does not need Internet permission.

**Alarms and reminders (Android 12+):** Enables the most accurate timing; otherwise uses an inexact fallback.

**Device administrator (optional):** Required **only** when you turn on the **Tự khóa màn hình khi hết giờ (TikTok, video)** switch. The app requests Android's standard Device Admin permission with only the `force-lock` policy; then calls `DevicePolicyManager.lockNow()` at timeout. It does **not** close TikTok directly and does not change your lock-screen PIN or password. You can revoke its device administrator access in your Android settings.

**Notifications (Android 13+):** Optional completion notification.

Some media apps ignore pause requests. Enabling the screen lock option helps with video apps that stop playback when the phone screen locks. Check behavior on your specific device.

## Build APK via GitHub Actions

Open **Actions → Build Android APK → Run workflow**. Download the successful run's artifact **SleepTune-Timer-debug-apk** (containing `app-debug.apk`).

The workflow uses the Android SDK already on GitHub's Ubuntu runner rather than `android-actions/setup-android`. It installs API 36 and uses Java 17 / Gradle 8.13 / Android Gradle Plugin 8.13.2.

## Build in Android Studio

Import this project and use JDK 17 with Android SDK 36.

## Privacy

This app has no network or ad-tracking permissions and stores all timer settings locally. The project has not yet been confirmed to compile and run on a physical phone.
