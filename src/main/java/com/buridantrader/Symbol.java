package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

@ThreadSafe
@Immutable
public class Symbol {
    private final Currency baseCurrency;
    private final Currency quoteCurrency;

    public Symbol(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
    }

    @Nonnull
    public String getName() {
        return baseCurrency.getName() + quoteCurrency.getName();
    }

    @Nonnull
    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    @Nonnull
    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }
        Symbol that = (Symbol) other;
        return baseCurrency.equals(that.baseCurrency) &&
                quoteCurrency.equals(that.quoteCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseCurrency, quoteCurrency);
    }

    @Override
    public String toString() {
        return getName();
    }
}
