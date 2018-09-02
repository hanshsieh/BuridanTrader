package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class TradingResolver {

    @Nonnull
    public List<Order> findTradingPath(
            @Nonnull Currency sourceCurrency,
            @Nonnull Currency targetCurrency,
            @Nonnull BigDecimal quantity) throws IOException {
        // TODO
        return null;
    }

}
