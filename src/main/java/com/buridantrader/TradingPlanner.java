package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TradingPlanner {

    private final PricePredictor pricePredictor;

    public TradingPlanner(@Nonnull PricePredictor pricePredictor) {
        this.pricePredictor = pricePredictor;
    }

    @Nonnull
    public TradingPlan nextPlan() throws IOException {
        // TODO
        return null;
    }
}
