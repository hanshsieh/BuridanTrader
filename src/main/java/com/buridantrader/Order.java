package com.buridantrader;


import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }

        Order that = (Order) other;
        return orderSpec.equals(that.orderSpec)
                && quantity.equals(that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderSpec, quantity);
    }
}
