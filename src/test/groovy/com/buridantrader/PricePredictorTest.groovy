package com.buridantrader

import spock.lang.Specification

import java.time.Instant

class PricePredictorTest extends Specification {

    def currencyPriceViewer = Mock(CurrencyPriceViewer)
    def predicationCalculator = Mock(PredictionCalculator)
    def system = Mock(SystemService)
    def pricePredictor = new PricePredictor(currencyPriceViewer, predicationCalculator, system)

    def "Get prediction with same currency"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("BTC")

        when:
        def result = pricePredictor.getPrediction(baseCurrency, quoteCurrency)

        then:
        result == new PricePrediction(true, BigDecimal.ZERO)
    }

    def "Get prediction"() {
        given:
        def baseCurrency = new Currency("BTC")
        def quoteCurrency = new Currency("ETH")
        def candlesticks = [
            new Candlestick(
                    Instant.ofEpochSecond(999996400),
                    Instant.ofEpochSecond(999996459),
                    new BigDecimal("100.01")
            ),
            new Candlestick(
                    Instant.ofEpochSecond(999996460),
                    Instant.ofEpochSecond(999996519),
                    new BigDecimal("110.02")
            ),
            new Candlestick(
                    Instant.ofEpochSecond(999996520),
                    Instant.ofEpochSecond(999996579),
                    new BigDecimal("113.03")
            ),
            new Candlestick(
                    Instant.ofEpochSecond(999996580),
                    Instant.ofEpochSecond(999996639),
                    new BigDecimal("117.04")
            ),
        ]
        def pricePrediction = Mock(PricePrediction)

        when:
        def result = pricePredictor.getPrediction(baseCurrency, quoteCurrency)

        then:
        1 * system.currentTimeMillis() >> 1000000000000L
        1 * currencyPriceViewer.getPriceHistoryPerMinute(
                baseCurrency,
                quoteCurrency,
                Instant.ofEpochMilli(1000000000000L - 1000 * 60 * 60),
                Instant.ofEpochMilli(1000000000000L)
        ) >> candlesticks

        1 * predicationCalculator.calPrediction({ List<Point> points ->
            assert points.size() == 4
            assert points[0].x == new BigDecimal("999996429.5")
            assert points[0].y == new BigDecimal("100.01")
            assert points[1].x == new BigDecimal("999996489.5")
            assert points[1].y == new BigDecimal("110.02")
            assert points[2].x == new BigDecimal("999996549.5")
            assert points[2].y == new BigDecimal("113.03")
            assert points[3].x == new BigDecimal("999996609.5")
            assert points[3].y == new BigDecimal("117.04")
            return true
        } as List<Point>) >> pricePrediction

        result.is(pricePrediction)
    }
}
