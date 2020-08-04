package com.bonushunter.task;

public interface ITask {

    void start();

    void doInBackground();

    void updateRemainSeconds(int remainSeconds);
}
