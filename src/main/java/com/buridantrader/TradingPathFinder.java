package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ThreadSafe
public class TradingPathFinder {

    private static class PathEntry {
        Symbol symbolToNext;
        int length;

        PathEntry(@Nonnull Symbol symbolToNext, int length) {
            this.symbolToNext = symbolToNext;
            this.length = length;
        }
    }

    // Be careful the referenced instance may be changed by other threads
    private Map<Currency, Map<Currency, PathEntry>> pathGraph;
    private final SymbolPriceProvider symbolPriceProvider;
    private final SymbolProvider symbolProvider;
    private Instant lastUpdateTime;

    public TradingPathFinder(@Nonnull SymbolProvider symbolProvider, @Nonnull SymbolPriceProvider symbolPriceProvider) {
        this.symbolPriceProvider = symbolPriceProvider;
        this.symbolProvider = symbolProvider;
    }

    @Nonnull
    public Optional<List<OrderSpec>> findPathOfOrderSpecs(
            @Nonnull final Currency sourceCurrency,
            @Nonnull final Currency targetCurrency
    ) throws IOException {
        checkFreshness();
        Optional<PathEntry> optPathEntry = getPathEntry(sourceCurrency, targetCurrency);
        List<OrderSpec> path = new ArrayList<>();
        Currency nowCurrency = sourceCurrency;
        while (!nowCurrency.equals(targetCurrency)) {
            if (!optPathEntry.isPresent()) {
                return Optional.empty();
            }
            PathEntry pathEntry = optPathEntry.get();
            path.add(createOrderSpec(nowCurrency, pathEntry.symbolToNext));
            Currency nextCurrency = getNextCurrency(nowCurrency, pathEntry);
            optPathEntry = getPathEntry(nextCurrency, targetCurrency);
            nowCurrency = nextCurrency;
        }
        return Optional.of(path);
    }

    @Nonnull
    public Optional<List<Order>> findPathOfOrders(
            @Nonnull final Currency sourceCurrency,
            @Nonnull final Currency targetCurrency,
            @Nonnull final BigDecimal quantity) throws IOException {
        Optional<List<OrderSpec>> optOrderSpecs = findPathOfOrderSpecs(sourceCurrency, targetCurrency);
        if (!optOrderSpecs.isPresent()) {
            return Optional.empty();
        }
        List<OrderSpec> orderSpecs = optOrderSpecs.get();
        List<Order> orders = new ArrayList<>(orderSpecs.size());
        BigDecimal nowQuantity = quantity;
        for (OrderSpec orderSpec : orderSpecs) {
            SymbolInfo symbolInfo = getSymbolInfo(orderSpec.getSymbol());
            BigDecimal price = getPriceForSymbol(symbolInfo.getSymbol());

            BigDecimal orderQuantity;
            BigDecimal nextQuantity;
            int scale = getQuantityStepScale(symbolInfo);
            if (OrderSide.SELL.equals(orderSpec.getOrderSide())) {
                orderQuantity = nowQuantity;
                nextQuantity = nowQuantity.multiply(price);
            } else {
                orderQuantity = nowQuantity.divide(price, scale, RoundingMode.DOWN);
                nextQuantity = orderQuantity;
            }

            Optional<Order> optOrder = symbolInfo.getQuantityFormalizer()
                    .formalize(orderQuantity, RoundingMode.DOWN)
                    .map(formalizedQuantity -> new Order(
                        orderSpec,
                        formalizedQuantity));

            if (optOrder.isPresent()) {
                orders.add(optOrder.get());
                nowQuantity = nextQuantity;
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(orders);
    }

    @Nonnull
    private SymbolInfo getSymbolInfo(@Nonnull Symbol symbol) throws IOException {
        return symbolProvider.getSymbolInfo(symbol)
                .orElseThrow(() -> new IOException("Fail to get symbol info of " + symbol));
    }

    @Nonnull
    private BigDecimal getPriceForSymbol(@Nonnull Symbol symbol) throws IOException {
        return symbolPriceProvider.getPrice(symbol)
                .orElseThrow(() -> new IOException("Fail to get current price of symbol " + symbol));
    }

    private synchronized void checkFreshness() throws IOException {
        if (lastUpdateTime == null || symbolProvider.isUpdatedSince(lastUpdateTime)) {

            // Resolve the shortest path, and make the shortest path graph unmodifiable
            // prevent thread-safety bugs
            pathGraph = resolvePaths(symbolProvider.getAllSymbolInfos()).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        (entry) -> Collections.unmodifiableMap(entry.getValue())));
            lastUpdateTime = Instant.now();
        }
    }

