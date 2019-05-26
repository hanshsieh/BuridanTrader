package com.buridantrader.services.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.impl.BinanceApiRestClientImpl;
import com.buridantrader.DaemonThreadFactory;
import com.buridantrader.services.system.SystemService;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HistoryCollector {
    private final Logger logger = LoggerFactory.getLogger(HistoryCollector.class);
    private static final int MAX_CALLS_PER_MINUTE = 1200;
    private final BinanceApiRestClient client;
    private final SqlSessionFactory sqlSessionFactory;
    private final SystemService systemService;
    private final ModelConverter modelConverter;
    private final RateLimiter rateLimiter = RateLimiter.create(MAX_CALLS_PER_MINUTE / 60.0);
    private final ExecutorService executor;

    public HistoryCollector(
            @Nonnull BinanceApiRestClient client,
            @Nonnull SystemService systemService,
            @Nonnull ModelConverter modelConverter)
            throws IOException {
        this.client = client;
        this.systemService = systemService;
        this.sqlSessionFactory = createSessionFactory();
        this.modelConverter = modelConverter;
        this.executor = Executors.newCachedThreadPool(new DaemonThreadFactory());
    }

    public void collect(@Nonnull Instant startTime,
                        @Nonnull Instant endTime) throws IOException {
        Instant execStartTime = Instant.now();
        try {
            collectWithoutTimer(startTime, endTime);
        } finally {
            Instant execEndTime = Instant.now();
            logger.info("Elapsed time: {}", Duration.between(execStartTime, execEndTime));
        }
    }

    private void collectWithoutTimer(@Nonnull Instant startTime,
                         @Nonnull Instant endTime) throws IOException {
        List<SymbolInfo> symbols = getSymbols();
        List<Future<?>> futures = new ArrayList<>();
        for (SymbolInfo symbol : symbols) {
            Future<?> future = executor.submit(new CandlestickCollector(
                    symbol.getSymbol(),
                    startTime,
                    endTime,
                    client,
                    rateLimiter,
                    sqlSessionFactory,
                    modelConverter
            ));
            futures.add(future);
        }
        try {
            for (Future future : futures) {
                future.get();
            }
        } catch (Exception ex) {
            throw new IOException("Error occurs when waiting workers to finish", ex);
        }
    }

    @Nonnull
    private List<SymbolInfo> getSymbols() throws IOException {
        try {
            rateLimiter.acquire();
            ExchangeInfo exchangeInfo = client.getExchangeInfo();
            List<SymbolInfo> symbols = exchangeInfo.getSymbols();
            logger.info("Found {} symbols", symbols.size());
            return symbols;
        } catch (Exception ex) {
            throw new IOException("Fail to get exchange info", ex);
        }
    }

    @Nonnull
    private SqlSessionFactory createSessionFactory() throws IOException {
        try {
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            InputStream inputStream = systemService.getResourceAsStream("conf/mybatis-config.xml");
            return builder.build(inputStream);
        } catch (Exception ex) {
            throw new IOException("Fail to create session factory", ex);
        }
    }

    public static void main(String[] args) throws Exception {
        // The operations we need here doesn't need API key and secret.
        BinanceApiRestClient client = new BinanceApiRestClientImpl(
                null,
                null
        );
        HistoryCollector collector = new HistoryCollector(
                client,
                new SystemService(),
                new ModelConverter()
        );
        Instant startTime = Instant.ofEpochMilli(1546300800000L);
        Instant endTime   = Instant.ofEpochMilli(1554076800000L);
        collector.collect(startTime, endTime);
    }
}
