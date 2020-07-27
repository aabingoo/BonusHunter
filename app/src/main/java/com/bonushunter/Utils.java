package com.bonushunter;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static boolean isAccessibilityEnabled(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return accessibilityManager.isEnabled();
    }

    public static boolean isAccessibilitySettingsOn(Context context, String service) {
        Log.d(TAG, "isAccessibilitySettingsOn - service:" + service);
        String settingsValue = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (!TextUtils.isEmpty(settingsValue)) {
            Log.d(TAG, "isAccessibilitySettingsOn - settingsValue:" + settingsValue);
            String[] serviceList = settingsValue.split(":");
            for (String serviceName: serviceList) {
                if (serviceName.contains(service)) {
                    return true;
                }
            }
        }
        return false;
    }
}
