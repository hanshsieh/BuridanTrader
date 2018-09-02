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
        resolveDirectPath(symbols);

        // Floyd-Warshall
        Set<Currency> currencies = pathGraph.keySet();
        for (Currency midCurrency : currencies) {
            for (Currency sourceCurrency : currencies) {
                for (Currency targetCurrency : currencies) {
                    Entry firstEntry = pathGraph.getOrDefault(sourceCurrency, Collections.emptyMap())
                            .get(midCurrency);
                    Entry secondEntry = pathGraph.getOrDefault(midCurrency, Collections.emptyMap())
                            .get(targetCurrency);
                    if (firstEntry == null || secondEntry == null) {
                        continue;
                    }
                    Map<Currency, Entry> map = pathGraph.computeIfAbsent(sourceCurrency, (c) -> new HashMap<>());
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

    private void resolveDirectPath(@Nonnull Collection<Symbol> symbols) {
        symbols.forEach((symbol) -> {
            Currency baseCurrency = symbol.getBaseCurrency();
            Currency quoteCurrency = symbol.getQuoteCurrency();
            pathGraph.computeIfAbsent(baseCurrency, (c) -> new HashMap<>())
                    .put(quoteCurrency, new Entry(symbol, quoteCurrency, 1));
            pathGraph.computeIfAbsent(quoteCurrency, (c) -> new HashMap<>())
                    .put(baseCurrency, new Entry(symbol, baseCurrency, 1));
        });
    }

}