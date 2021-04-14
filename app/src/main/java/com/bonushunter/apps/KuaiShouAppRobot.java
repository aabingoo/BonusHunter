package com.bonushunter.apps;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.bonushunter.manager.ScreenManager;
import com.bonushunter.task.ITask;
import com.bonushunter.task.LaunchAppTask;
import com.bonushunter.utils.AppRobotUtils;
import com.bonushunter.utils.LogUtils;

import java.util.List;

public class KuaiShouAppRobot extends BaseAppRobot {

    public static final String TAG = KuaiShouAppRobot.class.getSimpleName();

    public static final int SEE_AD_EMPTY_MAX_NUM = 50;

    private String mPackageName = AppRobotUtils.PACKAGE_NAME_KUAISHOU;
    private ScreenManager mScreenManager;

    private boolean completeCheckIn = false;
    private boolean completeSeeAD = false;
    private boolean completeSeeLive = false;
    private boolean completeOpenBox = false;
    private boolean completeSeeVideo = false;

    public KuaiShouAppRobot(Context context) {
        super(context);
        mScreenManager = ScreenManager.getInstance(getContext());
        mAppTitle = "快手极速版";
    }

    @Override
    public void doInBackground() throws InterruptedException {
        if (!launchApp()) stop();

        handleUnexpectedView(15);

        while (!checkStop()) {

            // check in
            if (!completeCheckIn) {
                completeCheckIn = checkIn();
                LogUtils.d(TAG, "completeCheckIn:" + completeCheckIn);
            }

            if (!completeOpenBox) {
                completeOpenBox = openBox();
                LogUtils.d(TAG, "completeOpenBox:" + completeOpenBox);
            }

            // start see AD task
            if (!completeSeeAD) {
                completeSeeAD = seeAd();
                LogUtils.d(TAG, "completeSeeAD:" + completeSeeAD);
            }

            if (!completeSeeLive) {
                completeSeeLive = seeLive();
                LogUtils.d(TAG, "completeSeeLive:" + completeSeeLive);
            }

            if (!completeSeeVideo) {
                completeSeeVideo = seeVideo();
                LogUtils.d(TAG, "completeSeeVideo:" + completeSeeVideo);
            }
        }
    }

    // 我知道了 com.kuaishou.nebula:id/positive
    // 版本升级关闭按钮 com.kuaishou.nebula:id/iv_close
    public void handleUnexpectedView(int tryCnt) throws InterruptedException {
        updateFloatPrompt("检查弹窗中");
        while (tryCnt-- >= 0) {
            AccessibilityNodeInfo closeNode = mScreenManager.getNodeById(mAppTitle, "com.kuaishou.nebula:id/positive");
            if (closeNode != null) {
                mScreenManager.tap(closeNode);
            }
            closeNode = mScreenManager.getNodeById(mAppTitle, "com.kuaishou.nebula:id/iv_close");
            if (closeNode != null) {
                mScreenManager.tap(closeNode);
            }
            Thread.sleep(1000);
        }
    }

    private boolean launchApp() {
        // start app to foreground
        LogUtils.d(TAG, "启动快手极速版中");
        updateFloatPrompt("启动快手极速版中");
        return AppRobotUtils.launchApp(getContext(), mPackageName);
    }

    //
    // 去签到
    // 签到成功加866金币
    // 今天已签
    // 邀请好友赚更多
    // 立即签到
    // 已连续签到
    public boolean checkIn() throws InterruptedException {
        LogUtils.d(TAG, "checkIn");
        if (checkStop()) return false;

        // Enter task list view
        if (!gotoTaskList()) {
            // Enter task list view failed, exit.
            updateFloatPrompt("失败退出");
            return false;
        }

        handleUnexpectedView(10);

        updateFloatPrompt("签到中");

        List<AccessibilityNodeInfo> alreadyCheckinNodes = mScreenManager.getNodesByFuzzySearch(mAppTitle, "已连续签到", 5);
        if (alreadyCheckinNodes != null && alreadyCheckinNodes.size() > 0) {
            return true;
        }

        // 立即签到 -> 今天已签 -> 关闭
        int cnt = 5;
        while (cnt-- >= 0) {
            List<AccessibilityNodeInfo> checkinNodes = mScreenManager.getNodesByFuzzySearch(mAppTitle, "去签到");
            if (checkinNodes != null && checkinNodes.size() > 0) {
                mScreenManager.tap(checkinNodes.get(0));
                break;
            }
            checkinNodes = mScreenManager.getNodesByFuzzySearch(mAppTitle, "立即签到");
            if (checkinNodes != null && checkinNodes.size() > 0) {
                mScreenManager.tap(checkinNodes.get(0));
                break;
            }
        }

        // check result
        if (tryFindViewByTextContains("今日已签", 5) != null) {
            mScreenManager.back();
            return true;
        }

        return false;
    }

