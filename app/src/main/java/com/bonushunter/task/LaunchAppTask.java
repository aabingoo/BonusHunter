package com.bonushunter.task;

import android.content.Context;

import com.bonushunter.utils.AppRobotUtils;

public class LaunchAppTask extends BaseTask {

    private Context mContext;
    private String mPkgName;

    public LaunchAppTask(Context context, String pkgName, int waitSeconds) {
        super(waitSeconds);
        mContext = context;
        mPkgName = pkgName;
    }

    @Override
    public void doInBackground() {
        AppRobotUtils.launchApp(mContext, mPkgName);
    }

    @Override
    public void updateRemainSeconds(int remainSeconds) {

    }
}
