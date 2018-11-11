package com.buridantrader;

import com.buridantrader.exceptions.ValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class PlanProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanProducer.class);

    // TODO Make it configurable
    private static final BigDecimal MIN_TRADING_QUOTE_QUANTITY = new BigDecimal("0.05");
    private static final Currency QUOTE_CURRENCY = new Currency("USDT");
    private static final BigDecimal TRADING_FEE_RATE = new BigDecimal("0.001");
    private static final Set<Currency> EXCLUDE_CURRENCIES = Collections.singleton(new Currency("BNB"));
    private static final BigDecimal MEASURE_PERIOD_SEC = new BigDecimal(60 * 15);
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
        List<CandidateAsset> candidates = buildAllCandidates();
        List<CandidateAsset> sources = findSourceCandidates(candidates);
        for (CandidateAsset source : sources) {
            LOGGER.debug("Source asset: {}", source.getAsset().getCurrency());
            buildMostProfitableOrders(source, candidates)
                .ifPresent((orders) -> orders.forEach(plan::addOrder));
        }
        LOGGER.debug("Producing a plan with {} orders", plan.getOrders().size());
        return plan;
    }

    @Nonnull
    private Optional<List<Order>> buildMostProfitableOrders(
        @Nonnull CandidateAsset source, @Nonnull List<CandidateAsset> candidates) throws IOException {
        Asset sourceAsset = source.getAsset();
        Optional<List<Order>> mostProfitableOrders = Optional.empty();
        BigDecimal maxGrowthRateDiff = BigDecimal.ZERO;
        for (CandidateAsset target : candidates) {
            Asset targetAsset = target.getAsset();

            // TODO Should allow more fine-grain control of how to keep the quantity of
            // BNB
            if (EXCLUDE_CURRENCIES.contains(targetAsset.getCurrency())) {
                continue;
            }
            Currency sourceCurrency = sourceAsset.getCurrency();
            Currency targetCurrency = targetAsset.getCurrency();
            if (sourceCurrency.equals(targetCurrency)) {
                continue;
            }
            Optional<List<Order>> optOrders;
            try {
                BigDecimal quantity = sourceAsset.getBalance();
                optOrders = tradingPathFinder.findPathOfOrders(
                        sourceCurrency,
                        targetCurrency,
                        quantity);
            } catch (ValueException ex) {
                continue;
            }
            if (!optOrders.isPresent() || optOrders.get().isEmpty()) {
                continue;
            }
            BigDecimal growthDiff = calGrowthRateDiff(source, target, optOrders.get());
            LOGGER.debug("Growth rate diff {} relative to {} is {}",
                    target.getAsset().getCurrency(),
                    source.getAsset().getCurrency(),
                    growthDiff);
            if (growthDiff.compareTo(maxGrowthRateDiff) > 0) {
                mostProfitableOrders = optOrders;
                maxGrowthRateDiff = growthDiff;
            }
        }
        LOGGER.debug("Max growth rate diff is {}", maxGrowthRateDiff);
        return mostProfitableOrders;
    }

    @Nonnull
    private BigDecimal calGrowthRateDiff(
            @Nonnull CandidateAsset source,
            @Nonnull CandidateAsset target,
            @Nonnull List<Order> orders) throws IOException {

        BigDecimal sourceGrowthRate = source.getPricePrediction().getGrowthPerSec();
        BigDecimal targetGrowthRate = target.getPricePrediction().getGrowthPerSec();

        Order lastOrder = orders.get(orders.size() - 1);

        // If no convert, after time t, the value will be:
        // sourcePrice * sourceQuantity + sourceGrowthRate * t * sourceQuantity
        // If convert, after time t, the value will be:
        // sourcePrice * sourceQuantity - transactionFeeRate * sourcePrice * sourceQuantity
        // + targetGrowthRate * t * targetQuantity
        BigDecimal targetQuantity = tradingPathFinder.getOrderTargetQuantity(lastOrder);
        BigDecimal growthAfterConvert = targetGrowthRate.multiply(MEASURE_PERIOD_SEC)
                .multiply(targetQuantity);

        // If the "sourceGrowthRate" is negative, don't consider the transaction fee.
        if (sourceGrowthRate.signum() >= 0) {
            BigDecimal totalFee = TRADING_FEE_RATE
                    .multiply(new BigDecimal(orders.size()))
                    .multiply(source.getFreeValue());
            growthAfterConvert = growthAfterConvert.subtract(totalFee);
        }

        BigDecimal growthNoConvert = sourceGrowthRate.multiply(MEASURE_PERIOD_SEC)
                .multiply(source.getFreeQuantity());
        return growthAfterConvert.subtract(growthNoConvert);
    }

    @Nonnull
    private List<CandidateAsset> findSourceCandidates(@Nonnull List<CandidateAsset> candidates) {
        return candidates.stream()

                // TODO Should allow more fine-grain control of how to keep the quantity of
                // BNB
                .filter((c) -> !EXCLUDE_CURRENCIES.contains(c.getAsset().getCurrency())
                        && c.getFreeValue().compareTo(MIN_TRADING_QUOTE_QUANTITY) >= 0)
                .sorted(Comparator.comparing(e -> e.getPricePrediction().getGrowthPerSec()))
                .collect(Collectors.toList());
    }

    @Nonnull
    private List<CandidateAsset> buildAllCandidates() throws IOException {
        List<CandidateAsset> candidates = new ArrayList<>();
        // FIXME Should look at all currencies, not asset
        for (Asset asset : assetViewer.getAccountAssets()) {

            BigDecimal freeValue;

            // TODO Allow the "free quantity" to be limited by config
            BigDecimal freeQuantity = asset.getBalance();
            if (asset.getCurrency().getName().equals("USDT")
                    && freeQuantity.compareTo(new BigDecimal(10)) > 0) {
                freeQuantity = new BigDecimal(10);
            }
            try {
                Optional<BigDecimal> optValue = priceConverter.getRelativePrice(
                        asset.getCurrency(), QUOTE_CURRENCY, freeQuantity);
                if (!optValue.isPresent()) {
                    LOGGER.info("Unable to get the price of {} relative to {}. Skipping the asset",
                            asset.getCurrency(), QUOTE_CURRENCY);
                    continue;
                }
                freeValue = optValue.get();
            } catch (ValueException ex) {
                LOGGER.debug("The balance of asset {} is ignored because the balance is {}",
                        asset.getCurrency(), ex.getReason());
                freeValue = BigDecimal.ZERO;
            }

            PricePrediction prediction;
            try {
                prediction = pricePredictor.getPrediction(asset.getCurrency(), QUOTE_CURRENCY);
            } catch (IllegalArgumentException ex) {
                LOGGER.info("Unable to get prediction for {} relative to {}. Skipping it",
                        asset.getCurrency(),
                        QUOTE_CURRENCY);
                continue;
            }

            LOGGER.debug("For asset {}, growth per sec: {}, free quantity: {}, free value relative to {}: {}",
                    asset.getCurrency(),
                    prediction.getGrowthPerSec(),
                    freeQuantity,
                    QUOTE_CURRENCY,
                    freeValue);
            candidates.add(new CandidateAsset(asset, prediction, freeQuantity, freeValue));
        }
        return candidates;
    }
}
