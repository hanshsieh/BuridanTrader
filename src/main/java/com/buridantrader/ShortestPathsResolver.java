package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
@Immutable
public class ShortestPathsResolver {

    @Nonnull
    public TradingPaths resolveAllShortestPaths(@Nonnull Collection<Symbol> symbols) {

        Map<Currency, Map<Currency, PathStep>> newPathGraph = buildInitialGraph(symbols);

        // Floyd-Warshall
        Set<Currency> currencies = newPathGraph.keySet();
        for (Currency midCurrency : currencies) {
            for (Currency sourceCurrency : currencies) {
                for (Currency targetCurrency : currencies) {
                    PathStep firstPathStep = newPathGraph.get(sourceCurrency).get(midCurrency);
                    PathStep secondPathStep = newPathGraph.get(midCurrency).get(targetCurrency);
                    if (firstPathStep == null || secondPathStep == null) {
                        continue;
                    }
                    Map<Currency, PathStep> map = newPathGraph.get(sourceCurrency);
                    PathStep oriPathStep = map.get(targetCurrency);
                    int newLength = firstPathStep.getLength() + secondPathStep.getLength();
                    if (oriPathStep == null) {
                        oriPathStep = new PathStep(firstPathStep.getSymbolToNext(), newLength);
                        map.put(targetCurrency, oriPathStep);
                        continue;
                    }
                    if (newLength < oriPathStep.getLength()) {
                        oriPathStep.setLength(newLength);
                        oriPathStep.setSymbolToNext(firstPathStep.getSymbolToNext());
                    }
                }
            }
        }
        return new TradingPaths(newPathGraph);
    }

    @Nonnull
    private static Map<Currency, Map<Currency, PathStep>> buildInitialGraph(
            @Nonnull Collection<Symbol> symbols) {
        Map<Currency, Map<Currency, PathStep>> pathGraph = new HashMap<>();
        symbols.forEach((symbol) -> {
            Currency baseCurrency = symbol.getBaseCurrency();
            Currency quoteCurrency = symbol.getQuoteCurrency();
            addDirectPath(pathGraph, baseCurrency, quoteCurrency, symbol);
            addDirectPath(pathGraph, quoteCurrency, baseCurrency, symbol);
        });
        return pathGraph;
    }

    private static void addDirectPath(
            @Nonnull Map<Currency, Map<Currency, PathStep>> pathGraph,
            @Nonnull Currency currency1,
            @Nonnull Currency currency2,
            @Nonnull Symbol symbol) {
        pathGraph.computeIfAbsent(currency1, (c) -> new HashMap<>())
                .put(currency2, new PathStep(symbol, 1));
    }
}
