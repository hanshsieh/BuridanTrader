package com.buridantrader.services.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.DepositAddress;
import com.binance.api.client.domain.account.DepositHistory;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.TradeHistoryItem;
import com.binance.api.client.domain.account.WithdrawHistory;
import com.binance.api.client.domain.account.WithdrawResult;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.CancelOrderResponse;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.market.AggTrade;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.domain.market.TickerStatistics;
import com.google.common.util.concurrent.RateLimiter;

import javax.annotation.Nonnull;
import java.util.List;

public class RateLimitBinanceClient implements BinanceApiRestClient {

    private static final int ORDER_BOOK_DEFAULT_LIMIT = 100;
    private static final double ORDER_BOOK_WEIGHT_RATIO = 100;
    private static final int GET_HISTORICAL_TRADE_WEIGHT = 5;
    private static final int GET_ALL_24H_PRICE_STATISTICS_WEIGHT = 40;
    private static final int GET_ALL_OPEN_ORDERS_WEIGHT = 40;
    private static final int GET_ALL_ORDERS_WEIGHT = 5;
    private static final int GET_ACCOUNT_WEIGHT = 5;
    private static final int GET_MY_TRADES_WEIGHT = 5;

    // Rate limit of Binance
    // See https://github.com/binance-exchange/binance-official-api-docs/blob/master/rest-api.md
    // Total sum of the API weights can be at most 1200 per minute.
    private final RateLimiter weightLimiter = RateLimiter.create(1200.0/60);
    // Total number of created orders can be at most 10 per second.
    private final RateLimiter orderLimiter = RateLimiter.create(10);
    // Total number of requests can be at most 5000 per 5 minutes.
    private final RateLimiter requestLimiter = RateLimiter.create(5000.0/(5 * 60.0));
    private final BinanceApiRestClient delegate;

    public RateLimitBinanceClient(@Nonnull BinanceApiRestClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public void ping() {
        weightLimiter.acquire();
        requestLimiter.acquire();
        this.delegate.ping();
    }

    @Override
    public Long getServerTime() {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getServerTime();
    }

    @Override
    public ExchangeInfo getExchangeInfo() {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getExchangeInfo();
    }

    @Override
    public List<Asset> getAllAssets() {
        // It's a static json file
        // It looks like it doesn't count for the rate limit
        // But let's still count it for one request
        requestLimiter.acquire();
        return delegate.getAllAssets();
    }

    @Override
    public OrderBook getOrderBook(String symbol, Integer limit) {
        int realLimit = limit == null ? ORDER_BOOK_DEFAULT_LIMIT : limit;
        weightLimiter.acquire((int) Math.max(1.0, realLimit / ORDER_BOOK_WEIGHT_RATIO));
        requestLimiter.acquire();
        return delegate.getOrderBook(symbol, limit);
    }

    @Override
    public List<TradeHistoryItem> getTrades(String symbol, Integer limit) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getTrades(symbol, limit);
    }

    @Override
    public List<TradeHistoryItem> getHistoricalTrades(String symbol, Integer limit, Long fromId) {
        weightLimiter.acquire(GET_HISTORICAL_TRADE_WEIGHT);
        requestLimiter.acquire();
        return delegate.getHistoricalTrades(symbol, limit, fromId);
    }

