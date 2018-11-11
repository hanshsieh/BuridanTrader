package com.buridantrader;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Immutable
public class LinearRegressionFinder {

    private final MeanCalculator meanCalculator;
    private final MathContext mathContext;

    public LinearRegressionFinder(
        @Nonnull MathContext mathContext) {
        this(mathContext, new MeanCalculator(mathContext));
    }

    public LinearRegressionFinder(
        @Nonnull MathContext mathContext,
        @Nonnull MeanCalculator meanCalculator) {
        this.mathContext = mathContext;
        this.meanCalculator = meanCalculator;
    }

    /**
     * Find the simple regression line for the given points.
     * Unless the line is vertical, the line will have the coefficient of y being 1.
     *
     * @param points Points.
     * @return Line.
     * @throws ArithmeticException Fail to calculate the line.
     * @throws IllegalArgumentException The linear regression line is vertical.
     */
    @Nonnull
    public RegressionLine findLinearRegression(@Nonnull List<Point> points)
        throws ArithmeticException, IllegalArgumentException {
        BigDecimal xMean = meanCalculator.calMean(points.stream().map(Point::getX).collect(Collectors.toList()));
        BigDecimal yMean = meanCalculator.calMean(points.stream().map(Point::getY).collect(Collectors.toList()));
        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        for (Point point : points) {
            BigDecimal xDiff = point.getX().subtract(xMean, mathContext);
            BigDecimal yDiff = point.getY().subtract(yMean, mathContext);
            numerator = numerator.add(xDiff.multiply(yDiff, mathContext), mathContext);
            denominator = denominator.add(xDiff.pow(2, mathContext));
        }
        if (denominator.signum() == 0) {
            // Vertical line
            throw new IllegalArgumentException("The linear regression line of the points is vertical");
        }
        BigDecimal slope = numerator.divide(denominator, mathContext);
        BigDecimal intercept = yMean.subtract(slope.multiply(xMean, mathContext), mathContext);
        // y = (slope) x + (intercept)
        // -> -(slope) x + y = (intercept)
        return new RegressionLine(slope, intercept, mathContext);
    }

}
