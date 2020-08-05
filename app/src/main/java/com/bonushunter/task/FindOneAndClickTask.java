package com.bonushunter.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bonushunter.FloatWindow;
import com.bonushunter.R;
import com.bonushunter.manager.ScreenManager;

import org.opencv.core.Point;

public class FindOneAndClickTask extends BaseTask {

    public static final String TAG = FindOneAndClickTask.class.getSimpleName();

    public FindOneAndClickTask(Context context, int waitSeconds) {
        super(context, waitSeconds);
    }

    @Override
    public boolean doInBackground() {

//        String format = mContext.getString(R.string.desc_find_view);
//        FloatWindow.getInstance(mContext).setTaskDesc(String.format(format, "直播"));

        Bitmap liveTabBm = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.xigua_live_tab);
        ScreenManager screenManager = ScreenManager.getInstance(mContext);
        Point tapPoint = screenManager.findView(liveTabBm);
        Log.d(TAG, "find point:" + (tapPoint != null));
        if (tapPoint != null) {
            // find
            screenManager.tap((int)tapPoint.x, (int)tapPoint.y);
        }
        return false;
    }

    @Override
    public void updateTaskDesc(String desc) {

    }
}
