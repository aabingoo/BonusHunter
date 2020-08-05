package com.bonushunter.apps;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.lang.ref.WeakReference;
import java.util.logging.LogRecord;

public abstract class BaseAppRobot implements IAppRobot {

    public static final String TAG = BaseAppRobot.class.getSimpleName();

    private WeakReference<Context> mContextRef;

    public BaseAppRobot(Context context) {
        mContextRef = new WeakReference<>(context);
    }

    protected Context getContext() {
        return mContextRef.get();
    }

}