    @Nonnull
    private Optional<PathEntry> getPathEntry(@Nonnull Currency sourceCurrency, @Nonnull Currency targetCurrency) {
        PathEntry pathEntry = pathGraph.getOrDefault(sourceCurrency, Collections.emptyMap()).get(targetCurrency);
        return Optional.ofNullable(pathEntry);
    }

    @Nonnull
    private Currency getNextCurrency(@Nonnull Currency nowCurrency, @Nonnull PathEntry pathEntry) {
        Symbol symbolToNext = pathEntry.symbolToNext;
        if (nowCurrency.equals(symbolToNext.getBaseCurrency())) {
            return symbolToNext.getQuoteCurrency();
        } else {
            return symbolToNext.getBaseCurrency();
        }
    }

    @Nonnull
    private OrderSpec createOrderSpec(
            @Nonnull Currency sourceCurrency,
            @Nonnull Symbol symbol) {
        OrderSide orderSide;
        if (symbol.getBaseCurrency().equals(sourceCurrency)) {
            orderSide = OrderSide.SELL;
        } else {
            orderSide = OrderSide.BUY;
        }

        return new OrderSpec(symbol, orderSide);
    }

    private int getQuantityStepScale(@Nonnull SymbolInfo symbolInfo) {
        return symbolInfo.getQuantityStepSize().stripTrailingZeros().scale();
    }

    @Nonnull
    private static Map<Currency, Map<Currency, PathEntry>> resolvePaths(
            @Nonnull Collection<SymbolInfo> symbols) {
        Map<Currency, Map<Currency, PathEntry>> newPathGraph = buildInitialGraph(symbols);

        // Floyd-Warshall
        Set<Currency> currencies = newPathGraph.keySet();
        for (Currency midCurrency : currencies) {
            for (Currency sourceCurrency : currencies) {
                for (Currency targetCurrency : currencies) {
                    PathEntry firstPathEntry = newPathGraph.get(sourceCurrency).get(midCurrency);
                    PathEntry secondPathEntry = newPathGraph.get(midCurrency).get(targetCurrency);
                    if (firstPathEntry == null || secondPathEntry == null) {
                        continue;
                    }
                    Map<Currency, PathEntry> map = newPathGraph.get(sourceCurrency);
                    PathEntry oriPathEntry = map.get(targetCurrency);
                    int newLength = firstPathEntry.length + secondPathEntry.length;
                    if (oriPathEntry == null) {
                        oriPathEntry = new PathEntry(firstPathEntry.symbolToNext, newLength);
                        map.put(targetCurrency, oriPathEntry);
                        continue;
                    }
                    if (newLength < oriPathEntry.length) {
                        oriPathEntry.length = newLength;
                        oriPathEntry.symbolToNext = firstPathEntry.symbolToNext;
                    }
                }
            }
        }
        return newPathGraph;
    }

    @Nonnull
    private static Map<Currency, Map<Currency, PathEntry>> buildInitialGraph(
            @Nonnull Collection<SymbolInfo> symbolInfos) {
        Map<Currency, Map<Currency, PathEntry>> pathGraph = new HashMap<>();
        symbolInfos.forEach((symbolInfo) -> {
            Symbol symbol = symbolInfo.getSymbol();
            Currency baseCurrency = symbol.getBaseCurrency();
            Currency quoteCurrency = symbol.getQuoteCurrency();
            addDirectPath(pathGraph, baseCurrency, quoteCurrency, symbol);
            addDirectPath(pathGraph, quoteCurrency, baseCurrency, symbol);
        });
        return pathGraph;
    }

    private static void addDirectPath(
            @Nonnull Map<Currency, Map<Currency, PathEntry>> pathGraph,
            @Nonnull Currency currency1,
            @Nonnull Currency currency2,
            @Nonnull Symbol symbol) {
        pathGraph.computeIfAbsent(currency1, (c) -> new HashMap<>())
                .put(currency2, new PathEntry(symbol, 1));
    }

}