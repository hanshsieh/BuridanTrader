package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Instant;

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
}