    @Override
    public List<AggTrade> getAggTrades(String symbol, String fromId, Integer limit, Long startTime, Long endTime) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getAggTrades(symbol, fromId, limit, startTime, endTime);
    }

    @Override
    public List<AggTrade> getAggTrades(String symbol) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getAggTrades(symbol);
    }

    @Override
    public List<Candlestick> getCandlestickBars(String symbol, CandlestickInterval interval, Integer limit, Long startTime, Long endTime) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getCandlestickBars(symbol, interval, limit, startTime, endTime);
    }

    @Override
    public List<Candlestick> getCandlestickBars(String symbol, CandlestickInterval interval) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getCandlestickBars(symbol, interval);
    }

    @Override
    public TickerStatistics get24HrPriceStatistics(String symbol) {
        if (symbol != null) {
            weightLimiter.acquire();
        } else {
            weightLimiter.acquire(GET_ALL_24H_PRICE_STATISTICS_WEIGHT);
        }
        requestLimiter.acquire();
        return delegate.get24HrPriceStatistics(symbol);
    }

    @Override
    public List<TickerStatistics> getAll24HrPriceStatistics() {
        weightLimiter.acquire(GET_ALL_24H_PRICE_STATISTICS_WEIGHT);
        requestLimiter.acquire();
        return delegate.getAll24HrPriceStatistics();
    }

    @Override
    public List<TickerPrice> getAllPrices() {
        weightLimiter.acquire(2);
        requestLimiter.acquire();
        return delegate.getAllPrices();
    }

    @Override
    public TickerPrice getPrice(String symbol) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getPrice(symbol);
    }

    @Override
    public List<BookTicker> getBookTickers() {
        weightLimiter.acquire(2);
        requestLimiter.acquire();
        return delegate.getBookTickers();
    }

    @Override
    public NewOrderResponse newOrder(NewOrder order) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        orderLimiter.acquire();
        return delegate.newOrder(order);
    }

    @Override
    public void newOrderTest(NewOrder order) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        orderLimiter.acquire();
        delegate.newOrderTest(order);
    }

    @Override
    public Order getOrderStatus(OrderStatusRequest orderStatusRequest) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getOrderStatus(orderStatusRequest);
    }

    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        orderLimiter.acquire();
        return delegate.cancelOrder(cancelOrderRequest);
    }

    @Override
    public List<Order> getOpenOrders(OrderRequest orderRequest) {
        if (orderRequest.getSymbol() != null) {
            weightLimiter.acquire();
        } else {
            weightLimiter.acquire(GET_ALL_OPEN_ORDERS_WEIGHT);
        }
        requestLimiter.acquire();
        return delegate.getOpenOrders(orderRequest);
    }

    @Override
    public List<Order> getAllOrders(AllOrdersRequest orderRequest) {
        weightLimiter.acquire(GET_ALL_ORDERS_WEIGHT);
        requestLimiter.acquire();
        return delegate.getAllOrders(orderRequest);
    }

    @Override
    public Account getAccount(Long recvWindow, Long timestamp) {
        weightLimiter.acquire(GET_ACCOUNT_WEIGHT);
        requestLimiter.acquire();
        return delegate.getAccount(recvWindow, timestamp);
    }

    @Override
    public Account getAccount() {
        weightLimiter.acquire(GET_ACCOUNT_WEIGHT);
        requestLimiter.acquire();
        return delegate.getAccount();
    }

    @Override
    public List<Trade> getMyTrades(String symbol, Integer limit, Long fromId, Long recvWindow, Long timestamp) {
        weightLimiter.acquire(GET_MY_TRADES_WEIGHT);
        requestLimiter.acquire();
        return delegate.getMyTrades(symbol, limit, fromId, recvWindow, timestamp);
    }

    @Override
    public List<Trade> getMyTrades(String symbol, Integer limit) {
        weightLimiter.acquire(GET_MY_TRADES_WEIGHT);
        requestLimiter.acquire();
        return delegate.getMyTrades(symbol, limit);
    }

    @Override
    public List<Trade> getMyTrades(String symbol) {
        weightLimiter.acquire(GET_MY_TRADES_WEIGHT);
        requestLimiter.acquire();
        return delegate.getMyTrades(symbol);
    }

    @Override
    public WithdrawResult withdraw(String asset, String address, String amount, String name, String addressTag) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.withdraw(asset, address, amount, name, addressTag);
    }

    @Override
    public DepositHistory getDepositHistory(String asset) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getDepositHistory(asset);
    }

    @Override
    public WithdrawHistory getWithdrawHistory(String asset) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getWithdrawHistory(asset);
    }

    @Override
    public DepositAddress getDepositAddress(String asset) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.getDepositAddress(asset);
    }

    @Override
    public String startUserDataStream() {
        weightLimiter.acquire();
        requestLimiter.acquire();
        return delegate.startUserDataStream();
    }

    @Override
    public void keepAliveUserDataStream(String listenKey) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        delegate.keepAliveUserDataStream(listenKey);
    }

    @Override
    public void closeUserDataStream(String listenKey) {
        weightLimiter.acquire();
        requestLimiter.acquire();
        delegate.closeUserDataStream(listenKey);
    }
}
