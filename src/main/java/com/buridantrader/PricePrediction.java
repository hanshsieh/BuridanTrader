package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Objects;

public class PricePrediction {
    private boolean profitable;
    private final BigDecimal growthPerSec;

    public PricePrediction(@Nonnull boolean profitable, @Nonnull BigDecimal growthPerSec) {
        this.profitable = profitable;
        this.growthPerSec = growthPerSec;
    }

    public boolean isProfitable() {
        return profitable;
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
        return profitable == that.profitable
                && growthPerSec.equals(that.growthPerSec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profitable, growthPerSec);
    }
}
