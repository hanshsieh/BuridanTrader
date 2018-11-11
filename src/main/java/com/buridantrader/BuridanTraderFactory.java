package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BuridanTraderFactory {

    @Nonnull
    public BuridanTrader createTrader(@Nonnull String configPath) throws IOException {
        return new BuridanTrader(new TraderConfig(configPath));
    }
}
