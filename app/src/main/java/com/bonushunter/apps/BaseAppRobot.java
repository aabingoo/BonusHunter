package com.bonushunter.apps;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.bonushunter.FloatWindow;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.logging.LogRecord;

public abstract class BaseAppRobot implements IAppRobot {

    public static final String TAG = BaseAppRobot.class.getSimpleName();

    private WeakReference<Context> mContextRef;

    private Handler mUiHandler;
    private Handler mWorkHandler;

    protected String mAppTitle;

    protected volatile boolean mStop = false;
    private CountDownLatch mStopLatch;

    private CountDownLatch mCountDownLatch;

    public BaseAppRobot(Context context) {
        mContextRef = new WeakReference<>(context);

        // create UI handler
        mUiHandler = new Handler(Looper.getMainLooper());
        // create work Handler
        HandlerThread handlerThread = new HandlerThread("app_work_thread");
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());
    }

    protected Context getContext() {
        return mContextRef.get();
    }

    @Override
    public void start() {
        mStop = false;
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                doInBackground();
            }
        });
    }

    @Override
    public void stop(CountDownLatch stopLatch) {
        updateFloatPrompt("停止抖音极速版中");
        mStopLatch = stopLatch;
        mStop = true;
    }

    protected boolean checkStop() {
        if (mStop) {
            updateFloatPrompt(String.format("%s已停止", mAppTitle));
            mStopLatch.countDown();
        }
        return mStop;
    }

    @Override
    public void updateFloatPrompt(final String prompt) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mDescView.setText(prompt);
            }
        });
    }

    private volatile int mWaitSeconds;

    private void updateRemainSeconds() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mRemainView.setText(String.valueOf(mWaitSeconds));
            }
        });
    }

    protected void wait(int seconds) {
        mWaitSeconds = seconds;
        mCountDownLatch = new CountDownLatch(mWaitSeconds + 1);
        mUiHandler.post(mWaitRunnable);
        try {
            mCountDownLatch.await();
        } catch (Exception e) {
            Log.d(TAG, "wait error:" + e.toString());
        }
    }

    private Runnable mWaitRunnable = new Runnable() {
        @Override
        public void run() {
            updateRemainSeconds();
            mCountDownLatch.countDown();
            if (mWaitSeconds > 0) {
                mWaitSeconds -= 1;
                mUiHandler.postDelayed(this, 1000);
            }
        }
    };

    private TextView mDescView;
    private TextView mRemainView;
    @Override
    public void setDescAndRemainView(TextView desc, TextView remain) {
        mDescView = desc;
        mRemainView = remain;
    }
}
