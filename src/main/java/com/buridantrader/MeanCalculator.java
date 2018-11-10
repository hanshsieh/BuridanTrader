package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

@ThreadSafe
public class MeanCalculator {
    private MathContext mathContext;

    public MeanCalculator(@Nonnull MathContext mathContext) {
        this.mathContext = mathContext;
    }

    @Nonnull
    public BigDecimal calMean(@Nonnull List<BigDecimal> values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            sum = sum.add(value);
        }
        return sum.divide(new BigDecimal(values.size()), mathContext);
    }
}
