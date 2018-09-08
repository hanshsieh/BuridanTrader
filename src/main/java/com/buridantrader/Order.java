package com.buridantrader;

import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;

@ThreadSafe
@Immutable
public class Order {
    private final OrderSpec orderSpec;
    private final BigDecimal quantity;

    public Order(
            @Nonnull OrderSpec orderSpec,
            @Nonnull BigDecimal quantity) {
        this.orderSpec = orderSpec;
        this.quantity = quantity;
    }

    @Nonnull
    public BigDecimal getQuantity() {
        return quantity;
    }

    @Nonnull
    public OrderSpec getOrderSpec() {
        return orderSpec;
    }
}
