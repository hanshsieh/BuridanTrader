package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.account.NewOrder;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlanConsumeWorkerTest {

    @Injectable
    private TradingPlanner planner;

    @Injectable
    private BinanceApiRestClient client;

    private PlanConsumeWorker worker;

    @BeforeMethod
    public void setup() {
        worker = new PlanConsumeWorker(planner, client);
    }

    @Test
    public void testRun(@Mocked Thread unused) throws Exception {
        // Given
        TradingPlan plan = new TradingPlan();
        OrderSpec orderSpec1 = new OrderSpec(
                new Symbol(new Currency("BTC"), new Currency("USDT")),
                OrderSide.BUY);
        Order order1 = new Order(orderSpec1, new BigDecimal("100.1"));
        plan.addOrder(order1);

        OrderSpec orderSpec2 = new OrderSpec(
                new Symbol(new Currency("BTC"), new Currency("ETH")),
                OrderSide.SELL);
        Order order2 = new Order(orderSpec2, new BigDecimal("90.2"));
        plan.addOrder(order2);

        new Expectations() {{
            //currentThread.isInterrupted();
            //returns(false, true);

            planner.nextPlan();
            result = plan;

            client.newOrder(withInstanceOf(NewOrder.class));
            times = 2;
        }};

        // When
        worker.run();

        // Then
        new Verifications() {{
            List<NewOrder> newOrders = new ArrayList<>();
            client.newOrder(withCapture(newOrders));
            Assert.assertEquals(newOrders.size(), 2);
            NewOrder newOrder1 = newOrders.get(0);
            NewOrder newOrder2 = newOrders.get(1);
            Assert.assertEquals(newOrder1.getSymbol(), "BTCUSDT");
            Assert.assertEquals(newOrder1.getSide(), com.binance.api.client.domain.OrderSide.BUY);
            Assert.assertEquals(newOrder1.getType(), OrderType.MARKET);
            Assert.assertNull(newOrder1.getTimeInForce());
            Assert.assertNull(newOrder1.getQuantity(), "100.1");

            Assert.assertEquals(newOrder2.getSymbol(), "BTCETH");
            Assert.assertEquals(newOrder2.getSide(), com.binance.api.client.domain.OrderSide.SELL);
            Assert.assertEquals(newOrder2.getType(), OrderType.MARKET);
            Assert.assertNull(newOrder2.getTimeInForce());
            Assert.assertNull(newOrder2.getQuantity(), "90.2");

        }};
    }
}