    // treasurebox
    // 看精彩视频赚更多
    // com.kuaishou.nebula:id/video_close_icon
    // 明日再来
    // 领取奖励 com.kuaishou.nebula:id/empty_btn
    public boolean openBox() throws InterruptedException {
        LogUtils.d(TAG, "openBox");
        if (checkStop()) return false;

        // Enter task list view
        if (!gotoTaskList()) {
            // Enter task list view failed, exit.
            updateFloatPrompt("失败退出");
            return false;
        }

        updateFloatPrompt("寻找宝箱中");
        List<AccessibilityNodeInfo> openBoxNodes =
                mScreenManager.getNodesByFuzzySearch(mAppTitle, "开宝箱得金币", 10);
        if (openBoxNodes != null && openBoxNodes.size() > 0) {
            updateFloatPrompt("找到宝箱");
            if (mScreenManager.tap(openBoxNodes.get(0).getParent())){
                updateFloatPrompt("已打开宝箱，寻找看精彩视频赚更多");
                LogUtils.d(TAG, "openBox - try to see ad for more");
                List<AccessibilityNodeInfo> earnMoreNodes =
                        mScreenManager.getNodesByFuzzySearch(mAppTitle, "看精彩视频赚更多", 10);
                if (earnMoreNodes != null && earnMoreNodes.size() > 0) {
                    updateFloatPrompt("找到看精彩视频赚更多");
                    if (mScreenManager.tap(earnMoreNodes.get(0).getParent())){
                        LogUtils.d(TAG, "openBox - try to close see ad");
                        updateFloatPrompt("点击看精彩视频赚更多");
                        handleSeeAd();
                    }
                }
            }
        }

        return false;
    }

    private boolean seeAd() throws InterruptedException {
        LogUtils.d(TAG, "seeAd");
        if (checkStop()) return false;

        // Enter task list view
        if (!gotoTaskList()) {
            // Enter task list view failed, exit.
            updateFloatPrompt("失败退出");
            return false;
        }

        updateFloatPrompt("看广告中");

        // start to see ad
        return startSeeAd(SEE_AD_EMPTY_MAX_NUM);
    }

    // com.kuaishou.nebula:id/award_count_down_group
    // com.kuaishou.nebula:id/award_count_down_text

