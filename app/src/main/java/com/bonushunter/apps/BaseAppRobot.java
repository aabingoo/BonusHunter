package com.bonushunter.apps;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class BaseAppRobot implements IAppRobot {

    public static final String TAG = BaseAppRobot.class.getSimpleName();

    private WeakReference<Context> mContextRef;

    private Handler mUiHandler;
    private Handler mWorkHandler;

    protected String mAppTitle;

    protected boolean mStop = false;
//    private CountDownLatch mStopLatch;
//    private CountDownLatch mCountDownLatch;

    public BaseAppRobot(Context context) {
        mContextRef = new WeakReference<>(context);

        // create UI handler
        mUiHandler = new Handler(Looper.getMainLooper());
        // create work Handler
        HandlerThread handlerThread = new HandlerThread(mAppTitle + "_thread");
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
                try {
                    doInBackground();
                } catch (Exception e) {
                    Log.d(TAG, "exception:" + e.toString());
                }
            }
        });
    }

    @Override
    public void stop(CountDownLatch stopLatch) {
//        updateFloatPrompt("停止抖音极速版中");
//        mStopLatch = stopLatch;
        mStop = true;
    }

    public void stop() {
        mStop = true;
    }

    protected boolean checkStop() {
//        if (mStop) {
//            updateFloatPrompt(String.format("%s已停止", mAppTitle));
//            mStopLatch.countDown();
//        }
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

    private final int MAX_LINES = 30;
    private List<String> mLogList = new ArrayList<>();
    private int mStartIndex = 0;
    private int mEndIndex = 0;

    public void appendLog(final String log) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    protected void wait(int seconds) {
//        mWaitSeconds = seconds;
//        mCountDownLatch = new CountDownLatch(mWaitSeconds + 1);
//        mUiHandler.post(mWaitRunnable);
//        try {
//            mCountDownLatch.await();
//        } catch (Exception e) {
//            Log.d(TAG, "wait error:" + e.toString());
//        }
    }

    private Runnable mWaitRunnable = new Runnable() {
        @Override
        public void run() {
//            updateRemainSeconds();
//            mCountDownLatch.countDown();
//            if (mWaitSeconds > 0) {
//                mWaitSeconds -= 1;
//                mUiHandler.postDelayed(this, 1000);
//            }
        }
    };

    private TextView mDescView;
    private TextView mLog;
    @Override
    public void setDescAndRemainView(TextView desc, TextView log) {
        mDescView = desc;
        mLog = log;
    }
}
