package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SymbolFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SymbolFetcher.class);
    private final BinanceApiRestClient client;

    public SymbolFetcher(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    @Nonnull
    public List<SymbolInfo> getSymbolInfos() throws IOException {
        List<SymbolInfo> symbolInfos = new ArrayList<>();
        ExchangeInfo exchangeInfo;
        try {
            exchangeInfo = client.getExchangeInfo();
        } catch (Exception ex) {
            throw new IOException("Fail to get exchange information", ex);
        }
        exchangeInfo.getSymbols().forEach((symbolInfo ->
                buildSymbolInfo(symbolInfo).ifPresent(symbolInfos::add)));
        return symbolInfos;
    }

    @Nonnull
    private Optional<SymbolInfo> buildSymbolInfo(
            @Nonnull com.binance.api.client.domain.general.SymbolInfo symbolInfo) {

        if (!SymbolStatus.TRADING.equals(symbolInfo.getStatus())) {
            LOGGER.debug("Symbol {} not in TRADING status. Skip it.",
                    symbolInfo.getSymbol());
            return Optional.empty();
        }

        Optional<SymbolFilter> optLotSizeFilter = getSymbolFilter(symbolInfo, FilterType.LOT_SIZE);
        if (!optLotSizeFilter.isPresent()) {
            LOGGER.debug("Symbol {} doesn't have LOT_SIZE filter. Skip it.",
                    symbolInfo.getSymbol());
            return Optional.empty();
        }
        SymbolFilter lotSizeFilter = optLotSizeFilter.get();

        BigDecimal minQuantity = new BigDecimal(lotSizeFilter.getMinQty());
        BigDecimal maxQuantity = new BigDecimal(lotSizeFilter.getMaxQty());
        BigDecimal quantityStepSize = new BigDecimal(lotSizeFilter.getStepSize());

        Optional<SymbolFilter> optPriceFilter = getSymbolFilter(symbolInfo, FilterType.PRICE_FILTER);
        if (!optPriceFilter.isPresent()) {
            LOGGER.debug("Symbol {} doesn't have PRICE_FILTER filter. Skip it.",
                    symbolInfo.getSymbol());
            return Optional.empty();
        }
        SymbolFilter priceFilter = optPriceFilter.get();

        BigDecimal minPrice = new BigDecimal(priceFilter.getMinPrice());
        BigDecimal maxPrice = new BigDecimal(priceFilter.getMaxPrice());
        BigDecimal priceTickSize = new BigDecimal(priceFilter.getTickSize());

        Currency baseCurrency = new Currency(symbolInfo.getBaseAsset());
        Currency quoteCurrency = new Currency(symbolInfo.getQuoteAsset());

        return Optional.of(new SymbolInfo.Builder()
                .setSymbol(new Symbol(baseCurrency, quoteCurrency))
                .setMinQuantity(minQuantity)
                .setMaxQuantity(maxQuantity)
                .setQuantityStepSize(quantityStepSize)
                .setMinPrice(minPrice)
                .setMaxPrice(maxPrice)
                .setPriceTickSize(priceTickSize)
                .build());
    }


    @Nonnull
    private Optional<SymbolFilter> getSymbolFilter(
            @Nonnull com.binance.api.client.domain.general.SymbolInfo symbolInfo,
            @Nonnull FilterType filterType) {
        return symbolInfo.getFilters().stream()
                .filter((f) -> filterType.equals(f.getFilterType()))
                .findFirst();
    }
}
