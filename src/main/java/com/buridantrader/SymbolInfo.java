package com.buridantrader;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.util.Objects;

@ThreadSafe
@Immutable
public class SymbolInfo {

    public static class Builder {
        private Symbol symbol;
        private BigDecimal minQuantity;
        private BigDecimal maxQuantity;
        private BigDecimal quantityStepSize;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private BigDecimal priceTickSize;

        @Nonnull
        public Builder setSymbol(@Nonnull Symbol symbol) {
            this.symbol = symbol;
            return this;
        }

        @Nonnull
        public Builder setMinQuantity(@Nonnull BigDecimal minQuantity) {
            this.minQuantity = minQuantity;
            return this;
        }

        @Nonnull
        public Builder setMaxQuantity(@Nonnull BigDecimal maxQuantity) {
            this.maxQuantity = maxQuantity;
            return this;
        }

        @Nonnull
        public Builder setQuantityStepSize(BigDecimal quantityStepSize) {
            this.quantityStepSize = quantityStepSize;
            return this;
        }

        @Nonnull
        public Builder setMinPrice(@Nonnull BigDecimal minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        @Nonnull
        public Builder setMaxPrice(@Nonnull BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        @Nonnull
        public Builder setPriceTickSize(@Nonnull BigDecimal priceTickSize) {
            this.priceTickSize = priceTickSize;
            return this;
        }

        @Nonnull
        public SymbolInfo build() {
            Preconditions.checkNotNull(symbol, "Symbol cannot be null");
            Preconditions.checkNotNull(minQuantity, "Min quantity cannot be null");
            Preconditions.checkNotNull(maxQuantity, "Max quantity cannot be null");
            Preconditions.checkNotNull(quantityStepSize, "Quantity step size cannot be null");
            Preconditions.checkNotNull(minPrice, "Min price cannot be null");
            Preconditions.checkNotNull(maxPrice, "Max price cannot be null");
            Preconditions.checkNotNull(priceTickSize, "Price tick size cannot be null");
            return new SymbolInfo(this);
        }
    }

    private final Symbol symbol;
    private final BigDecimal minQuantity;
    private final BigDecimal maxQuantity;
    private final BigDecimal quantityStepSize;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final BigDecimal priceTickSize;
    private final DecimalFormalizer quantityFormalizer;
    private final DecimalFormalizer priceFormalizer;

    SymbolInfo(@Nonnull Builder builder) {
        this.symbol = builder.symbol;
        this.minQuantity = builder.minQuantity;
        this.maxQuantity = builder.maxQuantity;
        this.quantityStepSize = builder.quantityStepSize;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.priceTickSize = builder.priceTickSize;
        this.quantityFormalizer = new DecimalFormalizer(
                minQuantity,
                maxQuantity,
                quantityStepSize
        );
        this.priceFormalizer = new DecimalFormalizer(
                minPrice,
                maxPrice,
                priceTickSize
        );
    }

    @Nonnull
    public Symbol getSymbol() {
        return symbol;
    }

    @Nonnull
    public BigDecimal getMinQuantity() {
        return minQuantity;
    }

    @Nonnull
    public BigDecimal getMaxQuantity() {
        return maxQuantity;
    }

    @Nonnull
    public BigDecimal getQuantityStepSize() {
        return quantityStepSize;
    }

    @Nonnull
    public BigDecimal getMinPrice() {
        return minPrice;
    }

    @Nonnull
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    @Nonnull
    public BigDecimal getPriceTickSize() {
        return priceTickSize;
    }

    @Nonnull
    public DecimalFormalizer getQuantityFormalizer() {
        return quantityFormalizer;
    }

    @Nonnull
    public DecimalFormalizer getPriceFormalizer() {
        return priceFormalizer;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SymbolInfo)) {
            return false;
        }
        SymbolInfo that = (SymbolInfo) other;
        return symbol.equals(that.symbol) &&
                minQuantity.equals(that.minQuantity) &&
                maxQuantity.equals(that.maxQuantity) &&
                quantityStepSize.equals(that.quantityStepSize) &&
                minPrice.equals(that.minPrice) &&
                maxPrice.equals(that.maxPrice) &&
                priceTickSize.equals(that.priceTickSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                symbol,
                minQuantity,
                maxQuantity,
                quantityStepSize,
                minPrice,
                maxPrice,
                priceTickSize);
    }
}
