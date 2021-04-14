package com.bonushunter.manager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.opencv.text.Text;
import org.opencv.xfeatures2d.SIFT;
import org.opencv.xfeatures2d.SURF;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import com.bonushunter.utils.LogUtils;

public class ScreenManager {

    private static final String TAG = ScreenManager.class.getSimpleName();

    private Context mContext;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mDpi;
    private Handler mHandler;

    private AccessibilityService mAccessibilityService;

    private static ScreenManager singleton;

    private ScreenManager(Context context) {
        mContext = context;

        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.d(TAG, "OpenCV Libraries loaded...");
        }

        HandlerThread handlerThread = new HandlerThread("screen_thread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

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

    public boolean splitWindowEnabled() {
        if (mAccessibilityService == null) return false;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            Log.d(TAG, "window info:" + windowInfo.toString());
            if (windowInfo.getType() == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER) {
                return true;
            }
        }
        return false;
    }

    public List<AccessibilityNodeInfo> getViewsById(String appTitle, String viewId) {
        Log.d(TAG, "getViewText:" + viewId);
        if (mAccessibilityService == null) return null;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            Log.d(TAG, "tapViewById - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootNode = windowInfo.getRoot();
                if (rootNode != null) {
                    List<AccessibilityNodeInfo> targetNodes = rootNode.findAccessibilityNodeInfosByViewId(viewId);
                    Log.d(TAG, "tapViewById - targetNodes:" + targetNodes.size());
                    return targetNodes;
                }
            }
        }
        return null;
    }

    private int mLoopCnt;
    public void loopAllViews() {
        LogUtils.d(TAG, "loopAllViews");
        if (mAccessibilityService == null) return ;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            LogUtils.d(TAG, "loopAllViews - window info:" + windowInfo.toString());
            AccessibilityNodeInfo rootNode = windowInfo.getRoot();
            if (rootNode != null) {
                LogUtils.d(TAG, "loopAllViews - childCnt:" + rootNode.getChildCount()
                        + ", id:" + rootNode.getViewIdResourceName()
                        + ", viewTextd:" + rootNode.getText()
                        + ", node info:" + rootNode.toString());

                mLoopCnt = 0;
                loopAllViews(rootNode, 1);
            }
        }
    }

    private void loopAllViews(AccessibilityNodeInfo node, int dep) {
        if (node != null) {
            int childCnt = node.getChildCount();
            CharSequence viewText = node.getText();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < dep; i++) {
                stringBuilder.append(">");
            }
            mLoopCnt += 1;
            LogUtils.d(TAG, "loopAllViews: " + mLoopCnt + " " + stringBuilder.toString()
                    + " childCnt:" + childCnt
                    + ", id:" + node.getViewIdResourceName()
                    + ", viewText:" + viewText + ", node info:" + node.toString());
            for (int i = 0; i < childCnt; i++) {
                loopAllViews(node.getChild(i), ++dep);
            }
        }
    }



    public AccessibilityNodeInfo getNodeById(String appTitle, String id, int tryNum) {
        AccessibilityNodeInfo ret = getNodeById(appTitle, id);
        while (ret == null && --tryNum >= 0) {
            try {
                Thread.sleep(1000);
                LogUtils.d(TAG, "getNodeById - not find, remain try num:" + tryNum);
                ret = getNodeById(appTitle, id);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return ret;
    }

    public AccessibilityNodeInfo getNodeById(String appTitle, String id) {
        LogUtils.d(TAG, "getNodeById - id:" + id);
        if (mAccessibilityService == null) return null;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            AccessibilityNodeInfo rootNode = windowInfo.getRoot();
            if (rootNode != null && windowInfo.getType() == 1) {
                List<AccessibilityNodeInfo> result = new ArrayList<>(rootNode.findAccessibilityNodeInfosByViewId(id));
                LogUtils.d(TAG, "getNodeById - use API - result:" + result.size());
                if (result.size() <= 0) {
                    mLoopCnt = 0;
                    loopAllNodesForId(result, rootNode, id);
                    LogUtils.d(TAG, "getNodeById - loop all nodes - result:" + result.size());
                }
                if (result.size() > 0) {
                    return result.get(0);
                }
            }
        }
        return null;
    }

    private void loopAllNodesForId(List<AccessibilityNodeInfo> result,
                                   AccessibilityNodeInfo sourceNode, String id) {
        if (sourceNode == null) return ;

        int childCnt = sourceNode.getChildCount();
        String viewId = sourceNode.getViewIdResourceName();
        mLoopCnt += 1;
//        LogUtils.d(TAG, "loopAllNodesForId: "
//                + ", id:" + viewId
//                + ", loopCnt:" + mLoopCnt);
        if (id.equals(viewId)) {
            result.add(sourceNode);
        }

        for (int i = 0; i < childCnt; i++) {
            loopAllNodesForId(result, sourceNode.getChild(i), id);
        }
    }

    public List<AccessibilityNodeInfo> getNodesByExactlySearch(String appTitle, String keyword, int tryNum) {
        List<AccessibilityNodeInfo> ret = getNodesByExactlySearch(appTitle, keyword);
        while ((ret == null || ret.size() <= 0) && --tryNum >= 0) {
            try {
                Thread.sleep(1000);
                LogUtils.d(TAG, "getNodesByExactlySearch - not find, remain try num:" + tryNum);
                ret = getNodesByExactlySearch(appTitle, keyword);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return ret;
    }

    public List<AccessibilityNodeInfo> getNodesByExactlySearch(String appTitle, String keyword) {
        LogUtils.d(TAG, "getNodesByExactlySearch - keyword:" + keyword);
        if (mAccessibilityService == null) return null;

        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            AccessibilityNodeInfo rootNode = windowInfo.getRoot();
            if (rootNode != null  && windowInfo.getType() == 1) {
                result.addAll(rootNode.findAccessibilityNodeInfosByText(keyword));
                LogUtils.d(TAG, "getNodesByExactlySearch - use API - result:" + result.size());
                if (result.size() <= 0) {
                    mLoopCnt = 0;
                    loopAllNodesByExactlySearch(result, rootNode, keyword);
                    LogUtils.d(TAG, "getNodesByExactlySearch - loop all nodes - result:" + result.size());
                }
            }
        }
        return result;
    }

    private void loopAllNodesByExactlySearch(List<AccessibilityNodeInfo> result,
                                   AccessibilityNodeInfo sourceNode, String keyword) {
        if (sourceNode == null) return ;

        int childCnt = sourceNode.getChildCount();
        CharSequence text = sourceNode.getText();
        mLoopCnt += 1;
//        LogUtils.d(TAG, "loopAllNodesByExactlySearch: "
//                + ", text:" + text
//                + ", loopCnt:" + mLoopCnt);
        if (keyword.equals(text)) {
            result.add(sourceNode);
        }

        for (int i = 0; i < childCnt; i++) {
            loopAllNodesByExactlySearch(result, sourceNode.getChild(i), keyword);
        }
    }

    public List<AccessibilityNodeInfo> getNodesByFuzzySearch(String appTitle, String keyword, int tryNum) {
        List<AccessibilityNodeInfo> ret = getNodesByFuzzySearch(appTitle, keyword);
        while ((ret == null || ret.size() <= 0) && --tryNum >= 0) {
            try {
                Thread.sleep(1000);
                LogUtils.d(TAG, "getNodesByFuzzySearch - not find, remain try num:" + tryNum);
                ret = getNodesByFuzzySearch(appTitle, keyword);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return ret;
    }

    public List<AccessibilityNodeInfo> getNodesByFuzzySearch(String appTitle, String keyword) {
        LogUtils.d(TAG, "getNodesByFuzzySearch - keyword:" + keyword);
        if (mAccessibilityService == null) return null;

        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            AccessibilityNodeInfo rootNode = windowInfo.getRoot();
            LogUtils.d(TAG, "getNodesByFuzzySearch - window:" + windowInfo.toString());
            if (rootNode != null && windowInfo.getType() == 1) {
                result.addAll(rootNode.findAccessibilityNodeInfosByText(keyword));
                LogUtils.d(TAG, "getNodesByFuzzySearch - use API - result:" + result.size());
                if (result.size() <= 0) {
                    mLoopCnt = 0;
                    loopAllNodesByFuzzySearch(result, rootNode, keyword);
                    LogUtils.d(TAG, "getNodesByFuzzySearch - loop all nodes - result:" + result.size());
                }
            }
        }
        return result;
    }

    private void loopAllNodesByFuzzySearch(List<AccessibilityNodeInfo> result,
                                             AccessibilityNodeInfo sourceNode, String keyword) {
        if (sourceNode == null) return ;

        int childCnt = sourceNode.getChildCount();
        CharSequence text = sourceNode.getText();
        mLoopCnt += 1;
//        LogUtils.d(TAG, "loopAllNodesByExactlySearch: "
//                + ", text:" + text
//                + ", loopCnt:" + mLoopCnt);
        if (text != null && text.toString().contains(keyword)) {
            result.add(sourceNode);
        }

        for (int i = 0; i < childCnt; i++) {
            loopAllNodesByFuzzySearch(result, sourceNode.getChild(i), keyword);
        }
    }













    public AccessibilityNodeInfo findNodeById(String appTitle, String id) {
//        LogUtils.d(TAG, "findNodeById - appTitle:" + appTitle + ", id:" + id);
        if (mAccessibilityService == null) return null;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
//            LogUtils.d(TAG, "findNodeById - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootNode = windowInfo.getRoot();
                if (rootNode != null) {
                    List<AccessibilityNodeInfo> targetNodes = rootNode.findAccessibilityNodeInfosByViewId(id);
//                    LogUtils.d(TAG, "findNodeById - targetNodes:" + targetNodes.size());
                    if (targetNodes != null && targetNodes.size() > 0) {
                        return targetNodes.get(0);
                    }
                }
            }
        }
        return null;
    }

    public String getWholeTextByStartString(String appTitle, String words) {
        LogUtils.d(TAG, "getWholeTextByStartString - words:" + words);
        if (mAccessibilityService == null) return null;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
//            LogUtils.d(TAG, "getWholeTextByStartString - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootNode = windowInfo.getRoot();
                if (rootNode != null) {
                    AccessibilityNodeInfo targetNode = findNodeByTextContains(rootNode, words);
                    if (targetNode != null) {
//                        LogUtils.d(TAG, "getWholeTextByStartString - find targetNodes:" + targetNode.toString());
                        String viewText = targetNode.getText().toString().trim();
                        if (!TextUtils.isEmpty(viewText) && viewText.startsWith(words)) {
                            return viewText;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean tapViewByTextContains(String appTitle, String text) {
        LogUtils.d(TAG, "tapViewByTextContains - appTitle:" + appTitle + ", text:" + text);
        boolean clickRet = false;
        AccessibilityNodeInfo targetNode = findNodeByTextContains(appTitle, text);
        if (targetNode != null) {
            clickRet = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            LogUtils.d(TAG, "tapViewByTextContains - targetNodes:" + targetNode.toString() + ", tap ret:" + clickRet);
            if (!clickRet) {
                Rect bound = new Rect();
                targetNode.getBoundsInScreen(bound);
                tap(bound.centerX(), bound.centerY());
                clickRet = true;
            }
        }
        return clickRet;
    }

    public boolean tapViewByFuzzySearch(String appTitle, String clazz, String text) {
        LogUtils.d(TAG, "tapViewByFuzzySearch - appTitle:" + appTitle + ", text:" + text);
        boolean clickRet = false;
        AccessibilityNodeInfo targetNode = fuzzySearchNode(appTitle, clazz, text);
        if (targetNode != null) {
            if (targetNode.isClickable()) {
                clickRet = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else {
                Rect bound = new Rect();
                targetNode.getBoundsInScreen(bound);
                tap(bound.centerX(), bound.centerY());
                clickRet = true;
            }
            LogUtils.d(TAG, "tapViewByTextContains - targetNodes:" + targetNode.toString()
                    + ", tap ret:" + clickRet
                    + ", clickable:" + targetNode.isClickable());
        }
        return clickRet;
    }

    public AccessibilityNodeInfo fuzzySearchNode(String appTitle, String clazz, String searchText) {
        if (mAccessibilityService == null) return null;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootNode = windowInfo.getRoot();
                if (rootNode != null) {
                    AccessibilityNodeInfo targetNode = fuzzySearchNode(rootNode, clazz, searchText);
                    return targetNode;
                }
            }
        }
        return null;
    }

    private AccessibilityNodeInfo fuzzySearchNode(AccessibilityNodeInfo nodeInfo, String clazz, String searchText) {
        if (nodeInfo == null) return null;

        int childCnt = nodeInfo.getChildCount();
        CharSequence viewText = nodeInfo.getText();
        if (childCnt == 0 && !TextUtils.isEmpty(viewText) && !TextUtils.isEmpty(clazz)) {
            if (viewText.toString().trim().contains(searchText)
                    && clazz.equals(nodeInfo.getClassName())) {
                return nodeInfo;
            } else {
                return null;
            }
        } else {
            for (int i = 0; i < childCnt; i++) {
                AccessibilityNodeInfo searchNode = fuzzySearchNode(nodeInfo.getChild(i), clazz, searchText);
                if (searchNode != null) {
                    return searchNode;
                }
            }
            return null;
        }
    }


    public AccessibilityNodeInfo findNodeByTextContains(String appTitle, String words) {
//        LogUtils.d(TAG, "findNodeByTextContains - words:" + words);
        if (mAccessibilityService == null) return null;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
//            LogUtils.d(TAG, "findNodeByTextContains - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootNode = windowInfo.getRoot();
                if (rootNode != null) {
                    AccessibilityNodeInfo targetNode = findNodeByTextContains(rootNode, words);
                    return targetNode;
                }
            }
        }
        return null;
    }

    public List<AccessibilityNodeInfo> findNodesByTextContains(String appTitle, String targetText) {
//        LogUtils.d(TAG, "findNodesByTextContains - targetText:" + targetText);
        if (mAccessibilityService == null) return null;

        List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
//            LogUtils.d(TAG, "findNodeByTextContains - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootNode = windowInfo.getRoot();
                if (rootNode != null) {
                    findNodesByTextContains(nodeInfos, rootNode, targetText);
                }
            }
        }
        return nodeInfos;
    }

    private void findNodesByTextContains(List<AccessibilityNodeInfo> nodes, AccessibilityNodeInfo nodeInfo, String searchText) {
        if (nodeInfo == null) return ;

        int childCnt = nodeInfo.getChildCount();
        CharSequence viewText = nodeInfo.getText();
        if (childCnt == 0 && !TextUtils.isEmpty(viewText)) {
            if (viewText.toString().trim().contains(searchText)) {
                nodes.add(nodeInfo);
            }
        } else {
            for (int i = 0; i < childCnt; i++) {
                findNodesByTextContains(nodes, nodeInfo.getChild(i), searchText);
            }
        }
    }

    private AccessibilityNodeInfo findNodeByTextContains(AccessibilityNodeInfo nodeInfo, String searchText) {
        if (nodeInfo == null) return null;

        int childCnt = nodeInfo.getChildCount();
        CharSequence viewText = nodeInfo.getText();
//        LogUtils.d(TAG, "findNodeByTextContains - node info:" + nodeInfo.toString() + "\n childCnt:" + childCnt + "\n viewText:" + viewText);
        if (childCnt == 0 && !TextUtils.isEmpty(viewText)) {
            if (viewText.toString().trim().contains(searchText)) {
                return nodeInfo;
            } else {
                return null;
            }
        } else {
            for (int i = 0; i < childCnt; i++) {
                AccessibilityNodeInfo searchNode = findNodeByTextContains(nodeInfo.getChild(i), searchText);
                if (searchNode != null) {
                    return searchNode;
                }
            }
            return null;
        }
    }

    public boolean tapViewById(String appTitle, String id) {
//        LogUtils.d(TAG, "tapViewById - appTitle:" + appTitle + ", id:" + id);
        boolean clickRet = false;
        AccessibilityNodeInfo targetNode = findNodeById(appTitle, id);
        if (targetNode != null) {
            clickRet = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            LogUtils.d(TAG, "tapViewById - targetNodes:" + targetNode.toString() + ", tap ret:" + clickRet);
            if (!clickRet) {
                Rect bound = new Rect();
                targetNode.getBoundsInScreen(bound);
                tap(bound.centerX(), bound.centerY());
                clickRet = true;
            }
        }
        return clickRet;
    }

    public boolean tapViewByText(String appTitle) {
        return false;
    }

    public boolean tapViewByText(String appTitle, String text) {
//        LogUtils.d(TAG, "tapViewByText - appTitle:" + appTitle + ", text:" + text);
        boolean clickRet = false;
        AccessibilityNodeInfo targetNode = findNodeByText(appTitle, text);
        if (targetNode != null) {
            clickRet = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            LogUtils.d(TAG, "tapViewByText - targetNodes:" + targetNode.toString() + ", tap ret:" + clickRet);
            if (!clickRet) {
                Rect bound = new Rect();
                targetNode.getBoundsInScreen(bound);
                tap(bound.centerX(), bound.centerY());
                clickRet = true;
            }
        }
        return clickRet;
    }

    public AccessibilityNodeInfo findNodeByText(String appTitle, String text) {
        if (mAccessibilityService == null) return null;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
//            LogUtils.d(TAG, "findNodeByText - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootInfo = windowInfo.getRoot();
                if (rootInfo != null) {
//                    LogUtils.d(TAG, "findNodeByText - rootInfo info:" + rootInfo.toString());
                    AccessibilityNodeInfo targetNode = findNodeByText(rootInfo, text);
                    return targetNode;
                }
            }
        }

        return null;
    }

    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo nodeInfo, String searchText) {
        if (nodeInfo == null) return null;

//        LogUtils.d(TAG, "findNodeByText:" + nodeInfo.toString());
        int childCnt = nodeInfo.getChildCount();
        if (childCnt == 0 && nodeInfo.getText() != null) {
            if (searchText.equals(nodeInfo.getText().toString().trim())) {
                return nodeInfo;
            } else {
                return null;
            }
        } else {
            for (int i = 0; i < childCnt; i++) {
                AccessibilityNodeInfo searchNode = findNodeByText(nodeInfo.getChild(i), searchText);
                if (searchNode != null) {
                    return searchNode;
                }
            }
            return null;
        }
    }

    public int existForGivenTestList(String appTitle, List<String> textList) {
        if (mAccessibilityService == null) return -1;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            LogUtils.d(TAG, "existForGivenTestList - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                AccessibilityNodeInfo rootInfo = windowInfo.getRoot();
                if (rootInfo != null) {
                    return existForGivenTestList(rootInfo, textList);
                }
            }
        }

        return -1;
    }

    private int existForGivenTestList(AccessibilityNodeInfo nodeInfo, List<String> textList) {
        LogUtils.d(TAG, "existForGivenTestList:" + nodeInfo.toString());
        int ret = -1;
        int childCnt = nodeInfo.getChildCount();
        if (childCnt == 0 && nodeInfo.getText() != null) {
            for (int index = 0; index < textList.size(); index++) {
                if (textList.get(index).equals(nodeInfo.getText().toString().trim())) {
                    return index;
                }
            }
        } else {
            for (int i = 0; i < childCnt; i++) {
                ret = existForGivenTestList(nodeInfo.getChild(i), textList);
                if (ret > 0) {
                    break;
                }
            }
        }
        return ret;
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
                Imgproc.drawMarker(source, p, new Scalar(0, 255, 0, 255));
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

    public Point findViewBySIFT(Bitmap templateBm) {
        Log.d(TAG, "findViewBySURF");

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
            Utils.bitmapToMat(screenBitmap, sourceImage, true);
            Mat templateImage = new Mat();
            Utils.bitmapToMat(templateBm, templateImage, true);

            MatOfKeyPoint templateMatOfKeyPoints = new MatOfKeyPoint();
            MatOfKeyPoint sourceMatOfKeyPoints = new MatOfKeyPoint();
            Mat templateDescriptor = new Mat();
            Mat sourceDescriptor = new Mat();
            //        FastFeatureDetector fastFeatureDetector = FastFeatureDetector.create();
            SIFT surf = SIFT.create();

            // detect features
            //        fastFeatureDetector.detect(templateImage, templateMatOfKeyPoints);
            //        fastFeatureDetector.detect(sourceImage, sourceMatOfKeyPoints);
            surf.detect(templateImage, templateMatOfKeyPoints);
            surf.detect(sourceImage, sourceMatOfKeyPoints);

            // compute
            surf.compute(templateImage, templateMatOfKeyPoints, templateDescriptor);
            surf.compute(sourceImage, sourceMatOfKeyPoints, sourceDescriptor);

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

                //            Imgproc.drawMarker(sourceImage, newPoint, new Scalar(0, 255, 0, 255));
                Imgproc.drawMarker(sourceImage, newPoint, new Scalar(0, 0, 255, 255),
                        Imgproc.MARKER_STAR, 100, 30, 0);

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
                //            Imgproc.drawMarker(sourceImage, maxNearNumPoint, new Scalar(255, 0, 0, 255));
                Imgproc.drawMarker(sourceImage, maxNearNumPoint, new Scalar(255, 0, 0, 255),
                        Imgproc.MARKER_STAR, 100, 30, 0);
                org.opencv.android.Utils.matToBitmap(sourceImage, screenBitmap);

                if (mFindView != null) {
                    mFindView.onFind(screenBitmap);
                }

                return maxNearNumPoint;
            }
        }

        return null;
    }

    public Point findViewBySURF(Bitmap templateBm) {
        Log.d(TAG, "findViewBySURF");

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
            Utils.bitmapToMat(screenBitmap, sourceImage, true);
            Mat templateImage = new Mat();
            Utils.bitmapToMat(templateBm, templateImage, true);

            MatOfKeyPoint templateMatOfKeyPoints = new MatOfKeyPoint();
            MatOfKeyPoint sourceMatOfKeyPoints = new MatOfKeyPoint();
            Mat templateDescriptor = new Mat();
            Mat sourceDescriptor = new Mat();
    //        FastFeatureDetector fastFeatureDetector = FastFeatureDetector.create();
            SURF surf = SURF.create();

            // detect features
    //        fastFeatureDetector.detect(templateImage, templateMatOfKeyPoints);
    //        fastFeatureDetector.detect(sourceImage, sourceMatOfKeyPoints);
            surf.detect(templateImage, templateMatOfKeyPoints);
            surf.detect(sourceImage, sourceMatOfKeyPoints);

            // compute
            surf.compute(templateImage, templateMatOfKeyPoints, templateDescriptor);
            surf.compute(sourceImage, sourceMatOfKeyPoints, sourceDescriptor);

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

    //            Imgproc.drawMarker(sourceImage, newPoint, new Scalar(0, 255, 0, 255));
                Imgproc.drawMarker(sourceImage, newPoint, new Scalar(0, 0, 255, 255),
                        Imgproc.MARKER_STAR, 100, 30, 0);

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
    //            Imgproc.drawMarker(sourceImage, maxNearNumPoint, new Scalar(255, 0, 0, 255));
                Imgproc.drawMarker(sourceImage, maxNearNumPoint, new Scalar(255, 0, 0, 255),
                        Imgproc.MARKER_STAR, 100, 30, 0);
                org.opencv.android.Utils.matToBitmap(sourceImage, screenBitmap);

                if (mFindView != null) {
                    mFindView.onFind(screenBitmap);
                }

                return maxNearNumPoint;
            }
        }

        return null;
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

                Imgproc.drawMarker(sourceImage, newPoint, new Scalar(0, 0, 255, 255),
                        Imgproc.MARKER_STAR, 100, 30, 0);

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
//                Imgproc.drawMarker(sourceImage, maxNearNumPoint, new Scalar(255, 0, 0, 255));
                Imgproc.drawMarker(sourceImage, maxNearNumPoint, new Scalar(255, 0, 0, 255),
                        Imgproc.MARKER_STAR, 100, 30, 0);
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
                Imgproc.drawMarker(sourceImage, matchPoint, new Scalar(0, 255, 0, 255));
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


    /**
     * Screen actions
     */

    public void back() {
        Log.d(TAG, "back:");
        if (mAccessibilityService == null) return ;

        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public boolean tap(AccessibilityNodeInfo nodeInfo) {
        boolean clickRet = false;
        if (nodeInfo != null) {
            clickRet = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            LogUtils.d(TAG, "tap - nodeInfo:" + nodeInfo.toString() + ", tap ret:" + clickRet);
            if (!clickRet) {
                Rect bound = new Rect();
                nodeInfo.getBoundsInScreen(bound);
                tap(bound.centerX(), bound.centerY());
                return true;
            }
        }
        return clickRet;
    }

    public void tap(int x, int y) {
        if (mAccessibilityService == null) return;
        LogUtils.d(TAG, "tap - x:" + x + ", y:" + y);

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
                LogUtils.d(TAG, "onCompleted");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                LogUtils.d(TAG, "onCancelled");
            }
        }, null);
    }

    private volatile boolean startLoopTap = false;

    public void stopLoopTap() {
        startLoopTap = false;
    }
    public void startLoopTap(final int x, final int y)  {
        startLoopTap = true;
        loopTap(x, y);
    }

    public void loopTap(final int x, final int y) {
        if (mAccessibilityService == null) return;
        LogUtils.d(TAG, "tap - x:" + x + ", y:" + y);

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription strokeDescription =
                new GestureDescription.StrokeDescription(path, 0, 1);
        GestureDescription description = new GestureDescription.Builder()
                .addStroke(strokeDescription)
                .build();
        mAccessibilityService.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                LogUtils.d(TAG, "onCompleted");
                if (startLoopTap) {
                    loopTap(x, y);
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                LogUtils.d(TAG, "onCancelled");
            }
        }, null);
    }

    public void longPress() {

    }

    public void swipeUp(String appTitle) {
        Log.d(TAG, "swipeUp:" + appTitle);
        if (mAccessibilityService == null) return ;

        int targetWidth = mScreenWidth;
        int targetHeight = mScreenHeight;
        int top = 0;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            Log.d(TAG, "swipeUp - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                Rect bound = new Rect();
                windowInfo.getBoundsInScreen(bound);
                targetWidth = bound.width();
                targetHeight = bound.height();
                top = bound.top;
                break;
            }
        }

        Log.d(TAG, "swipeUp - targetWidth:" + targetWidth + ", targetHeight:" + targetHeight + ", top" + top);
        swipe(targetWidth / 2, targetHeight / 4 * 3 + top,
                targetWidth / 2, 0);
    }

    public void swipeDown(String appTitle) {
        Log.d(TAG, "swipeDown:" + appTitle);
        if (mAccessibilityService == null) return ;

        int targetWidth = mScreenWidth;
        int targetHeight = mScreenHeight;
        int top = 0;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            Log.d(TAG, "swipeDown - window info:" + windowInfo.toString());
            if (appTitle.equals(windowInfo.getTitle())) {
                Rect bound = new Rect();
                windowInfo.getBoundsInScreen(bound);
                targetWidth = bound.width();
                targetHeight = bound.height();
                top = bound.top;
                break;
            }
        }

        Log.d(TAG, "swipeDown - targetWidth:" + targetWidth + ", targetHeight:" + targetHeight + ", top" + top);
        swipe(targetWidth/2, targetHeight / 4 + top,
                targetWidth/2, mScreenHeight);
    }

    private boolean mSwipeBottom = false;

    public void screenSwipeUp(){
        Log.d(TAG, "screenSwipeUp:" + (mAccessibilityService == null) + ", y:" + mScreenHeight / 10);
        if (mAccessibilityService == null) return;

        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            Log.d(TAG, "window info:" + windowInfo.toString());
            if (windowInfo.getType() == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER) {
                mSwipeBottom = true;
            }
        }

        final int realHeight = mSwipeBottom ? mScreenHeight / 2 : mScreenHeight;
        swipe(mScreenWidth / 2, realHeight / 4 * 3,
                mScreenWidth / 2, 0);

        if (mSwipeBottom) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipe(mScreenWidth / 2, realHeight / 4 * 3 + realHeight,
                            mScreenWidth / 2, 0);
                    mSwipeBottom = false;
                }
            }, 1000);
        }
    }

    public void screenSwipeDown() {
        Log.d(TAG, "screenSwipeDown:" + (mAccessibilityService == null) + ", y:" + mScreenHeight / 10);
        if (mAccessibilityService == null) return;

        mSwipeBottom = false;
        for (AccessibilityWindowInfo windowInfo: mAccessibilityService.getWindows()) {
            Log.d(TAG, "window info:" + windowInfo.toString());
            if (windowInfo.getType() == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER) {
                mSwipeBottom = true;
            }
        }

        final int realHeight = mSwipeBottom ? mScreenHeight / 2 : mScreenHeight;
        swipe(mScreenWidth/2, realHeight / 4,
                mScreenWidth/2, mScreenHeight);

        if (mSwipeBottom) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipe(mScreenWidth / 2, realHeight / 4 + realHeight,
                            mScreenWidth / 2, mScreenHeight);
                    mSwipeBottom = false;
                }
            }, 1000);
        }
    }

    private void swipe(int startX, int startY, int endX, int endY) {
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);

        GestureDescription.StrokeDescription strokeDescription =
                new GestureDescription.StrokeDescription(path, 0, 500);
        GestureDescription description = new GestureDescription.Builder()
                .addStroke(strokeDescription)
                .build();
        mAccessibilityService.dispatchGesture(description, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
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
