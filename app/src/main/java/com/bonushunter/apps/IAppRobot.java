package com.bonushunter.apps;

import android.widget.TextView;

import java.util.concurrent.CountDownLatch;

public interface IAppRobot {

    void start();

    void stop(CountDownLatch stopLatch);

    void doInBackground() throws InterruptedException;

    void updateFloatPrompt(String prompt);

    void setDescAndRemainView(TextView desc, TextView remain);
}
