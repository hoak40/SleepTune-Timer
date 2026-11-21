package com.hoak40.sleeptunetimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public final class TimerScheduler {
    private static final String PREFS = "timer_prefs";
    private static final String KEY_DEADLINE = "deadline";
    private static final String KEY_MUTE = "mute_media";
    private static final String KEY_LOCK = "lock_screen";
    static final String ACTION_FIRE = "com.hoak40.sleeptunetimer.ACTION_FIRE";

    private TimerScheduler() { }

    static long getDeadline(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_DEADLINE, 0L);
    }

    static boolean shouldMute(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_MUTE, false);
    }

    static boolean shouldLock(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOCK, false);
    }

    static boolean canUseExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarm != null && alarm.canScheduleExactAlarms();
    }

    static void start(Context context, int minutes, boolean mute, boolean lock) {
        if (minutes < 1 || minutes > 720) throw new IllegalArgumentException("Minutes out of range");
        cancelAlarm(context);
        long deadline = System.currentTimeMillis() + minutes * 60_000L;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_DEADLINE, deadline).putBoolean(KEY_MUTE, mute).putBoolean(KEY_LOCK, lock).commit();
        schedule(context, deadline);
    }

    static void cancel(Context context) {
        cancelAlarm(context);
        clear(context);
    }

    static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY_DEADLINE).remove(KEY_MUTE).remove(KEY_LOCK).commit();
    }

    static void rescheduleIfNeeded(Context context) {
        long deadline = getDeadline(context);
        if (deadline == 0L) return;
        if (deadline <= System.currentTimeMillis()) {
            clear(context);
            return;
        }
        schedule(context, deadline);
    }

    private static void schedule(Context context, long deadline) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;
        PendingIntent intent = pendingIntent(context);
        try {
            if (canUseExactAlarms(context)) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadline, intent);
            } else {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadline, intent);
            }
        } catch (SecurityException ignored) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadline, intent);
        }
    }

    private static void cancelAlarm(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) alarm.cancel(pendingIntent(context));
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, TimerReceiver.class).setAction(ACTION_FIRE);
        return PendingIntent.getBroadcast(context, 50, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
