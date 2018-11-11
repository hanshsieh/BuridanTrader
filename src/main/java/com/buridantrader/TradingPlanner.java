package com.buridantrader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Semaphore;

@ThreadSafe
public class TradingPlanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradingPlanner.class);
    // TODO Make it configurable
    private static final long SUCCESS_COOL_DOWN_MS = 1000 * 60 * 10;
    private static final long FAILURE_COOL_DOWN_MS = 1000 * 10;
    private final PlanProducer planProducer;
    private final SystemService system;
    private Semaphore semaphore = new Semaphore(1);
    private Instant lastPlanTime = null;
    private long nextCooldownMs = SUCCESS_COOL_DOWN_MS;

    public TradingPlanner(@Nonnull PlanProducer planProducer) {
        this(planProducer, new SystemService());
    }

    public TradingPlanner(
            @Nonnull PlanProducer planProducer,
            @Nonnull SystemService system) {
        this.planProducer = planProducer;
        this.system = system;
    }

    @Nonnull
    public TradingPlan nextPlan() throws IOException, InterruptedException {
        semaphore.acquire();
        try {
            waitUntilReadyToMakePlan();
            TradingPlan plan = planProducer.get();
            lastPlanTime = Instant.ofEpochMilli(system.currentTimeMillis());
            nextCooldownMs = SUCCESS_COOL_DOWN_MS;
            return plan;
        } finally {
            semaphore.release();
        }
    }

    public void markLastPlanAsFailed() {
        nextCooldownMs = FAILURE_COOL_DOWN_MS;
    }

    private void waitUntilReadyToMakePlan() throws InterruptedException {
        long remainingMs = remainingMsToMakePlan();
        if (remainingMs > 0) {
            LOGGER.info("Waiting for {} ms for next plan", remainingMs);
            system.sleep(remainingMs);
        }
    }

    @Nonnegative
    private long remainingMsToMakePlan() {
        if (lastPlanTime == null) {
            return 0;
        }
        return Math.max(lastPlanTime.toEpochMilli() + nextCooldownMs - system.currentTimeMillis(), 0);
    }
}
