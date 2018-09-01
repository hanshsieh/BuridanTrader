package com.buridantrader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class PlanConsumeWorker implements Runnable {

    private static final long FAILURE_DELAY_MS = 3000;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanConsumeWorker.class);

    private final TradingPlanner planner;

    public PlanConsumeWorker(@Nonnull TradingPlanner planner) {
        this.planner = planner;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TradingPlan plan = planner.nextPlan();
                // TODO
            } catch (Exception ex) {
                LOGGER.error("Fail to get next plan. Sleep a while and retry", ex);
                sleep();
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(FAILURE_DELAY_MS);
        } catch (InterruptedException ex) {

            // Recover the interrupted state
            Thread.currentThread().interrupt();
        }
    }
}
