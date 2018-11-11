package com.buridantrader

import com.buridantrader.exceptions.ValueException
import spock.lang.Specification

import java.math.RoundingMode
import java.time.Instant

class TradingPathFinderTest extends Specification {

    def symbolProvider = Mock(SymbolService)
    def symbolPriceProvider = Mock(SymbolPriceService)
    def shortestPathsResolver = Mock(ShortestPathsResolver)
    def system = Mock(SystemService)
    def tradingPathFinder = new TradingPathFinder(symbolProvider, symbolPriceProvider, shortestPathsResolver, system)

    def "constructor"() {
        when:
        new TradingPathFinder(symbolProvider, symbolPriceProvider)

        then:
        noExceptionThrown()
    }

    def "find path of order specs"() {
        given:
        def sourceCurrency = new Currency("ETH")
        def middleCurrency = new Currency("BTC")
        def targetCurrency = new Currency("USDT")
        def symbol1 = new Symbol(middleCurrency, targetCurrency);
        def symbol2 = new Symbol(middleCurrency, sourceCurrency)
        def symbolInfos = [
                Mock(SymbolInfo) {
                    getSymbol() >> symbol1
                    0 * _
                },
                Mock(SymbolInfo) {
                    getSymbol() >> symbol2
                    0 * _
                }
        ]
        def tradingPaths = Mock(TradingPaths)
        def pathStep1 = new PathStep(symbol2, 2)
        def pathStep2 = new PathStep(symbol1, 1)

        when:
        def path = tradingPathFinder.findPathOfOrderSpecs(sourceCurrency, targetCurrency)

        then:
        1 * symbolProvider.getAllSymbolInfos() >> symbolInfos
        1 * shortestPathsResolver.resolveAllShortestPaths([symbol1, symbol2]) >> tradingPaths
        1 * system.currentTimeMillis() >> 1536931586123
        1 * tradingPaths.getNextStep(sourceCurrency, targetCurrency) >> Optional.of(pathStep1)
        1 * tradingPaths.getNextStep(middleCurrency, targetCurrency) >> Optional.of(pathStep2)
        path.get() == [
            new OrderSpec(symbol2, OrderSide.BUY),
            new OrderSpec(symbol1, OrderSide.SELL)
        ]

        when:
        path = tradingPathFinder.findPathOfOrderSpecs(sourceCurrency, targetCurrency)

        then:
        symbolProvider.isUpdatedSince(Instant.ofEpochMilli(1536931586123)) >> true
        1 * symbolProvider.getAllSymbolInfos() >> []
        1 * shortestPathsResolver.resolveAllShortestPaths([]) >> tradingPaths
        1 * system.currentTimeMillis() >> 1536931586124
        1 * tradingPaths.getNextStep(sourceCurrency, targetCurrency) >> Optional.empty()
        !path.isPresent()

        when:
        tradingPathFinder.findPathOfOrderSpecs(sourceCurrency, targetCurrency)

        then:
        symbolProvider.isUpdatedSince(Instant.ofEpochMilli(1536931586124)) >> false
        0 * shortestPathsResolver.resolveAllShortestPaths([] as Collection)
        0 * symbolProvider.getAllSymbolInfos()
        1 * tradingPaths.getNextStep(sourceCurrency, targetCurrency) >> Optional.empty()
        !path.isPresent()
    }

