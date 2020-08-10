package com.bonushunter.apps;

import android.content.Context;
import android.os.Environment;

import com.bonushunter.task.FindOneAndClickTask;
import com.bonushunter.task.ITask;
import com.bonushunter.task.LaunchAppTask;
import com.bonushunter.utils.AppRobotUtils;

public class XiGuaAppRobot extends BaseAppRobot {

    public static final String TAG = XiGuaAppRobot.class.getSimpleName();

    private static final String[] COPY_FILES = {
            "xigua/",
            "server.key",
            "test_icon.ppm",
            "endpoint"
    };

    private ITask mStartTask;
    private ITask mSelectLiveNavTask;
    private ITask mSelectLiveCategoryTask;
    private ITask mEnterFudaiLiveRoomTask;

    public XiGuaAppRobot(Context context) {
        super(context);

        mStartTask = new LaunchAppTask(context, AppRobotUtils.PACKAGE_NAME_XIGUA, 15);
        String liveNavPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/xigua/xigua_zhibo.png";
        mSelectLiveNavTask = new FindOneAndClickTask(context, 20, "直播", liveNavPath);
        mStartTask.setNextTask(mSelectLiveNavTask);

        String xiangyePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/xigua/xigua_zhibo_xiangye.png";
        mSelectLiveCategoryTask = new FindOneAndClickTask(context, 15, "乡野", xiangyePath);
        mSelectLiveNavTask.setNextTask(mSelectLiveCategoryTask);

        String fudaiPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/xigua/xigua_fudai.png";
        mEnterFudaiLiveRoomTask = new FindOneAndClickTask(context, 15, "福袋直播间", fudaiPath);
        mSelectLiveCategoryTask.setNextTask(mEnterFudaiLiveRoomTask);

    }

    @Override
    public void doInBackground() {

    }

    @Override
    public void start() {
        mStartTask.start();
    }
}
