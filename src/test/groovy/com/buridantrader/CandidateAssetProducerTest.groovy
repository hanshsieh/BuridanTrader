package com.buridantrader

import com.buridantrader.config.AssetConfig
import com.buridantrader.config.TradingConfig
import com.buridantrader.exceptions.NoSuchPathException
import com.buridantrader.exceptions.ValueLimitException
import spock.lang.Specification

/**
 * Test class for {@link CandidateAssetProducer}.
 */
class CandidateAssetProducerTest extends Specification {

    def config = Mock(TradingConfig)
    def assetViewer = Mock(AssetViewer)
    def pricePredicator = Mock(PricePredictor)
    def priceConverter = Mock(PriceConverter)
    def candidateAssetProducer = new CandidateAssetProducer(config, assetViewer, pricePredicator, priceConverter)

    def "Get candidates"() {
        given:
        def btcCurrency = new Currency("BTC")
        def ethCurrency = new Currency("ETH")
        def hahaCurrency = new Currency("HAHA")
        def heheCurrency = new Currency("HEHE")
        def hohoCurrency = new Currency("HOHO")
        def usdtCurrency = new Currency("USDT")
        def btcPrediction = Mock(PricePrediction)
        def ethPrediction = Mock(PricePrediction)
        def hehePrediction= Mock(PricePrediction)
        def btcAsset = new Asset(btcCurrency, new BigDecimal("0.1"))
        def ethAsset = new Asset(ethCurrency, new BigDecimal("0.2"))
        def hahaAsset = new Asset(hahaCurrency, new BigDecimal("0.3"))
        def heheAsset = new Asset(heheCurrency, new BigDecimal("0.0000001"))
        def hohoAsset = new Asset(hohoCurrency, BigDecimal.ZERO)

        when:
        def result = candidateAssetProducer.getCandidates()


        then:
        1 * assetViewer.getAccountAssets() >> [
            btcAsset,
            ethAsset,
            hahaAsset,
            heheAsset,
            hohoAsset
        ]
        (1 .. _) * config.getAssetConfig(btcCurrency) >> Mock(AssetConfig) {
            getMinPreferredQuantity() >> new BigDecimal("0.00001")
        }
        (1 .. _) * config.getAssetConfig(ethCurrency) >> Mock(AssetConfig) {
            getMinPreferredQuantity() >> new BigDecimal("0.3")
        }
        (1 .. _) * config.getAssetConfig(hahaCurrency) >> Mock(AssetConfig) {
            getMinPreferredQuantity() >> BigDecimal.ZERO
        }
        (1 .. _) * config.getAssetConfig(heheCurrency) >> Mock(AssetConfig) {
            getMinPreferredQuantity() >> BigDecimal.ZERO
        }
        (1 .. _) * config.getAssetConfig(hohoCurrency) >> Mock(AssetConfig) {
            getMinPreferredQuantity() >> BigDecimal.ZERO
        }
        (1 .. _) * config.getQuoteCurrency() >> usdtCurrency
        (1 .. _) * config.getMinTradingValue() >> new BigDecimal("0.01")
        1 * priceConverter.getRelativePrice(btcCurrency, usdtCurrency, new BigDecimal("0.09999")) >> new BigDecimal("100")
        1 * priceConverter.getRelativePrice(ethCurrency, usdtCurrency, BigDecimal.ZERO) >> BigDecimal.ZERO
        1 * priceConverter.getRelativePrice(hahaCurrency, usdtCurrency, new BigDecimal("0.3")) >> {throw new NoSuchPathException("")}
        1 * priceConverter.getRelativePrice(heheCurrency, usdtCurrency, new BigDecimal("0.0000001")) >> {throw new ValueLimitException("")}
        1 * priceConverter.getRelativePrice(hohoCurrency, usdtCurrency, BigDecimal.ZERO) >> BigDecimal.ZERO
        1 * pricePredicator.getPrediction(btcCurrency, usdtCurrency) >> btcPrediction
        1 * pricePredicator.getPrediction(ethCurrency, usdtCurrency) >> ethPrediction
        1 * pricePredicator.getPrediction(heheCurrency, usdtCurrency) >> hehePrediction
        1 * pricePredicator.getPrediction(hohoCurrency, usdtCurrency) >> {throw new NoSuchPathException("")}
        result.size() == 3
        result[0].asset == btcAsset
        result[0].pricePrediction == btcPrediction
        result[0].freeQuantity == new BigDecimal("0.09999")
        result[0].freeValue == new BigDecimal("100")
        result[0].eligibleForSource
        result[1].asset == ethAsset
        result[1].pricePrediction == ethPrediction
        result[1].freeQuantity == BigDecimal.ZERO
        result[1].freeValue == BigDecimal.ZERO
        !result[1].eligibleForSource
        result[2].asset == heheAsset
        result[2].pricePrediction == hehePrediction
        result[2].freeQuantity == new BigDecimal("0.0000001")
        result[2].freeValue == BigDecimal.ZERO
        !result[2].eligibleForSource
    }
}
