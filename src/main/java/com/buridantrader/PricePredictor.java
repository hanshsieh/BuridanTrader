package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class PricePredictor {

    // TODO Make it configurable
    private static final long PREDICTION_PERIOD_MS = 1000 * 60 * 60;
    private static final MathContext MATH_CONTEXT = new MathContext(20);

    private final CurrencyPriceViewer currencyPriceViewer;
    private final System system;
    private final PredictionCalculator predictionCalculator;

    public PricePredictor(@Nonnull CurrencyPriceViewer priceViewer) {
        this(priceViewer, new PredictionCalculator(MATH_CONTEXT), new System());
    }
    public PricePredictor(
            @Nonnull CurrencyPriceViewer currencyPriceViewer,
            @Nonnull PredictionCalculator predictionCalculator,
            @Nonnull System system) {
        this.currencyPriceViewer = currencyPriceViewer;
        this.predictionCalculator = predictionCalculator;
        this.system = system;
    }

    @Nonnull
    public PricePrediction getPrediction(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency) throws IOException, IllegalArgumentException {

        if (baseCurrency.equals(quoteCurrency)) {
            return new PricePrediction(true, BigDecimal.ZERO);
        }

        Instant endTime = Instant.ofEpochMilli(system.currentTimeMillis());
        Instant startTime = endTime.minusMillis(PREDICTION_PERIOD_MS);

        List<Candlestick> candlesticks = currencyPriceViewer.getPriceHistoryPerMinute(
                baseCurrency, quoteCurrency, startTime, endTime);

        List<Point> points = candlesticks.stream()
                .map((c) -> {
                    long openEpochSec = c.getOpenTime().getEpochSecond();
                    long closeEpochSec = c.getCloseTime().getEpochSecond();
                    double avgEpochSec = (openEpochSec + closeEpochSec) / 2.0;
                    return new Point(new BigDecimal(avgEpochSec), c.getAveragePrice());
                }).collect(Collectors.toList());

        return predictionCalculator.calPrediction(points);
    }
}
