package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;

public class Symbol {
    private final Currency baseCurrency;
    private final Currency quoteCurrency;
    private final BigDecimal minOrderQuantity;

    public Symbol(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency,
            @Nonnull BigDecimal minOrderQuantity) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.minOrderQuantity = minOrderQuantity;
    }

    @Nonnull
    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    @Nonnull
    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }

    @Nonnull
    public BigDecimal getMinOrderQuantity() {
        return minOrderQuantity;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Symbol)) {
            return false;
        }
        Symbol that = (Symbol) other;
        return baseCurrency.equals(that.baseCurrency) &&
                quoteCurrency.equals(that.quoteCurrency) &&
                minOrderQuantity.equals(that.minOrderQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseCurrency, quoteCurrency, minOrderQuantity);
    }
}
