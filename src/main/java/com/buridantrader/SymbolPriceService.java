package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SymbolPriceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SymbolPriceService.class);
    private static final long REFRESH_MS = 1000 * 60;
    private final BinanceApiRestClient client;
    private final SymbolService symbolService;
    private Instant lastRefreshTime;
    private final Map<Symbol, BigDecimal> priceMap = new HashMap<>();

    public SymbolPriceService(@Nonnull BinanceApiRestClient client,
                              @Nonnull SymbolService symbolService) {
        this.client = client;
        this.symbolService = symbolService;
    }

    @Nonnull
    public Optional<BigDecimal> getPrice(@Nonnull Symbol symbol) throws IOException {
        checkFreshness();
        return Optional.ofNullable(priceMap.get(symbol));
    }

    private void checkFreshness() throws IOException {
        if (shouldRefresh()) {
            LOGGER.info("Refreshing symbol prices");
            priceMap.clear();
            List<TickerPrice> tickerPrices = client.getAllPrices();
            for (TickerPrice tickerPrice : tickerPrices) {
                BigDecimal price = new BigDecimal(tickerPrice.getPrice());
                symbolService.getSymbolInfoByName(tickerPrice.getSymbol())
                        .map(SymbolInfo::getSymbol)
                        .ifPresent((s) -> priceMap.put(s, price));
            }
            lastRefreshTime = Instant.now();
            LOGGER.info("Symbol prices are refreshed");
        }
    }

    private boolean shouldRefresh() {
        return lastRefreshTime == null
                || Instant.now().toEpochMilli() > lastRefreshTime.toEpochMilli() + REFRESH_MS;
    }
}
