package com.bonushunter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bonushunter.apps.AppRobotFactory;
import com.bonushunter.apps.IAppRobot;
import com.bonushunter.manager.ScreenManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FloatWindow implements View.OnTouchListener {

    private static final String TAG = FloatWindow.class.getSimpleName();

    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mFloatView;

    private ImageView mExpend;
    private TextView mStartBtn;
    private TextView mTaskDesc;
    private TextView mRemainTime;
    private LinearLayout mContent2;
    private TextView mTaskDesc2;
    private TextView mRemainTime2;

    private static FloatWindow singleton;

    private ScreenManager mScreenManager;

    private CountDownLatch mStopLatch;

    private boolean mShown = false;

    private FloatWindow(Context context) {
        mContext = context;

        mScreenManager = ScreenManager.getInstance(mContext);

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
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // set the origin
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.x = 50;
        mLayoutParams.y = 100;

        mFloatView = LayoutInflater.from(mContext).inflate(R.layout.view_float, null);
        mFloatView.setOnTouchListener(this);
        mExpend = mFloatView.findViewById(R.id.expend_btn);
        mStartBtn = mFloatView.findViewById(R.id.start_btn);
        mTaskDesc = mFloatView.findViewById(R.id.task_desc);
        mRemainTime = mFloatView.findViewById(R.id.remain_time);
        mContent2 = mFloatView.findViewById(R.id.content2);
        mContent2.setVisibility(View.GONE);
        mTaskDesc2 = mFloatView.findViewById(R.id.task_desc2);
        mRemainTime2 = mFloatView.findViewById(R.id.remain_time2);

        mExpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = mStartBtn.getVisibility();
                if (visibility == View.GONE) {
                    mStartBtn.setVisibility(View.VISIBLE);
                } else {
                    mStartBtn.setVisibility(View.GONE);
                }

//                Bitmap xigua_fudai = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.xigua_fudai);
//                mScreenManager.findViewBySIFT(xigua_fudai);
                mScreenManager.loopAllViews();
            }
        });

        mRemainTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap xigua_fudai = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.xigua_fudai);
                mScreenManager.findViewBySURF(xigua_fudai);
            }
        });


        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mShown) {
                            for (IAppRobot appRobot: mRunningApps) {
                                appRobot.stop(mStopLatch);
                                mRunningApps.clear();
                                mShown = false;
                                mWindowManager.removeView(mFloatView);
                            }
                        }
//                        if (mShown) {
//                            mStopLatch = new CountDownLatch(mRunningApps.size());
//                            for (IAppRobot appRobot: mRunningApps) {
//                                appRobot.stop(mStopLatch);
//                            }
//                            try {
//                                mStopLatch.await();
//                                mRunningApps.clear();
//                                mShown = false;
//                                mWindowManager.removeView(mFloatView);
//                            } catch (Exception e) {
//                                Log.d(TAG, "stop error:" + e.toString());
//                            }
//                        }
                    }
                }).start();

//                Bitmap xigua_fudai = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.xigua_fudai);
//                mScreenManager.findViewBySURF(xigua_fudai);
//                mScreenManager.findViewBySURF(xigua_fudai);
//                mScreenManager.findViewByFAST(xigua_fudai);



//                List<AccessibilityNodeInfo> mBottomNavNodes = mScreenManager.getViewsById("今日头条极速版",
//                    "com.ss.android.article.lite:id/ey");
//                AccessibilityNodeInfo taskNavNode = mBottomNavNodes.get(3);
//                Rect bound = new Rect();
//
////                taskNavNode.getBoundsInScreen(bound);
////                mScreenManager.tap(bound.centerX(), bound.centerY());
//                mScreenManager.tapViewByText("treasure-box-enable-1.da338c08");
            }

        });
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

    private void show() {
        if (!mShown) {
            mShown = true;
            mRunningApps.clear();
            // show float window
            mWindowManager.addView(mFloatView, mLayoutParams);
            // start screen capture
            mScreenManager.startCapture();
        }
    }

    List<IAppRobot> mRunningApps = new ArrayList<>();

    public void start(String packageName) {
        show();

        if (mRunningApps.size() == 0) {
            mContent2.setVisibility(View.GONE);
            // start task
            IAppRobot appRobot = AppRobotFactory.getAppRobot(mContext, packageName);
            appRobot.setDescAndRemainView(mTaskDesc, mRemainTime);
            appRobot.start();
            mRunningApps.add(appRobot);
            Toast.makeText(mContext, "脚本启动成功", Toast.LENGTH_SHORT).show();
        } else if (mRunningApps.size() == 1) {
            if (mScreenManager.splitWindowEnabled()) {
                mContent2.setVisibility(View.VISIBLE);
                IAppRobot appRobot = AppRobotFactory.getAppRobot(mContext, packageName);
                appRobot.setDescAndRemainView(mTaskDesc2, mRemainTime2);
                appRobot.start();
                mRunningApps.add(appRobot);
                Toast.makeText(mContext, "脚本启动成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "请先关闭正在运行的脚本再启动新的脚本", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "请先关闭正在运行的脚本再启动新的脚本", Toast.LENGTH_SHORT).show();
        }
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

                if (mLayoutParams.y < 96) {
                    mLayoutParams.y = 96;
                }

                mPressX = x;
                mPressY = y;

                mWindowManager.updateViewLayout(v, mLayoutParams);
                break;
        }

        return false;
    }

    public void setRemianTime (int seconds) {
        mRemainTime.setText(String.valueOf(seconds));
    }

    public void setTaskDesc(String desc) {
        mTaskDesc.setText(desc);
    }

    private IAppRobot mAppRobot;
    public void setAppRobot(IAppRobot appRobot) {
        mAppRobot = appRobot;
    }
}
