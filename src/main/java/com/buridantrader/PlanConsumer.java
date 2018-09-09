package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

@ThreadSafe
public class PlanConsumer {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final TradingPlanner planner;
    private final BinanceApiRestClient client;
    private Future workerFuture;

    public PlanConsumer(@Nonnull TradingPlanner planner,
                        @Nonnull BinanceApiRestClient client) {
        this.planner = planner;
        this.client = client;
    }

    public synchronized void start() {
        if (workerFuture != null) {
            throw new IllegalStateException("Already started");
        }
        workerFuture = executor.submit(new PlanConsumeWorker(planner, client));
    }

    public synchronized void stop(long timeout, @Nonnull TimeUnit timeUnit)
        throws InterruptedException {
        if (workerFuture == null) {
            return;
        }

        // Stop accepting new tasks and waiting tasks
        executor.shutdownNow();

        // Try to stop the running task
        workerFuture.cancel(true);

        // Wait for termination
        executor.awaitTermination(timeout, timeUnit);
        workerFuture = null;
    }
}
