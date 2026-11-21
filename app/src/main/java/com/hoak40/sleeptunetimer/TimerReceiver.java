package com.hoak40.sleeptunetimer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class TimerReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "sleeptune_timer_complete";

    @Override public void onReceive(Context context, Intent intent) {
        if (!TimerScheduler.ACTION_FIRE.equals(intent.getAction())) return;
        long deadline = TimerScheduler.getDeadline(context);
        if (deadline == 0L) return;
        if (deadline > System.currentTimeMillis() + 2_000L) {
            TimerScheduler.rescheduleIfNeeded(context);
            return;
        }
        boolean mute = TimerScheduler.shouldMute(context);
        boolean lockScreen = TimerScheduler.shouldLock(context);
        MediaStopper.pauseMedia(context, mute);
        TimerScheduler.clear(context);
        showCompleteNotification(context);
        if (lockScreen) {
            ScreenLockAdminReceiver.lock(context);
        }
    }

    private void showCompleteNotification(Context context) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                "SleepTune timer", NotificationManager.IMPORTANCE_LOW));
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("SleepTune Timer")
                .setContentText("Hết thời gian. Đã gửi yêu cầu tạm dừng nhạc.")
                .setAutoCancel(true).setContentIntent(pending).build();
        manager.notify(101, notification);
    }
}
