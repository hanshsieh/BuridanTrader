package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Currency {
    private final String name;
    public Currency(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (!(other instanceof Currency)) {
            return false;
        }
        Currency that = (Currency) other;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
