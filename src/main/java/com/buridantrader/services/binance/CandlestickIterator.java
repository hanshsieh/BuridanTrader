package com.buridantrader.services.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.impl.BinanceApiRestClientImpl;
import com.buridantrader.Candlestick;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class CandlestickIterator implements Iterator<Candlestick> {
    private final BinanceApiRestClient client;
    private static final CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    private static final int MAX_CANDLESTICKS_PER_CALL = 1000;
    private final String symbol;
    private Instant startTime;
    private final Instant endTime;

    // Whether the candlesticks for the time range is already fetched.
    // If the range is updated, it should be updated to false.
    private boolean timeRangeFetched = false;
    private Deque<Candlestick> candlesticks = new ArrayDeque<>();

    /**
     * Constructs a new iterator that will iterate over the candlesticks with the given start and end time.
     * It may involves multiple calls of Binance API.
     * The interval of the candlesticks is 1 minute, and the duration between the open time and epoch 0 of a
     * candlestick will be a multiple of 1 minute.
     *
     * @param client    The Binance client to use.
     * @param symbol    Symbol name, such as "BTCUSDT".
     * @param startTime Starting time (inclusive) of the candlesticks' open time.
     * @param endTime   Ending time (inclusive) of the candlesticks' open time.
     */
    public CandlestickIterator(
            @Nonnull BinanceApiRestClient client,
            @Nonnull String symbol,
            @Nonnull Instant startTime,
            @Nonnull Instant endTime) {
        this.client = client;
        this.symbol = symbol;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Whether there are more candlesticks.
     *
     * @return {@code true} if the iteration has more candlesticks.
     * @throws RuntimeException Fails to call Binance API.
     */
    @Override
    public boolean hasNext() throws RuntimeException {
        fetchCandlesticksIfNeeded();
        return !candlesticks.isEmpty();
    }

    /**
     * Gets the next candlestick.
     *
     * @return Candlestick.
     * @throws NoSuchElementException No more candlesticks.
     * @throws RuntimeException Fails to call Binance API.
     */
    @Override
    public Candlestick next() throws NoSuchElementException, RuntimeException {
        fetchCandlesticksIfNeeded();
        return candlesticks.getFirst();
    }

    private void fetchCandlesticksIfNeeded() throws RuntimeException {
        if (!candlesticks.isEmpty() || timeRangeFetched) {
            return;
        }
        List<Candlestick> newCandlesticks = client.getCandlestickBars(
                symbol,
                INTERVAL,
                MAX_CANDLESTICKS_PER_CALL,
                startTime.toEpochMilli(),
                endTime.toEpochMilli()).stream()
                .map(Candlestick::new)
                .collect(Collectors.toList());
        if (!newCandlesticks.isEmpty()) {
            Candlestick lastCandlestick = newCandlesticks.get(newCandlesticks.size() - 1);
            Instant openTime = lastCandlestick.getOpenTime();
            startTime = openTime.plusMillis(1);
            candlesticks.addAll(newCandlesticks);
            timeRangeFetched = false;
        } else {
            timeRangeFetched = true;
        }
    }

    public static void main(String[] args) throws Exception {
        BinanceApiRestClient client = new BinanceApiRestClientImpl(
                "kLx7bHIih1rN02s1EJ80AdZ6MdCbtGsz7MPASWdaQHRRzXnbZHEhVQtbT8pKSS9S",
                "Zdk9jd8dI9p341rocA3K1pydS3WALNW3FxL94Hl5PEZBHViSK5k3bsd4tSpJScGZ"
        );
        client.getExchangeInfo().getSymbols().stream()
            .map(SymbolInfo::getSymbol)
            .forEach(System.out::println);
        List<com.binance.api.client.domain.market.Candlestick> candlesticks =
                client.getCandlestickBars("BTCUSDT", INTERVAL, MAX_CANDLESTICKS_PER_CALL,
                1558723560000L, 1558723620000L);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(candlesticks));
    }
}
