package com.bonushunter.manager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.bonushunter.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ScreenManager {

    private static final String TAG = ScreenManager.class.getSimpleName();

    private Context mContext;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mDpi;

    private AccessibilityService mAccessibilityService;

    private static ScreenManager singleton;

    private ScreenManager(Context context) {
        mContext = context;

        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.d(TAG, "OpenCV Libraries loaded...");
        }

        // Get screen params
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mDpi = dm.densityDpi;

        Log.d(TAG, "mScreenWidth:" + mScreenWidth + ", mScreenHeight:" + mScreenHeight + " mDpi:" + mDpi);
    }

    public static ScreenManager getInstance(Context context) {
        if (singleton == null) {
            synchronized (ScreenManager.class) {
                if (singleton == null) {
                    singleton = new ScreenManager(context);
                }
            }
        }
        return singleton;
    }

    public void setAccessibilityService(AccessibilityService service) {
        mAccessibilityService = service;
    }

    public void findView(String title) {
        if (mAccessibilityService != null) {
            AccessibilityNodeInfo windowNode = mAccessibilityService.getRootInActiveWindow();
            if (windowNode != null) {
                List<AccessibilityNodeInfo> targetNodes = windowNode.findAccessibilityNodeInfosByText(title);
                for (AccessibilityNodeInfo nodeInfo: targetNodes) {
                    Log.d(TAG, "nodeInfo:" + nodeInfo.toString());
                }
            }
        }
    }

    public Point findView(Bitmap templateBm) {
        Bitmap xigua = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.xigua_home);

        Mat source = new Mat();
        org.opencv.android.Utils.bitmapToMat(xigua, source);
        Mat template = new Mat();
        org.opencv.android.Utils.bitmapToMat(templateBm, template);

        Mat ret = Mat.zeros(source.rows() - template.rows() + 1,
                source.cols() - template.cols() + 1, CvType.CV_32FC1);
        Imgproc.matchTemplate(source, template, ret, Imgproc.TM_SQDIFF_NORMED);

        Core.normalize(ret, ret, 0, 1, Core.NORM_MINMAX, -1);
        Core.MinMaxLocResult mlr = Core.minMaxLoc(ret);
        Log.d(TAG, "findView - value:" + mlr.minVal);

        if (mlr.minVal < 0) {
            double scale = (double)source.width() / (double)mScreenWidth;
            Point matchLoc = new Point((mlr.minLoc.x + template.width() / 2.0) / scale,
                    (mlr.minLoc.y + template.height() / 2.0) / scale);
            Log.d(TAG, "findView x:" + matchLoc.x + ", y:" + matchLoc.y +
                    ", mScreenWidth:" + mScreenWidth + ", mScreenHeight:" + mScreenHeight +
                    ", source.width():" + source.width() + ", source.height():" + source.height() +
                    ", scale Width:" + mScreenWidth/source.width() + ", scale Height:" + mScreenHeight/source.height());
            return matchLoc;
        } else {
            return null;
        }
    }

    public void tap(int x, int y) {
        if (mAccessibilityService == null) return;
        Log.d(TAG, "tap - x:" + x + ", y:" + y);

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription strokeDescription =
                new GestureDescription.StrokeDescription(path, 0, 50);
        GestureDescription description = new GestureDescription.Builder()
                .addStroke(strokeDescription)
                .build();
        mAccessibilityService.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "onCompleted");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "onCancelled");
            }
        }, null);
    }

    public void longPress() {

    }

    public void screenSwipeUp(){
        Log.d(TAG, "screenSwipeUp:" + (mAccessibilityService == null) + ", y:" + mScreenHeight / 10);
        if (mAccessibilityService == null) return;

        Path path = new Path();
        path.moveTo(mScreenWidth/2, mScreenHeight / 100 * 88);
        path.lineTo(mScreenWidth/2, mScreenHeight / 100 * 16);

        GestureDescription.StrokeDescription strokeDescription =
                new GestureDescription.StrokeDescription(path, 0, 600);
        GestureDescription description = new GestureDescription.Builder()
                .addStroke(strokeDescription)
                .build();
        mAccessibilityService.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "onCompleted");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "onCancelled");
            }
        }, null);
    }

    public void screenSwipeDown() {
        Log.d(TAG, "screenSwipeDown:" + (mAccessibilityService == null) + ", y:" + mScreenHeight / 10);
        if (mAccessibilityService == null) return;

        Path path = new Path();
        path.moveTo(mScreenWidth/2, mScreenHeight / 100 * 16);
        path.lineTo(mScreenWidth/2, mScreenHeight / 100 * 88);

        GestureDescription.StrokeDescription strokeDescription =
                new GestureDescription.StrokeDescription(path, 0, 600);
        GestureDescription description = new GestureDescription.Builder()
                .addStroke(strokeDescription)
                .build();
        mAccessibilityService.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "onCompleted");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "onCancelled");
            }
        }, null);
    }

//    private void dispatchTapGesture()

}
