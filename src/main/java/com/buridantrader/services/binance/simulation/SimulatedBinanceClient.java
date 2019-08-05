package com.buridantrader.services.binance.simulation;

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
import com.buridantrader.services.system.SystemService;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class SimulatedBinanceClient implements BinanceApiRestClient {

    private final SystemService systemService;

    public SimulatedBinanceClient(@NonNull SystemService systemService) {
        this.systemService = systemService;
    }

    @Override
    public void ping() {
        // Do nothing
    }

    @Override
    public Long getServerTime() {
        return systemService.currentTimeMillis();
    }

    @Override
    public ExchangeInfo getExchangeInfo() {
        return null;
    }

    @Override
    public List<Asset> getAllAssets() {
        return null;
    }

    @Override
    public OrderBook getOrderBook(String s, Integer integer) {
        return null;
    }

    @Override
    public List<TradeHistoryItem> getTrades(String s, Integer integer) {
        return null;
    }

    @Override
    public List<TradeHistoryItem> getHistoricalTrades(String s, Integer integer, Long aLong) {
        return null;
    }

    @Override
    public List<AggTrade> getAggTrades(String s, String s1, Integer integer, Long aLong, Long aLong1) {
        return null;
    }

    @Override
    public List<AggTrade> getAggTrades(String s) {
        return null;
    }

    @Override
    public List<Candlestick> getCandlestickBars(String s, CandlestickInterval candlestickInterval, Integer integer,
                                                Long aLong, Long aLong1) {
        return null;
    }

    @Override
    public List<Candlestick> getCandlestickBars(String s, CandlestickInterval candlestickInterval) {
        return null;
    }

    @Override
    public TickerStatistics get24HrPriceStatistics(String s) {
        return null;
    }

    @Override
    public List<TickerStatistics> getAll24HrPriceStatistics() {
        return null;
    }

    @Override
    public List<TickerPrice> getAllPrices() {
        return null;
    }

    @Override
    public TickerPrice getPrice(String s) {
        return null;
    }

    @Override
    public List<BookTicker> getBookTickers() {
        return null;
    }

    @Override
    public NewOrderResponse newOrder(NewOrder newOrder) {
        return null;
    }

    @Override
    public void newOrderTest(NewOrder newOrder) {

    }

    @Override
    public Order getOrderStatus(OrderStatusRequest orderStatusRequest) {
        return null;
    }

    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest) {
        return null;
    }

    @Override
    public List<Order> getOpenOrders(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public List<Order> getAllOrders(AllOrdersRequest allOrdersRequest) {
        return null;
    }

    @Override
    public Account getAccount(Long aLong, Long aLong1) {
        return null;
    }

    @Override
    public Account getAccount() {
        return null;
    }

    @Override
    public List<Trade> getMyTrades(String s, Integer integer, Long aLong, Long aLong1, Long aLong2) {
        return null;
    }

    @Override
    public List<Trade> getMyTrades(String s, Integer integer) {
        return null;
    }

    @Override
    public List<Trade> getMyTrades(String s) {
        return null;
    }

    @Override
    public WithdrawResult withdraw(String s, String s1, String s2, String s3, String s4) {
        return null;
    }

    @Override
    public DepositHistory getDepositHistory(String s) {
        return null;
    }

    @Override
    public WithdrawHistory getWithdrawHistory(String s) {
        return null;
    }

    @Override
    public DepositAddress getDepositAddress(String s) {
        return null;
    }

    @Override
    public String startUserDataStream() {
        return null;
    }

    @Override
    public void keepAliveUserDataStream(String s) {

    }

    @Override
    public void closeUserDataStream(String s) {

    }
}
