package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;

import javax.annotation.Nonnull;

public class PlanWorkerFactory {

    private final BinanceApiRestClient client;

    public PlanWorkerFactory(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    @Nonnull
    public PlanWorker createPlanWorker(@Nonnull TradingPlanner planner) {
        return new PlanWorker(planner, client);
    }
}
