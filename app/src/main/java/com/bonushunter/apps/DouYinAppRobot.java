package com.bonushunter.apps;

import android.content.Context;

import com.bonushunter.manager.ScreenManager;
import com.bonushunter.utils.AppRobotUtils;
import com.bonushunter.utils.LogUtils;

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
    public void doInBackground() throws InterruptedException {
        if (!launchApp()) stop();

        seeVideo();
    }

    private boolean launchApp() {
        // start app to foreground
        LogUtils.d(TAG, "启动抖音极速版中");
        return AppRobotUtils.launchApp(getContext(), mPackageName);
    }

    private void seeVideo() throws InterruptedException {
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
            Thread.sleep(8000);
        }
    }
}
