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
        super.start();
        mStartTask.start();
    }

    @Override
    public void doInBackground() {
        try {
//            response("正在启动应用.");
            // Launch app
            AppRobotUtils.launchApp(getContext(), AppRobotUtils.PACKAGE_NAME_KUAISHOU);

            waitSeconds(5);

            // click live tab

            // random click live category tab

            // find fudai live room and then enter or scroll screen and find again

            // find fudai icon and click to join


        } catch (Exception e) {
            Log.d(TAG, "Exception:" + e.toString());
        }
    }

    @Override
    public void updateRemainSeconds(int remainSeconds) {

    }
}
