package com.buridantrader

import spock.lang.Specification
import spock.lang.Unroll

import java.math.MathContext
import java.math.RoundingMode

/**
 * Test class of {@link Line}.
 */
class RegressionLineTest extends Specification {

    RegressionLine line

    @Unroll
    def "Get slope and intercept of Y"() {
        given:
        line = new RegressionLine(
                new BigDecimal("1.23"),
                new BigDecimal("2.34"),
                new MathContext(5, RoundingMode.HALF_UP))

        when:
        def slope = line.getSlope()
        def intercept = line.getInterceptOfY()

        then:
        slope == new BigDecimal("1.23")
        intercept == new BigDecimal("2.34")
    }

}
