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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    private static final int BUFFER_SIZE = 1024;

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

    public static void prepareTemplates(Context context, List<String> filePathList) {
        String targetDirPath = context.getFilesDir().getAbsolutePath();
        for (String filePath: filePathList) {
            File targetFile = new File(targetDirPath + File.separator + filePath);
            Log.d(TAG, "file path:" + targetFile.getAbsolutePath() + "  isExist:" + targetFile.exists());
            if (!targetFile.exists()) {
                InputStream is;
                FileOutputStream fos;
                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    is = context.getAssets().open(filePath);
                    fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);

                    while (is.read(buffer) > 0) {
                        fos.write(buffer);
                    }

                    Log.d(TAG, "Copy done file:" + targetFile.getAbsolutePath());

                    is.close();
                    fos.close();
                } catch (Exception e) {
                    Log.w(TAG, "Exception while copyFiles:" + e.getMessage());
                }
            }
        }
    }
}