    def "find path of orders"() {
        given:
        def sourceCurrency = new Currency("ETH")
        def middleCurrency1 = new Currency("BTC")
        def middleCurrency2 = new Currency("MNT")
        def targetCurrency = new Currency("USDT")
        def symbol1 = new Symbol(middleCurrency1, sourceCurrency)
        def symbol2 = new Symbol(middleCurrency1, middleCurrency2)
        def symbol3 = new Symbol(targetCurrency, middleCurrency2)
        def symbolInfo1 = Mock(SymbolInfo)
        def symbolInfo2 = Mock(SymbolInfo)
        def symbolInfo3 = Mock(SymbolInfo)
        def quantityFormalizer1 = Mock(DecimalFormalizer)
        def quantityFormalizer2 = Mock(DecimalFormalizer)
        def quantityFormalizer3 = Mock(DecimalFormalizer)
        def priceFormalizer1 = Mock(DecimalFormalizer)
        def priceFormalizer2 = Mock(DecimalFormalizer)
        def priceFormalizer3 = Mock(DecimalFormalizer)
        def symbolInfos = [
            symbolInfo1,
            symbolInfo2,
            symbolInfo3,
        ]
        def tradingPaths = Mock(TradingPaths)
        def pathStep1 = new PathStep(symbol1, 3)
        def pathStep2 = new PathStep(symbol2, 2)
        def pathStep3 = new PathStep(symbol3, 1)

        when:
        def path = tradingPathFinder.findPathOfOrders(sourceCurrency, targetCurrency, new BigDecimal("100.0"))

        then:
        (1 .. _) * symbolInfo1.getSymbol() >> symbol1
        (1 .. _) * symbolInfo2.getSymbol() >> symbol2
        (1 .. _) * symbolInfo3.getSymbol() >> symbol3
        (1 .. _) * symbolInfo1.getPriceFormalizer() >> priceFormalizer1
        (1 .. _) * symbolInfo2.getPriceFormalizer() >> priceFormalizer2
        (1 .. _) * symbolInfo3.getPriceFormalizer() >> priceFormalizer3
        (1 .. _) * symbolInfo1.getQuantityStepSize() >> new BigDecimal("0.0002")
        0 * symbolInfo2.getQuantityStepSize()
        (1 .. _) * symbolInfo3.getQuantityStepSize() >> new BigDecimal("0.003")
        (1 .. _) * symbolInfo1.getQuantityFormalizer() >> quantityFormalizer1
        (1 .. _) * symbolInfo2.getQuantityFormalizer() >> quantityFormalizer2
        (1 .. _) * symbolInfo3.getQuantityFormalizer() >> quantityFormalizer3
        1 * symbolProvider.getAllSymbolInfos() >> symbolInfos
        1 * shortestPathsResolver.resolveAllShortestPaths([symbol1, symbol2, symbol3]) >> tradingPaths
        1 * system.currentTimeMillis() >> 1536931586123
        1 * tradingPaths.getNextStep(sourceCurrency, targetCurrency) >> Optional.of(pathStep1)
        1 * tradingPaths.getNextStep(middleCurrency1, targetCurrency) >> Optional.of(pathStep2)
        1 * tradingPaths.getNextStep(middleCurrency2, targetCurrency) >> Optional.of(pathStep3)
        1 * symbolProvider.getSymbolInfo(symbol1) >> Optional.of(symbolInfos[0])
        1 * symbolProvider.getSymbolInfo(symbol2) >> Optional.of(symbolInfos[1])
        1 * symbolProvider.getSymbolInfo(symbol3) >> Optional.of(symbolInfos[2])
        1 * symbolPriceProvider.getPrice(symbol1) >> Optional.of(new BigDecimal("2.15"))
        1 * symbolPriceProvider.getPrice(symbol2) >> Optional.of(new BigDecimal("3.1415927"))
        1 * symbolPriceProvider.getPrice(symbol3) >> Optional.of(new BigDecimal("0.0122"))
        1 * quantityFormalizer1.formalize(new BigDecimal("46.2962"), RoundingMode.DOWN) >> new BigDecimal("46.2961")
        1 * quantityFormalizer2.formalize(new BigDecimal("46.2961"), RoundingMode.DOWN) >> new BigDecimal("46.2960")
        1 * quantityFormalizer3.formalize(new BigDecimal("11824.648"), RoundingMode.DOWN) >> new BigDecimal("11824.647")
        1 * priceFormalizer1.formalize(new BigDecimal("2.15"), RoundingMode.UP) >> new BigDecimal("2.16")
        1 * priceFormalizer2.formalize(new BigDecimal("3.1415927"), RoundingMode.DOWN) >> new BigDecimal("3.1415926")
        1 * priceFormalizer3.formalize(new BigDecimal("0.0122"), RoundingMode.UP) >> new BigDecimal("0.0123")

        path.get() == [
                // Source quantity: 100.0
                // Order quantity: 100.0 / 2.16 = 46.2962962... -> use scale 4 -> 46.2962 -> formalize -> 46.2961
                new Order(new OrderSpec(symbol1, OrderSide.BUY), new BigDecimal("46.2961")),
                // Source quantity: 46.2961
                // Order quantity: 46.2961 -> formalize -> 46.2960
                new Order(new OrderSpec(symbol2, OrderSide.SELL), new BigDecimal("46.2960")),
                // Source quantity: 46.2960 * 3.1415926 = 145.4431710096
                // Order quantity: 145.4431710096 / 0.0123 = 11824.64804... -> use scale 3 -> 11824.648 -> formalize -> 11824.647
                new Order(new OrderSpec(symbol3, OrderSide.BUY), new BigDecimal("11824.647")),
        ]
    }

