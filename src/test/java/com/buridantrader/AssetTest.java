package com.buridantrader;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class AssetTest {

    @Test
    public void testHashCodeAndEquals() {
        // Given
        Currency currency1 = new Currency("BTCUSDT");
        BigDecimal balance1 = new BigDecimal("100.01");
        Asset asset1 = new Asset(currency1, balance1);

        Currency currency2 = new Currency("BTCUSDT");
        BigDecimal balance2 = new BigDecimal("100.01");
        Asset asset2 = new Asset(currency2, balance2);

        Currency currency3 = new Currency("ETHUSDT");
        BigDecimal balance3 = new BigDecimal("100.01");
        Asset asset3 = new Asset(currency3, balance3);

        Currency currency4 = new Currency("BTCUSDT");
        BigDecimal balance4 = new BigDecimal("100.010");
        Asset asset4 = new Asset(currency4, balance4);

        // When, Then
        Assert.assertEquals(asset1.hashCode(), asset2.hashCode());
        Assert.assertNotEquals(asset1.hashCode(), asset3.hashCode());
        Assert.assertNotEquals(asset1.hashCode(), asset4.hashCode());

        Assert.assertEquals(asset1, asset1);
        Assert.assertEquals(asset1, asset2);
        Assert.assertNotEquals(asset1, asset3);
        Assert.assertNotEquals(asset1, asset4);
        Assert.assertNotEquals(asset1, null);
        Assert.assertNotEquals(asset1, new Asset(currency1, balance1) {});
    }

    @Test
    public void testGettersAndSetters() {
        // Given
        Currency currency = new Currency("BTCUSDT");
        BigDecimal balance = new BigDecimal("100.01");
        Asset asset = new Asset(currency, balance);

        // When, Then
        Assert.assertEquals(asset.getCurrency(), currency);
        Assert.assertEquals(asset.getBalance(), balance);
    }

}
