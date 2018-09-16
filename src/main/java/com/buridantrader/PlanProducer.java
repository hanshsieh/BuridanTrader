package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class PlanProducer {

    private static class Candidate {
        public final Asset asset;
        public final PricePrediction pricePrediction;
        public final BigDecimal value;
        public Candidate(
                @Nonnull Asset asset,
                @Nonnull PricePrediction pricePrediction,
                @Nonnull BigDecimal value) {
            this.asset = asset;
            this.pricePrediction = pricePrediction;
            this.value = value;
        }
    }

    // TODO Make it configurable
    private static final BigDecimal MIN_TRADING_QUOTE_QUANTITY = new BigDecimal("0.05");
    private static final Currency QUOTE_CURRENCY = new Currency("USDT");
    private static final BigDecimal TRADING_FEE_RATE = new BigDecimal("0.001");
    private static final Set<Currency> EXCLUDE_CURRENCIES = Collections.singleton(new Currency("BNB"));
    private final PricePredictor pricePredictor;
    private final AssetViewer assetViewer;
    private final TradingPathFinder tradingPathFinder;
    private final PriceConverter priceConverter;

    public PlanProducer(
            @Nonnull PricePredictor pricePredictor,
            @Nonnull AssetViewer assetViewer,
            @Nonnull TradingPathFinder tradingPathFinder,
            @Nonnull PriceConverter priceConverter
    ) {
        this.pricePredictor = pricePredictor;
        this.assetViewer = assetViewer;
        this.tradingPathFinder = tradingPathFinder;
        this.priceConverter = priceConverter;
    }

    @Nonnull
    public TradingPlan get() throws IOException {
        TradingPlan plan = new TradingPlan();
        List<Candidate> candidates = buildAllCandidates();
        List<Candidate> sources = findSourceCandidates(candidates);
        for (Candidate source : sources) {
            buildMostProfitableOrders(source, candidates)
                .ifPresent((orders) -> orders.forEach(plan::addOrder));
        }
        return plan;
    }

    @Nonnull
    private Optional<List<Order>> buildMostProfitableOrders(
            @Nonnull Candidate source, @Nonnull List<Candidate> candidates) throws IOException {
        Asset sourceAsset = source.asset;
        Optional<List<Order>> mostProfitableOrders = Optional.empty();
        BigDecimal maxGrowthRateDiff = BigDecimal.ZERO;
        for (Candidate target : candidates) {
            Asset targetAsset = target.asset;
            Currency sourceCurrency = sourceAsset.getCurrency();
            Currency targetCurrency = targetAsset.getCurrency();
            if (sourceCurrency.equals(targetCurrency)) {
                continue;
            }
            BigDecimal sourceGrowthRate = source.pricePrediction.getGrowthPerSec();
            BigDecimal targetGrowthRate = target.pricePrediction.getGrowthPerSec();
            if (sourceGrowthRate.compareTo(targetGrowthRate) <= 0) {
                continue;
            }
            Optional<List<Order>> orders = tradingPathFinder.findPathOfOrders(
                    sourceCurrency,
                    targetCurrency,
                    sourceAsset.getBalance());
            if (!orders.isPresent()) {
                continue;
            }
            // If no convert, after time t, the value will be:
            // sourcePrice * sourceQuantity * (1 + sourceGrowthRate) * t
            // If convert, after time t, the value will be:
            // sourcePrice * sourceQuantity * (1 - transactionFeeRate) * (1 + targetGrowthRate) * t
            BigDecimal totalFeeRate = TRADING_FEE_RATE.multiply(new BigDecimal(orders.get().size()));
            BigDecimal growthRateAfterConvert = BigDecimal.ONE
                    .add(targetGrowthRate)
                    .multiply(BigDecimal.ONE.subtract(totalFeeRate));
            BigDecimal growthRateDiff = growthRateAfterConvert.subtract(BigDecimal.ONE.add(sourceGrowthRate));
            if (growthRateDiff.compareTo(maxGrowthRateDiff) > 0) {
                mostProfitableOrders = orders;
                maxGrowthRateDiff = growthRateDiff;
            }
        }
        return mostProfitableOrders;
    }

    @Nonnull
    private List<Candidate> findSourceCandidates(@Nonnull List<Candidate> candidates) {
        return candidates.stream()
                .filter((c) -> c.value.compareTo(MIN_TRADING_QUOTE_QUANTITY) >= 0)
                .sorted(Comparator.comparing(e -> e.pricePrediction.getGrowthPerSec()))
                .collect(Collectors.toList());
    }

    @Nonnull
    private List<Candidate> buildAllCandidates() throws IOException {
        List<Candidate> candidates = new ArrayList<>();
        for (Asset asset : assetViewer.getAccountAssets()) {

            // TODO Should allow more fine-grain control of how to keep the quantity of
            // BNB
            if (EXCLUDE_CURRENCIES.contains(asset.getCurrency())) {
                continue;
            }
            Optional<PricePrediction> prediction = pricePredictor.getPrediction(asset.getCurrency(), QUOTE_CURRENCY);
            if (!prediction.isPresent()) {
                continue;
            }
            Optional<BigDecimal> value = priceConverter.getRelativePrice(
                    asset.getCurrency(), QUOTE_CURRENCY, asset.getBalance());
            if (!value.isPresent()) {
                continue;
            }
            candidates.add(new Candidate(asset, prediction.get(), value.get()));
        }
        return candidates;
    }
}
