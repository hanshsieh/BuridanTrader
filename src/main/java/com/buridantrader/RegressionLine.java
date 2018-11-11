package com.buridantrader;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
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
    private final VarianceCalculator varianceCalculator;
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
                new VarianceCalculator(mathContext),
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
            @Nonnull VarianceCalculator varianceCalculator,
            @Nonnull MeanCalculator meanCalculator) {
        this.slope = slope;
        this.interceptOfY = interceptOfY;
        this.mathContext = mathContext;
        this.varianceCalculator = varianceCalculator;
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

    /**
     * Gets the volatility for points for this line.
     * The volatility is defined as below:
     * Let ð to be the variance of the difference between
     * the given points and the points on the line for a same X.
     * Let Θ be the mean of the Y values on this line for the X values of the points.
     * The volatility is calculated as ð / Θ.
     * If Θ = 0, {@link ArithmeticException} is thrown.
     *
     * @param points Points.
     * @return Volatility.
     * @throws ArithmeticException Arithmetic error occurs. E.g. Θ = 0.
     */
    @Nonnull
    public BigDecimal getVolatilityForPoints(@Nonnull List<Point> points) throws ArithmeticException {
        List<BigDecimal> ysOnLine = new ArrayList<>();
        List<BigDecimal> residuals = points.stream()
                .map((point) -> {
                    BigDecimal yOnLine = getYForX(point.getX());
                    ysOnLine.add(yOnLine);
                    return point.getY().subtract(yOnLine);
                })
                .collect(Collectors.toList());
        BigDecimal variance = varianceCalculator.calVariance(residuals);
        BigDecimal meanOfYOnLine = meanCalculator.calMean(ysOnLine);
        return variance.divide(meanOfYOnLine, mathContext);
    }

}
