package com.bonushunter.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.bonushunter.BHAccessibilityService;

import java.util.HashSet;
import java.util.Set;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    public static boolean isAccessibilityEnabled(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return accessibilityManager.isEnabled();
    }

    public static boolean isAccessibilitySettingsOn(Context context, String service) {
        Log.d(TAG, "isAccessibilitySettingsOn - service:" + service);
        String enabledAccessibilityService = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (!TextUtils.isEmpty(enabledAccessibilityService)) {
            Log.d(TAG, "isAccessibilitySettingsOn - enabledAccessibilityService:" + enabledAccessibilityService);
            String[] serviceList = enabledAccessibilityService.split(":");
            for (String serviceName: serviceList) {
                if (serviceName.contains(service)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void open(Context context) {

        Log.d(TAG, "clss name:" + BHAccessibilityService.class.getCanonicalName());

        String enabledAccessibilityService = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (!TextUtils.isEmpty(enabledAccessibilityService)) {
            final Set<ComponentName> enabledServices = new HashSet<>();
            final TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
            colonSplitter.setString(enabledAccessibilityService);
            while (colonSplitter.hasNext()) {
                final String componentNameString = colonSplitter.next();
                final ComponentName enabledService = ComponentName.unflattenFromString(
                        componentNameString);
                if (enabledService != null) {
                    enabledServices.add(enabledService);
                }
            }
        }


    }

    public static void swipeUp(){
        Path path = new Path();
        path.moveTo(100, 2000);
        path.lineTo(100, 300);

        GestureDescription.StrokeDescription strokeDescription =
                new GestureDescription.StrokeDescription(path, 0, 800);
        GestureDescription description = new GestureDescription.Builder()
                .addStroke(strokeDescription)
                .build();
        boolean ret = CommonUtils.service.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "onCompleted");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "onCancelled");
            }
        }, null);
    }

    public static void swipeDown() {
        Path path = new Path();
        path.moveTo(100, 300);
        path.lineTo(100, 2000);

        GestureDescription.StrokeDescription strokeDescription =
                new GestureDescription.StrokeDescription(path, 0, 800);
        GestureDescription description = new GestureDescription.Builder()
                .addStroke(strokeDescription)
                .build();
        boolean ret = CommonUtils.service.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "onCompleted");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "onCancelled");
            }
        }, null);

    }

    public static BHAccessibilityService service;
}
