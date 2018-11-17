package com.buridantrader;

import com.buridantrader.config.TradingConfig;
import com.buridantrader.config.TradingConfigImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TraderConfig {
    private final Config config;
    private Properties secret = null;
    private final TradingConfig tradingConfig;
    public TraderConfig(@Nonnull String configPath) {
        this.config = ConfigFactory.load(configPath);
        this.tradingConfig = new TradingConfigImpl(config.getConfig("trading"));
    }

    @Nonnull
    public String getApiKey() throws IOException {
        loadSecret();
        String result = secret.getProperty("api_key");
        if (result == null) {
            throw new IOException("Unable to find \"api_key\" in secret property file");
        }
        return result;
    }

    @Nonnull
    public String getApiSecret() throws IOException {
        loadSecret();
        String result = secret.getProperty("secret");
        if (result == null) {
            throw new IOException("Unable to find \"secret\" in secret property file");
        }
        return result;
    }

    @Nonnull
    public TradingConfig getTradingConfig() {
        return tradingConfig;
    }

    private void loadSecret() throws IOException {
        if (secret != null) {
            return;
        }
        Properties newSecret = new Properties();
        String secretFilePath = config.getString("binance.secretFile");
        try (InputStream secretInput = getClass().getResourceAsStream(secretFilePath)) {
            if (secretInput == null) {
                throw new IOException("Unable to find resource with name \"" + secretFilePath + "\"");
            }
            newSecret.load(secretInput);
            secret = newSecret;
        }
    }

}
