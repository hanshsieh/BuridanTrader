package com.buridantrader;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

class CandidateAsset {
    private boolean eligibleForSource = true;
    private final Asset asset;
    private final PricePrediction pricePrediction;
    private final BigDecimal freeQuantity;
    private final BigDecimal freeValue;
    public CandidateAsset(
            @Nonnull Asset asset,
            @Nonnull PricePrediction pricePrediction,
            @Nonnull BigDecimal freeQuantity,
            @Nonnull BigDecimal freeValue) {
        this.asset = asset;
        this.pricePrediction = pricePrediction;
        this.freeQuantity = freeQuantity;
        this.freeValue = freeValue;
    }

    public boolean isEligibleForSource() {
        return eligibleForSource;
    }

    public void setEligibleForSource(boolean eligibleForSource) {
        this.eligibleForSource = eligibleForSource;
    }

    @Nonnull
    public Asset getAsset() {
        return asset;
    }

    @Nonnull
    public PricePrediction getPricePrediction() {
        return pricePrediction;
    }

    @Nonnull
    public BigDecimal getFreeQuantity() {
        return freeQuantity;
    }

    @Nonnull
    public BigDecimal getFreeValue() {
        return freeValue;
    }
}
