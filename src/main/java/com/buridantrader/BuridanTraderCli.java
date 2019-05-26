package com.buridantrader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BuridanTraderCli {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuridanTraderCli.class);
    private final BuridanTraderFactory traderFactory;
    private final Runtime runtime;
    private BuridanTrader trader;

    public BuridanTraderCli() {
        this(new BuridanTraderFactory(), Runtime.getRuntime());
    }

    public BuridanTraderCli(
            @Nonnull BuridanTraderFactory traderFactory,
            @Nonnull Runtime runtime) {
        this.traderFactory = traderFactory;
        this.runtime = runtime;
    }

    public static void main(String[] args) throws Exception {
        BuridanTraderCli cli = new BuridanTraderCli();
        try {
            cli.run();
        } catch (Throwable ex) {
            LOGGER.error("Exception thrown", ex);
        } finally {
            cli.stop();
        }
    }

    public void run() throws IOException {
        trader = traderFactory.createTrader("conf/beta.conf");
        runtime.addShutdownHook(new ExitHandler(trader));
        trader.start();
    }

    public void stop() throws InterruptedException {
        if (trader != null) {
            LOGGER.info("Waiting for trader to stop...");
            trader.stop(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            LOGGER.info("Trader has been gracefully shutdown");
        }
    }
}
