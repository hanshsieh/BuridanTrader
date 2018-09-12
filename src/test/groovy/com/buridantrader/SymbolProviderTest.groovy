package com.buridantrader

import spock.lang.Specification

import java.time.Instant

class SymbolProviderTest extends Specification {

    def symbolService = Mock(SymbolService)
    def system = Mock(System)
    SymbolProvider symbolProvider = new SymbolProvider(symbolService, system)

    def "get symbol by name"() {
        given:
        def symbolInfosV1 = [
            new SymbolInfo(
                    new Symbol(new Currency("BTC"), new Currency("USDT")),
                    new BigDecimal("0.012"),
                    new BigDecimal("100000.0"),
                    new BigDecimal("0.01")),
            new SymbolInfo(
                    new Symbol(new Currency("ETH"), new Currency("BTC")),
                    new BigDecimal("0.013"),
                    new BigDecimal("200000.0"),
                    new BigDecimal("0.02"))
        ]
        def symbolInfosV2 = [
                new SymbolInfo(
                        new Symbol(new Currency("BTC"), new Currency("USDT")),
                        new BigDecimal("0.012"),
                        new BigDecimal("100000.0"),
                        new BigDecimal("0.01")),
                new SymbolInfo(
                        new Symbol(new Currency("ETH"), new Currency("BTC")),
                        new BigDecimal("0.013"),
                        new BigDecimal("200000.0"),
                        new BigDecimal("0.02")),
                new SymbolInfo(
                        new Symbol(new Currency("ETH"), new Currency("USDT")),
                        new BigDecimal("0.014"),
                        new BigDecimal("200000.1"),
                        new BigDecimal("0.03"))
        ]

        when:
        // Initially, always say "updated"
        def updated = symbolProvider.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        updated

        when:
        def symbolInfo = symbolProvider.getSymbolInfoByName("BTCUSDT")

        then:
        1 * system.currentTimeMillis() >> 1000
        1 * symbolService.getSymbolInfos() >> symbolInfosV1
        symbolInfo.get().symbol.name == "BTCUSDT"

        when:
        updated = symbolProvider.isUpdatedSince(Instant.ofEpochMilli(999))

        then:
        updated

        when:
        updated = symbolProvider.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        !updated

        when:
        // Not yet passing expiration time, should not refresh
        symbolInfo = symbolProvider.getSymbolInfoByName("ETHBTC")

        then:
        1 * system.currentTimeMillis() >> 601000
        0 * symbolService.getSymbolInfos()
        symbolInfo.get().symbol.name == "ETHBTC"

        when:
        updated = symbolProvider.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        !updated

        when:
        // Passing expiration time, should refresh
        symbolInfo = symbolProvider.getSymbolInfoByName("ETHUSDT")

        then:
        2 * system.currentTimeMillis() >>> [601001, 601011]
        1 * symbolService.getSymbolInfos() >> symbolInfosV1
        !symbolInfo.isPresent()

        when:
        // Though refreshed, not updated
        updated = symbolProvider.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        !updated

        when:
        symbolInfo = symbolProvider.getSymbolInfoByName("ETHUSDT")

        then:
        2 * system.currentTimeMillis() >>> [1201012, 1201022]
        1 * symbolService.getSymbolInfos() >> symbolInfosV2
        symbolInfo.get().symbol.name == "ETHUSDT"

        when:
        // Refreshed and updated
        updated = symbolProvider.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        updated
    }

    def "get all symbol info"() {
        given:
        def symbolInfos = [
                new SymbolInfo(
                        new Symbol(new Currency("BTC"), new Currency("USDT")),
                        new BigDecimal("0.012"),
                        new BigDecimal("100000.0"),
                        new BigDecimal("0.01")),
                new SymbolInfo(
                        new Symbol(new Currency("ETH"), new Currency("BTC")),
                        new BigDecimal("0.013"),
                        new BigDecimal("200000.0"),
                        new BigDecimal("0.02"))
        ]

        when:
        def result = symbolProvider.getAllSymbolInfos()

        then:
        1 * system.currentTimeMillis() >> 1000
        1 * symbolService.getSymbolInfos() >> symbolInfos
        result == symbolInfos
    }

    def "get symbol info"() {
        given:
        def symbolInfos = [
                new SymbolInfo(
                        new Symbol(new Currency("BTC"), new Currency("USDT")),
                        new BigDecimal("0.012"),
                        new BigDecimal("100000.0"),
                        new BigDecimal("0.01")),
                new SymbolInfo(
                        new Symbol(new Currency("ETH"), new Currency("BTC")),
                        new BigDecimal("0.013"),
                        new BigDecimal("200000.0"),
                        new BigDecimal("0.02"))
        ]

        when:
        def symbolInfo = symbolProvider.getSymbolInfo(
                new Symbol(new Currency("BTC"), new Currency("USDT")))

        then:
        1 * system.currentTimeMillis() >> 1000
        1 * symbolService.getSymbolInfos() >> symbolInfos
        symbolInfo.get().symbol.name == "BTCUSDT"
    }
}
