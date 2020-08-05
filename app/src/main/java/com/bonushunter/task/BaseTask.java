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

    private ITask mPreviousTask;
    private ITask mNextTask;

    private volatile boolean mRunning = false;

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

            boolean ret = doInBackground();

            if (!mRunning) return;

            // run the task
            if (ret) {
                // perform next task
                if (mNextTask != null) {
                    mNextTask.start();
                }
            } else {
                // back to previous task
                if (mPreviousTask != null) {
                    mPreviousTask.start();
                }
            }
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
        mRunning = true;
        mWorkHandler.post(mWorkRunnable);
    }

    @Override
    public void stop() {
        mRunning = false;
    }

    @Override
    public void updateRemainSeconds(int remainSeconds) {
        FloatWindow.getInstance(mContext).setRemianTime(remainSeconds);
    }

    @Override
    public void setPreviousTask(ITask previousTask) {
        mPreviousTask = previousTask;
    }

    @Override
    public void setNextTask(ITask nextTask) {
        mNextTask = nextTask;
    }
}
