package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TradingPlannerImpl implements TradingPlanner {
    private final PricePredictor pricePredictor;

    public TradingPlannerImpl(@Nonnull PricePredictor pricePredictor) {
        this.pricePredictor = pricePredictor;
    }

    @Nonnull
    @Override
    public TradingPlan nextPlan() throws IOException {
        // TODO
        return null;
    }

}
