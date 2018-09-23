package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Candlestick {
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
