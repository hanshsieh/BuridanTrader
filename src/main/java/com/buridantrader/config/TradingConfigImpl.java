package com.buridantrader.config;

import com.buridantrader.Currency;
import com.typesafe.config.Config;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class TradingConfigImpl implements TradingConfig {

    private final BigDecimal minTradingValue;
    private final BigDecimal tradingFeeRate;
    private final Currency quoteCurrency;
    private final int measuringSec;
    private final Map<Currency, AssetConfig> assets;

    public TradingConfigImpl(@Nonnull Config config) {
        this.assets = new HashMap<>();
        for (Config rawAssetConfig : config.getConfigList("assets")) {
            AssetConfig assetConfig = buildAssetConfig(rawAssetConfig);
            assets.put(assetConfig.getCurrency(), assetConfig);
        }
        this.minTradingValue = new BigDecimal(config.getString("minTradingValue"));
        this.quoteCurrency = new Currency(config.getString("quoteCurrency"));
        this.tradingFeeRate = new BigDecimal(config.getString("tradingFeeRate"));
        this.measuringSec = config.getInt("measuringPeriodSec");
    }

    @Nonnull
    private static AssetConfig buildAssetConfig(@Nonnull Config rawAssetConfig) {
        AssetConfig.Builder builder = new AssetConfig.Builder()
                .setCurrency(new Currency(rawAssetConfig.getString("currency")));
        if (rawAssetConfig.hasPath("minPreferredQuantity")) {
            builder.setMinPreferredQuantity(new BigDecimal(rawAssetConfig.getString("minPreferredQuantity")));
        }
        return builder.build();
    }

    @Nonnull
    @Override
    public BigDecimal getMinTradingValue() {
        return minTradingValue;
    }

    @Nonnull
    @Override
    public BigDecimal getTradingFeeRate() {
        return tradingFeeRate;
    }

    @Nonnull
    @Override
    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }

    @Override
    public int getMeasuringSec() {
        return measuringSec;
    }

    @Nonnull
    @Override
    public AssetConfig getAssetConfig(@Nonnull Currency currency) {
        AssetConfig assetConfig = assets.get(currency);
        if (assetConfig == null) {
            return new AssetConfig.Builder()
                    .setCurrency(currency)
                    .build();
        } else {
            return assetConfig;
        }
    }
}