    //com.kuaishou.nebula:id/live_normal_red_pack_background_view_normal
    //com.kuaishou.nebula:id/live_normal_red_pack_image_view
    public boolean seeLive() throws InterruptedException {
        LogUtils.d(TAG, "seeLive");
        if (checkStop()) return false;

        // Enter task list view
        if (!gotoTaskList()) {
            // Enter task list view failed, exit.
            updateFloatPrompt("失败退出");
            return false;
        }

        updateFloatPrompt("看直播中");

        int remainNum = getLiveRemainNum(15);
        LogUtils.d(TAG, "seeLive - remainNum:" + remainNum);
        if (remainNum < 0) {
            return false;
        } else if (remainNum < 10) {
            // see live
            updateFloatPrompt("寻找看直播:" + remainNum);
            List<AccessibilityNodeInfo> seeLiveNodes = mScreenManager.getNodesByFuzzySearch(mAppTitle, "看直播", 10);// tryFindViewsByTextContains("看直播", 5);
            if (seeLiveNodes != null && seeLiveNodes.size() > 0) {
                if (mScreenManager.tap(seeLiveNodes.get(seeLiveNodes.size() - 1))) {
                    updateFloatPrompt("开始看直播:" + remainNum);
                    int cnt = remainNum;
                    while (cnt++ < 10) {
                        AccessibilityNodeInfo awardCountDownNode =
                                mScreenManager.getNodeById(mAppTitle, "com.kuaishou.nebula:id/award_count_down_text", 15);
                                //tryFindViewById("com.kuaishou.nebula:id/award_count_down_text", 15);
                        if (awardCountDownNode != null) {
                            // get time
                            String countDownText = awardCountDownNode.getText().toString();
                            int minutes = Integer.valueOf(countDownText.split(":")[0]);
                            int seconds = Integer.valueOf(countDownText.split(":")[1]);
                            int countDown = minutes * 60 + seconds;
                            LogUtils.d(TAG, "minutes:" + minutes + ", seconds:" + seconds + ", countDown:" + countDown);
                            if (countDown <= 0) {
                                countDown = 92;
                            }
                            updateFloatPrompt("看:" + cnt + ",需:" + countDown);

                            AccessibilityNodeInfo earnedNode = mScreenManager
                                    .findNodeById(mAppTitle, "com.kuaishou.nebula:id/earn_fans_top_coin_count_group");
                            while (earnedNode == null && countDown-- >= 0) {
                                AccessibilityNodeInfo closeNode = mScreenManager
                                        .getNodeById(mAppTitle, "com.kuaishou.nebula:id/live_red_packet_container_close_view");
                                if (closeNode != null) {
                                    mScreenManager.tap(closeNode);
                                }
                                closeNode = mScreenManager
                                        .getNodeById(mAppTitle, "com.kuaishou.nebula:id/close");
                                if (closeNode != null) {
                                    mScreenManager.tap(closeNode);
                                }
                                Thread.sleep(1000);

                                updateFloatPrompt("看:" + cnt + ",需:" + countDown);
                                earnedNode = mScreenManager
                                        .findNodeById(mAppTitle, "com.kuaishou.nebula:id/earn_fans_top_coin_count_group");
                            }
                        } else {
                            updateFloatPrompt("没找到倒计时");
                        }
                        ScreenManager.getInstance(getContext()).swipeUp(mAppTitle);
                    }
                    updateFloatPrompt("退出看直播中");
                    mScreenManager.back();
                    AccessibilityNodeInfo exitLiveNode = mScreenManager.getNodeById(mAppTitle, "com.kuaishou.nebula:id/exit_btn", 10);
                    if (exitLiveNode != null) {
                        mScreenManager.tap(exitLiveNode);
                    }
                    return seeLive();
                }
            }
            return false;
        } else {
            // complete ad
            return true;
        }
    }

    //com.kuaishou.nebula:id/redFloat
    //com.kuaishou.nebula:id/circular_progress_bar
    // com.kuaishou.nebula:id/red_packet
    //com.kuaishou.nebula:id/cycle_layout
    //com.kuaishou.nebula:id/cycle_progress
    //com.kuaishou.nebula:id/circular_progress_bar
    //com.kuaishou.nebula:id/gold_egg_packet
    //com.kuaishou.nebula:id/open_long_atlas
    private boolean seeVideo() throws InterruptedException {
        LogUtils.d(TAG, "seeVideo");
        if (checkStop()) return false;

        if (inTaskListView()) {
            mScreenManager.back();
        }

        updateFloatPrompt("看视频中");

        long startTime = System.currentTimeMillis();
        int upCnt = 5;
        int downCnt = 2;
        int cnt = 0;
        while(true) {
            if (checkStop()) return false;

            if (System.currentTimeMillis() - startTime > 10 *60 * 1000) {
                return false;
            }

            if (cnt > upCnt) {
                ScreenManager.getInstance(getContext()).swipeDown(mAppTitle);
                if (cnt == upCnt + downCnt) {
                    cnt = 0;
                }
            } else {
                ScreenManager.getInstance(getContext()).swipeUp(mAppTitle);
            }
            cnt += 1;

            Thread.sleep(8000);

//            AccessibilityNodeInfo openLongAtalsNode = tryFindViewById("com.kuaishou.nebula:id/open_long_atlas", 5);
//            LogUtils.d(TAG, "swipe to next video now - openLongAtalsNode:" + (openLongAtalsNode != null));
        }
    }

