package com.bonushunter.apps;

import android.content.Context;

import com.bonushunter.apps.IAppRobot;
import com.bonushunter.utils.AppRobotUtils;

public class AppRobotFactory {

    public static IAppRobot getAppRobot(Context context, String pkgName) {
        if (AppRobotUtils.PACKAGE_NAME_XIGUA.equals(pkgName)) {
            return new XiGuaAppRobot(context);
        }
        return null;
    }
}
