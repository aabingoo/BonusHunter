package com.bonushunter.task;

import android.content.Context;

import com.bonushunter.FloatWindow;
import com.bonushunter.R;
import com.bonushunter.utils.AppRobotUtils;

public class LaunchAppTask extends BaseTask {

    private String mPkgName;

    public LaunchAppTask(Context context, String pkgName, int waitSeconds) {
        super(context, waitSeconds);
        mPkgName = pkgName;
    }

    @Override
    public boolean doInBackground() {
        updateTaskDesc(mContext.getString(R.string.desc_launching_app));
        return AppRobotUtils.launchApp(mContext, mPkgName);
    }
}
