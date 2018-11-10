package com.buridantrader;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * This class represents a line on a 2-dimensional plane.
 */
public class RegressionLine {
    private final BigDecimal slope;
    private final BigDecimal interceptOfY;
    private final MathContext mathContext;
    private final StandardDeviationCalculator standardDeviationCalculator;
    private final MeanCalculator meanCalculator;

   /**
     * Constructs a regression line.
     * The line is represented by the formula: y = ax + b
     *
     * @param slope Slope of the line; that is, "a".
     * @param interceptOfY Constant; that is "b".
     */
    public RegressionLine(
        @Nonnull BigDecimal slope,
        @Nonnull BigDecimal interceptOfY,
        @Nonnull MathContext mathContext) {
        this(slope,
                interceptOfY,
                mathContext,
                new StandardDeviationCalculator(mathContext),
                new MeanCalculator(mathContext));
    }

    /**
     * Constructs a regression line.
     * The line is represented by the formula: y = ax + b
     *
     * @param slope Slope of the line; that is, "a".
     * @param interceptOfY Constant; that is "b".
     */
    public RegressionLine(
            @Nonnull BigDecimal slope,
            @Nonnull BigDecimal interceptOfY,
            @Nonnull MathContext mathContext,
            @Nonnull StandardDeviationCalculator standardDeviationCalculator,
            @Nonnull MeanCalculator meanCalculator) {
        this.slope = slope;
        this.interceptOfY = interceptOfY;
        this.mathContext = mathContext;
        this.standardDeviationCalculator = standardDeviationCalculator;
        this.meanCalculator = meanCalculator;
    }

    @Nonnull
    public BigDecimal getInterceptOfY() {
        return interceptOfY;
    }

    /**
     * Gets slope of the line.
     *
     * @return Slope.
     * @throws ArithmeticException The line if vertical or fail to calculate the slope.
     */
    @Nonnull
    public BigDecimal getSlope() throws ArithmeticException {
        return slope;
    }

    /**
     * Gets the value of y for the given x.
     *
     * @param xValue      X value.
     * @return Value of Y.
     * @throws ArithmeticException The line is vertical or fail to calculate the value.
     */
    @Nonnull
    public BigDecimal getYForX(@Nonnull BigDecimal xValue) {
        return xValue.multiply(slope).add(interceptOfY, mathContext);
    }

    @Nonnull
    public BigDecimal getVolatilityForPoints(@Nonnull List<Point> points) throws ArithmeticException {
        List<BigDecimal> residuals = points.stream()
                .map((point) -> {
                    BigDecimal yOnLine = getYForX(point.getX());
                    return point.getY().subtract(yOnLine);
                })
                .collect(Collectors.toList());
        BigDecimal squaredStdDev = standardDeviationCalculator.calSquaredStandardDeviation(residuals);
        BigDecimal meanOfY = meanCalculator.calMean(points
                .stream()
                .map(Point::getY)
                .collect(Collectors.toList()));
        if (meanOfY.signum() == 0) {
            return BigDecimal.ONE;
        } else {
            return squaredStdDev.divide(meanOfY, mathContext);
        }
    }

}
