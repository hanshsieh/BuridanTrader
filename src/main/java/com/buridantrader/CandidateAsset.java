package com.buridantrader;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

class CandidateAsset {
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
