package com.buridantrader

import spock.lang.Specification

import java.time.Instant

class SymbolServiceTest extends Specification {

    def symbolFetcher = Mock(SymbolFetcher)
    def system = Mock(System)
    SymbolService symbolService = new SymbolService(symbolFetcher, system)

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
        def updated = symbolService.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        updated

        when:
        def symbolInfo = symbolService.getSymbolInfoByName("BTCUSDT")

        then:
        1 * system.currentTimeMillis() >> 1000
        1 * symbolFetcher.getSymbolInfos() >> symbolInfosV1
        symbolInfo.get().symbol.name == "BTCUSDT"

        when:
        updated = symbolService.isUpdatedSince(Instant.ofEpochMilli(999))

        then:
        updated

        when:
        updated = symbolService.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        !updated

        when:
        // Not yet passing expiration time, should not refresh
        symbolInfo = symbolService.getSymbolInfoByName("ETHBTC")

        then:
        1 * system.currentTimeMillis() >> 601000
        0 * symbolFetcher.getSymbolInfos()
        symbolInfo.get().symbol.name == "ETHBTC"

        when:
        updated = symbolService.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        !updated

        when:
        // Passing expiration time, should refresh
        symbolInfo = symbolService.getSymbolInfoByName("ETHUSDT")

        then:
        2 * system.currentTimeMillis() >>> [601001, 601011]
        1 * symbolFetcher.getSymbolInfos() >> symbolInfosV1
        !symbolInfo.isPresent()

        when:
        // Though refreshed, not updated
        updated = symbolService.isUpdatedSince(Instant.ofEpochMilli(1000))

        then:
        !updated

        when:
        symbolInfo = symbolService.getSymbolInfoByName("ETHUSDT")

        then:
        2 * system.currentTimeMillis() >>> [1201012, 1201022]
        1 * symbolFetcher.getSymbolInfos() >> symbolInfosV2
        symbolInfo.get().symbol.name == "ETHUSDT"

        when:
        // Refreshed and updated
        updated = symbolService.isUpdatedSince(Instant.ofEpochMilli(1000))

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
        def result = symbolService.getAllSymbolInfos()

        then:
        1 * system.currentTimeMillis() >> 1000
        1 * symbolFetcher.getSymbolInfos() >> symbolInfos
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
        def symbolInfo = symbolService.getSymbolInfo(
                new Symbol(new Currency("BTC"), new Currency("USDT")))

        then:
        1 * system.currentTimeMillis() >> 1000
        1 * symbolFetcher.getSymbolInfos() >> symbolInfos
        symbolInfo.get().symbol.name == "BTCUSDT"
    }
}
