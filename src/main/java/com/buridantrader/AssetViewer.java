package com.buridantrader;

import com.binance.api.client.BinanceApiRestClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class AssetViewer {

    private final BinanceApiRestClient client;

    public AssetViewer(@Nonnull BinanceApiRestClient client) {
        this.client = client;
    }

    /**
     * Get the assets of the accounts.
     * It include the assets that the account has no money inside.
     *
     * @return List of assets.
     * @throws IOException Fail to get the assets.
     */
    @Nonnull
    public List<Asset> getAccountAssets() throws IOException {
        try {
            return client.getAccount().getBalances()
                    .stream()
                    .map((assetBalance) -> new Asset(
                            new Currency(assetBalance.getAsset()),
                            new BigDecimal(assetBalance.getFree())))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IOException("Fail to get the account's assets", ex);
        }
    }
}
