package com.bonushunter;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Button;

//import com.android.uiautomator.core.UiDevice;

public class FloatWindow implements View.OnTouchListener {

    private static final String TAG = FloatWindow.class.getSimpleName();

    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private static FloatWindow singleton;

    private FloatWindow(Context context) {
        mContext = context;
    }

    public static FloatWindow getInstance(Context context) {
        if (singleton == null) {
            synchronized (FloatWindow.class) {
                if (singleton == null) {
                    singleton = new FloatWindow(context);
                }
            }
        }
        return singleton;
    }

    public void requestPermissionIfNeed() {
        if (!Settings.canDrawOverlays(mContext)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
            mContext.startActivity(intent);
        }
    }

    private int cnt = 0;
    private volatile boolean enable = false;

    public void show() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.width = 200;
        mLayoutParams.height = 200;
        // set the origin
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.x = 0;
        mLayoutParams.y = 100;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (enable && Utils.service != null) {
                        cnt += 1;
                        Log.d(TAG, "cnt:" + cnt);
                        if (cnt > 20) {
                            Utils.swipeDown();
                            if (cnt == 35) {
                                cnt = 0;
                            }
                        } else {
                            Utils.swipeUp();
                        }
                        try {
                            Thread.sleep(15000);
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }).start();

        Button button = new Button(mContext);
        button.setText("111");
        button.setBackgroundColor(Color.BLUE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enable = !enable;

//                enable = !enable;
//                Log.d(TAG, "enable:" + enable);
//
////                UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
////                uiDevice.pressHome();
////                UiDevice.getInstance().pressHome();
//                if (Utils.service != null) {
//                    Log.d(TAG, "22222");
//
////                    Log.d(TAG, "return:" + ret);
//
//
////                    for (AccessibilityWindowInfo windowInfo: Utils.service.getWindows()) {
////                        Log.d(TAG, "window:" + windowInfo.toString());
////                        if (windowInfo.getType() == AccessibilityWindowInfo.TYPE_APPLICATION) {
////                            windowInfo.getRoot().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
////                            windowInfo.getRoot().performAction(AccessibilityNodeInfo.ACTION_CLICK);
////                            try {
////
////                            } catch (Exception e) {
////
////                            }
////                        }
////                    }
//
//                }
            }
        });
        button.setOnTouchListener(this);

        mWindowManager.addView(button, mLayoutParams);
    }

    private int mPressX;
    private int mPressY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressX = (int) event.getRawX();
                mPressY = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                mLayoutParams.x += x - mPressX;
                mLayoutParams.y += y - mPressY;

                mPressX = x;
                mPressY = y;
                Log.d(TAG, "x,"+ x+ ",y,"+y);

                mWindowManager.updateViewLayout(v, mLayoutParams);
                break;
        }

        return false;
    }


}
