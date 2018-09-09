package com.buridantrader;

public class ThreadRuntime {
    public boolean isCurrentThreadInterrupted() {
        return Thread.currentThread().isInterrupted();
    }
}
