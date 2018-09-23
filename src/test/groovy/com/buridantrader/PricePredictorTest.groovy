package com.buridantrader

import spock.lang.Specification

import java.time.Instant

class PricePredictorTest extends Specification {

    def currencyPriceViewer = Mock(CurrencyPriceViewer)
    def system = Mock(System)
    def pricePredictor = new PricePredictor(currencyPriceViewer, system)

    def "Get prediction with same currency"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("BTC")

        when:
        def result = pricePredictor.getPrediction(baseCurrency, quoteCurrency)

        then:
        result == new PricePrediction(BigDecimal.ZERO)
    }

    def "Get prediction"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("ETH")
        def candlesticks = [
            new Candlestick(
                    Instant.ofEpochMilli(1537694953123),
                    Instant.ofEpochMilli(1537695013122),
                    new BigDecimal("100.01")
            ),
            new Candlestick(
                    Instant.ofEpochMilli(1537695013123),
                    Instant.ofEpochMilli(1537695073122),
                    new BigDecimal("110.02")
            ),
            new Candlestick(
                    Instant.ofEpochMilli(1537695073123),
                    Instant.ofEpochMilli(1537695133122),
                    new BigDecimal("113.03")
            ),
            new Candlestick(
                    Instant.ofEpochMilli(1537695133123),
                    Instant.ofEpochMilli(1537695193122),
                    new BigDecimal("117.04")
            ),
        ]

        when:
        def result = pricePredictor.getPrediction(baseCurrency, quoteCurrency)

        then:
        1 * system.currentTimeMillis() >> 1537698553123
        1 * currencyPriceViewer.getPriceHistoryPerMinute(
                baseCurrency,
                quoteCurrency,
                Instant.ofEpochMilli(1537698553123 - 1000 * 60 * 60),
                Instant.ofEpochMilli(1537698553123)
        ) >> candlesticks

        // ((110.02 - 100.01) / 60 * 0.949660^2 + (113.03 - 110.02) / 60 * 0.949660^1 + (117.04 - 113.03) / 60 * 0.949660^0) / (0.949660^0 + 0.949660^1 + 0.949660^2)
        // = (10.01 / 60 * 0.9018541156 + 3.01 / 60 * 0.949660 + 4.01 / 60 * 1) / (1 + 0.949660 + 0.9018541156)
        // = (0.1668333333 * 0.901854115600 + 0.050166666667 * 0.949660 + 0.0668333333 * 1) / 2.8515141156
        // = 0.2649339382228547494800 / 2.851514115600
        // = 0.09290991645624295975342006124
        result == new PricePrediction(new BigDecimal("0.09290991645624295975342006124"))
    }

    def "Get prediction with only one candlestick"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("ETH")
        def candlesticks = [
                new Candlestick(
                        Instant.ofEpochMilli(1537694953123),
                        Instant.ofEpochMilli(1537695013122),
                        new BigDecimal("100.01")
                )
        ]

        when:
        pricePredictor.getPrediction(baseCurrency, quoteCurrency)

        then:
        thrown(IOException)
        1 * system.currentTimeMillis() >> 1537698553123
        1 * currencyPriceViewer.getPriceHistoryPerMinute(
                baseCurrency,
                quoteCurrency,
                Instant.ofEpochMilli(1537698553123 - 1000 * 60 * 60),
                Instant.ofEpochMilli(1537698553123)
        ) >> candlesticks
    }
}
