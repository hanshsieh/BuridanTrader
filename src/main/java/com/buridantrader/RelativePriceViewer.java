package com.buridantrader;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Iterator;

public class RelativePriceViewer implements PriceViewer<Currency> {

    private final Currency baseCurrency;

    public RelativePriceViewer(@Nonnull Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    @Nonnull
    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    @Nonnull
    @Override
    public Iterator<Candlestick> getPriceHistory(@Nonnull Currency currency, @Nonnull Instant startTime) {
        // TODO
        return null;
    }

    @Override
    public void close() {
    }
}
