package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

@ThreadSafe
@Immutable
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }

        OrderSpec that = (OrderSpec) other;
        return symbol.equals(that.symbol)
                && orderSide.equals(that.orderSide);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, orderSide);
    }
}
