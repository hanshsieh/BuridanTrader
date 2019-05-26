package com.buridantrader;

import com.buridantrader.exceptions.NoSuchPathException;
import com.buridantrader.exceptions.ValueLimitException;
import com.buridantrader.services.binance.CandlestickIterator;
import com.buridantrader.services.symbol.SymbolPriceViewer;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CurrencyPriceViewer {

    // TODO Make it configurable
    private final int ADDITIONAL_SCALE = 6;
    private final SymbolPriceViewer symbolPriceViewer;
    private final TradingPathFinder tradingPathFinder;

    public CurrencyPriceViewer(
            @Nonnull SymbolPriceViewer symbolPriceViewer,
            @Nonnull TradingPathFinder tradingPathFinder) {
        this.symbolPriceViewer = symbolPriceViewer;
        this.tradingPathFinder = tradingPathFinder;
    }

    @Nonnull
    public List<Candlestick> getPriceHistoryPerMinute(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency,
            @Nonnull Instant startTime,
            @Nonnull Instant endTime) throws IOException, ValueLimitException, NoSuchPathException {

        List<OrderSpec> orderSpecs =
                tradingPathFinder.findPathOfOrderSpecs(baseCurrency, quoteCurrency);

        List<Candlestick> finalCandlesticks = null;

        List<List<Candlestick>> candlesticksForOrders =
                collectCandlesticks(orderSpecs, startTime, endTime);

        for (List<Candlestick> candlesticks: candlesticksForOrders) {
            if (finalCandlesticks == null) {
                finalCandlesticks = candlesticks;
            } else {
                finalCandlesticks = calRelativeCandlesticks(finalCandlesticks, candlesticks);
            }
        }

        if (finalCandlesticks == null) {
            throw new IOException("Expecting at least one order spec, but see 0");
        }

        return finalCandlesticks;
    }

    @Nonnull
    private List<List<Candlestick>> collectCandlesticks(
            @Nonnull List<OrderSpec> orderSpecs,
            @Nonnull Instant startTime,
            @Nonnull Instant endTime) {
        List<List<Candlestick>> candlesticksForOrders = new ArrayList<>();
        int minSize = Integer.MAX_VALUE;
        for (OrderSpec orderSpec : orderSpecs) {
            Symbol symbol = orderSpec.getSymbol();
            Iterator<Candlestick> candlesticksItr = symbolPriceViewer.getPriceHistoryPerMinute(symbol, startTime, endTime);
            List<Candlestick> convertedCandlesticks = convertCandlesticksForOrder(candlesticksItr, orderSpec);
            candlesticksForOrders.add(convertedCandlesticks);
            minSize = Math.min(convertedCandlesticks.size(), minSize);
        }

        // For unknown reason, Binance API sometimes return different number of candlesticks with the same
        // set of query parameters
        // Therefore, we drop the trailing candlesticks if the number mismatch
        List<List<Candlestick>> result = new ArrayList<>(candlesticksForOrders.size());
        for (List<Candlestick> candlesticks : candlesticksForOrders) {
            if (candlesticks.size() == minSize) {
                result.add(candlesticks);
            } else {
                result.add(candlesticks.subList(0, minSize));
            }
        }

        return result;
    }

    @Nonnull
    private List<Candlestick> calRelativeCandlesticks(
            @Nonnull List<Candlestick> oriCandlesticks,
            @Nonnull List<Candlestick> newCandlesticks) {
        List<Candlestick> result = new ArrayList<>(newCandlesticks.size());
        Iterator<Candlestick> oriItr = oriCandlesticks.iterator();
        Iterator<Candlestick> newItr = newCandlesticks.iterator();
        oriItr.forEachRemaining((c) -> result.add(new Candlestick(
                c.getOpenTime(),
                c.getCloseTime(),
                c.getAveragePrice().multiply(newItr.next().getAveragePrice())
        )));
        return result;
    }

    @Nonnull
    private List<Candlestick> convertCandlesticksForOrder(
            @Nonnull Iterator<Candlestick> candlestickItr,
            @Nonnull OrderSpec orderSpec) throws ValueLimitException {
        List<Candlestick> candlesticks = Lists.newArrayList(candlestickItr);
        if (OrderSide.SELL.equals(orderSpec.getOrderSide())) {
            return candlesticks;
        }
        try {
            return candlesticks.stream()
                    .map((c) -> {
                        int scale = c.getAveragePrice().toBigInteger().toString().length() + ADDITIONAL_SCALE;
                        return new Candlestick(
                                c.getOpenTime(),
                                c.getCloseTime(),
                                BigDecimal.ONE.divide(c.getAveragePrice(), scale, RoundingMode.HALF_UP));
                    })
                    .collect(Collectors.toList());
        } catch (ArithmeticException ex) {
            throw new ValueLimitException("Price cannot be 0", ex);
        }
    }

}
