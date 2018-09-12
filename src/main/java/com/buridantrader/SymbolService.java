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

public class SymbolService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SymbolService.class);
    private final BinanceApiRestClient client;

    public SymbolService(@Nonnull BinanceApiRestClient client) {
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
        exchangeInfo.getSymbols().forEach((symbolInfo -> {

            if (!SymbolStatus.TRADING.equals(symbolInfo.getStatus())) {
                LOGGER.debug("Symbol {} not in TRADING status. Skip it.",
                        symbolInfo.getSymbol());
                return;
            }

            Optional<SymbolFilter> lotSizeFilter = getSymbolFilter(symbolInfo, FilterType.LOT_SIZE);
            Optional<SymbolFilter> priceFilter = getSymbolFilter(symbolInfo, FilterType.PRICE_FILTER);
            if (!lotSizeFilter.isPresent() || !priceFilter.isPresent()) {
                LOGGER.debug("Symbol {} doesn't have LOT_SIZE or PRICE_FILTER filter. Skip it."
                        , symbolInfo.getSymbol());
                return;
            }

            BigDecimal minQuantity = new BigDecimal(lotSizeFilter.get().getMinQty());
            BigDecimal maxQuantity = new BigDecimal(lotSizeFilter.get().getMaxQty());
            BigDecimal quantityStepSize = new BigDecimal(lotSizeFilter.get().getStepSize());

            Currency baseCurrency = new Currency(symbolInfo.getBaseAsset());
            Currency quoteCurrency = new Currency(symbolInfo.getQuoteAsset());
            symbolInfos.add(new SymbolInfo(
                    new Symbol(baseCurrency, quoteCurrency),
                    minQuantity,
                    maxQuantity,
                    quantityStepSize));
        }));
        return symbolInfos;
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
