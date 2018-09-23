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
    private static final BigDecimal WEIGHT_MUL_FACTOR = new BigDecimal("0.949660");
    private static final BigDecimal SECONDS_PER_MINUTE = new BigDecimal("60");
    private static final MathContext MATH_CONTEXT = new MathContext(6, RoundingMode.HALF_UP);
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

        if (candlesticks.size() <= 1) {
            throw new IOException("Cannot get enough price candlesticks");
        }

        List<BigDecimal> growthRates = calGrowthRates(candlesticks);

        BigDecimal weight = BigDecimal.ONE;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;

        ListIterator<BigDecimal> reversedItr = growthRates.listIterator(growthRates.size());
        while (reversedItr.hasPrevious()) {
            BigDecimal growthRate = reversedItr.previous();
            result = result.add(weight.multiply(growthRate));
            totalWeight = totalWeight.add(weight);
            weight = weight.multiply(WEIGHT_MUL_FACTOR, MATH_CONTEXT);
        }
        result = result.divide(totalWeight, MATH_CONTEXT);

        return new PricePrediction(result);
    }

    @Nonnull
    private List<BigDecimal> calGrowthRates(List<Candlestick> candlesticks) {
        List<BigDecimal> growthRates = new ArrayList<>(candlesticks.size() - 1);
        Iterator<Candlestick> itr = candlesticks.iterator();
        Candlestick lastCandlestick = itr.next();
        while (itr.hasNext()) {
            Candlestick candlestick = itr.next();
            BigDecimal growthRate = candlestick.getAveragePrice()
                    .subtract(lastCandlestick.getAveragePrice())
                    .divide(SECONDS_PER_MINUTE, RoundingMode.HALF_UP);
            growthRates.add(growthRate);
            lastCandlestick = candlestick;
        }
        return growthRates;
    }
}
