package com.buridantrader

import spock.lang.Specification

import java.math.MathContext
import java.math.RoundingMode

/**
 * Test class for {@link VarianceCalculator}.
 */
class VarianceCalculatorTest extends Specification {

    VarianceCalculator varianceCalculator

    def "Calculate variance"() {
        given:
        varianceCalculator = new VarianceCalculator(
                new MathContext(6, RoundingMode.HALF_UP))

        when:
        def result = varianceCalculator.calVariance(values)

        then:
        // https://www.calculator.net/standard-deviation-calculator.html?numberinputs=1.23%2C2.34%2C4.69%2C3.71%2C0.12&x=54&y=16
        result == expectedResult

        where:
        values << [[
                new BigDecimal("1.23"),
                new BigDecimal("2.34"),
                new BigDecimal("4.69"),
                new BigDecimal("3.71"),
                new BigDecimal("0.12")
        ], [
                new BigDecimal("3.1415"),
                new BigDecimal("3.1415"),
                new BigDecimal("3.1415")
        ]]

        expectedResult << [
                new BigDecimal("2.70590"),
                new BigDecimal("-0.00000225"),
        ]
    }
}
