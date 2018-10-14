package com.buridantrader

import spock.lang.Specification
import spock.lang.Unroll

import java.math.MathContext
import java.math.RoundingMode

/**
 * Test class of {@link Line}.
 */
class LineTest extends Specification {

    Line line

    @Unroll
    def "Get slope for #a x + #b = #c"() {
        given:
        line = new Line(new BigDecimal(a), new BigDecimal(b), new BigDecimal(c))

        when:
        def slope = line.getSlope(new MathContext(5, roundingMode))

        then:
        slope.stripTrailingZeros() == new BigDecimal(result)

        where:
        a       | b         | c         | roundingMode          || result
        "1.0"   | "1.0"     | "0.0"     | RoundingMode.HALF_UP  || "-1"
        "1.0"   | "1.0"     | "5.0"     | RoundingMode.HALF_UP  || "-1"
        "3.0"   | "2.0"     | "5.0"     | RoundingMode.HALF_UP  || "-1.5"
        "0.0"   | "2.0"     | "3.0"     | RoundingMode.HALF_UP  || "0"
        "2.0"   | "-3.0"    | "3.0"     | RoundingMode.HALF_UP  || "0.66667"
        "2.0"   | "-3.0"    | "3.0"     | RoundingMode.FLOOR    || "0.66666"
        "-2.0"  | "-3.0"    | "-10.0"   | RoundingMode.HALF_UP  || "-0.66667"
        "-2.0"  | "-3.0"    | "-10.0"   | RoundingMode.FLOOR    || "-0.66667"
    }

    @Unroll
    def "Get slope for #a x + #b = #c, but the slope doesn't exist"() {
        given:
        line = new Line(new BigDecimal(a), new BigDecimal(b), new BigDecimal(c))

        when:
        line.getSlope(new MathContext(5, RoundingMode.HALF_UP))

        then:
        thrown(ArithmeticException)

        where:
        a       | b         | c
        "3.0"   | "0"       | "4.0"
        "0"     | "0"       | "4.0"
    }

    @Unroll
    def "Gets vertical distance of line #a x + #b y = #c from point (#x, #y)"() {
        given:
        def point = new Point(new BigDecimal(x), new BigDecimal(y))
        line = new Line(new BigDecimal(a), new BigDecimal(b), new BigDecimal(c))

        when:
        def distance = line.getVerticalDistanceFrom(point, new MathContext(5, RoundingMode.HALF_UP))

        then:
        distance.stripTrailingZeros() == new BigDecimal(exDistance)

        where:
        a       | b         | c         | x         | y         || exDistance
        "2.0"   | "3.0"     | "4.0"     | "5"       | "-1"      || "-1"
        "2.0"   | "3.0"     | "4.0"     | "5"       | "-2"      || "0"
        "0"     | "3.0"     | "4.0"     | "5"       | "-2"      || "3.3333"
        "0"     | "3.0"     | "6.0"     | "5"       | "1000000" || "-1000000"
    }
}
