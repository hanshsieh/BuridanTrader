package com.buridantrader.services.binance.simulation;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.buridantrader.services.binance.simulation.mappers.CandlestickMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class CandlestickCollector implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CandlestickCollector.class);
    private static final int MAX_CANDLESTICKS_PER_CALL = 1000;
    private final BinanceApiRestClient client;
    private final String symbol;
    private final Instant startTime, endTime;
    private final SimulationWriter simulationWriter;

    public CandlestickCollector(
            @Nonnull String symbol,
            @Nonnull Instant startTime,
            @Nonnull Instant endTime,
            @Nonnull BinanceApiRestClient client,
            @Nonnull SimulationWriter simulationWriter) {
        this.symbol = symbol;
        this.startTime = startTime;
        this.endTime = endTime;
        this.client = client;
        this.simulationWriter = simulationWriter;
    }

    @Override
    public void run() {
        Instant nextStartTime = startTime;
        try {
            while (true) {
                logger.info("Collecting candlesticks for symbol {} from {} to {}", symbol, nextStartTime, endTime);
                List<Candlestick> candlesticks = client.getCandlestickBars(
                        symbol,
                        CandlestickInterval.ONE_MINUTE,
                        MAX_CANDLESTICKS_PER_CALL,
                        nextStartTime.toEpochMilli(),
                        endTime.toEpochMilli());
                logger.info("Got {} candlesticks for symbol {}", candlesticks.size(), symbol);
                OptionalLong maxOpenTime = candlesticks.stream()
                    .mapToLong(Candlestick::getOpenTime)
                    .max();
                if (!maxOpenTime.isPresent()) {
                    break;
                }
                simulationWriter.writeCandlesticks(symbol, candlesticks);
                nextStartTime = Instant.ofEpochMilli(maxOpenTime.getAsLong());
                if (candlesticks.size() < MAX_CANDLESTICKS_PER_CALL) {
                    break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Fail to collect candlesticks", ex);
        }
    }
}