    public boolean startSeeAd(int maxEmptyNum) throws InterruptedException {
        LogUtils.d(TAG, "startSeeAd - maxEmptyNum:" + maxEmptyNum);

        if (maxEmptyNum <= 0) return false;

//        mScreenManager.getWholeTextByStartString(mAppTitle, "宝箱");

        // check if already complete ad task
        int adRemainNum = getAdRemainNum(15);
        LogUtils.d(TAG, "startSeeAd - adRemainNum:" + adRemainNum);
        if (adRemainNum < 0) {
            return false;
        } else if (adRemainNum < 10) {
            // see Ad
            if (tryTapViewByFuzzySearch("android.widget.Button", "福利", 5)) {
                if (handleSeeAd()) {
                    maxEmptyNum = SEE_AD_EMPTY_MAX_NUM;
                } else {
                    maxEmptyNum -= 1;
                }
            }
            Thread.sleep(1000);
            return startSeeAd(maxEmptyNum);
        } else {
            // complete ad
            return true;
        }
    }

    private boolean handleSeeAd() throws InterruptedException {
        updateFloatPrompt("看广告中");
        // com.kuaishou.nebula:id/video_close_icon
        // Xs后可领取奖励, com.kuaishou.nebula:id/video_countdown
        // 任务被抢光了
        // com.kuaishou.nebula:id/empty_btn
        // com.kuaishou.nebula:id/empty_msg
        AccessibilityNodeInfo emptyBtnNode = mScreenManager
                .getNodeById(mAppTitle, "com.kuaishou.nebula:id/empty_btn");
//        AccessibilityNodeInfo countdownNode = mScreenManager
//                .findNodeById(mAppTitle, "com.kuaishou.nebula:id/video_countdown");
        AccessibilityNodeInfo retryBtnNode = mScreenManager
                .getNodeById(mAppTitle, "com.kuaishou.nebula:id/award_video_play_retry_btn");
        AccessibilityNodeInfo closeNode = null;
        int timeToGetCloseNode = 0;
        while (emptyBtnNode == null /*&& countdownNode == null*/ && retryBtnNode == null && closeNode == null) {
            LogUtils.d(TAG, "startSeeAd - Not found empty or countdown, sleep 1s, timeToGetCloseNode:" + ++timeToGetCloseNode);
            Thread.sleep(1000);
            emptyBtnNode = mScreenManager
                    .findNodeById(mAppTitle, "com.kuaishou.nebula:id/empty_btn");
//            countdownNode = mScreenManager
//                    .findNodeById(mAppTitle, "com.kuaishou.nebula:id/video_countdown");
            retryBtnNode = mScreenManager
                    .findNodeById(mAppTitle, "com.kuaishou.nebula:id/award_video_play_retry_btn");
            if (timeToGetCloseNode >= 10) {
                closeNode = mScreenManager
                        .findNodeById(mAppTitle, "com.kuaishou.nebula:id/video_close_icon");
            }
        }

        if (emptyBtnNode != null) {
            LogUtils.d(TAG, "startSeeAd - empty AD task");
            mScreenManager.tap(emptyBtnNode);
            return false;
        } else if (retryBtnNode != null) {
            LogUtils.d(TAG, "startSeeAd - find retry");
            mScreenManager.tap(retryBtnNode);
            return handleSeeAd();
        }
//        else if (countdownNode != null) {
//            LogUtils.d(TAG, "startSeeAd - find count down");
//            closeNode = mScreenManager
//                    .findNodeById(mAppTitle, "com.kuaishou.nebula:id/video_close_icon");
//            while (closeNode == null) {
//                LogUtils.d(TAG, "startSeeAd - Not found closeNode, sleep 1s");
//                Thread.sleep(1000);
//                closeNode = mScreenManager
//                        .findNodeById(mAppTitle, "com.kuaishou.nebula:id/video_close_icon");
//            }
//            LogUtils.d(TAG, "startSeeAd - close to continue next see AD.");
//            if (!mScreenManager.tap(closeNode)) {
//                mScreenManager.back();
//            }
//        }
        else if (closeNode != null) {
            LogUtils.d(TAG, "startSeeAd - find close node");
//            if (tryFindViewByTextContains("退出", 5)
            if (!mScreenManager.tap(closeNode)) {
                mScreenManager.back();
            }

            AccessibilityNodeInfo ensureNode =
                    mScreenManager.getNodeById(mAppTitle, "com.kuaishou.nebula:id/award_video_close_dialog_ensure_button", 5);
            if (ensureNode != null && mScreenManager.tap(ensureNode)) {
                return handleSeeAd();
            }
        }
        return true;
    }

