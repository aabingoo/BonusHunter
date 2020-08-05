package com.bonushunter.manager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

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

    public interface IFindView {
        void onFind(Bitmap bitmap);
    }

    public IFindView mFindView;
    public void setFindView(IFindView findView) {
        mFindView = findView;
    }

    public Point findView(Bitmap templateBm) {
        Point tapPoint = null;
//        Image image = mImageReader.acquireLatestImage();
//        if (image != null) {
//            Image.Plane[] planes = image.getPlanes();
//            mByteBuffer = planes[0].getBuffer();
//            int pixelStride = planes[0].getPixelStride();
//            int rowStride = planes[0].getRowStride();
//            int rowPadding = rowStride - pixelStride * mScreenWidth;
//            Bitmap screenBitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride,
//                                mScreenHeight, Bitmap.Config.ARGB_8888);
//            screenBitmap.copyPixelsFromBuffer(mByteBuffer);
//            image.close();

            Bitmap screenBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.xigua_home);

            Mat source = new Mat();
            org.opencv.android.Utils.bitmapToMat(screenBitmap, source);
            Mat template = new Mat();
            org.opencv.android.Utils.bitmapToMat(templateBm, template);

            Mat ret = Mat.zeros(source.rows() - template.rows() + 1,
                    source.cols() - template.cols() + 1, CvType.CV_32FC1);
            Imgproc.matchTemplate(source, template, ret, Imgproc.TM_SQDIFF_NORMED);

            Core.normalize(ret, ret, 0, 1, Core.NORM_MINMAX, -1);
            Core.MinMaxLocResult mlr = Core.minMaxLoc(ret);

            Point matchLoc = mlr.minLoc;
            Log.d(TAG, "findView - value:" + mlr.minVal + ", x:" + mlr.minLoc.x + ", y:" + mlr.minLoc.y);
            Imgproc.rectangle(source, new Rect((int)matchLoc.x, (int)matchLoc.y,
                    template.width(), template.height()), new Scalar(100, 100, 100), 5);
            org.opencv.android.Utils.matToBitmap(source, screenBitmap);

            if (mFindView != null) {
                mFindView.onFind(screenBitmap);
            }

            if (mlr.minVal < 0) {
                double scale = (double)source.width() / (double)mScreenWidth;
                tapPoint = new Point((mlr.minLoc.x + template.width() / 2.0) / scale,
                        (mlr.minLoc.y + template.height() / 2.0) / scale);
                Log.d(TAG, "findView x:" + tapPoint.x + ", y:" + tapPoint.y +
                        ", mScreenWidth:" + mScreenWidth + ", mScreenHeight:" + mScreenHeight +
                        ", source.width():" + source.width() + ", source.height():" + source.height() +
                        ", scale Width:" + mScreenWidth/source.width() + ", scale Height:" + mScreenHeight/source.height());
            }
//        }
        return tapPoint;
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

    private static final String DISPLAY_NAME = "ScreenCaptureDisplay";
    private static final String CAPTURE_THREAD = "ScreenCaptureThread";
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private Handler mCaptureHandler;
    private ByteBuffer mByteBuffer;
    private Bitmap mScreenBitmap;
    private final Object mByteBufferLock = new Object();

    public boolean canCaptureScreen() {
        return mMediaProjection != null;
    }

    public void setMediaProjection(@NonNull MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
    }

    public void startCapture() {
        if (mMediaProjection == null) return;

        Log.d(TAG, "startCapture");

        HandlerThread handlerThread = new HandlerThread(CAPTURE_THREAD);
        handlerThread.start();
        mCaptureHandler = new android.os.Handler(handlerThread.getLooper());

        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 3);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(DISPLAY_NAME,
                mScreenWidth, mScreenHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(), null, null);

//        mImageReader.acquireLatestImage();
//        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//            @Override
//            public void onImageAvailable(ImageReader reader) {
//                Image image = reader.acquireLatestImage();
//                if (image != null) {
//                    Image.Plane[] planes = image.getPlanes();
//                    synchronized (mByteBufferLock) {
//                        mByteBuffer = planes[0].getBuffer();
//                    }
//                    if (mScreenBitmap == null) {
//                        int pixelStride = planes[0].getPixelStride();
//                        int rowStride = planes[0].getRowStride();
//                        int rowPadding = rowStride - pixelStride * mScreenWidth;
//                        mScreenBitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride,
//                                mScreenHeight, Bitmap.Config.ARGB_8888);
//                    }
//
//                    // Should close after used or will not get the next image..
//                    image.close();
//
////                    mScreenBitmap.copyPixelsFromBuffer(mByteBuffer);
////                    mScreenBitmap = Bitmap.createBitmap(bitmap, 0, 0, mScreenWidth, mScreenHeight);
//                }
//            }
//        }, mCaptureHandler);
    }

    public void stopCapture() {
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }
}
