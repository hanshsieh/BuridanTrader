package com.buridantrader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradingPlan {
    private final List<Order> orders = new ArrayList<>();

    public void addOrder(@Nonnull Order order) {
        orders.add(order);
    }

    @Nonnull
    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }
}
