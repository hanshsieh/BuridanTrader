package com.buridantrader;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

public class PlanConsumer {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final TradingPlanner planner;
    private Future workerFuture;

    public PlanConsumer(@Nonnull TradingPlanner planner) {
        this.planner = planner;
    }

    public synchronized void start() {
        if (workerFuture != null) {
            throw new IllegalStateException("Already started");
        }
        workerFuture = executor.submit(new PlanConsumeWorker(planner));
    }

    public synchronized void stop(long timeout, @Nonnull TimeUnit timeUnit)
        throws InterruptedException {
        executor.shutdownNow();
        workerFuture.cancel(true);
        executor.awaitTermination(timeout, timeUnit);
        workerFuture = null;
    }
}
