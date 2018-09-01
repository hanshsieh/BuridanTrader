package com.buridantrader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradingPlan {
    private final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(@Nonnull Transaction transaction) {
        transactions.add(transaction);
    }

    @Nonnull
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
