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

    private final ShortestPathsResolver shortestPathsResolver;
    private final SymbolPriceProvider symbolPriceProvider;
    private final SymbolProvider symbolProvider;
    private final System system;

    // Be careful the referenced instance may be changed by other threads
    private Instant lastUpdateTime;
    private TradingPaths tradingPaths;

    public TradingPathFinder(
            @Nonnull SymbolProvider symbolProvider,
            @Nonnull SymbolPriceProvider symbolPriceProvider) {
        this(symbolProvider,
            symbolPriceProvider,
            new ShortestPathsResolver(),
            new System());
    }

    public TradingPathFinder(
            @Nonnull SymbolProvider symbolProvider,
            @Nonnull SymbolPriceProvider symbolPriceProvider,
            @Nonnull ShortestPathsResolver shortestPathsResolver,
            @Nonnull System system) {
        this.symbolPriceProvider = symbolPriceProvider;
        this.symbolProvider = symbolProvider;
        this.shortestPathsResolver = shortestPathsResolver;
        this.system = system;
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
            BigDecimal price = getPriceForSymbol(orderSpec.getSymbol());
            SymbolInfo symbolInfo = getSymbolInfo(orderSpec.getSymbol());
            BigDecimal orderQuantity = calOrderQuantity(nowQuantity, orderSpec, symbolInfo, price);

            BigDecimal formalizedQuantity;
            try {
                formalizedQuantity = symbolInfo.getQuantityFormalizer()
                        .formalize(orderQuantity, RoundingMode.DOWN);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            Order order = new Order(orderSpec, formalizedQuantity);

            orders.add(order);

            nowQuantity = calNextQuantity(order, price);
        }
        return Optional.of(orders);
    }

    @Nonnull
    private BigDecimal calOrderQuantity(
            @Nonnull BigDecimal nowQuantity,
            @Nonnull OrderSpec orderSpec,
            @Nonnull SymbolInfo symbolInfo,
            @Nonnull BigDecimal price) {
        if (OrderSide.SELL.equals(orderSpec.getOrderSide())) {
            return nowQuantity;
        } else {
            int scale = getQuantityStepScale(symbolInfo);
            return nowQuantity.divide(price, scale, RoundingMode.DOWN);
        }
    }

    @Nonnull
    private BigDecimal calNextQuantity(
            @Nonnull Order order,
            @Nonnull BigDecimal price) {
        BigDecimal orderQuantity = order.getQuantity();
        if (OrderSide.SELL.equals(order.getOrderSpec().getOrderSide())) {
            return orderQuantity.multiply(price);
        } else {
            return orderQuantity;
        }
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
            lastUpdateTime = Instant.ofEpochMilli(system.currentTimeMillis());
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