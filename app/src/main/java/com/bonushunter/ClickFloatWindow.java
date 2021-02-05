package com.bonushunter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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

public class ClickFloatWindow implements View.OnTouchListener {

    private static final String TAG = ClickFloatWindow.class.getSimpleName();

    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mFloatView;

    private TextView mStartBtn;
    private TextView mChangeBtn;
    private TextView mCloseBtn;

    private static ClickFloatWindow singleton;

    private ScreenManager mScreenManager;

    private int mX, mY;
    private boolean mShown = false;

    private ClickFloatWindow(Context context, int x, int y) {
        mContext = context;
        mX = x;
        mY = y;

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

        mFloatView = LayoutInflater.from(mContext).inflate(R.layout.view_click_float, null);
        mFloatView.setOnTouchListener(this);
        mStartBtn = mFloatView.findViewById(R.id.start_btn);
        mChangeBtn = mFloatView.findViewById(R.id.change_btn);
        mCloseBtn = mFloatView.findViewById(R.id.close_btn);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mScreenManager.startLoopTap(mX, mY);
                    }
                }).start();
            }
        });

        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShown) {
                    mShown = false;
                    mScreenManager.stopLoopTap();
                    mWindowManager.removeView(mFloatView);
                    Intent intent = new Intent(mContext, ClickActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShown) {
                    mShown = false;
                    mScreenManager.stopLoopTap();
                    mWindowManager.removeView(mFloatView);
                }
            }
        });
    }

    public static ClickFloatWindow getInstance(Context context, int x, int y) {
        if (singleton == null) {
            synchronized (ClickFloatWindow.class) {
                if (singleton == null) {
                    singleton = new ClickFloatWindow(context, x, y);
                }
            }
        }
        return singleton;
    }

    public void show() {
        if (!mShown) {
            mWindowManager.addView(mFloatView, mLayoutParams);
            mShown = true;
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
}
