package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;

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

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }
        Asset that = (Asset) other;
        return currency.equals(that.currency) && balance.equals(that.balance);
    }

    public int hashCode() {
        return Objects.hash(currency, balance);
    }
}
