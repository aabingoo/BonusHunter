package com.bonushunter.manager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Environment;
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
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.ORB;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        Image image = mImageReader.acquireLatestImage();
        if (image != null) {
            Image.Plane[] planes = image.getPlanes();
            mByteBuffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mScreenWidth;
            Bitmap screenBitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride,
                                mScreenHeight, Bitmap.Config.ARGB_8888);
            screenBitmap.copyPixelsFromBuffer(mByteBuffer); //1440,2560
            image.close();
            screenBitmap = Bitmap.createBitmap(screenBitmap, 0, mScreenHeight/2, mScreenWidth, mScreenHeight);

//            Bitmap
//            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xigua_home.png";
//            Bitmap screenBitmap = BitmapFactory.decodeFile(path).copy(Bitmap.Config.ARGB_8888, true);


//            int bmWidth = screenBitmap.getWidth();
//            int bmHeight = screenBitmap.getHeight();
//            if (bmWidth != 1440 || bmHeight != 2560) {
//                float scaleWidth = 1440f / bmWidth;
//                float scaleHeight = 2560f / bmHeight;
//                Matrix matrix = new Matrix();
//                matrix.postScale(scaleWidth, scaleHeight);
//                Log.d(TAG, "screenBitmap.getWidth():" + screenBitmap.getWidth()
//                        + ", screenBitmap.getHeight():" + screenBitmap.getHeight()
//                        + ", scaleWidth:" + scaleWidth
//                        + ", scaleHeight:" + scaleHeight);
//                screenBitmap = Bitmap.createBitmap(screenBitmap, 0, 0, bmWidth, bmHeight, matrix, true);
//            }

            Log.d(TAG, "screenBitmap.getWidth():" + screenBitmap.getWidth()
                    + ", screenBitmap.getHeight():" + screenBitmap.getHeight()
                    + ", templateBm.getWidth():" + templateBm.getWidth()
                    + ", templateBm.getHeight():" + templateBm.getHeight());

            Mat source = new Mat();
            org.opencv.android.Utils.bitmapToMat(screenBitmap, source);
            Mat template = new Mat();
            org.opencv.android.Utils.bitmapToMat(templateBm, template, true);

//            Mat ret = Mat.zeros(source.rows() - template.rows() + 1,
//                    source.cols() - template.cols() + 1, CvType.CV_32FC1);
//            Imgproc.matchTemplate(source, template, ret, Imgproc.TM_SQDIFF_NORMED);
//
//            Core.normalize(ret, ret, 0, 1, Core.NORM_MINMAX, -1);
//            Core.MinMaxLocResult mlr = Core.minMaxLoc(ret);

//            Point matchLoc = mlr.minLoc;
//            Log.d(TAG, "findView - value:" + mlr.minVal + ", x:" + mlr.minLoc.x + ", y:" + mlr.minLoc.y
//                    + ", template.width():" + template.width() + ", template.height():" + template.height());

//            Imgproc.rectangle(source, new Rect((int)matchLoc.x, (int)matchLoc.y,
//                    template.width(), template.height()), new Scalar(0, 0, 0, 255), 10);



//            org.opencv.android.Utils.matToBitmap(finViewBySurf(null, template), templateBm);
            List<Point> matchPoints = finViewBySurf(source, template);
            for (Point p: matchPoints) {
                Imgproc.drawMarker(source, p, new Scalar(0, 255, 0, 255), 20);
            }

            org.opencv.android.Utils.matToBitmap(source, screenBitmap);

            if (mFindView != null) {
                mFindView.onFind(screenBitmap);
            }

