package com.bonushunter.manager;

import java.util.HashMap;
import java.util.Map;

public class AppManager {

    public static Map<String, String> sSupportApps = new HashMap<String, String>() {
        {
            put("111", "222");
            put("222", "222");
        }
    };
}
