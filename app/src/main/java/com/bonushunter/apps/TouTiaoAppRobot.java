package com.bonushunter.apps;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.bonushunter.R;
import com.bonushunter.manager.ScreenManager;
import com.bonushunter.utils.AppRobotUtils;

import java.util.List;

public class TouTiaoAppRobot extends BaseAppRobot {

    public static final String TAG = TouTiaoAppRobot.class.getSimpleName();

    private String mPackageName = AppRobotUtils.PACKAGE_NAME_TOUTIAO;
    private ScreenManager mScreenManager;

    AccessibilityNodeInfo mBottomNavNodes;

    public TouTiaoAppRobot(Context context) {
        super(context);

        mScreenManager = ScreenManager.getInstance(getContext());
        mAppTitle = "今日头条极速版";
    }

    @Override
    public void doInBackground() {
        if (!launchApp()) return;

        List<AccessibilityNodeInfo> mBottomNavNodes = null;
        AccessibilityNodeInfo videoNavNode;
        AccessibilityNodeInfo taskNavNode;
        boolean boxOpened = true;
        Rect bound = new Rect();

        while (true) {
            if (checkStop()) return;

            mBottomNavNodes = mScreenManager.getViewsById(mAppTitle,
                    "com.ss.android.article.lite:id/ey");
            videoNavNode = mBottomNavNodes.get(1);
            taskNavNode = mBottomNavNodes.get(3);

            updateFloatPrompt("判断是否能开宝箱");
            // If it is time to open box
            String taskNavText = taskNavNode.getText().toString();
            Log.d(TAG, "taskNavText:" + taskNavText);
            if (!"任务".equals(taskNavText)) {
                int minute = Integer.valueOf(taskNavText.substring(1, 2));
                int seconds = Integer.valueOf(taskNavText.substring(3, 5));
                int remainOpenTime = minute * 60 + seconds;
                Log.d(TAG, "remain time seconds:" + remainOpenTime);
                // not need to open now, see video
                if (boxOpened) {
                    updateFloatPrompt("前往视频界面");
                    // go to video nav
                    videoNavNode.getBoundsInScreen(bound);
                    mScreenManager.tap(bound.centerX(), bound.centerY());
                    wait(3);

                    boxOpened = false;
                }
                if (!mScreenManager.tapViewByText("重播")) {
                    updateFloatPrompt("看视频赚钱中");
                    List<AccessibilityNodeInfo> mNodes = mScreenManager.getViewsById("今日头条极速版",
                        "com.ss.android.article.lite:id/iy");
                    mNodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

                if (remainOpenTime > 30) {
                    updateFloatPrompt("看视频赚钱中");
                    wait(30);
                } else {
                    updateFloatPrompt("看视频赚钱中");
                    wait(remainOpenTime);
                }

            } else {
                // it is time to open
                updateFloatPrompt("前往开宝箱");
                taskNavNode.getBoundsInScreen(bound);
                mScreenManager.tap(bound.centerX(), bound.centerY());
                wait(6);

                updateFloatPrompt("开宝箱");
                mScreenManager.tapViewByText("treasure-box-enable-1.da338c08");
                wait(5);

                updateFloatPrompt("开宝箱看广告");
                mScreenManager.tapViewByText("看完视频再领");
                wait(20);

                mScreenManager.tapViewByText("关闭广告");

                boxOpened = true;

                updateFloatPrompt("进入下一动作");
                wait(3);
            }
        }

    }

    private boolean launchApp() {
        // start app to foreground
        updateFloatPrompt(
                String.format(getContext().getString(R.string.desc_launching_app), mAppTitle));
        boolean ret = AppRobotUtils.launchApp(getContext(), mPackageName);
        if (ret) {
            wait(10);
        }
        return ret;
    }

    private void seeVideo() {
        if (checkStop()) return;


    }
}
