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

    // Be careful the referenced instance may be changed by other threads
    private final ShortestPathsResolver shortestPathsResolver;
    private final SymbolPriceProvider symbolPriceProvider;
    private final SymbolProvider symbolProvider;
    private Instant lastUpdateTime;
    private TradingPaths tradingPaths;

    public TradingPathFinder(
            @Nonnull SymbolProvider symbolProvider,
            @Nonnull SymbolPriceProvider symbolPriceProvider) {
        this(symbolProvider, symbolPriceProvider, new ShortestPathsResolver());
    }

    public TradingPathFinder(
            @Nonnull SymbolProvider symbolProvider,
            @Nonnull SymbolPriceProvider symbolPriceProvider,
            @Nonnull ShortestPathsResolver shortestPathsResolver) {
        this.symbolPriceProvider = symbolPriceProvider;
        this.symbolProvider = symbolProvider;
        this.shortestPathsResolver = shortestPathsResolver;
    }

    @Nonnull
    public Optional<List<OrderSpec>> findPathOfOrderSpecs(
            @Nonnull final Currency sourceCurrency,
            @Nonnull final Currency targetCurrency
    ) throws IOException {
        checkFreshness();
        Optional<PathStep> optPathEntry = tradingPaths.getNextStep(sourceCurrency, targetCurrency);
        List<OrderSpec> path = new ArrayList<>();
        Currency nowCurrency = sourceCurrency;
        while (!nowCurrency.equals(targetCurrency)) {
            if (!optPathEntry.isPresent()) {
                return Optional.empty();
            }
            PathStep pathStep = optPathEntry.get();
            path.add(createOrderSpec(nowCurrency, pathStep.getSymbolToNext()));
            Currency nextCurrency = pathStep.getNextCurrency(nowCurrency);
            optPathEntry = tradingPaths.getNextStep(nextCurrency, targetCurrency);
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
            if (OrderSide.SELL.equals(orderSpec.getOrderSide())) {
                orderQuantity = nowQuantity;
                nextQuantity = nowQuantity.multiply(price);
            } else {
                int scale = getQuantityStepScale(symbolInfo);
                orderQuantity = nowQuantity.divide(price, scale, RoundingMode.DOWN);
                nextQuantity = orderQuantity;
            }


            BigDecimal formalizedQuantity;
            try {
                formalizedQuantity = symbolInfo.getQuantityFormalizer()
                        .formalize(orderQuantity, RoundingMode.DOWN);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            Order order = new Order(orderSpec, formalizedQuantity);

            orders.add(order);
            nowQuantity = nextQuantity;
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
            tradingPaths = shortestPathsResolver.resolveAllShortestPaths(
                    symbolProvider.getAllSymbolInfos().stream()
                            .map(SymbolInfo::getSymbol)
                            .collect(Collectors.toList()));
            lastUpdateTime = Instant.now();
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

}