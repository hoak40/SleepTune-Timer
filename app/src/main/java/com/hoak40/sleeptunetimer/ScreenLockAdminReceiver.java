package com.hoak40.sleeptunetimer;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

public class ScreenLockAdminReceiver extends DeviceAdminReceiver {
    static ComponentName component(Context context) {
        return new ComponentName(context, ScreenLockAdminReceiver.class);
    }

    static boolean isEnabled(Context context) {
        DevicePolicyManager policy = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return policy != null && policy.isAdminActive(component(context));
    }

    static boolean lock(Context context) {
        DevicePolicyManager policy = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (policy == null || !policy.isAdminActive(component(context))) return false;
        try {
            policy.lockNow();
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
}
