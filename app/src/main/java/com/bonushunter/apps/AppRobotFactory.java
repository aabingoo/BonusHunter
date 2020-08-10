package com.bonushunter.apps;

import android.content.Context;

import com.bonushunter.utils.AppRobotUtils;

public class AppRobotFactory {

    public static IAppRobot getAppRobot(Context context, String pkgName) {
        if (AppRobotUtils.PACKAGE_NAME_XIGUA.equals(pkgName)) {
            return new XiGuaAppRobot(context);
        } else if (AppRobotUtils.PACKAGE_NAME_KUAISHOU.equals(pkgName)) {
            return new KuaiShouAppRobot(context);
        } else if (AppRobotUtils.PACKAGE_NAME_DOUYIN.equals(pkgName)) {
            return new DouYinAppRobot(context);
        }
        return null;
    }
}
