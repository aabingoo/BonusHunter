package com.bonushunter;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;


import com.bonushunter.utils.CommonUtils;

public class BHAccessibilityService extends AccessibilityService {
    private static final String TAG = BHAccessibilityService.class.getSimpleName();

    public BHAccessibilityService() {
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
//        Log.d(TAG, "onAccessibilityEvent - event:" + event.toString());

        if (CommonUtils.service == null) {
            CommonUtils.service = this;
        }

//        getWindows().


//        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
//         UiAutomation



        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED - content change type:" + event.getContentChangeTypes());
//                String pckName = event.getPackageName().toString();
//                if (pckName.contains("launcher")) {
//                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//                    List<AccessibilityNodeInfo> appNodes = rootNode.findAccessibilityNodeInfosByText("西瓜视频");
//                    Log.d(TAG, "find node cnt:" + appNodes.size());
//                    if (appNodes.size() == 1) {
//                        Log.d(TAG, "node info:" + appNodes.toString());
//                        appNodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    }
//
//                    List<AccessibilityWindowInfo> windowNodes = getWindows();
//                    Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED windowNodes cnt:" + windowNodes.size());
//                    for (AccessibilityWindowInfo windowNode: windowNodes) {
//                        Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED window info:" + windowNode.toString());
//                    }
//                }
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED");
//                String pck = event.getPackageName().toString();
//                if (pck.equals("com.ss.android.article.video")) {
//                    Log.d(TAG, "open 西瓜视频");
//
//
//                    List<AccessibilityWindowInfo> windowNodes = getWindows();
//                    Log.d(TAG, "windowNodes cnt:" + windowNodes.size());
//                    for (AccessibilityWindowInfo windowNode: windowNodes) {
//                        Log.d(TAG, "window info:" + windowNode.toString());
//                    }
//
////                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
////                    List<AccessibilityNodeInfo> appNodes = rootNode.findAccessibilityNodeInfosByText("直播");
////                    Log.d(TAG, "find node cnt:" + appNodes.size());
////                    for (AccessibilityNodeInfo node: appNodes) {
////                        Log.d(TAG, "node info:" + appNodes.toString());
////                    }
//                }
//
//                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                break;

            case AccessibilityEvent.TYPE_VIEW_CLICKED:
//                Log.d(TAG, "TYPE_VIEW_CLICKED");
//                backToHome();
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    public void backToHome() {
        Intent intent= new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
