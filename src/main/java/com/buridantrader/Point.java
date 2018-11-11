package com.buridantrader;

import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Point point = (Point) other;
        return Objects.equals(x, point.x) &&
               Objects.equals(y, point.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
