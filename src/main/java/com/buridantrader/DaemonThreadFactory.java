package com.buridantrader;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(@Nonnull Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
