package com.bonushunter.task;

import android.content.Context;

import com.bonushunter.manager.ScreenManager;

public class KuaiShouSeeAdTask extends BaseTask {

    public static final String TAG = KuaiShouSeeAdTask.class.getSimpleName();

    private String mTitle;
    private String mTemplateImgPath;

    public KuaiShouSeeAdTask(Context context, int waitSeconds, String title, String path) {
        super(context, waitSeconds);
        mTitle = title;
        mTemplateImgPath = path;
    }

    @Override
    public boolean doInBackground() {

        ScreenManager screenManager = ScreenManager.getInstance(mContext);

        // Check if under AD view
        String viewId = "com.kuaishou.nebula:id/circular_progress_bar";
        boolean ret;
//        ret =

        return false;
    }
}
