package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;

@ThreadSafe
public class StandardDeviationCalculator {

    private final MathContext mathContext;
    private final MeanCalculator meanCalculator;

    public StandardDeviationCalculator(@Nonnull MathContext mathContext) {
        this(mathContext, new MeanCalculator(mathContext));
    }

    public StandardDeviationCalculator(
            @Nonnull MathContext mathContext,
            @Nonnull MeanCalculator meanCalculator) {
        this.mathContext = mathContext;
        this.meanCalculator = meanCalculator;
    }

    @Nonnull
    public BigDecimal calSquaredStandardDeviation(@Nonnull List<BigDecimal> values) {
        BigDecimal mean = meanCalculator.calMean(values);
        BigDecimal meanOfSquares = meanCalculator.calMean(values.stream()
            .map((v) -> v.pow(2)).collect(Collectors.toList()));
        return meanOfSquares.subtract(mean.pow(2), mathContext);
    }
}
