package com.buridantrader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.util.Objects;

@ThreadSafe
@Immutable
public class SymbolInfo {
    private final Symbol symbol;
    private final BigDecimal minQuantity;
    private final BigDecimal maxQuantity;
    private final BigDecimal quantityStepSize;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final BigDecimal priceTickSize;
    private final DecimalFormalizer quantityFormalizer;
    private final DecimalFormalizer priceFormalizer;

    public SymbolInfo(@Nonnull Symbol symbol,
                      @Nonnull BigDecimal minQuantity,
                      @Nonnull BigDecimal maxQuantity,
                      @Nonnull BigDecimal quantityStepSize,
                      @Nonnull BigDecimal minPrice,
                      @Nonnull BigDecimal maxPrice,
                      @Nonnull BigDecimal priceTickSize) {
        this.symbol = symbol;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.quantityStepSize = quantityStepSize;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.priceTickSize = priceTickSize;
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
