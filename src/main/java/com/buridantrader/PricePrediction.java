package com.buridantrader;

import javax.annotation.Nonnull;
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
}
