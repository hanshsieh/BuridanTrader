package com.buridantrader.services.binance.simulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

import javax.annotation.Nonnull;

public class SimulationConfig {
    public static final class Builder {
        private Instant startTime;
        private Instant endTime;
        private File dataDir;
        private String apiKey;
        private String apiSecret;

        @Nonnull
        public Builder setStartTime(@Nonnull Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        @Nonnull
        public Builder setEndTime(@Nonnull Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        @Nonnull
        public Builder setDataDir(@Nonnull File dataDir) {
            this.dataDir = dataDir;
            return this;
        }

        @Nonnull
        public Builder setApiKey(@Nonnull String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        @Nonnull
        public Builder setApiSecret(@Nonnull String apiSecret) {
            this.apiSecret = apiSecret;
            return this;
        }

        @Nonnull
        public SimulationConfig build() {
            Validate.notNull(startTime, "Missing start time");
            Validate.notNull(endTime, "Missing end time");
            Validate.notNull(dataDir, "Missing data directory");
            Validate.notNull(apiKey, "Missing api key");
            Validate.notNull(apiSecret, "Missing api secret");
            return new SimulationConfig(this);
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(SimulationConfig.class);
    private final Instant startTime;
    private final Instant endTime;
    private final File dataDir;
    private final String apiKey;
    private final String apiSecret;

    private SimulationConfig(@Nonnull Builder builder) {
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.dataDir = builder.dataDir;
        this.apiKey = builder.apiKey;
        this.apiSecret = builder.apiSecret;
    }

    @Nonnull
    public static SimulationConfig loadFromFile(@Nonnull File configFile) throws IOException {
        JsonNode configNode = new ObjectMapper(new YAMLFactory()).readTree(configFile);
        Validate.notNull(configNode, "File " + configFile.getPath() + " doesn't contain valid YAML");
        File configDir = configFile.getParentFile();
        File relativeDataDir = new File(getJsonNode(configNode, "dataDir").asText());
        File dataDir;
        if (relativeDataDir.isAbsolute()) {
            dataDir = relativeDataDir.getCanonicalFile();
        } else {
            dataDir = new File(configDir, relativeDataDir.getPath()).getCanonicalFile();
        }
        FileUtils.forceMkdir(dataDir);
        logger.debug("Data directory: {}", dataDir.getPath());
        return new SimulationConfig.Builder()
            .setStartTime(Instant.ofEpochMilli(getJsonNode(configNode, "startTimeMs").asLong()))
            .setEndTime(Instant.ofEpochMilli(getJsonNode(configNode, "endTimeMs").asLong()))
            .setDataDir(dataDir)
            .setApiKey(getJsonNode(configNode, "apiKey").asText())
            .setApiSecret(getJsonNode(configNode, "apiSecret").asText())
            .build();
    }

    private static JsonNode getJsonNode(@Nonnull JsonNode parent, @Nonnull String key) {
        JsonNode node = parent.get(key);
        if (node == null) {
            throw new IllegalArgumentException("Missing \"" + key + "\"");
        }
        return node;
    }

    @Nonnull
    public Instant getStartTime() {
        return startTime;
    }

    @Nonnull
    public Instant getEndTime() {
        return endTime;
    }

    @Nonnull
    public File getDataDir() {
        return dataDir;
    }

    @Nonnull
    public String getApiKey() {
        return apiKey;
    }

    @Nonnull
    public String getApiSecret() {
        return apiSecret;
    }

    @Nonnull
    public SqlSessionFactory createSqlSessionFactory() throws IOException {
        try {
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            InputStream configStream = getClass().getResourceAsStream("mybatis-config.xml");
            Properties properties = new Properties();
            String dbFilePath = new File(getDataDir(), "data.sqlite").getCanonicalPath();
            logger.info("Using \"{}\" to store history records", dbFilePath);
            properties.setProperty("sqliteDbPath", dbFilePath);
            return builder.build(configStream, properties);
        } catch (Exception ex) {
            throw new IOException("Fail to create session factory", ex);
        }
    }
}
