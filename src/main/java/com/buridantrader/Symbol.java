package com.buridantrader;

import javax.annotation.Nonnull;

public class Symbol {
    private final Currency baseCurrency;
    private final Currency quoteCurrency;

    public Symbol(@Nonnull Currency baseCurrency, @Nonnull Currency quoteCurrency) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
    }

    @Nonnull
    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    @Nonnull
    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }
}
