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
    private static final BigDecimal WEIGHT_MUL_FACTOR = new BigDecimal("0.99917");
    private static final int PRECISION_SCALE = 6;
    private final CurrencyPriceViewer currencyPriceViewer;
    private final System system;

    public PricePredictor(
            @Nonnull CurrencyPriceViewer currencyPriceViewer,
            @Nonnull System system) {
        this.currencyPriceViewer = currencyPriceViewer;
        this.system = system;
    }

    @Nonnull
    public Optional<PricePrediction> getPrediction(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency) throws IOException {

        Instant endTime = Instant.ofEpochMilli(system.currentTimeMillis());
        Instant startTime = endTime.minusMillis(PREDICTION_PERIOD_MS);

        Optional<List<Candlestick>> optCandlesticks =
                currencyPriceViewer.getPriceHistory(baseCurrency, quoteCurrency, startTime, endTime);

        if (!optCandlesticks.isPresent() || optCandlesticks.get().size() <= 1) {
            return Optional.empty();
        }

        List<BigDecimal> growthRates = calGrowthRates(optCandlesticks.get());

        BigDecimal weight = BigDecimal.ONE;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;

        ListIterator<BigDecimal> reversedItr = growthRates.listIterator(growthRates.size());
        MathContext mathContext = new MathContext(PRECISION_SCALE, RoundingMode.HALF_UP);
        while (reversedItr.hasPrevious()) {
            BigDecimal growthRate = reversedItr.previous();
            result = result.add(weight.multiply(growthRate));
            totalWeight = totalWeight.add(weight);
            weight = weight.multiply(WEIGHT_MUL_FACTOR, mathContext);
        }
        result = result.divide(totalWeight, mathContext);

        return Optional.of(new PricePrediction(result));
    }

    @Nonnull
    private List<BigDecimal> calGrowthRates(List<Candlestick> candlesticks) {
        List<BigDecimal> growthRates = new ArrayList<>(candlesticks.size() - 1);
        Iterator<Candlestick> itr = candlesticks.iterator();
        Candlestick lastCandlestick = itr.next();
        while (itr.hasNext()) {
            Candlestick candlestick = itr.next();
            BigDecimal growthRate = candlestick.getAveragePrice().subtract(lastCandlestick.getAveragePrice());
            growthRates.add(growthRate);
            lastCandlestick = candlestick;
        }
        return growthRates;
    }
}
