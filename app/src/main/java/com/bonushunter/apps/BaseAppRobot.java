package com.bonushunter.apps;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.lang.ref.WeakReference;
import java.util.logging.LogRecord;

public abstract class BaseAppRobot implements IAppRobot {

    public static final String TAG = BaseAppRobot.class.getSimpleName();

    private WeakReference<Context> mContextRef;

    private Handler mUiHandler;
    private int mWaitSeconds;

    private Handler mWorkHandler;

    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWaitSeconds > 0) {
                updateRemainSeconds(mWaitSeconds);
                mWaitSeconds -= 1;
                mUiHandler.postDelayed(this, 1000);
            }
        }
    };

    private Runnable mWorkRunnable = new Runnable() {
        @Override
        public void run() {
            doInBackground();
        }
    };

    public BaseAppRobot(Context context) {
        mContextRef = new WeakReference<>(context);
        mUiHandler = new Handler();
        HandlerThread handlerThread = new HandlerThread("app_work_thread");
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());
    }

    protected Context getContext() {
        return mContextRef.get();
    }

    /**
     * should run at thread
     * @param seconds
     */
    public void waitSeconds(int seconds) throws InterruptedException {
        mWaitSeconds = seconds;
        mUiHandler.post(mTimerRunnable);
        Thread.sleep(seconds * 1000);
    }

    @Override
    public void start() {
//        mWorkHandler.post(mWorkRunnable);
    }
}
