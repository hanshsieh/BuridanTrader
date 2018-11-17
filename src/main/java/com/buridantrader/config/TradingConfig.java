package com.buridantrader.config;

import com.buridantrader.Currency;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public interface TradingConfig {

    @Nonnull
    BigDecimal getMinTradingValue();

    @Nonnull
    BigDecimal getTradingFeeRate();

    @Nonnull
    Currency getQuoteCurrency();

    int getMeasuringSec();

    /**
     * Gets config for the asset.
     * If an unknown currency is given, a default asset config will be returned.
     *
     * @param currency Currency of the asset.
     * @return Config for the asset.
     */
    @Nonnull
    AssetConfig getAssetConfig(@Nonnull Currency currency);
}
