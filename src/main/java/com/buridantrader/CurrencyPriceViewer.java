package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CurrencyPriceViewer {

    private final SymbolPriceViewer symbolPriceViewer;
    private final TradingPathFinder tradingPathFinder;

    public CurrencyPriceViewer(
            @Nonnull SymbolPriceViewer symbolPriceViewer,
            @Nonnull TradingPathFinder tradingPathFinder) {
        this.symbolPriceViewer = symbolPriceViewer;
        this.tradingPathFinder = tradingPathFinder;
    }

    @Nonnull
    public Optional<List<Candlestick>> getPriceHistory(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency,
            @Nonnull Instant startTime,
            @Nonnull Instant endTime) throws IOException {

        Optional<List<OrderSpec>> optOrderSpecs =
                tradingPathFinder.findPathOfOrderSpecs(baseCurrency, quoteCurrency);

        if (!optOrderSpecs.isPresent()) {
            return Optional.empty();
        }

        // TODO
        return null;
    }

}
