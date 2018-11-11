package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

public class PredictionCalculator {

    // TODO  Make it configurable
    private final BigDecimal MAX_LONG_TERM_VOLATILITY = new BigDecimal("0.02");
    private final BigDecimal MAX_SHORT_LONG_TERM_VOLATILITY = new BigDecimal("0.01");
    private final LinearRegressionFinder linearRegressionFinder;

    public PredictionCalculator(@Nonnull MathContext mathContext) {
        this(new LinearRegressionFinder(mathContext));
    }

    public PredictionCalculator(
            @Nonnull LinearRegressionFinder linearRegressionFinder) {
        this.linearRegressionFinder = linearRegressionFinder;
    }

    @Nonnull
    public PricePrediction calPrediction(@Nonnull List<Point> points) {
        RegressionLine longTermRegressionLine = linearRegressionFinder.findLinearRegression(points);
        BigDecimal longTermVolatility = longTermRegressionLine.getVolatilityForPoints(points);
        int numPoints = points.size();
        List<Point> recentPoints = points.subList(numPoints / 2, numPoints);
        RegressionLine shortTermRegressionLine = linearRegressionFinder.findLinearRegression(recentPoints);
        BigDecimal shortTermVolatility = shortTermRegressionLine.getVolatilityForPoints(recentPoints);
        boolean profitable = true;
        if (longTermRegressionLine.getSlope().compareTo(BigDecimal.ZERO) < 0
                || longTermVolatility.compareTo(MAX_LONG_TERM_VOLATILITY) > 0
                || shortTermRegressionLine.getSlope().compareTo(BigDecimal.ZERO) < 0
                || shortTermVolatility.compareTo(MAX_SHORT_LONG_TERM_VOLATILITY) > 0) {
            profitable = false;
        }
        return new PricePrediction(profitable, shortTermRegressionLine.getSlope());
    }
}
