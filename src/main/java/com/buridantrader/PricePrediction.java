package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

public class PricePrediction {
    private final BigDecimal growthPerSec;

    public PricePrediction(@Nonnull BigDecimal growthPerSec) {
        this.growthPerSec = growthPerSec;
    }

    @Nonnull
    public BigDecimal getGrowthPerSec() {
        return growthPerSec;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }

        PricePrediction that = (PricePrediction) other;
        return growthPerSec.equals(that.growthPerSec);
    }

    @Override
    public int hashCode() {
        return growthPerSec.hashCode();
    }
}
