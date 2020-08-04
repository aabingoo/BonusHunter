package com.bonushunter.task;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.bonushunter.apps.BaseAppRobot;

import java.lang.ref.WeakReference;

public abstract class BaseTask implements ITask {

    public static final String TAG = BaseTask.class.getSimpleName();

    private Handler mUiHandler;
    private int mWaitSeconds;

    private Handler mWorkHandler;

    public BaseTask(int waitSeconds) {
        mUiHandler = new Handler();

        HandlerThread handlerThread = new HandlerThread("app_work_thread");
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());

        mWaitSeconds = waitSeconds;
    }

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

    public void start() {
        mWorkHandler.post(mWorkRunnable);
    }
}
