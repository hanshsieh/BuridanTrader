package com.buridantrader;

import com.buridantrader.exceptions.NoSuchPathException;
import com.buridantrader.exceptions.ValueLimitException;

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
    public BigDecimal getRelativePrice(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency,
            @Nonnull BigDecimal quantity
    ) throws IOException, ValueLimitException, NoSuchPathException {
        if (baseCurrency.equals(quoteCurrency)) {
            return quantity;
        }
        List<Order> orders;

        orders = tradingPathFinder.findPathOfOrders(
                baseCurrency,
                quoteCurrency,
                quantity);

        Order lastOrder = orders.get(orders.size() - 1);
        return tradingPathFinder.getOrderTargetQuantity(lastOrder);
    }
}
