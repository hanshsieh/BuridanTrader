package com.buridantrader;

import com.buridantrader.exceptions.ValueException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ThreadSafe
@Immutable
public class DecimalFormalizer {
    private final BigDecimal maxValue;
    private final BigDecimal minValue;
    private final BigDecimal stepSize;

    public DecimalFormalizer(
            @Nonnull BigDecimal minValue,
            @Nonnull BigDecimal maxValue,
            @Nonnull BigDecimal stepSize
    ) {
        if (maxValue.compareTo(minValue) < 0) {
            throw new IllegalArgumentException(
                    "Max value must be greater than or equal to min value");
        }
        if (stepSize.signum() <= 0) {
            throw new IllegalArgumentException("Step size must be positive");
        }
        this.minValue = minValue.stripTrailingZeros();
        this.maxValue = maxValue.stripTrailingZeros();
        this.stepSize = stepSize.stripTrailingZeros();
    }

    /**
     * Formalize the given value.
     *
     * @param oldValue Old value.
     * @param roundingMode Rouding mode.
     * @return Formalized value.
     *
     * @throws ValueException The value is out of min or max value.
     */
    @Nonnull
    public BigDecimal formalize(
            @Nonnull BigDecimal oldValue,
            @Nonnull RoundingMode roundingMode) throws ValueException {
        BigDecimal numSteps = oldValue.subtract(minValue).
                divide(stepSize, 0, roundingMode);
        BigDecimal newValue = numSteps.multiply(stepSize).add(minValue);
        if (newValue.compareTo(minValue) < 0) {
            throw new ValueException(
                    "Value " + newValue + " is smaller than the minimum value " + minValue);
        }
        if (newValue.compareTo(maxValue) > 0) {
            throw new ValueException(
                    "Value " + newValue + " is smaller than the minimum value " + minValue);
        }
        return newValue.stripTrailingZeros();
    }
}
