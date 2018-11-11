package com.buridantrader

import spock.lang.Specification
import spock.lang.Unroll

import java.math.MathContext
import java.math.RoundingMode

/**
 * Test class for {@link PredictionCalculator}.
 */
class PredictionCalculatorTest extends Specification {

    def linearRegressionFinder = Mock(LinearRegressionFinder)
    PredictionCalculator predictionCalculator = new PredictionCalculator(linearRegressionFinder)

    def "Constructs a new instance"() {
        when:
        predictionCalculator = new PredictionCalculator(new MathContext(6, RoundingMode.HALF_UP))

        then:
        noExceptionThrown()
    }

    @Unroll
    def "Calculate prediction, and profitable = #profitable"() {
        given:
        def longTermRegressionLine = Mock(RegressionLine)
        def shortTermRegressionLine = Mock(RegressionLine)
        def longTermPoints = [
                new Point(new BigDecimal("1.23"), new BigDecimal("2.34")),
                new Point(new BigDecimal("2.23"), new BigDecimal("4.34")),
                new Point(new BigDecimal("3.23"), new BigDecimal("5.34")),
        ]
        def shortTermPoints = [
                new Point(new BigDecimal("2.23"), new BigDecimal("4.34")),
                new Point(new BigDecimal("3.23"), new BigDecimal("5.34")),
        ]


        when:
        def result = predictionCalculator.calPrediction(longTermPoints)

        then:
        1 * linearRegressionFinder.findLinearRegression(longTermPoints) >> longTermRegressionLine
        1 * linearRegressionFinder.findLinearRegression(shortTermPoints) >> shortTermRegressionLine
        (1.._) * longTermRegressionLine.getSlope() >> new BigDecimal(longTermSlope)
        (1.._) * shortTermRegressionLine.getSlope() >> new BigDecimal(shortTermSlope)
        1 * longTermRegressionLine.getVolatilityForPoints(longTermPoints) >> new BigDecimal(longTermVolatility)
        1 * shortTermRegressionLine.getVolatilityForPoints(shortTermPoints) >> new BigDecimal(shortTermVolatility)
        result.profitable == profitable
        result.growthPerSec == new BigDecimal(shortTermSlope)

        where:
        longTermSlope   | shortTermSlope    | longTermVolatility | shortTermVolatility | profitable
        "1.0"           | "1.5"             | "0.001"            | "0.001"             | true
        "0.0"           | "0.0"             | "0.001"            | "0.001"             | true
        "-0.1"          | "0.0"             | "0.001"            | "0.001"             | false
        "0.0"           | "-0.1"            | "0.001"            | "0.001"             | false
        "1.0"           | "1.5"             | "0.02"             | "0.01"              | true
        "1.0"           | "1.5"             | "0.021"            | "0.01"              | false
        "1.0"           | "1.5"             | "0.02"             | "0.011"             | false
    }
}
