package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;

@ThreadSafe
public class VolatilityCalculator {
    private final MathContext mathContext;
    private final StandardDeviationCalculator standardDeviationCalculator;
    private final MeanCalculator meanCalculator;

    public VolatilityCalculator(@Nonnull MathContext mathContext) {
        this(mathContext,
                new StandardDeviationCalculator(mathContext),
                new MeanCalculator(mathContext));
    }

    public VolatilityCalculator(
            @Nonnull MathContext mathContext,
            @Nonnull StandardDeviationCalculator standardDeviationCalculator,
            @Nonnull MeanCalculator meanCalculator) {
        this.mathContext = mathContext;
        this.standardDeviationCalculator = standardDeviationCalculator;
        this.meanCalculator = meanCalculator;
    }

    @Nonnull
    public BigDecimal calVolatility(@Nonnull RegressionLine regressionLine, @Nonnull List<Point> points) {
        List<BigDecimal> residuals = points.stream()
                .map((point) -> {
                    BigDecimal yOnLine = regressionLine.getYForX(point.getX());
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
