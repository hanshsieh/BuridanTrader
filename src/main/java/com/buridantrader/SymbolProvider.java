package com.buridantrader;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@ThreadSafe
public class SymbolProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SymbolProvider.class);
    private static final long REFRESH_MS = 1000 * 60 * 10;
    private final System system;
    private final SymbolService symbolService;
    private Map<String, SymbolInfo> symbolMap;
    private Instant lastRefreshTime;
    private Instant timeOfVersion;

    public SymbolProvider(@Nonnull SymbolService symbolService) {
        this(symbolService, new System());
    }

    public SymbolProvider(@Nonnull SymbolService symbolService,
                          @Nonnull System system) {
        this.symbolService = symbolService;
        this.system = system;
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
            List<SymbolInfo> symbolInfos = symbolService.getSymbolInfos();
            for (SymbolInfo symbolInfo : symbolInfos) {
                newSymbolMap.put(symbolInfo.getSymbol().getName(), symbolInfo);
            }
            lastRefreshTime = Instant.ofEpochMilli(system.currentTimeMillis());
            LOGGER.info("Symbols are refreshed");
            if (symbolMap != null && symbolMap.equals(newSymbolMap)) {
                LOGGER.info("Symbols are not changed");
                return;
            }
            symbolMap = newSymbolMap;
            timeOfVersion = lastRefreshTime;
        }
    }

    private boolean shouldRefresh() {
        return lastRefreshTime == null
                || system.currentTimeMillis() > lastRefreshTime.toEpochMilli() + REFRESH_MS;
    }

}
