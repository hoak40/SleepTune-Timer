package com.hoak40.sleeptunetimer;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.SystemClock;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;

import java.util.List;

final class MediaStopper {
    private MediaStopper() { }

    static boolean hasNotificationAccess(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return false;
        ComponentName component = new ComponentName(context, MediaListenerService.class);
        if (Build.VERSION.SDK_INT >= 27) return manager.isNotificationListenerAccessGranted(component);
        String enabled = Settings.Secure.getString(context.getContentResolver(),
                "enabled_notification_listeners");
        return enabled != null && enabled.contains(component.flattenToString());
    }

    static int pauseMedia(Context context, boolean muteVolume) {
        int paused = 0;
        MediaSessionManager manager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        if (manager != null && hasNotificationAccess(context)) {
            try {
                List<MediaController> sessions = manager.getActiveSessions(
                        new ComponentName(context, MediaListenerService.class));
                for (MediaController session : sessions) {
                    PlaybackState state = session.getPlaybackState();
                    if (state != null && (state.getState() == PlaybackState.STATE_PLAYING
                            || state.getState() == PlaybackState.STATE_BUFFERING
                            || state.getState() == PlaybackState.STATE_FAST_FORWARDING
                            || state.getState() == PlaybackState.STATE_REWINDING)) {
                        try {
                            session.getTransportControls().pause();
                            paused++;
                        } catch (RuntimeException ignored) {
                        }
                    }
                }
            } catch (SecurityException | IllegalStateException ignored) {
            }
        }
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            if (paused == 0) {
                try {
                    long now = SystemClock.uptimeMillis();
                    audio.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_MEDIA_PAUSE, 0));
                    audio.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_MEDIA_PAUSE, 0));
                } catch (SecurityException ignored) { }
            }
            if (muteVolume) {
                try {
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                } catch (SecurityException ignored) { }
            }
        }
        return paused;
    }
}
