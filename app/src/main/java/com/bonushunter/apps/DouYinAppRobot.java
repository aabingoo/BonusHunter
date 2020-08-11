package com.bonushunter.apps;

import android.content.Context;

import com.bonushunter.R;
import com.bonushunter.manager.ScreenManager;
import com.bonushunter.task.ITask;
import com.bonushunter.task.LaunchAppTask;
import com.bonushunter.utils.AppRobotUtils;

public class DouYinAppRobot extends BaseAppRobot {

    public static final String TAG = DouYinAppRobot.class.getSimpleName();

    private String mPackageName = AppRobotUtils.PACKAGE_NAME_DOUYIN;
    private ScreenManager mScreenManager;

    public DouYinAppRobot(Context context) {
        super(context);

        mScreenManager = ScreenManager.getInstance(getContext());
        mAppTitle = "抖音极速版";
    }

    @Override
    public void doInBackground() {
        launchApp();

        seeVideo();
    }

    private void launchApp() {
        // start app to foreground
        updateFloatPrompt("启动抖音极速版中");
        AppRobotUtils.launchApp(getContext(), mPackageName);
        wait(10);
    }

    private void seeVideo() {
        if (checkStop()) return;

        updateFloatPrompt("看抖音视频赚钱中...");
        int upCnt = 5;
        int downCnt = 2;
        int cnt = 0;
        while(true) {
            if (checkStop()) return;
            if (cnt > upCnt) {
                mScreenManager.swipeDown(mAppTitle);
                if (cnt == upCnt + downCnt) {
                    cnt = 0;
                }
            } else {
                mScreenManager.swipeUp(mAppTitle);
            }
            cnt += 1;
            wait(12);
        }
    }
}
