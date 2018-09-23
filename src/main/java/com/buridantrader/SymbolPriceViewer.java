package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.impl.BinanceApiRestClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SymbolPriceViewer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SymbolPriceViewer.class);
    private static final BigDecimal TWO = new BigDecimal("2");
    private final BinanceApiRestClient client;

    public SymbolPriceViewer(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    @Nonnull
    public List<Candlestick> getPriceHistoryPerMinute(
            @Nonnull Symbol symbol,
            @Nonnull Instant startTime,
            @Nonnull Instant endTime) {

        int numMinutes = (int) TimeUnit.MINUTES.convert(
                endTime.toEpochMilli() - startTime.toEpochMilli(),
                TimeUnit.MILLISECONDS);

        // TODO Cached previous result
        LOGGER.debug("Getting candlesticks for symbol {} for {} minutes from epoch {} ms to {} ms",
                symbol.getName(),
                numMinutes,
                startTime.toEpochMilli(),
                endTime.toEpochMilli());
        return client.getCandlestickBars(
                symbol.getName(),
                CandlestickInterval.ONE_MINUTE,
                numMinutes,
                startTime.toEpochMilli(),
                endTime.toEpochMilli()
            ).stream()
            .map((c) -> {
                BigDecimal highPrice = new BigDecimal(c.getHigh());
                BigDecimal lowPrice = new BigDecimal(c.getLow());
                BigDecimal avgPrice = highPrice.add(lowPrice).divide(TWO, RoundingMode.HALF_UP);
                return new Candlestick(
                        Instant.ofEpochMilli(c.getOpenTime()),
                        Instant.ofEpochMilli(c.getCloseTime()),
                        avgPrice);
            })
            .collect(Collectors.toList());
    }

}