    def "when finding path of orders, fail to find path for order specs"() {
        given:
        def sourceCurrency = new Currency("ETH")
        def targetCurrency = new Currency("USDT")
        def symbol1 = new Symbol(sourceCurrency, targetCurrency)
        def symbolInfo1 = Mock(SymbolInfo)
        def symbolInfos = [
           symbolInfo1
        ]
        def tradingPaths = Mock(TradingPaths)

        when:
        def path = tradingPathFinder.findPathOfOrders(sourceCurrency, targetCurrency, new BigDecimal("100.0"))

        then:
        (1 .. _) * symbolInfo1.getSymbol() >> symbol1
        1 * symbolProvider.getAllSymbolInfos() >> symbolInfos
        1 * shortestPathsResolver.resolveAllShortestPaths([symbol1]) >> tradingPaths
        1 * system.currentTimeMillis() >> 1536931586123
        1 * tradingPaths.getNextStep(sourceCurrency, targetCurrency) >> Optional.empty()
        !path.isPresent()
    }

    def "when finding path of orders, fail to formalize the order quantity"() {
        given:
        def sourceCurrency = new Currency("ETH")
        def targetCurrency = new Currency("USDT")
        def symbol1 = new Symbol(sourceCurrency, targetCurrency)
        def symbolInfo1 = Mock(SymbolInfo)
        def quantityFormalizer1 = Mock(DecimalFormalizer)
        def symbolInfos = [
                symbolInfo1
        ]
        def tradingPaths = Mock(TradingPaths)
        def pathStep1 = new PathStep(symbol1, 3)

        when:
        tradingPathFinder.findPathOfOrders(sourceCurrency, targetCurrency, new BigDecimal("100.0"))

        then:
        def ex = thrown(ValueException)
        ex.reason == ValueException.Reason.TOO_SMALL
        (1 .. _) * symbolInfo1.getSymbol() >> symbol1
        (1 .. _) * symbolInfo1.getQuantityFormalizer() >> quantityFormalizer1
        1 * symbolProvider.getAllSymbolInfos() >> symbolInfos
        1 * shortestPathsResolver.resolveAllShortestPaths([symbol1]) >> tradingPaths
        1 * system.currentTimeMillis() >> 1536931586123
        1 * tradingPaths.getNextStep(sourceCurrency, targetCurrency) >> Optional.of(pathStep1)
        1 * symbolProvider.getSymbolInfo(symbol1) >> Optional.of(symbolInfos[0])
        1 * symbolPriceProvider.getPrice(symbol1) >> Optional.of(new BigDecimal("2.16"))
        1 * quantityFormalizer1.formalize(new BigDecimal("100.0"), RoundingMode.DOWN) >> {
            throw new ValueException(ValueException.Reason.TOO_SMALL, "")
        }
    }

}
