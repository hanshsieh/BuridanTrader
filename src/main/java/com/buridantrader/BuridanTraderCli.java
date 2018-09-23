package com.buridantrader;

public class BuridanTraderCli {
    public static void main(String[] args) throws Exception {
        TraderConfig config = new TraderConfig("conf/beta.conf");
        BuridanTrader trader = new BuridanTrader(config);
        Runtime.getRuntime().addShutdownHook(new ExitHandler(trader));
        trader.start();
    }
}
