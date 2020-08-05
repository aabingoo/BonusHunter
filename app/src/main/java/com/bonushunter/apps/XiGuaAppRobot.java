package com.bonushunter.apps;

import android.content.Context;
import android.util.Log;

import com.bonushunter.task.FindOneAndClickTask;
import com.bonushunter.task.ITask;
import com.bonushunter.task.LaunchAppTask;
import com.bonushunter.utils.AppRobotUtils;

public class XiGuaAppRobot extends BaseAppRobot {

    public static final String TAG = XiGuaAppRobot.class.getSimpleName();

    private ITask mStartTask;
    private ITask mSelectLiveTabTask;

    public XiGuaAppRobot(Context context) {
        super(context);
        mStartTask = new LaunchAppTask(context, AppRobotUtils.PACKAGE_NAME_XIGUA, 10);
        mSelectLiveTabTask = new FindOneAndClickTask(context, 30);
        mStartTask.setNextTask(mSelectLiveTabTask);
    }

    @Override
    public void start() {
        mStartTask.start();
    }
}
