package com.bonushunter.manager;

import java.util.HashMap;
import java.util.Map;

public class AppManager {

    public static Map<String, String> sSupportApps = new HashMap<String, String>() {
        {
            put("西瓜视频", "com.ss.android.article.video");
            put("快手极速版", "com.kuaishou.nebula");
        }
    };
}
