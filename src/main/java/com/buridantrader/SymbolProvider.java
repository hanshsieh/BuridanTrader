package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolStatus;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@ThreadSafe
public class SymbolProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SymbolProvider.class);
    private static final long REFRESH_MS = 1000 * 60 * 10;
    private final BinanceApiRestClient client;
    private Map<String, SymbolInfo> symbolMap;
    private Instant lastRefreshTime;
    private Instant timeOfVersion;

    public SymbolProvider(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    @Nonnull
    public synchronized Optional<SymbolInfo> getSymbolInfoByName(@Nonnull String name) throws IOException {
        checkFreshness();
        return Optional.ofNullable(symbolMap.get(name));
    }

    @Nonnull
    public Optional<SymbolInfo> getSymbolInfo(@Nonnull Symbol symbol) throws IOException {
        Optional<SymbolInfo> optSymbolInfo = getSymbolInfoByName(symbol.getName());
        return optSymbolInfo.filter((info) -> info.getSymbol().equals(symbol));
    }

    @Nonnull
    public synchronized Collection<SymbolInfo> getAllSymbolInfos() throws IOException {
        checkFreshness();
        return ImmutableList.copyOf(symbolMap.values());
    }

    public boolean isUpdatedSince(@Nonnull Instant time) {
        return timeOfVersion == null || time.isBefore(timeOfVersion);
    }

    private void checkFreshness() throws IOException {
        if (shouldRefresh()) {
            LOGGER.info("Refreshing symbols");
            Map<String, SymbolInfo> newSymbolMap = new HashMap<>();
            List<SymbolInfo> symbolInfos = collectSymbolInfos();
            for (SymbolInfo symbolInfo : symbolInfos) {
                newSymbolMap.put(symbolInfo.getSymbol().getName(), symbolInfo);
            }
            lastRefreshTime = Instant.now();
            LOGGER.info("Symbols are refreshed");
            if (symbolMap.equals(newSymbolMap)) {
                LOGGER.info("Symbols are not changed");
                return;
            }
            symbolMap = newSymbolMap;
            timeOfVersion = lastRefreshTime;
        }
    }

    private boolean shouldRefresh() {
        return lastRefreshTime == null
                || Instant.now().toEpochMilli() > lastRefreshTime.toEpochMilli() + REFRESH_MS;
    }

    @Nonnull
    private List<SymbolInfo> collectSymbolInfos() throws IOException {
        List<SymbolInfo> symbolInfos = new ArrayList<>();
        ExchangeInfo exchangeInfo;
        try {
            exchangeInfo = client.getExchangeInfo();
        } catch (Exception ex) {
            throw new IOException("Fail to get exchange information", ex);
        }
        exchangeInfo.getSymbols().forEach((symbolInfo -> {

            if (!SymbolStatus.TRADING.equals(symbolInfo.getStatus())) {
                LOGGER.debug("Symbol {} not in TRADING status. Skip it.", symbolInfo.getSymbol());
                return;
            }

            Optional<SymbolFilter> lotSizeFilter = getSymbolFilter(symbolInfo, FilterType.LOT_SIZE);
            Optional<SymbolFilter> priceFilter = getSymbolFilter(symbolInfo, FilterType.PRICE_FILTER);
            if (!lotSizeFilter.isPresent() || !priceFilter.isPresent()) {
                LOGGER.debug("Symbol {} doesn't have LOT_SIZE or PRICE_FILTER filter. Skip it."
                        , symbolInfo.getSymbol());
                return;
            }

            BigDecimal minQuantity = new BigDecimal(lotSizeFilter.get().getMinQty());
            BigDecimal maxQuantity = new BigDecimal(lotSizeFilter.get().getMaxQty());
            BigDecimal quantityStepSize = new BigDecimal(lotSizeFilter.get().getStepSize());
            BigDecimal minPrice = new BigDecimal(priceFilter.get().getMinPrice());
            BigDecimal maxPrice = new BigDecimal(priceFilter.get().getMaxPrice());
            BigDecimal priceStepSize = new BigDecimal(priceFilter.get().getTickSize());

            Currency baseCurrency = new Currency(symbolInfo.getBaseAsset());
            Currency quoteCurrency = new Currency(symbolInfo.getQuoteAsset());
            symbolInfos.add(new SymbolInfo(
                    new Symbol(baseCurrency, quoteCurrency),
                    minQuantity,
                    maxQuantity,
                    quantityStepSize,
                    minPrice,
                    maxPrice,
                    priceStepSize));
        }));
        return symbolInfos;
    }

    @Nonnull
    private Optional<SymbolFilter> getSymbolFilter(
            @Nonnull com.binance.api.client.domain.general.SymbolInfo symbolInfo,
            @Nonnull FilterType filterType) {
        return symbolInfo.getFilters().stream()
                .filter((f) -> filterType.equals(f.getFilterType()))
                .findFirst();
    }
}
