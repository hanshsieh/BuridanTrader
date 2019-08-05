package com.buridantrader.services.binance.simulation;

import org.apache.commons.lang3.Validate;

import java.io.File;
import java.time.Instant;

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
}
