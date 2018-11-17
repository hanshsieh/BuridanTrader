package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.impl.BinanceApiRestClientImpl;
import com.buridantrader.config.TradingConfig;
import com.buridantrader.config.TradingConfigImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BuridanTrader {

    private final PlanConsumer planConsumer;

    public BuridanTrader(@Nonnull TraderConfig config) throws IOException {

        BinanceApiRestClient client = new BinanceApiRestClientImpl(
                config.getApiKey(),
                config.getApiSecret()
        );
        PlanWorkerFactory planWorkerFactory = new PlanWorkerFactory(client);
        SymbolPriceViewer symbolPriceViewer = new SymbolPriceViewer(client);
        SymbolFetcher symbolFetcher = new SymbolFetcher(client);
        AssetViewer assetViewer = new AssetViewer(client);
        SymbolService symbolService = new SymbolService(symbolFetcher);
        SymbolPriceService symbolPriceService = new SymbolPriceService(client, symbolService);
        TradingPathFinder tradingPathFinder = new TradingPathFinder(symbolService, symbolPriceService);
        CurrencyPriceViewer currencyPriceViewer = new CurrencyPriceViewer(symbolPriceViewer, tradingPathFinder);
        PricePredictor pricePredictor = new PricePredictor(currencyPriceViewer);
        PriceConverter priceConverter = new PriceConverter(tradingPathFinder);
        CandidateAssetProducer candidateAssetProducer = new CandidateAssetProducer(
                config.getTradingConfig(), assetViewer, pricePredictor, priceConverter);
        PlanProducer planProducer = new PlanProducer(config.getTradingConfig(), tradingPathFinder, candidateAssetProducer);
        TradingPlanner tradingPlanner = new TradingPlanner(planProducer);
        this.planConsumer = new PlanConsumer(tradingPlanner, planWorkerFactory);
    }

    public BuridanTrader(@Nonnull PlanConsumer planConsumer) {
        this.planConsumer = planConsumer;
    }

    public void start() {
        planConsumer.start();
    }

    public void stop(long timeout, @Nonnull TimeUnit timeUnit) throws InterruptedException {
        planConsumer.stop(timeout, timeUnit);
    }
}
