package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Instant;

public class Candlestick {
    private final Currency currency;
    private final Instant openTime;
    private final Instant closeTime;
    private final BigDecimal averagePrice;

    public Candlestick(
            @Nonnull Currency currency,
            @Nonnull Instant openTime,
            @Nonnull Instant closeTime,
            @Nonnull BigDecimal averagePrice) {
        this.currency = currency;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.averagePrice = averagePrice;
    }

    @Nonnull
    public Currency getCurrency() {
        return currency;
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
