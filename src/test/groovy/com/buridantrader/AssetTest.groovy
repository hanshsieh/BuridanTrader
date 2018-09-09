package com.buridantrader

import spock.lang.Specification

class AssetTest extends Specification {

    def "hash code and equals"() {
        given:
        def currency1 = new Currency("BTCUSDT")
        def balance1 = new BigDecimal("100.01")

        def currency2 = new Currency("BTCUSDT")
        def balance2 = new BigDecimal("100.01")

        def currency3 = new Currency("ETHUSDT")
        def balance3 = new BigDecimal("100.01")

        def currency4 = new Currency("BTCUSDT")
        def balance4 = new BigDecimal("100.010")

        when:
        def asset1 = new Asset(currency1, balance1)
        def asset2 = new Asset(currency2, balance2)
        def asset3 = new Asset(currency3, balance3)
        def asset4 = new Asset(currency4, balance4)

        then:
        asset1.hashCode() == asset2.hashCode()
        asset1.hashCode() != asset3.hashCode()
        asset1.hashCode() != asset4.hashCode()

        asset1.equals(asset1)
        asset1.equals(asset2)
        !asset1.equals(asset3)
        !asset1.equals(asset4)
        !asset1.equals(null)
        !asset1.equals(new Asset(currency1, balance1) {})
    }

    def "getters and setters"() {
        when:
        def currency = new Currency("BTCUSDT");
        def balance = new BigDecimal("100.01");
        def asset = new Asset(currency, balance);

        then:
        currency == asset.getCurrency()
        balance == asset.getBalance()
    }
}
