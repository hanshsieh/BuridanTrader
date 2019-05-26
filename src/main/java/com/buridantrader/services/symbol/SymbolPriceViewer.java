package com.buridantrader.services.symbol;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.buridantrader.Candlestick;
import com.buridantrader.Symbol;
import com.buridantrader.services.binance.CandlestickIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SymbolPriceViewer {

    private final BinanceApiRestClient client;

    public SymbolPriceViewer(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    @Nonnull
    public Iterator<Candlestick> getPriceHistoryPerMinute(
            @Nonnull Symbol symbol,
            @Nonnull Instant startTime,
            @Nonnull Instant endTime) {
        return new CandlestickIterator(client, symbol.getName(), startTime, endTime);
    }

}
