package com.buridantrader

import com.buridantrader.exceptions.NoSuchPathException
import com.buridantrader.exceptions.ValueLimitException
import spock.lang.Specification

import java.time.Instant

/**
 * Test class for {@link CurrencyPriceViewer}.
 */
class CurrencyPriceViewerTest extends Specification {

    def symbolPriceViewer = Mock(SymbolPriceViewer)
    def tradingPathFinder = Mock(TradingPathFinder)
    def currencyPriceViewer = new CurrencyPriceViewer(symbolPriceViewer, tradingPathFinder)

    def "get price history per minute"() {
        given:
        def baseCurrency = new Currency("BTC")
        def middleCurrency1 = new Currency("USDT")
        def quoteCurrency = new Currency("ETH")
        def startTime = Instant.ofEpochMilli(100)
        def endTime = Instant.ofEpochMilli(101)
        def symbol1 = new Symbol(baseCurrency, middleCurrency1)
        def symbol2 = new Symbol(quoteCurrency, middleCurrency1)
        def orderSpecs = [
            new OrderSpec(symbol1, OrderSide.SELL),
            new OrderSpec(symbol2, OrderSide.BUY),
        ]

        when:
        def result = currencyPriceViewer.getPriceHistoryPerMinute(baseCurrency, quoteCurrency, startTime, endTime)

        then:
        1 * tradingPathFinder.findPathOfOrderSpecs(baseCurrency, quoteCurrency) >> orderSpecs
        1 * symbolPriceViewer.getPriceHistoryPerMinute(symbol1, startTime, endTime) >> [
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(199),
                        new BigDecimal("100.01")),
                new Candlestick(
                        Instant.ofEpochMilli(200),
                        Instant.ofEpochMilli(299),
                        new BigDecimal("200.01")),
                new Candlestick(
                        Instant.ofEpochMilli(300),
                        Instant.ofEpochMilli(399),
                        new BigDecimal("300.01"))
        ]
        1 * symbolPriceViewer.getPriceHistoryPerMinute(symbol2, startTime, endTime) >> [
                new Candlestick(
                        Instant.ofEpochMilli(101),
                        Instant.ofEpochMilli(200),
                        new BigDecimal("400.01")),
                new Candlestick(
                        Instant.ofEpochMilli(201),
                        Instant.ofEpochMilli(300),
                        new BigDecimal("500.01"))
        ]
        result == [
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(199),
                        new BigDecimal("0.25001879938")),
                new Candlestick(
                        Instant.ofEpochMilli(200),
                        Instant.ofEpochMilli(299),
                        new BigDecimal("0.40001199960"))
        ]
    }

    def "when getting price history per minute, get 0 order spec"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("ETH")
        def startTime = Instant.ofEpochMilli(100)
        def endTime = Instant.ofEpochMilli(101)

        when:
        currencyPriceViewer.getPriceHistoryPerMinute(baseCurrency, quoteCurrency, startTime, endTime)

        then:
        thrown(IOException)
        1 * tradingPathFinder.findPathOfOrderSpecs(baseCurrency, quoteCurrency) >> []
    }

    def "when getting price history per minute, unable to get order spec"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("ETH")
        def startTime = Instant.ofEpochMilli(100)
        def endTime = Instant.ofEpochMilli(101)

        when:
        currencyPriceViewer.getPriceHistoryPerMinute(baseCurrency, quoteCurrency, startTime, endTime)

        then:
        thrown(NoSuchPathException)
        1 * tradingPathFinder.findPathOfOrderSpecs(baseCurrency, quoteCurrency) >> {throw new NoSuchPathException("")}
    }

    def "when getting price history per minute, price is 0"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("ETH")
        def startTime = Instant.ofEpochMilli(100)
        def endTime = Instant.ofEpochMilli(101)
        def symbol1 = new Symbol(quoteCurrency, baseCurrency)
        def orderSpecs = [
                new OrderSpec(symbol1, OrderSide.BUY),
        ]

        when:
        currencyPriceViewer.getPriceHistoryPerMinute(baseCurrency, quoteCurrency, startTime, endTime)

        then:
        thrown(ValueLimitException)
        1 * tradingPathFinder.findPathOfOrderSpecs(baseCurrency, quoteCurrency) >> orderSpecs
        1 * symbolPriceViewer.getPriceHistoryPerMinute(symbol1, startTime, endTime) >> [
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(199),
                        new BigDecimal("0"))
        ]
    }
}


