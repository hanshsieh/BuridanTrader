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
    private final DecimalFormalizer quantityFormalizer;

    public SymbolInfo(@Nonnull Symbol symbol,
                      @Nonnull BigDecimal minQuantity,
                      @Nonnull BigDecimal maxQuantity,
                      @Nonnull BigDecimal quantityStepSize) {
        this.symbol = symbol;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.quantityStepSize = quantityStepSize;
        this.quantityFormalizer = new DecimalFormalizer(
                minQuantity,
                maxQuantity,
                quantityStepSize
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
                quantityStepSize.equals(that.quantityStepSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                symbol,
                minQuantity,
                maxQuantity,
                quantityStepSize);
    }
}
