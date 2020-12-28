package com.bonushunter.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.bonushunter.FloatWindow;
import com.bonushunter.R;
import com.bonushunter.manager.ScreenManager;

import org.opencv.core.Point;

public class FindOneAndClickTask extends BaseTask {

    public static final String TAG = FindOneAndClickTask.class.getSimpleName();

    private String mTitle;
    private String mTemplateImgPath;

    public FindOneAndClickTask(Context context, int waitSeconds, String title, String path) {
        super(context, waitSeconds);
        mTitle = title;
        mTemplateImgPath = path;
    }

    @Override
    public boolean doInBackground() {

//        String format = mContext.getString(R.string.desc_find_view);
//        FloatWindow.getInstance(mContext).setTaskDesc(String.format(format, mTitle));
//
//        Bitmap bitmap = BitmapFactory.decodeFile(mTemplateImgPath).copy(Bitmap.Config.ARGB_8888, true);
//
//        ScreenManager screenManager = ScreenManager.getInstance(mContext);
//        Point tapPoint = screenManager.findViewByFAST(bitmap);
//        Log.d(TAG, "find point:" + (tapPoint != null));
//        if (tapPoint != null) {
//            //find
//            screenManager.tap((int)tapPoint.x, (int)tapPoint.y);
//            return true;
//        }
        return false;
    }
}
