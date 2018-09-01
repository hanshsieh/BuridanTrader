package com.buridantrader;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class Transaction {
    private final Currency sourceCurrency;
    private final Currency targetCurrency;
    private final BigDecimal quantity;

    public Transaction(
            @Nonnull Currency sourceCurrency,
            @Nonnull Currency targetCurrency,
            @Nonnull BigDecimal quantity) {
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.quantity = quantity;
    }

    @Nonnull
    public Currency getSourceCurrency() {
        return sourceCurrency;
    }

    @Nonnull
    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    @Nonnull
    public BigDecimal getQuantity() {
        return quantity;
    }
}
