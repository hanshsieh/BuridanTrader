package com.buridantrader.services.binance;

import com.binance.api.client.domain.market.Candlestick;

import javax.annotation.Nonnull;

public class ModelConverter {
    public CandlestickModel candlestickToModel(@Nonnull String symbol, @Nonnull Candlestick candlestick) {
        CandlestickModel model = new CandlestickModel();
        model.setSymbol(symbol);
        model.setOpenTime(candlestick.getOpenTime());
        model.setCloseTime(candlestick.getCloseTime());
        model.setOpen(candlestick.getOpen());
        model.setClose(candlestick.getClose());
        model.setHigh(candlestick.getHigh());
        model.setLow(candlestick.getLow());
        model.setVolume(candlestick.getVolume());
        model.setQuoteAssetVolume(candlestick.getQuoteAssetVolume());
        model.setNumberOfTrades(candlestick.getNumberOfTrades());
        model.setTakerBuyBaseAssetVolume(candlestick.getTakerBuyBaseAssetVolume());
        model.setQuoteAssetVolume(candlestick.getTakerBuyQuoteAssetVolume());
        return model;
    }
}