    private boolean gotoTaskList() throws InterruptedException {
        LogUtils.d(TAG, "gotoTaskList");
        updateFloatPrompt("打开任务列表中");
        // check if already in task list view
        if (!inTaskListView()) {
            if (checkStop()) return false;

            LogUtils.d(TAG, "gotoTaskList - not in task list view, click progress bar to go in.");
            int tryCnt = 20;
            while (!mScreenManager.tapViewById(mAppTitle, "com.kuaishou.nebula:id/circular_progress_bar")
                    && tryCnt-- > 0) {
                LogUtils.d(TAG, "gotoTaskList - Not found, sleep 1s, remain retry cnt:" + tryCnt);
                if (checkStop()) return false;
                Thread.sleep(1000);
            }
            return tryCnt > 0;
        }
        return true;
    }

    private boolean inTaskListView() throws InterruptedException {
        int tryCnt = 10;
        String words = "1000金币悬赏任务";
        LogUtils.d(TAG, "inTaskListView - retry cnt:" + tryCnt + ", find words:" + words);
        String text = mScreenManager.getWholeTextByStartString(mAppTitle, words);
        while (TextUtils.isEmpty(text) && tryCnt-- > 0) {
            LogUtils.d(TAG, "inTaskListView - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return false;
            Thread.sleep(1000);
            text = mScreenManager.getWholeTextByStartString(mAppTitle, words);
        }
        LogUtils.d(TAG, "inTaskListView - retry cnt:" + tryCnt + ", find words:" + words + ", text:" + text);
        return !TextUtils.isEmpty(text);
    }

    private int getLiveRemainNum(int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "getLiveRemainNum - retry cnt:" + tryCnt);
        String completeText = mScreenManager.getWholeTextByStartString(mAppTitle, "今日已成功领取直播奖励金币");
        String remainText = mScreenManager.getWholeTextByStartString(mAppTitle, "观看精彩直播得100金币");
        while (TextUtils.isEmpty(completeText) && TextUtils.isEmpty(remainText) && tryCnt-- > 0) {
            LogUtils.d(TAG, "getLiveRemainNum - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return -1;
            Thread.sleep(1000);
            remainText = mScreenManager.getWholeTextByStartString(mAppTitle, "观看精彩直播得100金币");
            completeText = mScreenManager.getWholeTextByStartString(mAppTitle, "今日已成功领取直播奖励金币");
        }
        if (!TextUtils.isEmpty(completeText)) {
            return 10;
        }
        if (!TextUtils.isEmpty(remainText)) {
            int remainNumIndex = remainText.indexOf('/') - 1;
            if (remainNumIndex > 0) {
                int r = remainText.charAt(remainNumIndex) - '0';
                LogUtils.d(TAG, "getLiveRemainNum:" + remainText + ", char:" + remainText.charAt(remainNumIndex) + ", r:" + r);
                return r;
            }
        }
        LogUtils.d(TAG, "getLiveRemainNum - not found");
        return -1;
    }

    private int getAdRemainNum(int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "getAdRemainNum - retry cnt:" + tryCnt);
        String completeText = mScreenManager.getWholeTextByStartString(mAppTitle, "明天看视频继续领取");
        String remainText = mScreenManager.getWholeTextByStartString(mAppTitle, "每次100金币");
        while (TextUtils.isEmpty(completeText) && TextUtils.isEmpty(remainText) && tryCnt-- > 0) {
            LogUtils.d(TAG, "getAdRemainNum - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return -1;
            Thread.sleep(1000);
            remainText = mScreenManager.getWholeTextByStartString(mAppTitle, "每次100金币");
            completeText = mScreenManager.getWholeTextByStartString(mAppTitle, "明天看视频继续领取");
        }
        if (!TextUtils.isEmpty(completeText)) {
            return 10;
        }
        if (!TextUtils.isEmpty(remainText)) {
            int remainNumIndex = remainText.indexOf('/') - 1;
            if (remainNumIndex > 0) {
                int r = remainText.charAt(remainNumIndex) - '0';
                LogUtils.d(TAG, "getAdRemainNum:" + remainText + ", char:" + remainText.charAt(remainNumIndex) + ", r:" + r);
                return r;
            }
        }
        LogUtils.d(TAG, "getAdRemainNum - not found");
        return -1;
    }

    public AccessibilityNodeInfo tryFindViewById(String id, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryFindViewById - id:" + id + ", retry cnt:" + tryCnt);
        if (checkStop()) return null;

        AccessibilityNodeInfo nodeInfo = mScreenManager.findNodeById(mAppTitle, id);
        while (nodeInfo == null && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryFindViewById - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return null;
            Thread.sleep(1000);
            nodeInfo = mScreenManager.findNodeById(mAppTitle, id);
        }
        return nodeInfo;
    }

    public AccessibilityNodeInfo tryFindViewByText(String text, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryFindViewByText - text" + text + ", retry cnt:" + tryCnt);
        if (checkStop()) return null;

        AccessibilityNodeInfo nodeInfo = mScreenManager.findNodeByText(mAppTitle, text);
        while (nodeInfo == null && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryFindViewByText - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return null;
            Thread.sleep(1000);
            nodeInfo = mScreenManager.findNodeByText(mAppTitle, text);
        }
        return nodeInfo;
    }

    public AccessibilityNodeInfo tryFindViewByTextContains(String text, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryFindViewByTextContains - text" + text + ", retry cnt:" + tryCnt);
        if (checkStop()) return null;

        AccessibilityNodeInfo nodeInfo = mScreenManager.findNodeByTextContains(mAppTitle, text);
        while (nodeInfo == null && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryFindViewByTextContains - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return null;
            Thread.sleep(1000);
            nodeInfo = mScreenManager.findNodeByTextContains(mAppTitle, text);
        }
        return nodeInfo;
    }

    public List<AccessibilityNodeInfo> tryFindViewsByTextContains(String text, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryFindViewsByTextContains - text:" + text + ", retry cnt:" + tryCnt);
        if (checkStop()) return null;

        List<AccessibilityNodeInfo> nodeInfos = mScreenManager.findNodesByTextContains(mAppTitle, text);
        while (nodeInfos == null && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryFindViewsByTextContains - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return null;
            Thread.sleep(1000);
            nodeInfos = mScreenManager.findNodesByTextContains(mAppTitle, text);
        }
        return nodeInfos;
    }

    /**
     * try tap view
     */

    public boolean tryTapParentThatContains(String text, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryTapParentThatContains - text:" + text + ", retry cnt:" + tryCnt);
        if (checkStop()) return false;

        AccessibilityNodeInfo nodeInfo = tryFindViewByTextContains(text, tryCnt);
        if (nodeInfo != null) {
//            LogUtils.d(TAG, "tryTapParentThatContains - nodeInfo:" + nodeInfo.toString());
//            LogUtils.d(TAG, "tryTapParentThatContains - getParent:" + nodeInfo.getParent().toString());
            return mScreenManager.tap(nodeInfo.getParent());
        } else {
            return false;
        }
    }

    public boolean tryTapViewByFuzzySearch(String clazz, String searchWords, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryTapViewByFuzzySearch - clazz:" + clazz
                + ", searchWords" + searchWords + ", retry cnt:" + tryCnt);
        if (checkStop()) return false;

        while (!mScreenManager.tapViewByFuzzySearch(mAppTitle, clazz, searchWords)
                && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryTapViewByTextContains - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return false;
            Thread.sleep(1000);
        }
        return tryCnt > 0;
    }

