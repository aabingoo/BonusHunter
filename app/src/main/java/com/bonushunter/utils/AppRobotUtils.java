package com.bonushunter.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class AppRobotUtils {

    public static final String TAG = AppRobotUtils.class.getSimpleName();

    public static final String PACKAGE_NAME_XIGUA = "com.ss.android.article.video";
    public static final String PACKAGE_NAME_KUAISHOU = "com.kuaishou.nebula";


    public static Map<String, String> sSupportApps = new HashMap<String, String>() {
        {
            put("西瓜视频", PACKAGE_NAME_XIGUA);
            put("快手极速版", PACKAGE_NAME_KUAISHOU);
        }
    };

    public static boolean launchApp(Context context, String pkgName) {
        boolean ret = false;
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(pkgName);
            context.startActivity(intent);
            ret = true;
        } catch (Exception e) {
            Log.w(TAG, "Exception while startActivity:" + e.toString());
            Toast.makeText(context, "请先安装该应用", Toast.LENGTH_SHORT).show();
        }
        return ret;
    }
}
