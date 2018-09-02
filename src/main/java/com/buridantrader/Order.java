package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class Order {
    private final Currency baseCurrency;
    private final Currency quoteCurrency;
    private final BigDecimal quantity;
    private final OrderSide orderSide;

    public Order(
            @Nonnull Currency baseCurrency,
            @Nonnull Currency quoteCurrency,
            @Nonnull BigDecimal quantity,
            @Nonnull OrderSide orderSide) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.quantity = quantity;
        this.orderSide = orderSide;
    }

    @Nonnull
    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    @Nonnull
    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }

    @Nonnull
    public BigDecimal getQuantity() {
        return quantity;
    }

    @Nonnull
    public OrderSide getOrderSide() {
        return orderSide;
    }
}
