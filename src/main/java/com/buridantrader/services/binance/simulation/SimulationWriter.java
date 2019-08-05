package com.buridantrader.services.binance.simulation;

import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.market.Candlestick;
import com.buridantrader.services.binance.simulation.mappers.CandlestickMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nonnull;

public class SimulationWriter {
    private static final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private final File outputDir;
    private final SqlSessionFactory sqlSessionFactory;
    private final ModelConverter modelConverter;

    public SimulationWriter(@Nonnull SimulationConfig config) throws IOException {
        this(config, new ModelConverter());
    }

    public SimulationWriter(@Nonnull SimulationConfig config, @Nonnull ModelConverter modelConverter) throws IOException {
        this.outputDir = config.getDataDir();
        this.sqlSessionFactory = config.createSqlSessionFactory();
        this.modelConverter = modelConverter;
    }

    public void initialize() throws IOException {
        try(SqlSession session = sqlSessionFactory.openSession()) {
            CandlestickMapper mapper = session.getMapper(CandlestickMapper.class);
            mapper.dropTableIfExist();
            mapper.createTable();
            session.commit();
        } catch (Exception ex) {
            throw new IOException("Fail to create table for candlesticks");
        }
    }

    public void writeExchangeInfo(@Nonnull ExchangeInfo exchangeInfo) throws IOException {
        serializeToFile(exchangeInfo, Simulation.PATH_EXCHANGE_INFO);
    }

    public void writeAllAssets(@Nonnull List<Asset> assets) throws IOException {
        serializeToFile(assets, Simulation.PATH_ALL_ASSETS);
    }

    public void writeAccount(@Nonnull Account account) throws IOException {
        serializeToFile(account, Simulation.PATH_ACCOUNT);
    }

    private void serializeToFile(@Nonnull Object object, @Nonnull String fileName) throws IOException {
        objectWriter.writeValue(new File(outputDir, fileName), object);
    }

    public void writeCandlesticks(@Nonnull String symbol, @Nonnull List<Candlestick> candlesticks) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CandlestickMapper mapper = session.getMapper(CandlestickMapper.class);
            for (Candlestick candlestick : candlesticks) {
                mapper.insertOrReplace(modelConverter.candlestickToModel(symbol, candlestick));
            }
            session.commit();
        }
    }
}
