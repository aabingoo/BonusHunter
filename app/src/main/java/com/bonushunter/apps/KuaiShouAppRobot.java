package com.bonushunter.apps;

import android.content.Context;
import android.util.Log;

import com.bonushunter.R;
import com.bonushunter.manager.ScreenManager;
import com.bonushunter.task.ITask;
import com.bonushunter.task.LaunchAppTask;
import com.bonushunter.utils.AppRobotUtils;

public class KuaiShouAppRobot extends BaseAppRobot {

    public static final String TAG = XiGuaAppRobot.class.getSimpleName();

    private static final String[] COPY_FILES = {
            "xigua/",
            "server.key",
            "test_icon.ppm",
            "endpoint"
    };

    private ITask mStartTask;
    private ITask mSeeAdTask;

    private String mPackageName = AppRobotUtils.PACKAGE_NAME_KUAISHOU;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private ScreenManager mScreenManager;

    public KuaiShouAppRobot(Context context) {
        super(context);
        mStartTask = new LaunchAppTask(context, AppRobotUtils.PACKAGE_NAME_KUAISHOU, 10);

        mScreenManager = ScreenManager.getInstance(getContext());
        mAppTitle = "快手极速版";
    }

    @Override
    public void doInBackground() {
        launchApp();

        // start see AD task
        seeAD();

        seeVideo();
    }

    private void launchApp() {
        // start app to foreground
        updateFloatPrompt("启动快手极速版中");
        AppRobotUtils.launchApp(getContext(), mPackageName);
        wait(10);
    }

    private void seeAD() {
        // 1) Enter AD task view
        if (checkStop()) return;

        updateFloatPrompt("进入任务窗口");
        mScreenManager.tapViewById(mAppTitle, "com.kuaishou.nebula:id/red_packet");
        wait(5);

        // 2) loop see AD task until done
        int doneCnt = 0;
        while (true) {
            if (checkStop()) return;

            updateFloatPrompt("看广告，已完成:" + doneCnt);
            if (mScreenManager.tapViewByText("福利")) {
                wait(20);
                mScreenManager.tapViewByText("关闭广告");
                wait(5);
                doneCnt += 1;
            } else {
                mScreenManager.back();
                break;
            }
        }
    }

    private void seeVideo() {
        updateFloatPrompt("看快手视频赚钱中...");
        int upCnt = 5;
        int downCnt = 2;
        int cnt = 0;
        while(true) {
            if (checkStop()) return;
            if (cnt > upCnt) {
                ScreenManager.getInstance(getContext()).swipeDown(mAppTitle);
                if (cnt == upCnt + downCnt) {
                    cnt = 0;
                }
            } else {
                ScreenManager.getInstance(getContext()).swipeUp(mAppTitle);
            }
            cnt += 1;
            wait(12);
        }
    }
}
