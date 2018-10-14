package com.buridantrader;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class LinearRegressionFinder {

    /**
     * Find the simple regression line for the given points.
     * Unless the line is vertical, the line will have the coefficient of y being 1.
     *
     * @param points Points.
     * @param mathContext Math context used for the calculation.
     * @return Line.
     * @throws ArithmeticException Fail to calculate the line.
     */
    @Nonnull
    public Line findLinearRegression(@Nonnull List<Point> points, @Nonnull MathContext mathContext)
        throws ArithmeticException {
        BigDecimal xMean = mean(points.stream().map(Point::getX).collect(Collectors.toList()), mathContext);
        BigDecimal yMean = mean(points.stream().map(Point::getY).collect(Collectors.toList()), mathContext);
        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        for (Point point : points) {
            BigDecimal xDiff = point.getX().subtract(xMean, mathContext);
            BigDecimal yDiff = point.getY().subtract(yMean, mathContext);
            numerator = numerator.add(xDiff.multiply(yDiff, mathContext), mathContext);
            denominator = denominator.add(xDiff.pow(2, mathContext));
        }
        if (denominator.signum() == 0) {
            // x = (mean of x)
            return new Line(BigDecimal.ONE, BigDecimal.ZERO, xMean);
        }
        BigDecimal slope = numerator.divide(denominator, mathContext);
        BigDecimal intercept = yMean.subtract(slope.multiply(xMean, mathContext), mathContext);
        // y = (slope) x + (intercept)
        // -> -(slope) x + y = (intercept)
        return new Line(slope.negate(mathContext), BigDecimal.ONE, intercept);
    }

    @Nonnull
    private BigDecimal mean(@Nonnull List<BigDecimal> values, @Nonnull MathContext mathContext) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            sum = sum.add(value, mathContext);
        }
        return sum.divide(new BigDecimal(values.size()), mathContext);
    }
}
