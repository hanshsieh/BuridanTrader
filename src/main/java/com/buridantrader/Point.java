package com.buridantrader;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

public class Point {
    private final BigDecimal x, y;
    public Point(@Nonnull BigDecimal x, @Nonnull BigDecimal y) {
        this.x = x;
        this.y = y;
    }

    @Nonnull
    public BigDecimal getX() {
        return x;
    }

    @Nonnull
    public BigDecimal getY() {
        return y;
    }
}
