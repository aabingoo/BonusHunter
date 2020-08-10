package com.bonushunter;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bonushunter.apps.IAppRobot;
import com.bonushunter.manager.ScreenManager;

public class FloatWindow implements View.OnTouchListener {

    private static final String TAG = FloatWindow.class.getSimpleName();

    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mFloatView;

    private ImageView mExpend;
    private TextView mStartBtn;
    private TextView mTaskDesc;
    private TextView mRemianTime;

    private static FloatWindow singleton;

    private ScreenManager mScreenManager;

    private FloatWindow(Context context) {
        mContext = context;

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
        mRemianTime = mFloatView.findViewById(R.id.remain_time);
        mExpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = mStartBtn.getVisibility();
                if (visibility == View.GONE) {
                    mStartBtn.setVisibility(View.VISIBLE);
                } else {
                    mStartBtn.setVisibility(View.GONE);
                }
            }
        });
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.d(TAG, "start task");
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        FindOneAndClickTask task = new FindOneAndClickTask(mContext, 20);
//                        task.doInBackground();
//                    }
//                }).start();

//                mScreenManager.findAndTapView("关闭广告");

                enable = !enable;
                if (enable) {
                    mStartBtn.setText(R.string.stop);
                    mScreenManager.findAndTapView("关闭广告");
                } else {
                    mStartBtn.setText(R.string.start);
                    mScreenManager.findAndTapView("福利");
                }
            }
        });

        mScreenManager = ScreenManager.getInstance(mContext);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (enable) {
                        cnt += 1;
                        Log.d(TAG, "cnt:" + cnt);
                        if (cnt > 3) {
                            ScreenManager.getInstance(mContext).screenSwipeDown();
                            if (cnt == 6) {
                                cnt = 0;
                            }
                        } else {
                            ScreenManager.getInstance(mContext).screenSwipeUp();
                        }
                        try {
                            Thread.sleep(15000);
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }).start();
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

    private int cnt = 0;
    private volatile boolean enable = false;

    public void show() {
        // show float window
        mWindowManager.addView(mFloatView, mLayoutParams);
        // start task
        if (mAppRobot != null) {
            mScreenManager.startCapture();
            mAppRobot.start();
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
        mRemianTime.setText(String.valueOf(seconds));
    }

    public void setTaskDesc(String desc) {
        mTaskDesc.setText(desc);
    }

    private IAppRobot mAppRobot;
    public void setAppRobot(IAppRobot appRobot) {
        mAppRobot = appRobot;
    }
}
