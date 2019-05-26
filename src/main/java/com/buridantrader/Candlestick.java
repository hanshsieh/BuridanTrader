package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

public class Candlestick {
    private static final BigDecimal TWO = new BigDecimal("2");
    private final Instant openTime;
    private final Instant closeTime;
    private final BigDecimal averagePrice;

    public Candlestick(
            @Nonnull Instant openTime,
            @Nonnull Instant closeTime,
            @Nonnull BigDecimal averagePrice) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.averagePrice = averagePrice;
    }

    public Candlestick(@Nonnull com.binance.api.client.domain.market.Candlestick candlestick) {
        BigDecimal highPrice = new BigDecimal(candlestick.getHigh());
        BigDecimal lowPrice = new BigDecimal(candlestick.getLow());
        BigDecimal avgPrice = highPrice.add(lowPrice).divide(TWO, RoundingMode.HALF_UP);
        this.openTime = Instant.ofEpochMilli(candlestick.getOpenTime());
        this.closeTime = Instant.ofEpochMilli(candlestick.getCloseTime());
        this.averagePrice = avgPrice;
    }

    @Nonnull
    public Instant getOpenTime() {
        return openTime;
    }

    @Nonnull
    public Instant getCloseTime() {
        return closeTime;
    }

    @Nonnull
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        }
        Candlestick that = (Candlestick) other;
        return openTime.equals(that.openTime)
                && closeTime.equals(that.closeTime)
                && averagePrice.equals(that.averagePrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(openTime, closeTime, averagePrice);
    }
}
