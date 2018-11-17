package com.buridantrader.config;

import com.buridantrader.Currency;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class AssetConfig {

    private final Currency currency;
    private final BigDecimal minPreferredQuantity;

    public static class Builder {

        private Currency currency;
        private BigDecimal minPreferredQuantity = BigDecimal.ZERO;

        /**
         * Sets the currency.
         *
         * @param currency Currency.
         * @return This builder.
         */
        @Nonnull
        public Builder setCurrency(@Nonnull Currency currency) {
            this.currency = currency;
            return this;
        }

        /**
         * Set the minimum preferred quantity.
         *
         * @param quantity Quantity.
         * @return This builder.
         */
        @Nonnull
        public Builder setMinPreferredQuantity(@Nonnull BigDecimal quantity) {
            this.minPreferredQuantity = quantity;
            return this;
        }

        /**
         * Builds the config instance.
         *
         * @return The config instance.
         * @throws NullPointerException If some necessary values haven't been set.
         */
        @Nonnull
        public AssetConfig build() throws NullPointerException {
            Preconditions.checkNotNull(currency, "Currency must be specified");
            return new AssetConfig(this);
        }

    }

    AssetConfig(@Nonnull Builder builder) {
        this.currency = builder.currency;
        this.minPreferredQuantity = builder.minPreferredQuantity;
    }

    @Nonnull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * The minimum preferred quantity for the asset.
     * During trading, the logic shouldn't make the quantity of the
     * asset less than this value. However, if the quantity of the asset becomes
     * less than this value for any reason, the logic won't transfer quantities from
     * other assets just to fulfill this value.
     *
     * @return Quantity;
     */
    @Nonnull
    public BigDecimal getMinPreferredQuantity() {
        return minPreferredQuantity;
    }
}
