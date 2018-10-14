package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

public class PricePredictor {

    // TODO Make it configurable
    private static final long PREDICTION_PERIOD_MS = 1000 * 60 * 60;
    private static final BigDecimal WEIGHT_MUL_FACTOR = new BigDecimal("0.95");
    private static final BigDecimal SECONDS_PER_MINUTE = new BigDecimal(60);
    private static final int MIN_PRECISION = 6;

    // 6 is the basic scale. 2 is because we are dividing by 60
    private static final int GROWTH_RATE_EXTRA_SCALE = MIN_PRECISION + 2;
    private final CurrencyPriceViewer currencyPriceViewer;
    private final System system;

    public PricePredictor(@Nonnull CurrencyPriceViewer priceViewer) {
        this(priceViewer, new System());
    }

    public PricePredictor(
            @Nonnull CurrencyPriceViewer currencyPriceViewer,
            @Nonnull System system) {
        this.currencyPriceViewer = currencyPriceViewer;
        this.system = system;
    }

    @Nonnull
    public PricePrediction getPrediction(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency) throws IOException, IllegalArgumentException {

        if (baseCurrency.equals(quoteCurrency)) {
            return new PricePrediction(BigDecimal.ZERO);
        }

        Instant endTime = Instant.ofEpochMilli(system.currentTimeMillis());
        Instant startTime = endTime.minusMillis(PREDICTION_PERIOD_MS);

        List<Candlestick> candlesticks = currencyPriceViewer.getPriceHistoryPerMinute(
                baseCurrency, quoteCurrency, startTime, endTime);

        List<BigDecimal> growthRates = calGrowthRates(candlesticks);

        BigDecimal weight = BigDecimal.ONE;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;

        ListIterator<BigDecimal> reversedItr = growthRates.listIterator(growthRates.size());
        while (reversedItr.hasPrevious()) {
            BigDecimal growthRate = reversedItr.previous();
            result = result.add(weight.multiply(growthRate));
            totalWeight = totalWeight.add(weight);
            weight = weight.multiply(WEIGHT_MUL_FACTOR);
        }
        int scale = result.scale()
                + totalWeight.toBigInteger().toString().length()
                + MIN_PRECISION;
        result = result.divide(totalWeight, scale, RoundingMode.HALF_UP);

        return new PricePrediction(result);
    }

    @Nonnull
    private List<BigDecimal> calGrowthRates(@Nonnull List<Candlestick> candlesticks) throws IOException {
        if (candlesticks.size() <= 1) {
            throw new IOException("Cannot get enough price candlesticks");
        }
        List<BigDecimal> growthRates = new ArrayList<>(candlesticks.size() - 1);
        Iterator<Candlestick> itr = candlesticks.iterator();
        Candlestick lastCandlestick = itr.next();
        while (itr.hasNext()) {
            Candlestick candlestick = itr.next();
            BigDecimal priceDiff = candlestick.getAveragePrice()
                    .subtract(lastCandlestick.getAveragePrice());
            int scale = priceDiff.scale() + GROWTH_RATE_EXTRA_SCALE;
            BigDecimal growthRate = priceDiff
                    .divide(SECONDS_PER_MINUTE, scale, RoundingMode.HALF_UP);
            growthRates.add(growthRate);
            lastCandlestick = candlestick;
        }
        return growthRates;
    }
}
