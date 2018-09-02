package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.impl.BinanceApiRestClientImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;

public class SymbolPriceViewer implements PriceViewer<Symbol> {

    private final BinanceApiRestClient client;

    public SymbolPriceViewer(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Iterator<Candlestick> getPriceHistory(@Nonnull Symbol symbol, @Nonnull Instant startTime) {

        // TODO
        return null;
    }

    @Override
    public void close() {
    }
}
