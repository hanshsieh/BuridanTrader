package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

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

    @Nonnull
    public Optional<BigDecimal> formalize(
            @Nonnull BigDecimal oldValue,
            @Nonnull RoundingMode roundingMode) {
        BigDecimal numSteps = oldValue.subtract(minValue).
                divide(stepSize, 0, roundingMode);
        BigDecimal newValue = numSteps.multiply(stepSize).add(minValue);
        if (newValue.compareTo(maxValue) > 0 || newValue.compareTo(minValue) < 0) {
            return Optional.empty();
        }
        return Optional.of(newValue.stripTrailingZeros());
    }
}
