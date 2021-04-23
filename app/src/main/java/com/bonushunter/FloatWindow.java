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
import android.widget.ScrollView;
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
    private TextView mLog;
    private ScrollView mLogContain;

    private static FloatWindow singleton;

    private ScreenManager mScreenManager;

    private CountDownLatch mStopLatch;

    private boolean mShown = false;

    int test = 0;

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
        mLog = mFloatView.findViewById(R.id.log);
        mLogContain = mFloatView.findViewById(R.id.log_content);

        mExpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = mLogContain.getVisibility();
                if (visibility == View.GONE) {
                    mLogContain.setVisibility(View.VISIBLE);
                } else {
                    mLogContain.setVisibility(View.GONE);
                }

//                Bitmap xigua_fudai = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.xigua_fudai);
//                mScreenManager.findViewBySIFT(xigua_fudai);
                mScreenManager.loopAllViews();
//                AccessibilityNodeInfo nodeInfo = mScreenManager.getNodeById("快手极速版", "com.kuaishou.nebula:id/exit_btn");
//                if (nodeInfo != null) {
//                    mScreenManager.tap(nodeInfo);
//                }
//                mScreenManager.getNodesByFuzzySearch("快手极速版", "看精彩视频赚更多");
//                mScreenManager.getNodesByFuzzySearch("快手极速版", "开宝箱得金币");
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
            // start task
            IAppRobot appRobot = AppRobotFactory.getAppRobot(mContext, packageName);
            appRobot.setDescAndRemainView(mTaskDesc, mLog);
            appRobot.start();
            mRunningApps.add(appRobot);
            Toast.makeText(mContext, "脚本启动成功", Toast.LENGTH_SHORT).show();
        }
//        else if (mRunningApps.size() == 1) {
//            if (mScreenManager.splitWindowEnabled()) {
//                IAppRobot appRobot = AppRobotFactory.getAppRobot(mContext, packageName);
//                appRobot.setDescAndRemainView(mTaskDesc2, mRemainTime2);
//                appRobot.start();
//                mRunningApps.add(appRobot);
//                Toast.makeText(mContext, "脚本启动成功", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(mContext, "请先关闭正在运行的脚本再启动新的脚本", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(mContext, "请先关闭正在运行的脚本再启动新的脚本", Toast.LENGTH_SHORT).show();
//        }
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
//        mRemainTime.setText(String.valueOf(seconds));
    }

    public void setTaskDesc(String desc) {
        mTaskDesc.setText(desc);
    }

    private IAppRobot mAppRobot;
    public void setAppRobot(IAppRobot appRobot) {
        mAppRobot = appRobot;
    }



    private final int MAX_LINES = 30;
    private List<String> mLogList = new ArrayList<>();
    private int mStartIndex = 0;
    private int mEndIndex = 0;

    public void appendLog(final String log) {
//        mUiHandler.post(new Runnable() {
//            @Override
//            public void run() {
                if (mLogList.size() < MAX_LINES) {
                    mStartIndex = 0;
                    mLogList.add(log);
                    mEndIndex = mLogList.size() - 1;
                } else {
                    mEndIndex += 1;
                    if (mEndIndex >= MAX_LINES) {
                        mEndIndex = 0;
                    }
                    mLogList.set(mEndIndex, log);
                    mStartIndex = mEndIndex + 1;
                    if (mStartIndex >= MAX_LINES) {
                        mStartIndex = 0;
                    }
                }

                int firstIndex = mStartIndex;
                int lastIndex = mEndIndex;
                if (mStartIndex > mEndIndex) {
                    lastIndex = mEndIndex + MAX_LINES;
                }
                StringBuilder stringBuilder = new StringBuilder();
                while (firstIndex <= lastIndex) {
                    int realIndex = firstIndex++;
                    if (realIndex >= MAX_LINES) {
                        realIndex -= MAX_LINES;
                    }
                    stringBuilder.append(mLogList.get(realIndex)).append("\n");
                }

                mLog.setText(stringBuilder);
//            }
//        });
    }
}
