package com.bonushunter.task;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.bonushunter.FloatWindow;

public abstract class BaseTask implements ITask {

    public static final String TAG = BaseTask.class.getSimpleName();

    protected Context mContext;

    private Handler mUiHandler;
    private int mWaitSeconds;

    private Handler mWorkHandler;

    public BaseTask(Context context, int waitSeconds) {
        mContext = context;

        mUiHandler = new Handler(Looper.getMainLooper());

        HandlerThread handlerThread = new HandlerThread("app_work_thread");
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());

        mWaitSeconds = waitSeconds;
    }

    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            updateRemainSeconds(mWaitSeconds);
            if (mWaitSeconds > 0) {
                mWaitSeconds -= 1;
                mUiHandler.postDelayed(this, 1000);
            }
        }
    };

    private Runnable mWorkRunnable = new Runnable() {
        @Override
        public void run() {
            // start to calculate time
            mUiHandler.post(mTimerRunnable);

            // run the task
            doInBackground();

            // perform next task
        }
    };

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
        mWorkHandler.post(mWorkRunnable);
    }

    @Override
    public void updateRemainSeconds(int remainSeconds) {
        FloatWindow.getInstance(mContext).setRemianTime(remainSeconds);
    }
}
