package com.buridantrader.services.system;

import org.apache.ibatis.io.Resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.InputStream;

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
