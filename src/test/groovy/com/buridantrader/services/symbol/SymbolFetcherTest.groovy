package com.buridantrader.services.symbol

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.general.ExchangeInfo
import com.binance.api.client.domain.general.FilterType
import com.binance.api.client.domain.general.SymbolFilter
import com.binance.api.client.domain.general.SymbolStatus
import com.buridantrader.Symbol
import com.buridantrader.Currency
import com.buridantrader.SymbolInfo
import spock.lang.Specification

/**
 * Test class for {@link SymbolFetcher}..
 */
class SymbolFetcherTest extends Specification {

    def client = Mock(BinanceApiRestClient)
    SymbolFetcher fetcher

    def setup() {
        fetcher = new SymbolFetcher(client)
    }

    def "get symbol info"() {
        given:
        def exchangeInfo = new ExchangeInfo()
        def symbolInfos = [
            new com.binance.api.client.domain.general.SymbolInfo(
                    symbol: "BTCUSDT",
                    status: SymbolStatus.TRADING,
                    baseAsset: "BTC",
                    quoteAsset: "USDT",
                    filters: [
                       new SymbolFilter(
                           filterType: FilterType.LOT_SIZE,
                           minQty: "0.001",
                           maxQty: "100000.123",
                           stepSize: "0.002"
                       ),
                       new SymbolFilter(
                           filterType: FilterType.PRICE_FILTER,
                           minPrice: "0.002",
                           maxPrice: "100001.123",
                           tickSize: "0.003"
                       ),
                    ]),
            new com.binance.api.client.domain.general.SymbolInfo(
                    symbol: "ETHBTC",
                    status: SymbolStatus.HALT,
                    baseAsset: "ETH",
                    quoteAsset: "BTC",
                    filters: [
                        new SymbolFilter(
                            filterType: FilterType.LOT_SIZE,
                            minQty: "0.002",
                            maxQty: "100000.124",
                            stepSize: "0.003"
                        )
                    ]),
            new com.binance.api.client.domain.general.SymbolInfo(
                    symbol: "CNDBTC",
                    status: SymbolStatus.TRADING,
                    baseAsset: "CND",
                    quoteAsset: "BTC",
                    filters: [])
        ]
        exchangeInfo.setSymbols(symbolInfos)

        when:
        def result = fetcher.getSymbolInfos()

        then:
        1 * client.getExchangeInfo() >> exchangeInfo
        result.size() == 1
        result.get(0).with { SymbolInfo it ->
            assert it.symbol == new Symbol(new Currency("BTC"), new Currency("USDT"))
            assert it.minQuantity == new BigDecimal("0.001")
            assert it.maxQuantity == new BigDecimal("100000.123")
            assert it.quantityStepSize == new BigDecimal("0.002")
            assert it.minPrice == new BigDecimal("0.002")
            assert it.maxPrice == new BigDecimal("100001.123")
            assert it.priceTickSize == new BigDecimal("0.003")
            return true
        }
    }

    def "exception thrown when getting symbol info"() {
        when:
        fetcher.getSymbolInfos()

        then:
        thrown(IOException)
        client.getExchangeInfo() >> {throw new RuntimeException()}
    }
}
