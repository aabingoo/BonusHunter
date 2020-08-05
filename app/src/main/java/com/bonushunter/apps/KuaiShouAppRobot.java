package com.bonushunter.apps;

import android.content.Context;
import android.util.Log;

import com.bonushunter.task.ITask;
import com.bonushunter.task.LaunchAppTask;
import com.bonushunter.utils.AppRobotUtils;

public class KuaiShouAppRobot extends BaseAppRobot {

    public static final String TAG = XiGuaAppRobot.class.getSimpleName();

    private ITask mStartTask;

    public KuaiShouAppRobot(Context context) {
        super(context);
        mStartTask = new LaunchAppTask(context, AppRobotUtils.PACKAGE_NAME_KUAISHOU, 5);
    }

    @Override
    public void start() {
        mStartTask.start();
    }
}
