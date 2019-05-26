package com.buridantrader

import com.buridantrader.services.system.SystemService
import spock.lang.Specification

class ShortestPathsResolverTest extends Specification {

    def system = Mock(SystemService)
    def resolver = new ShortestPathsResolver(system)

    def "resolve all shortest paths"() {
        given:
        def symbols = [
            new Symbol(new Currency("BTC"), new Currency("USDT")),
            new Symbol(new Currency("ETH"), new Currency("BTC")),
            new Symbol(new Currency("ETH"), new Currency("EOS")),
            new Symbol(new Currency("BCC"), new Currency("EOS")),
            new Symbol(new Currency("ETC"), new Currency("BCC")),
            new Symbol(new Currency("USDT"), new Currency("ETC")),
            new Symbol(new Currency("USDT"), new Currency("BCC")),
            new Symbol(new Currency("ETC"), new Currency("NEO")),
            new Symbol(new Currency("ONT"), new Currency("ADA")),
        ]

        when:
        def result = resolver.resolveAllShortestPaths(symbols)
        def step1 = result.getNextStep(new Currency("BTC"), new Currency("USDT"))
        def step2 = result.getNextStep(new Currency("ETH"), new Currency("USDT"))
        def step3 = result.getNextStep(new Currency("USDT"), new Currency("ETH"))
        def step4 = result.getNextStep(new Currency("EOS"), new Currency("NEO"))
        def step5 = result.getNextStep(new Currency("BCC"), new Currency("NEO"))
        def step6 = result.getNextStep(new Currency("ETC"), new Currency("NEO"))
        def step7 = result.getNextStep(new Currency("NEO"), new Currency("NEO"))
        def step8 = result.getNextStep(new Currency("USDT"), new Currency("ONT"))
        def step9 = result.getNextStep(new Currency("USDT"), new Currency("NOTEXIST"))

        then:
        step1.get().length == 1
        step1.get().symbolToNext.name == "BTCUSDT"
        step2.get().length == 2
        step2.get().symbolToNext.name == "ETHBTC"
        step3.get().length == 2
        step3.get().symbolToNext.name == "BTCUSDT"
        step4.get().length == 3
        step4.get().symbolToNext.name == "BCCEOS"
        step5.get().length == 2
        step5.get().symbolToNext.name == "ETCBCC"
        step6.get().length == 1
        step6.get().symbolToNext.name == "ETCNEO"
        step7.get().length == 2
        step7.get().symbolToNext.name == "ETCNEO"
        !step8.isPresent()
        !step9.isPresent()
        2 * system.currentTimeMillis() >>> [100, 200]
    }

}
