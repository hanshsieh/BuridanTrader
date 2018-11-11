package com.buridantrader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class ExitHandler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExitHandler.class);

    private final BuridanTrader trader;

    public ExitHandler(@Nonnull BuridanTrader trader) {
        this.trader = trader;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Shutdown signal received, releasing resources...");
            trader.stop(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            LOGGER.info("Resource released");
        } catch (InterruptedException ex) {
            LOGGER.warn("Interrupted. Not all resources are released", ex);
        }
    }
}
