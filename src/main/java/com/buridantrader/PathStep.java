package com.buridantrader;

import javax.annotation.Nonnull;

public class PathStep {
    private Symbol symbolToNext;
    private int length;

    public PathStep(@Nonnull Symbol symbolToNext, int length) {
        this.symbolToNext = symbolToNext;
        this.length = length;
    }

    @Nonnull
    public Symbol getSymbolToNext() {
        return symbolToNext;
    }

    public void setSymbolToNext(@Nonnull Symbol symbolToNext) {
        this.symbolToNext = symbolToNext;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Gets the next currency on the path.
     *
     * @param nowCurrency The current currency on the path.
     * @return Next currency.
     *
     * @throws IllegalArgumentException The given currency isn't base nor quote currency of the symbol.
     */
    @Nonnull
    public Currency getNextCurrency(@Nonnull Currency nowCurrency) throws IllegalArgumentException {
        Symbol symbol = getSymbolToNext();
        if (nowCurrency.equals(symbol.getBaseCurrency())) {
            return symbol.getQuoteCurrency();
        } else if (nowCurrency.equals(symbol.getQuoteCurrency())) {
            return symbol.getBaseCurrency();
        } else {
            throw new IllegalArgumentException("The given currency " + nowCurrency
                    + " isn't base nor quote currency of the symbol");
        }
    }
}
