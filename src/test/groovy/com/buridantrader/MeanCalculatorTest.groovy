package com.buridantrader

import spock.lang.Specification

import java.math.MathContext

/**
 * Test class for {@link MeanCalculator}.
 */
class MeanCalculatorTest extends Specification {
    MeanCalculator meanCalculator

    def "Calculate mean"() {
        given:
        meanCalculator = new MeanCalculator(new MathContext(6))

        when:
        def result = meanCalculator.calMean([
            new BigDecimal("1.12"),
            new BigDecimal("2.34"),
            new BigDecimal("3.45"),
        ])

        then:
        result == new BigDecimal("2.30333")
    }

    def "Calculate mean with 0 values"() {
        given:
        meanCalculator = new MeanCalculator(new MathContext(6))

        when:
        def result = meanCalculator.calMean([])

        then:
        result == new BigDecimal("0")
    }
}
