package com.buridantrader

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.CandlestickInterval
import spock.lang.Specification

import java.time.Instant

class SymbolPriceViewerTest extends Specification {

    def client = Mock(BinanceApiRestClient)
    def symbolPriceViewer = new SymbolPriceViewer(client)

    def "get price history"() {
        given:
        def symbol = new Symbol(new Currency("BTC"), new Currency("USDT"))
        def startTime = Instant.ofEpochMilli(100)
        def endTime = Instant.ofEpochMilli(100 + 1000 * 60 * 3 + 10)
        def binanceCandlesticks = [
                new com.binance.api.client.domain.market.Candlestick(
                        high: "1000.01",
                        low: "100.01",
                        openTime: 2000,
                        closeTime: 2001
                ),
                new com.binance.api.client.domain.market.Candlestick(
                        high: "3000.01",
                        low: "300.01",
                        openTime: 4000,
                        closeTime: 4001
                ),
        ]

        when:
        def result = symbolPriceViewer.getPriceHistoryPerMinute(symbol, startTime, endTime)

        then:
        1 * client.getCandlestickBars(
                "BTCUSDT",
                CandlestickInterval.ONE_MINUTE,
                3,
                100,
                100 + 1000 * 60 * 3 + 10
        ) >> binanceCandlesticks
        result == [
            new Candlestick(
                    Instant.ofEpochMilli(2000),
                    Instant.ofEpochMilli(2001),
                    new BigDecimal("550.01")),
            new Candlestick(
                    Instant.ofEpochMilli(4000),
                    Instant.ofEpochMilli(4001),
                    new BigDecimal("1650.01"))
        ]
    }
}
