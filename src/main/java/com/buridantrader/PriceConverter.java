package com.buridantrader;

import com.buridantrader.exceptions.ValueException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class PriceConverter {
    private final TradingPathFinder tradingPathFinder;

    public PriceConverter(
            @Nonnull TradingPathFinder tradingPathFinder) {
        this.tradingPathFinder = tradingPathFinder;
    }

    @Nonnull
    public Optional<BigDecimal> getRelativePrice(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency,
            @Nonnull BigDecimal quantity
    ) throws IOException, ValueException {
        if (baseCurrency.equals(quoteCurrency)) {
            return Optional.of(quantity);
        }
        Optional<List<Order>> optOrders = tradingPathFinder.findPathOfOrders(
                baseCurrency,
                quoteCurrency,
                quantity);

        if (!optOrders.isPresent()) {
            return Optional.empty();
        }
        List<Order> orders = optOrders.get();
        if (orders.isEmpty()) {
            return Optional.of(quantity);
        }
        Order lastOrder = orders.get(orders.size() - 1);
        return Optional.of(tradingPathFinder.getOrderTargetQuantity(lastOrder));
    }
}
