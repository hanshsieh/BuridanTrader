package com.buridantrader

import spock.lang.Specification

import java.math.RoundingMode

class DecimalFormalizerTest extends Specification {

    def "formalization with value in range"() {
        given:
        def formalizer = new DecimalFormalizer(
                new BigDecimal("-10.0"),
                new BigDecimal("101.0"),
                new BigDecimal("2.0")
        )

        when:
        def ret = formalizer.formalize(
                new BigDecimal(input),
                rounding)

        then:
        ret == new BigDecimal(output)

        where:
        input   | output | rounding
        "0.0"   | "0"    | RoundingMode.DOWN
        "1.0"   | "0"    | RoundingMode.DOWN
        "3.1"   | "2"    | RoundingMode.DOWN
        "3.1"   | "4"    | RoundingMode.UP
        "2.1"   | "2"    | RoundingMode.DOWN
        "2.1"   | "4"    | RoundingMode.UP
        "-1.1"  | "-2"   | RoundingMode.DOWN
        "-3.1"  | "-2"   | RoundingMode.UP
        "-10"   | "-10"  | RoundingMode.DOWN
        "101.0" | "100"  | RoundingMode.DOWN
        "-10.1" | "-10"  | RoundingMode.DOWN
        "-11.1" | "-10"  | RoundingMode.DOWN
    }

    def "formalization with value out of range"() {
        given:
        def formalizer = new DecimalFormalizer(
                new BigDecimal("-10.0"),
                new BigDecimal("101.0"),
                new BigDecimal("2.0")
        )

        when:
        formalizer.formalize(
                new BigDecimal(input),
                rounding)

        then:
        thrown(IllegalArgumentException)

        where:
        input   | rounding
        "-10.1" | RoundingMode.UP
        "-12"   | RoundingMode.DOWN
        "102.0" | RoundingMode.DOWN
        "101.0" | RoundingMode.UP
    }

    def "Valid constructor arguments"() {
        when:
        new DecimalFormalizer(
                new BigDecimal(min),
                new BigDecimal(max),
                new BigDecimal(step)
        )

        then:
        noExceptionThrown()

        where:
        min     | max   | step
        "10"    | "20"  | "1.0"
        "10"    | "10"  | "1.0"
    }

    def "Invalid constructor arguments"() {
        when:
        new DecimalFormalizer(
                new BigDecimal(min),
                new BigDecimal(max),
                new BigDecimal(step)
        )

        then:
        thrown(IllegalArgumentException)

        where:
        min     | max   | step
        "10"    | "9.9" | "1.0"
        "10"    | "20"  | "-1.0"
        "10"    | "20"  | "0"
    }
}
