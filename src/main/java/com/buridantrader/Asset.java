package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class Asset {
    private final Currency currency;
    private final BigDecimal balance;

    public Asset(@Nonnull Currency currency, @Nonnull BigDecimal balance) {
        this.currency = currency;
        this.balance = balance;
    }

    @Nonnull
    public Currency getCurrency() {
        return currency;
    }

    @Nonnull
    public BigDecimal getBalance() {
        return balance;
    }
}
