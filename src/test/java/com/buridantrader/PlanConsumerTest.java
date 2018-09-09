package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import mockit.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlanConsumerTest {
    @Injectable
    private TradingPlanner tradingPlanner;

    @Injectable
    private BinanceApiRestClient client;

    @Injectable
    private ExecutorService executor;

    private PlanConsumer planConsumer;

    @BeforeMethod
    public void setup() {
        new Expectations(Executors.class) {{
            Executors.newSingleThreadExecutor();
            times = 1;
            result = executor;
        }};

        planConsumer = new PlanConsumer(tradingPlanner, client);
    }

    @Test
    public void testStartAndStop(
            @Mocked PlanConsumeWorker worker,
            @Injectable Future future) throws Exception {
        new Expectations() {{
            new PlanConsumeWorker(tradingPlanner, client);
            times = 1;
            result = worker;

            executor.submit(worker);
            result = future;
        }};

        // When
        planConsumer.start();
        planConsumer.stop(10, TimeUnit.SECONDS);

        new VerificationsInOrder() {{
            executor.submit(worker);
            times = 1;

            executor.shutdownNow();
            times = 1;

            future.cancel(true);
            times = 1;

            executor.awaitTermination(10, TimeUnit.SECONDS);
            times = 1;
        }};
    }
}
