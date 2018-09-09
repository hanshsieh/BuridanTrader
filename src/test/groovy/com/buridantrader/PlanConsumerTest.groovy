package com.buridantrader

import com.binance.api.client.BinanceApiRestClient
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class PlanConsumerTest extends Specification {

    def executor = Mock(ExecutorService)
    def planner = Mock(TradingPlanner)
    def planWorkerFactory = Mock(PlanWorkerFactory)
    PlanConsumer planConsumer

    def setup() {
        planConsumer = new PlanConsumer(planner, planWorkerFactory, executor)
    }

    def "start and stop"() {
        given:
        def planWorker = Mock(PlanWorker)
        def future = Mock(Future)

        when:
        planConsumer.start()

        then:
        1 * planWorkerFactory.createPlanWorker(planner) >> planWorker
        1 * executor.submit(planWorker) >> future
        0 * _._

        when:
        planConsumer.start()

        then:
        thrown(IllegalStateException)

        when:
        planConsumer.stop(100, TimeUnit.SECONDS)

        then:
        1 * executor.shutdownNow()
        1 * future.cancel(true)
        1 * executor.awaitTermination(100, TimeUnit.SECONDS)
        0 * _._

        when:
        planConsumer.stop(100, TimeUnit.SECONDS)

        then:
        0 * _._
    }
}
