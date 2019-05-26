package com.buridantrader;

import com.buridantrader.exceptions.NoSuchPathException;
import com.buridantrader.exceptions.ValueLimitException;
import com.buridantrader.services.symbol.SymbolService;
import com.buridantrader.services.symbol.SymbolPriceService;
import com.buridantrader.services.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TradingPathFinder.class);
    private final ShortestPathsResolver shortestPathsResolver;
    private final SymbolPriceService symbolPriceService;
    private final SymbolService symbolService;
    private final SystemService system;

    // Be careful the referenced instance may be changed by other threads
    private Instant lastUpdateTime;
    private TradingPaths tradingPaths;

    public TradingPathFinder(
            @Nonnull SymbolService symbolService,
            @Nonnull SymbolPriceService symbolPriceService) {
        this(symbolService,
                symbolPriceService,
            new ShortestPathsResolver(),
            new SystemService());
    }

    public TradingPathFinder(
            @Nonnull SymbolService symbolService,
            @Nonnull SymbolPriceService symbolPriceService,
            @Nonnull ShortestPathsResolver shortestPathsResolver,
            @Nonnull SystemService system) {
        this.symbolPriceService = symbolPriceService;
        this.symbolService = symbolService;
        this.shortestPathsResolver = shortestPathsResolver;
        this.system = system;
    }

    @Nonnull
    public List<OrderSpec> findPathOfOrderSpecs(
            @Nonnull final Currency sourceCurrency,
            @Nonnull final Currency targetCurrency
    ) throws IOException, NoSuchPathException {
        checkFreshness();
        Optional<PathStep> optPathEntry = tradingPaths.getNextStep(sourceCurrency, targetCurrency);
        List<OrderSpec> path = new ArrayList<>();
        Currency nowCurrency = sourceCurrency;
        do {
            if (!optPathEntry.isPresent()) {
                throw new NoSuchPathException("No trading path from " +sourceCurrency + " to " + targetCurrency);
            }
            PathStep pathStep = optPathEntry.get();
            path.add(createOrderSpec(nowCurrency, pathStep.getSymbolToNext()));
            Currency nextCurrency = pathStep.getNextCurrency(nowCurrency);
            optPathEntry = tradingPaths.getNextStep(nextCurrency, targetCurrency);
            nowCurrency = nextCurrency;
        } while (!nowCurrency.equals(targetCurrency));
        return path;
    }

    @Nonnull
    public List<Order> findPathOfOrders(
            @Nonnull final Currency sourceCurrency,
            @Nonnull final Currency targetCurrency,
            @Nonnull final BigDecimal quantity) throws IOException, ValueLimitException, NoSuchPathException {
        List<OrderSpec> orderSpecs = findPathOfOrderSpecs(sourceCurrency, targetCurrency);
        List<Order> orders = new ArrayList<>(orderSpecs.size());
        BigDecimal nowQuantity = quantity;
        for (OrderSpec orderSpec : orderSpecs) {
            BigDecimal price = getPriceForSymbol(orderSpec.getSymbol());
            SymbolInfo symbolInfo = getSymbolInfo(orderSpec.getSymbol());
            BigDecimal orderQuantity = calOrderQuantity(nowQuantity, orderSpec, symbolInfo, price);

            BigDecimal formalizedQuantity = symbolInfo.getQuantityFormalizer()
                    .formalize(orderQuantity, RoundingMode.DOWN);
            Order order = new Order(orderSpec, formalizedQuantity);

            orders.add(order);

            nowQuantity = calTargetQuantity(order, symbolInfo, price);
        }
        return orders;
    }

    @Nonnull
    public BigDecimal getOrderTargetQuantity(@Nonnull Order order) throws IOException {
        if (OrderSide.SELL.equals(order.getOrderSpec().getOrderSide())) {
            return calOrderQuoteQuantity(order, RoundingMode.DOWN);
        } else {
            return order.getQuantity();
        }
    }

    @Nonnull
    public BigDecimal getOrderSourceQuantity(@Nonnull Order order) throws IOException {
        if (OrderSide.BUY.equals(order.getOrderSpec().getOrderSide())) {
            return calOrderQuoteQuantity(order, RoundingMode.UP);
        } else {
            return order.getQuantity();
        }
    }

    @Nonnull
    private BigDecimal calOrderQuoteQuantity(
            @Nonnull Order order, @Nonnull RoundingMode roundingMode) throws IOException {
        BigDecimal orderQuantity = order.getQuantity();
        OrderSpec orderSpec = order.getOrderSpec();
        SymbolInfo symbolInfo = getSymbolInfo(orderSpec.getSymbol());
        BigDecimal price = getPriceForSymbol(orderSpec.getSymbol());
        BigDecimal formalizedPrice = symbolInfo.getPriceFormalizer()
                .formalize(price, roundingMode);
        return orderQuantity.multiply(formalizedPrice);
    }

    @Nonnull
    private BigDecimal calOrderQuantity(
            @Nonnull BigDecimal sourceQuantity,
            @Nonnull OrderSpec orderSpec,
            @Nonnull SymbolInfo symbolInfo,
            @Nonnull BigDecimal price) {
        if (OrderSide.SELL.equals(orderSpec.getOrderSide())) {
            return sourceQuantity;
        } else {
            int scale = getQuantityStepScale(symbolInfo);
            BigDecimal formalizedPrice = symbolInfo.getPriceFormalizer()
                    .formalize(price, RoundingMode.UP);
            BigDecimal orderQuantity = sourceQuantity.divide(formalizedPrice, scale, RoundingMode.DOWN);
            LOGGER.debug("Calculation for order quantity. symbol: {}, price: {}, orderSide: {}, "
                            + "sourceQuantity: {}, formalizedPrice: {}, orderQuantity: {}",
                    symbolInfo.getSymbol(),
                    price,
                    orderSpec.getOrderSide(),
                    sourceQuantity,
                    formalizedPrice,
                    orderQuantity);
            return orderQuantity;
        }
    }

    @Nonnull
    private BigDecimal calTargetQuantity(
            @Nonnull Order order,
            @Nonnull SymbolInfo symbolInfo,
            @Nonnull BigDecimal price) {
        BigDecimal orderQuantity = order.getQuantity();
        if (OrderSide.SELL.equals(order.getOrderSpec().getOrderSide())) {
            BigDecimal formalizedPrice = symbolInfo.getPriceFormalizer()
                    .formalize(price, RoundingMode.DOWN);
            return orderQuantity.multiply(formalizedPrice);
        } else {
            return orderQuantity;
        }
    }

    @Nonnull
    private SymbolInfo getSymbolInfo(@Nonnull Symbol symbol) throws IOException {
        return symbolService.getSymbolInfo(symbol)
                .orElseThrow(() -> new IOException("Fail to get symbol info of " + symbol));
    }

    @Nonnull
    private BigDecimal getPriceForSymbol(@Nonnull Symbol symbol) throws IOException {
        return symbolPriceService.getPrice(symbol)
                .orElseThrow(() -> new IOException("Fail to get current price of symbol " + symbol));
    }

    private synchronized void checkFreshness() throws IOException {
        if (lastUpdateTime == null || symbolService.isUpdatedSince(lastUpdateTime)) {
            tradingPaths = shortestPathsResolver.resolveAllShortestPaths(
                    symbolService.getAllSymbolInfos().stream()
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