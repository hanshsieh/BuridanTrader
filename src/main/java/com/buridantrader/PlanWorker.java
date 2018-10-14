package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.account.NewOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

public class PlanWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanWorker.class);

    private static final long FAILURE_DELAY_MS = 3000;

    private final TradingPlanner planner;

    private final BinanceApiRestClient client;

    private final System system;

    public PlanWorker(
            @Nonnull TradingPlanner planner,
            @Nonnull BinanceApiRestClient client) {
        this(planner, client, new System());
    }

    public PlanWorker(
            @Nonnull TradingPlanner planner,
            @Nonnull BinanceApiRestClient client,
            @Nonnull System system) {
        this.planner = planner;
        this.client = client;
        this.system = system;
    }

    @Override
    public void run() {
        Optional<TradingPlan> optPlan;
        while ((optPlan = getNextPlan()).isPresent()) {
            try {
                executePlan(optPlan.get());
            } catch (Exception ex) {
                planner.markLastPlanAsFailed();
                LOGGER.error("Fail to execute the plan, skipping it and sleep a while", ex);
                sleep();
            }
        }
    }

    @Nonnull
    private Optional<TradingPlan> getNextPlan() {
        while (!system.currentThread().isInterrupted()) {
            try {
                return Optional.of(planner.nextPlan());
            } catch (Exception ex) {
                LOGGER.error("Fail to get next plan. Sleep a while and retry", ex);
                sleep();
            }
        }
        return Optional.empty();
    }

    private void executePlan(@Nonnull TradingPlan plan) throws IOException {
        for (Order order : plan.getOrders()) {
            executeOrder(order);
        }
    }

    private void executeOrder(@Nonnull Order order) throws IOException {
        OrderSpec orderSpec = order.getOrderSpec();
        Symbol symbol = orderSpec.getSymbol();
        com.binance.api.client.domain.OrderSide orderSide = createOrderSide(orderSpec.getOrderSide());
        String quantity = order.getQuantity().toPlainString();
        NewOrder newOrder = new NewOrder(
            symbol.getName(),
            orderSide,
            OrderType.MARKET,
            null,
            quantity
        );
        try {
            LOGGER.info("Placing an order. symbol={}, orderSide={}, quantity={}", symbol, orderSide, quantity);
            client.newOrderTest(newOrder);
        } catch (Exception ex) {
            throw new IOException("Fail to execute order", ex);
        }
    }

    @Nonnull
    private com.binance.api.client.domain.OrderSide createOrderSide(@Nonnull OrderSide orderSide) {
        switch (orderSide) {
            case BUY:
                return com.binance.api.client.domain.OrderSide.BUY;
            default:
                return com.binance.api.client.domain.OrderSide.SELL;
        }
    }

    private void sleep() {
        try {
            system.sleep(FAILURE_DELAY_MS);
        } catch (InterruptedException ex) {

            // Recover the interrupted state
            system.currentThread().interrupt();
        }
    }
}
