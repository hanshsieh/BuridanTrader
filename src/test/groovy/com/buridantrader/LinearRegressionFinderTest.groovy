package com.buridantrader

import spock.lang.Specification
import spock.lang.Unroll

import java.math.MathContext
import java.math.RoundingMode

/**
 * Test class for {@link LinearRegressionFinder}.
 */
class LinearRegressionFinderTest extends Specification {

    LinearRegressionFinder calculator

    @Unroll
    def "Find linear regression of points (#x1, #y1), (#x2, #y2), (#x3, #y3)"() {
        given:
        calculator = new LinearRegressionFinder(new MathContext(5, RoundingMode.HALF_UP))
        def points = [
            new Point(new BigDecimal(x1), new BigDecimal(y1)),
            new Point(new BigDecimal(x2), new BigDecimal(y2)),
            new Point(new BigDecimal(x3), new BigDecimal(y3)),
        ]

        when:
        def line = calculator.findLinearRegression(points)

        then:
        line.getSlope().stripTrailingZeros() == new BigDecimal(exSlope)
        line.getInterceptOfY().stripTrailingZeros() == new BigDecimal(exIntercept)

        where:
        x1      | y1    | x2    | y2    | x3    | y3    || exSlope      | exIntercept
        "1.0"   | "1.0" | "2.0" | "2.0" | "3.0" | "3.0" || "1"          | "0"
        "1.0"   | "1.0" | "2.0" | "1.0" | "3.0" | "1.0" || "0"          | "1"
        // meanOfX = (0+1+4)/3 = 1.6667
        // meanOfY = (0+3+1)/3 = 1.3333
        // slope = ((0-1.6667)*(0-1.3333)+(1-1.6667)*(3-1.3333)+(4-1.6667)*(1-1.3333))/((0-1.6667)^2+(1-1.6667)^2+(4-1.6667)^2)
        // = 0.33331/(2.7779+0.44449+5.4443)
        // = 0.038459
        // intercept = 1.3333-0.038459*1.6667 = 1.26920
        "0"     | "0"   | "1"   | "3"   | "4"   | "1"   || "0.038459"  | "1.2692"
    }

    @Unroll
    def "Find linear regression of vertical points (#x1, #y1), (#x2, #y2), (#x3, #y3) isn't allowed"() {
        given:
        calculator = new LinearRegressionFinder(new MathContext(5, RoundingMode.HALF_UP))
        def points = [
                new Point(new BigDecimal(x1), new BigDecimal(y1)),
                new Point(new BigDecimal(x2), new BigDecimal(y2)),
                new Point(new BigDecimal(x3), new BigDecimal(y3)),
        ]

        when:
        calculator.findLinearRegression(points)

        then:
        thrown(IllegalArgumentException)

        where:
        x1      | y1    | x2    | y2    | x3    | y3
       "1.0"    | "1.0" | "1.0" | "2.0" | "1.0" | "3.0"
        "0"     | "0"   | "0"   | "0"   | "0"   | "0"
    }
}
