package com.buridantrader

import spock.lang.Specification
import spock.lang.Unroll

import java.math.MathContext
import java.math.RoundingMode

/**
 * Test class of {@link RegressionLine}.
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

    def "Get Y for the given X"() {
        given:
        line = new RegressionLine(
                new BigDecimal("1.26"),
                new BigDecimal("2.3571"),
                new MathContext(5, RoundingMode.HALF_UP))

        when:
        def y = line.getYForX(new BigDecimal("4.568"))

        then:
        y == new BigDecimal("8.1128")
    }

    def "Get volatility for points"() {
        given:
        line = new RegressionLine(
                new BigDecimal("1.26"),
                new BigDecimal("2.3571"),
                new MathContext(6, RoundingMode.HALF_UP))

        when:
        // https://www.desmos.com/calculator/2drliyh10t
        def result = line.getVolatilityForPoints([
            new Point(new BigDecimal("10.1"), new BigDecimal("15.083")),
            new Point(new BigDecimal("0"), new BigDecimal("4")),
            new Point(new BigDecimal("4.12"), new BigDecimal("6.10")),
            new Point(new BigDecimal("8.97"), new BigDecimal("10.16")),
        ])

        then:
        // residuals:
        // 15.083 - 15.0831 = -0.0001
        // 4 - 2.3571 = 1.6429
        // 6.10 - 7.5483 = -1.4483
        // 10.16 - 13.6593 = -3.4993

        // https://www.calculator.net/standard-deviation-calculator.html?numberinputs=0%2C1.643%2C-1.448%2C-3.499&x=60&y=28
        // variance: 3.57784

        // mean of points on Y
        // (15.0831 + 2.3571 + 7.5483 + 13.6593) / 4 = 9.66195

        // volatility = 3.57784 / 9.66195 = 0.3703020612
        result == new BigDecimal("0.370302")
    }

    def "Get volatility for points with mean of points on Y being 0"() {
        given:
        line = new RegressionLine(
                new BigDecimal("3.27"),
                new BigDecimal("0"),
                new MathContext(6, RoundingMode.HALF_UP))

        when:
        // https://www.desmos.com/calculator/ulaxkcjht8
        line.getVolatilityForPoints([
                new Point(new BigDecimal("-10"), new BigDecimal("-21")),
                new Point(new BigDecimal("5"), new BigDecimal("15")),
                new Point(new BigDecimal("5"), new BigDecimal("16"))
        ])

        then:
        thrown(ArithmeticException)
    }

}
