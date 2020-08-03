package com.bonushunter.apps;

import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;

public abstract class BaseAppRobot implements IAppRobot {

    public static final String TAG = BaseAppRobot.class.getSimpleName();

    public WeakReference<Context> mContextRef;

    public BaseAppRobot(Context context) {
        mContextRef = new WeakReference<>(context);
    }

    public interface OnAppRobotListener {
        void onResponse(String text);
    }

    private XiGuaAppRobot.OnAppRobotListener mOnAppRobotListener;

    public void setOnAppRobotListener(XiGuaAppRobot.OnAppRobotListener listener) {
        mOnAppRobotListener = listener;
    }

    public void response(String text) {
        if (mOnAppRobotListener != null) {
            mOnAppRobotListener.onResponse(text);
        }
    }

    protected Context getContext() {
        return mContextRef.get();
    }
}
