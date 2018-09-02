package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class TradingResolver {

    private final Logger LOGGER = LoggerFactory.getLogger(TradingResolver.class);

    private final BinanceApiRestClient client;

    public TradingResolver(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    @Nonnull
    public List<Order> findTradingPath(
            @Nonnull Currency sourceCurrency,
            @Nonnull Currency targetCurrency,
            @Nonnull BigDecimal quantity) throws IOException {
        // TODO
        return null;
    }

    private void collectSymbols() {
        ExchangeInfo exchangeInfo = client.getExchangeInfo();
        exchangeInfo.getSymbols().stream()
            .forEach((symbolInfo -> {

                if (!SymbolStatus.TRADING.equals(symbolInfo.getStatus())) {
                    LOGGER.debug("Symbol {} not in TRADING status. Skip it.", symbolInfo.getSymbol());
                    return;
                }

                Optional<BigDecimal> optMinQuantity = getOrderMinQuantity(symbolInfo);
                if (!optMinQuantity.isPresent()) {
                    LOGGER.debug("Symbol {} doesn't have LOT_SIZE filter. Skip it.", symbolInfo.getSymbol());
                    return;
                }

                Currency baseCurrency = new Currency(symbolInfo.getBaseAsset());
                Currency quoteCurrency = new Currency(symbolInfo.getQuoteAsset());
            }));
    }

    @Nonnull
    private Optional<BigDecimal> getOrderMinQuantity(@Nonnull SymbolInfo symbolInfo) {
        return symbolInfo.getFilters().stream()
                .filter((f) -> FilterType.LOT_SIZE.equals(f.getFilterType()))
                .findFirst()
                .map((f) -> new BigDecimal(f.getMinQty()));
    }

}
