package com.buridantrader;

import javax.annotation.Nonnull;

public class OrderSpec {
    private final Symbol symbol;
    private final OrderSide orderSide;

    public OrderSpec(
            @Nonnull Symbol symbol,
            @Nonnull OrderSide orderSide) {
        this.symbol = symbol;
        this.orderSide = orderSide;
    }

    @Nonnull
    public Symbol getSymbol() {
        return symbol;
    }

    @Nonnull
    public OrderSide getOrderSide() {
        return orderSide;
    }
}
