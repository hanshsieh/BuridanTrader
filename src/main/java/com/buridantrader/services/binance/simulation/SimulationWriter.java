package com.buridantrader.services.binance.simulation;

import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

public class SimulationWriter {
    private static final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private final File outputDir;

    public SimulationWriter(@Nonnull File outputDir) {
        this.outputDir = outputDir;
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
}
