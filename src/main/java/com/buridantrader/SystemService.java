package com.buridantrader;

import javax.annotation.Nonnull;

public class SystemService {

    @Nonnull
    public Thread currentThread() {
        return Thread.currentThread();
    }

    public void sleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    public long currentTimeMillis() {
        return java.lang.System.currentTimeMillis();
    }
}