//            if (mlr.minVal < 0) {
//                double scale = (double)source.width() / (double)mScreenWidth;
//                tapPoint = new Point((mlr.minLoc.x + template.width() / 2.0) / scale,
//                        (mlr.minLoc.y + template.height() / 2.0) / scale);
//                Log.d(TAG, "findView x:" + tapPoint.x + ", y:" + tapPoint.y +
//                        ", mScreenWidth:" + mScreenWidth + ", mScreenHeight:" + mScreenHeight +
//                        ", source.width():" + source.width() + ", source.height():" + source.height() +
//                        ", scale Width:" + mScreenWidth/source.width() + ", scale Height:" + mScreenHeight/source.height());
//            }
        }
        return tapPoint;
    }

    public Point findViewByFAST(Bitmap templateBm) {
        Log.d(TAG, "findViewByFAST");

        Image image = mImageReader.acquireLatestImage();
        if (image != null) {
            Image.Plane[] planes = image.getPlanes();
            mByteBuffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mScreenWidth;
            Bitmap screenBitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride,
                    mScreenHeight, Bitmap.Config.ARGB_8888);
            screenBitmap.copyPixelsFromBuffer(mByteBuffer);
            image.close();
            screenBitmap = Bitmap.createBitmap(screenBitmap, 0, 0, mScreenWidth, mScreenHeight);


            Log.d(TAG, "screenBitmap.getWidth():" + screenBitmap.getWidth()
                    + ", screenBitmap.getHeight():" + screenBitmap.getHeight()
                    + ", templateBm.getWidth():" + templateBm.getWidth()
                    + ", templateBm.getHeight():" + templateBm.getHeight());

            Mat sourceImage = new Mat();
            org.opencv.android.Utils.bitmapToMat(screenBitmap, sourceImage, true);
            Mat templateImage = new Mat();
            org.opencv.android.Utils.bitmapToMat(templateBm, templateImage, true);

            MatOfKeyPoint templateMatOfKeyPoints = new MatOfKeyPoint();
            MatOfKeyPoint sourceMatOfKeyPoints = new MatOfKeyPoint();
            Mat templateDescriptor = new Mat();
            Mat sourceDescriptor = new Mat();
            FastFeatureDetector fastFeatureDetector = FastFeatureDetector.create();
            SIFT sift = SIFT.create();

            // detect features
            fastFeatureDetector.detect(templateImage, templateMatOfKeyPoints);
            fastFeatureDetector.detect(sourceImage, sourceMatOfKeyPoints);

            // compute
            sift.compute(templateImage, templateMatOfKeyPoints, templateDescriptor);
            sift.compute(sourceImage, sourceMatOfKeyPoints, sourceDescriptor);

            // match
            FlannBasedMatcher flannBasedMatcher = FlannBasedMatcher.create();
            MatOfDMatch matOfDMatch = new MatOfDMatch();
            flannBasedMatcher.match(templateDescriptor, sourceDescriptor, matOfDMatch);

            DMatch[] dMatches = matOfDMatch.toArray();
            KeyPoint[] sourceKeyPoints = sourceMatOfKeyPoints.toArray();
            Map<Point, Integer> nearPointsMap = new HashMap<>();
            Point maxNearNumPoint = null;
            int maxNearNum = 0;
            double campareDistance = templateImage.width() * templateImage.width() +
                    templateImage.height() * templateImage.height();
            for (int i = 0; i < dMatches.length; i++) {
                Point newPoint = new Point(sourceKeyPoints[dMatches[i].trainIdx].pt.x,
                                sourceKeyPoints[dMatches[i].trainIdx].pt.y);

                Imgproc.drawMarker(sourceImage, newPoint, new Scalar(0, 255, 0, 255), 5, 50);

                boolean findNear = false;
                for (Point nearPoint: nearPointsMap.keySet()) {
                    if (distance(nearPoint, newPoint) < campareDistance) {
                        int cnt = nearPointsMap.get(nearPoint) + 1;
                        nearPointsMap.put(nearPoint, cnt);
                        nearPoint.x = (nearPoint.x + nearPoint.x) / 2;
                        nearPoint.y = (nearPoint.y + nearPoint.y) / 2;
                        findNear = true;
                        Log.d(TAG, "find near x:" + nearPoint.x + ",y:" + nearPoint.y + ", cnt:" + nearPointsMap.get(nearPoint));
                        if (maxNearNum < cnt) {
                            maxNearNum = cnt;
                            maxNearNumPoint = nearPoint;
                        }
                    }
                }
                if (!findNear) {
                    nearPointsMap.put(newPoint, 1);
                    Log.d(TAG, "newPoint x:" + newPoint.x + ",y:" + newPoint.y + ", cnt:" + nearPointsMap.get(newPoint));
                }
            }

            Log.d(TAG, "maxNearNumPoint x:" + maxNearNumPoint.x + ",y:" + maxNearNumPoint.y +
                    ", cnt:" + maxNearNum);
            if (maxNearNumPoint != null) {
                Imgproc.drawMarker(sourceImage, maxNearNumPoint, new Scalar(255, 0, 0, 255), 10, 50);
                org.opencv.android.Utils.matToBitmap(sourceImage, screenBitmap);

                if (mFindView != null) {
                    mFindView.onFind(screenBitmap);
                }

                return maxNearNumPoint;
            }
        }

        return null;
    }

    private double distance(Point point1, Point point2) {
        return (point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y);
    }


    public Point findViewByFastKnn(Bitmap templateBm) {
        Log.d(TAG, "findViewByFAST");
        Point tapPoint = null;

        Image image = mImageReader.acquireLatestImage();
        if (image != null) {
            Image.Plane[] planes = image.getPlanes();
            mByteBuffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mScreenWidth;
            Bitmap screenBitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride,
                    mScreenHeight, Bitmap.Config.ARGB_8888);
            screenBitmap.copyPixelsFromBuffer(mByteBuffer);
            image.close();
            screenBitmap = Bitmap.createBitmap(screenBitmap, 0, 0, mScreenWidth, mScreenHeight);


            Log.d(TAG, "screenBitmap.getWidth():" + screenBitmap.getWidth()
                    + ", screenBitmap.getHeight():" + screenBitmap.getHeight()
                    + ", templateBm.getWidth():" + templateBm.getWidth()
                    + ", templateBm.getHeight():" + templateBm.getHeight());

            Mat sourceImage = new Mat();
            org.opencv.android.Utils.bitmapToMat(screenBitmap, sourceImage, true);
            Mat templateImage = new Mat();
            org.opencv.android.Utils.bitmapToMat(templateBm, templateImage, true);

            MatOfKeyPoint templateMatOfKeyPoints = new MatOfKeyPoint();
            MatOfKeyPoint sourceMatOfKeyPoints = new MatOfKeyPoint();
            Mat templateDescriptor = new Mat();
            Mat sourceDescriptor = new Mat();
            FastFeatureDetector fastFeatureDetector = FastFeatureDetector.create();
            SIFT sift = SIFT.create();

            // detect features
            fastFeatureDetector.detect(templateImage, templateMatOfKeyPoints);
            fastFeatureDetector.detect(sourceImage, sourceMatOfKeyPoints);

            // compute
            sift.compute(templateImage, templateMatOfKeyPoints, templateDescriptor);
            sift.compute(sourceImage, sourceMatOfKeyPoints, sourceDescriptor);

            // use knn match
            List<MatOfDMatch> matOfDMatches = new ArrayList<>();
            FlannBasedMatcher flannBasedMatcher = FlannBasedMatcher.create();
            flannBasedMatcher.knnMatch(templateDescriptor, sourceDescriptor, matOfDMatches, 2);

            KeyPoint[] sourceKeyPoints = sourceMatOfKeyPoints.toArray();

            List<DMatch> goodMatchList = new ArrayList<>();
            for (MatOfDMatch matOfDMatch: matOfDMatches) {
                DMatch[] dMatchArray = matOfDMatch.toArray();
                DMatch dMatch1 = dMatchArray[0];
                DMatch dMatch2 = dMatchArray[1];

                Log.d(TAG, "dMatch1 x:" + sourceKeyPoints[dMatch1.trainIdx].pt.x
                        + ", dMatch1 y:" + sourceKeyPoints[dMatch1.trainIdx].pt.y
                        + ", dMatch1 dis:" + dMatch1.distance
                        + ", dMatch2 x:" + sourceKeyPoints[dMatch2.trainIdx].pt.x
                        + ", dMatch2 y:" + sourceKeyPoints[dMatch2.trainIdx].pt.y
                        + ", dMatch2 dis:" + dMatch2.distance + ", 0.7:" + dMatch2.distance*0.7
                );


                if (dMatch1.distance <= dMatch2.distance * 0.7) {
                    goodMatchList.add(dMatch1);
                }
            }

            Log.d(TAG, "googmatchlist:" + goodMatchList.size());

            for (DMatch dMatch: goodMatchList) {
                Point matchPoint = new Point(sourceKeyPoints[dMatch.trainIdx].pt.x,
                        sourceKeyPoints[dMatch.trainIdx].pt.y);
                Imgproc.drawMarker(sourceImage, matchPoint, new Scalar(0, 255, 0, 255), 5, 50);
            }
            org.opencv.android.Utils.matToBitmap(sourceImage, screenBitmap);
            if (mFindView != null) {
                mFindView.onFind(screenBitmap);
            }


//            // find the min sum of knn distance
//            KeyPoint[] sourceKeyPoints = sourceMatOfKeyPoints.toArray();
//            int minSumIndex = -1;
//            float minSumDistance = Float.MAX_VALUE;
//            for(int i = 0; i < matOfDMatches.size(); i++) {
//                DMatch[] dMatches = matOfDMatches.get(i).toArray();
//                float sumDistance = 0f;
//                for (DMatch dMatch : dMatches) {
//                    sumDistance += dMatch.distance;
//                }
//                Log.d(TAG, "sum distance:" + sumDistance);
//                if (minSumDistance > sumDistance) {
//                    minSumDistance = sumDistance;
//                    minSumIndex = i;
//                }
//            }

//            Log.d(TAG, "minSumDistance:" + minSumDistance);
//            if (minSumIndex != -1) {
//                DMatch[] mostDMatches = matOfDMatches.get(minSumIndex).toArray();
//                double sumX = 0;
//                double sumY = 0;
//                int pointLength = mostDMatches.length;
//                for (int i = 0; i < pointLength; i++) {
//                    double matchX = sourceKeyPoints[mostDMatches[i].trainIdx].pt.x;
//                    double matchY = sourceKeyPoints[mostDMatches[i].trainIdx].pt.y;
//                    sumX += matchX;
//                    sumY += matchY;
//                    Log.d(TAG, "i:" + i + ", distance:" + mostDMatches[i].distance
//                            + ", x:" + matchX
//                            + ", y:" + matchY);
//                    Imgproc.drawMarker(sourceImage, new Point(matchX, matchY), new Scalar(0, 255, 0, 255), 20);
//                }
//                tapPoint = new Point(sumX/pointLength, sumY/pointLength);
//
//                org.opencv.android.Utils.matToBitmap(sourceImage, screenBitmap);
//
//                if (mFindView != null) {
//                    mFindView.onFind(screenBitmap);
//                }
//            }
        }

        return tapPoint;
    }

    public List<Point> finViewBySurf(Mat sourceImage, Mat templateImage) {
        MatOfKeyPoint templateKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sourceKeyPoints = new MatOfKeyPoint();
        Mat templateDescriptor = new Mat();
        Mat sourceDescriptor = new Mat();

        FastFeatureDetector fastFeatureDetector = FastFeatureDetector.create();
        SIFT sift = SIFT.create();
        ORB orb = ORB.create();

        fastFeatureDetector.detect(templateImage, templateKeyPoints);
        fastFeatureDetector.detect(sourceImage, sourceKeyPoints);
//        Mat outImage = new Mat();
//        Features2d.drawKeypoints(templateImage, templateKeyPoints, outImage, new Scalar(0, 255, 0, 255));

        sift.compute(templateImage, templateKeyPoints, templateDescriptor);
        sift.compute(sourceImage, sourceKeyPoints, sourceDescriptor);

        List<MatOfDMatch> matOfDMatches = new ArrayList<>();
        MatOfDMatch matOfDMatch = new MatOfDMatch();

        FlannBasedMatcher flannBasedMatcher = FlannBasedMatcher.create();
        flannBasedMatcher.match(templateDescriptor, sourceDescriptor, matOfDMatch);
//        flannBasedMatcher.knnMatch(templateDescriptor, sourceDescriptor, matOfDMatches, 2);

        BFMatcher bfMatcher = BFMatcher.create();
//        bfMatcher.match(templateDescriptor, sourceDescriptor, matOfDMatch);

        DMatch[] dMatchs = matOfDMatch.toArray();
        KeyPoint[] keyPoints = sourceKeyPoints.toArray();
        List<Point> matchPoints = new ArrayList<>();
        for (int i = 0; i < dMatchs.length; i++) {
            Log.d(TAG, "i:" + i + ", distance:" + dMatchs[i].distance
                    + ", x:" + keyPoints[dMatchs[i].trainIdx].pt.x
                    + ", y:" + keyPoints[dMatchs[i].trainIdx].pt.y);
//            if (dMatchs[i].distance < 100) {
                matchPoints.add(new Point(keyPoints[dMatchs[i].trainIdx].pt.x, keyPoints[dMatchs[i].trainIdx].pt.y));
//            }
        }

        return matchPoints;



//        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
//        descriptorMatcher.knnMatch();
//        return outImage;
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
