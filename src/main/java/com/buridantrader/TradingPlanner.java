package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface TradingPlanner {
    @Nonnull
    TradingPlan nextPlan() throws IOException;
}
