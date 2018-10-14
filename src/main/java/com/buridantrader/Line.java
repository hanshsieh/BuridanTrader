package com.buridantrader;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * This class represents a line on a 2-dimensional plane.
 */
public class Line {
    private final BigDecimal coefficientOfX;
    private final BigDecimal coefficientOfY;
    private final BigDecimal constant;

    /**
     * Construct a line with the given coefficients.
     * The line is represented by the formula: ax + by = c
     *
     * @param coefficientOfX Coefficient of x; that is "a".
     * @param coefficientOfY Coefficient of y; that is "b".
     * @param constant Constant; that is "c".
     */
    public Line(
        @Nonnull BigDecimal coefficientOfX,
        @Nonnull BigDecimal coefficientOfY,
        @Nonnull BigDecimal constant) {

        this.coefficientOfX = coefficientOfX;
        this.coefficientOfY = coefficientOfY;
        this.constant = constant;
    }

    @Nonnull
    public BigDecimal getCoefficientOfX() {
        return coefficientOfX;
    }

    @Nonnull
    public BigDecimal getCoefficientOfY() {
        return coefficientOfY;
    }

    @Nonnull
    public BigDecimal getConstant() {
        return constant;
    }

    /**
     * Gets slope of the line.
     *
     * @param mathContext Math context used for the calculation.
     * @return Slope.
     * @throws ArithmeticException The line if vertical or fail to calculate the slope.
     */
    @Nonnull
    public BigDecimal getSlope(@Nonnull MathContext mathContext) throws ArithmeticException {
        return coefficientOfX.negate(mathContext)
                               .divide(coefficientOfY, mathContext);
    }

    /**
     * Gets the value of y for the given x.
     *
     * @param xValue      X value.
     * @param mathContext Math context for the calculation.
     * @return Value of Y.
     * @throws ArithmeticException The line is vertical or fail to calculate the value.
     */
    @Nonnull
    public BigDecimal getYForX(@Nonnull BigDecimal xValue, @Nonnull MathContext mathContext) {
        BigDecimal xMulCoefficient = xValue.multiply(coefficientOfX, mathContext);
        return constant.subtract(xMulCoefficient, mathContext)
                               .divide(coefficientOfY, mathContext);
    }

    /**
     * Gets the vertical distance from the given point.
     * If the value of y for the {@code point.x} is y', then the value {@code y' - point.y} is returned.
     *
     * @param point       The point.
     * @param mathContext Math context used for the calculation.
     * @return Distance (may be negative)
     * @throws ArithmeticException The line is vertical or fail to calculate the value.
     */
    @Nonnull
    public BigDecimal getVerticalDistanceFrom(@Nonnull Point point, @Nonnull MathContext mathContext) throws ArithmeticException {
        BigDecimal y = getYForX(point.getX(), mathContext);
        return y.subtract(point.getY(), mathContext);
    }
}
