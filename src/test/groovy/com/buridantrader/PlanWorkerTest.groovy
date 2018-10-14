package com.buridantrader

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.OrderType
import com.binance.api.client.domain.account.NewOrder
import spock.lang.Specification

class PlanWorkerTest extends Specification {

    def tradingPlanner = Mock(TradingPlanner)
    def client = Mock(BinanceApiRestClient)
    def system = Mock(System)
    PlanWorker planWorker

    def setup() {
        planWorker = new PlanWorker(tradingPlanner, client, system)
    }

    def "run the worker and plans are executed correctly"() {
        given:
        def plan = new TradingPlan()
        def orderSpec1 = new OrderSpec(
                new Symbol(new Currency("BTC"), new Currency("USDT")),
                OrderSide.BUY)
        def order1 = new Order(orderSpec1, new BigDecimal("100.1"))
        plan.addOrder(order1)

        def orderSpec2 = new OrderSpec(
                new Symbol(new Currency("BTC"), new Currency("ETH")),
                OrderSide.SELL)
        def order2 = new Order(orderSpec2, new BigDecimal("90.2"))
        plan.addOrder(order2)

        def currentThread = Mock(Thread)

        when:
        planWorker.run()

        then:
        (1.._) * system.currentThread() >> currentThread
        2 * currentThread.isInterrupted() >>> [false, true]
        1 * tradingPlanner.nextPlan() >> plan
        1 * client.newOrder({ NewOrder it ->
                    it.symbol == "BTCUSDT" &&
                    it.side == com.binance.api.client.domain.OrderSide.BUY &&
                    it.type == OrderType.MARKET &&
                    it.timeInForce == null &&
                    it.quantity == "100.1"
        } as NewOrder)
        1 * client.newOrder({ NewOrder it ->
            it.symbol == "BTCETH" &&
                    it.side == com.binance.api.client.domain.OrderSide.SELL &&
                    it.type == OrderType.MARKET &&
                    it.timeInForce == null &&
                    it.quantity == "90.2"
        } as NewOrder)
        0 * _._
    }

    def "exception thrown when creating order"() {
        given:
        def plan = new TradingPlan()
        def orderSpec1 = new OrderSpec(
                new Symbol(new Currency("BTC"), new Currency("USDT")),
                OrderSide.BUY)
        def order1 = new Order(orderSpec1, new BigDecimal("100.1"))
        plan.addOrder(order1)

        def orderSpec2 = new OrderSpec(
                new Symbol(new Currency("BTC"), new Currency("ETH")),
                OrderSide.SELL)
        def order2 = new Order(orderSpec2, new BigDecimal("90.2"))
        plan.addOrder(order2)

        def currentThread = Mock(Thread)

        when:
        planWorker.run()

        then:
        (1.._) * system.currentThread() >> currentThread
        2 * currentThread.isInterrupted() >>> [false, true]
        1 * tradingPlanner.nextPlan() >> plan
        1 * client.newOrder(_ as NewOrder) >> {throw new RuntimeException()}
        1 * system.sleep(3000)
        1 * tradingPlanner.markLastPlanAsFailed()
        0 * _._
    }

    def "exception thrown when getting plan"() {
        given:
        def currentThread = Mock(Thread)

        when:
        planWorker.run()

        then:
        (1.._) * system.currentThread() >> currentThread
        2 * currentThread.isInterrupted() >>> [false, true]
        1 * tradingPlanner.nextPlan() >> {throw new IOException()}
        1 * system.sleep(3000)
        0 * _._
    }
}
