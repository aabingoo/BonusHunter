package com.bonushunter.task;

public interface ITask {

    void start();

    void stop();

    boolean doInBackground();

    void updateRemainSeconds(int remainSeconds);

    void updateTaskDesc(String desc);

    void setPreviousTask(ITask previousTask);

    void setNextTask(ITask nextTask);
}
