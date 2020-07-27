package com.bonushunter;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class BHAccessibilityService extends AccessibilityService {
    private static final String TAG = BHAccessibilityService.class.getSimpleName();

    public BHAccessibilityService() {
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
//        Log.d(TAG, "onAccessibilityEvent - eventType:" + eventType);
    }

    @Override
    public void onInterrupt() {

    }
}