    public boolean tryTapViewByTextContains(String text, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryTapViewByTextContains - text" + text + ", retry cnt:" + tryCnt);
        if (checkStop()) return false;

        while (!mScreenManager.tapViewByTextContains(mAppTitle, text)
                && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryTapViewByTextContains - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return false;
            Thread.sleep(1000);
        }
        return tryCnt > 0;
    }

    public boolean tryTapViewByText(String text, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryTapViewByText - text" + text + ", retry cnt:" + tryCnt);
        if (checkStop()) return false;

        while (!mScreenManager.tapViewByText(mAppTitle, text)
                && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryTapViewByText - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return false;
            Thread.sleep(1000);
        }
        return tryCnt > 0;
    }

    public boolean tryTapViewById(String id, int tryCnt) throws InterruptedException {
        LogUtils.d(TAG, "tryTapViewById - id" + id + ", retry cnt:" + tryCnt);
        if (checkStop()) return false;

        while (!mScreenManager.tapViewById(mAppTitle, id)
                && tryCnt-- > 0) {
            LogUtils.d(TAG, "tryTapViewById - Not found, sleep 1s, remain retry cnt:" + tryCnt);
            if (checkStop()) return false;
            Thread.sleep(1000);
        }
        return tryCnt > 0;
    }
}
