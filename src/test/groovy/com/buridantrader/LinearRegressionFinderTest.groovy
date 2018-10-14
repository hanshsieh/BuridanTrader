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
        calculator = new LinearRegressionFinder()
        def points = [
            new Point(new BigDecimal(x1), new BigDecimal(y1)),
            new Point(new BigDecimal(x2), new BigDecimal(y2)),
            new Point(new BigDecimal(x3), new BigDecimal(y3)),
        ]

        when:
        def line = calculator.findLinearRegression(points, new MathContext(5, RoundingMode.HALF_UP))

        then:
        line.getCoefficientOfX().stripTrailingZeros() == new BigDecimal(exA)
        line.getCoefficientOfY().stripTrailingZeros() == new BigDecimal(exB)
        line.getConstant().stripTrailingZeros() == new BigDecimal(exC)

        where:
        x1      | y1    | x2    | y2    | x3    | y3    || exA          | exB   | exC
        "1.0"   | "1.0" | "2.0" | "2.0" | "3.0" | "3.0" || "-1"         | "1"   | "0"
        "1.0"   | "1.0" | "1.0" | "2.0" | "1.0" | "3.0" || "1"          | "0"   | "1"
        "1.0"   | "1.0" | "2.0" | "1.0" | "3.0" | "1.0" || "0"          | "1"   | "1"
        "0"     | "0"   | "1"   | "3"   | "4"   | "1"   || "-0.038459"  | "1"   | "1.2692"
    }
}
