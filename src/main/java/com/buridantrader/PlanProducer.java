package com.buridantrader;

import com.buridantrader.config.TradingConfig;
import com.buridantrader.exceptions.ValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlanProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanProducer.class);

    private final TradingConfig config;
    private final TradingPathFinder tradingPathFinder;
    private final CandidateAssetProducer candidateAssetProducer;

    public PlanProducer(
            @Nonnull TradingConfig config,
            @Nonnull TradingPathFinder tradingPathFinder,
            @Nonnull CandidateAssetProducer candidateAssetProducer
    ) {
        this.config = config;
        this.tradingPathFinder = tradingPathFinder;
        this.candidateAssetProducer = candidateAssetProducer;
    }

    @Nonnull
    public TradingPlan get() throws IOException {
        TradingPlan plan = new TradingPlan();
        List<CandidateAsset> candidates = candidateAssetProducer.getCandidates();
        List<CandidateAsset> sources = candidates.stream()
                .filter(CandidateAsset::isEligibleForSource)
                .collect(Collectors.toList());
        for (CandidateAsset source : sources) {
            LOGGER.debug("Source asset: {}", source.getAsset().getCurrency());
            buildMostProfitableOrders(source, candidates)
                .forEach(plan::addOrder);
        }
        LOGGER.debug("Producing a plan with {} orders", plan.getOrders().size());
        return plan;
    }

    @Nonnull
    private List<Order> buildMostProfitableOrders(
        @Nonnull CandidateAsset source, @Nonnull List<CandidateAsset> candidates) throws IOException {
        Asset sourceAsset = source.getAsset();
        Currency sourceCurrency = sourceAsset.getCurrency();
        List<Order> mostProfitableOrders = Collections.emptyList();
        BigDecimal maxGrowthRateDiff = BigDecimal.ZERO;
        List<CandidateAsset> targets = candidates.stream()
                .filter(c -> !c.getAsset().getCurrency().equals(sourceCurrency)
                        && c.getPricePrediction().isProfitable())
                .collect(Collectors.toList());
        for (CandidateAsset target : targets) {
            Asset targetAsset = target.getAsset();

            Currency targetCurrency = targetAsset.getCurrency();
            Optional<List<Order>> optOrders;
            try {
                BigDecimal freeQuantity = source.getFreeQuantity();
                optOrders = tradingPathFinder.findPathOfOrders(
                        sourceCurrency,
                        targetCurrency,
                        freeQuantity);
            } catch (ValueException ex) {
                LOGGER.debug("Unable to find path of orders from {} to {} because: {}",
                        sourceCurrency, targetCurrency, ex.getReason());
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
                mostProfitableOrders = optOrders.get();
                maxGrowthRateDiff = growthDiff;
            }
        }
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

        BigDecimal measuringSec = new BigDecimal(config.getMeasuringSec());

        // If no convert, after time t, the value will be:
        // sourcePrice * sourceQuantity + sourceGrowthRate * t * sourceQuantity
        // If convert, after time t, the value will be:
        // sourcePrice * sourceQuantity - transactionFeeRate * sourcePrice * sourceQuantity
        // + targetGrowthRate * t * targetQuantity
        BigDecimal targetQuantity = tradingPathFinder.getOrderTargetQuantity(lastOrder);
        BigDecimal growthAfterConvert = targetGrowthRate.multiply(measuringSec)
                .multiply(targetQuantity);

        // If the "sourceGrowthRate" is negative, don't consider the transaction fee.
        if (sourceGrowthRate.signum() >= 0) {
            BigDecimal totalFee = config.getTradingFeeRate()
                    .multiply(new BigDecimal(orders.size()))
                    .multiply(source.getFreeValue());
            growthAfterConvert = growthAfterConvert.subtract(totalFee);
        }

        BigDecimal growthNoConvert = sourceGrowthRate.multiply(measuringSec)
                .multiply(source.getFreeQuantity());
        LOGGER.debug("{} -> {}. Predicted value growth with conversion: {}. "
                        + "Predicated value growth without conversion: {}",
                source.getAsset().getCurrency(),
                target.getAsset().getCurrency(),
                growthAfterConvert,
                growthNoConvert);
        return growthAfterConvert.subtract(growthNoConvert);
    }

}
