package com.buridantrader;

import javax.annotation.Nonnull;
import java.util.*;

public class TradingPathFinder {

    private static class Entry {
        public Symbol symbol;
        public Currency next;
        public int length = Integer.MAX_VALUE;

        public Entry(@Nonnull Symbol symbol, @Nonnull Currency next, int length) {
            this.symbol = symbol;
            this.next = next;
            this.length = length;
        }
    }

    private final Map<Currency, Map<Currency, Entry>> pathGraph = new HashMap<>();

    public TradingPathFinder(@Nonnull Collection<Symbol> symbols) {
        resolvePaths(symbols);
    }

    private void resolvePaths(@Nonnull Collection<Symbol> symbols) {
        resolveDirectPaths(symbols);

        // Floyd-Warshall
        Set<Currency> currencies = pathGraph.keySet();
        for (Currency midCurrency : currencies) {
            for (Currency sourceCurrency : currencies) {
                for (Currency targetCurrency : currencies) {
                    Entry firstEntry = pathGraph.get(sourceCurrency).get(midCurrency);
                    Entry secondEntry = pathGraph.get(midCurrency).get(targetCurrency);
                    if (firstEntry == null || secondEntry == null) {
                        continue;
                    }
                    Map<Currency, Entry> map = pathGraph.get(sourceCurrency);
                    Entry oriEntry = map.get(targetCurrency);
                    int newLength = firstEntry.length + secondEntry.length;
                    if (oriEntry == null) {
                        oriEntry = new Entry(firstEntry.symbol, firstEntry.next, newLength);
                        map.put(targetCurrency, oriEntry);
                        continue;
                    }
                    if (newLength < oriEntry.length) {
                        oriEntry.length = newLength;
                        oriEntry.next = firstEntry.next;
                        oriEntry.symbol = firstEntry.symbol;
                    }
                }
            }
        }
    }

    private void resolveDirectPaths(@Nonnull Collection<Symbol> symbols) {
        symbols.forEach((symbol) -> {
            Currency baseCurrency = symbol.getBaseCurrency();
            Currency quoteCurrency = symbol.getQuoteCurrency();
            addDirectPath(baseCurrency, quoteCurrency, symbol);
            addDirectPath(quoteCurrency, baseCurrency, symbol);
        });
    }

    private void addDirectPath(
            @Nonnull Currency currency1,
            @Nonnull Currency currency2,
            @Nonnull Symbol symbol) {
        pathGraph.computeIfAbsent(currency1, (c) -> new HashMap<>())
                .put(currency2, new Entry(symbol, currency2, 1));
    }

}