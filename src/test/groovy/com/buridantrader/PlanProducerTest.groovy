package com.buridantrader

import com.buridantrader.config.TradingConfig
import com.buridantrader.exceptions.ValueException
import spock.lang.Specification
import spock.lang.Unroll

class PlanProducerTest extends Specification {

    def tradingConfig = Mock(TradingConfig)
    def tradingPathFinder = Mock(TradingPathFinder)
    def candidateAssetProducer = Mock(CandidateAssetProducer)
    def planProducer = new PlanProducer(
            tradingConfig,
            tradingPathFinder,
            candidateAssetProducer)

    @Unroll
    def "Get plan, and there's no source"() {
        when:
        def plan = planProducer.get()

        then:
        1 * candidateAssetProducer.getCandidates() >> candidates
        plan.getOrders().isEmpty()

        where:
        candidates << [
            [],
            [
                Mock(CandidateAsset) {
                    isEligibleForSource() >> false
                }
            ]
        ]
    }

    def "Get plan, and non-profitable asset shouldn't be treated as target"() {
        given:
        def candidates = [
                // source
                Mock(CandidateAsset) {
                    (1.._) * isEligibleForSource() >> true
                    getAsset() >> new Asset(
                            new Currency("USDT"), new BigDecimal("22.2"))
                    getFreeQuantity() >> new BigDecimal("20.1")
                },
                // target
                // Not profitable, so shouldn't be taken into concern
                Mock(CandidateAsset) {
                    (1.._) * isEligibleForSource() >> false
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> false
                    }
                    getAsset() >> new Asset(
                            new Currency("BTC"), new BigDecimal("10"))
                }
        ]

        when:
        def plan = planProducer.get()

        then:
        1 * candidateAssetProducer.getCandidates() >> candidates
        0 * tradingPathFinder._
        plan.getOrders().isEmpty()
    }

    def "Get plan, and the target isn't profitable enough"() {
        given:
        def usdtToEthOrders = [Mock(Order), Mock(Order)]
        def candidates = [
            // source
            Mock(CandidateAsset) {
                isEligibleForSource() >> true
                getPricePrediction() >> Mock(PricePrediction) {
                    isProfitable() >> true
                    getGrowthPerSec() >> new BigDecimal("0.001")
                }
                getAsset() >> new Asset(
                        new Currency("USDT"), new BigDecimal("22.2"))
                getFreeQuantity() >> new BigDecimal("20.1")
                getFreeValue() >> new BigDecimal("600")
            },
            // target
            // Profitable
            Mock(CandidateAsset) {
                isEligibleForSource() >> false
                getPricePrediction() >> Mock(PricePrediction) {
                    isProfitable() >> true
                    getGrowthPerSec() >> new BigDecimal("0.00105")
                }
                getAsset() >> new Asset(
                        new Currency("ETH"), new BigDecimal("10"))
            }
        ]

        when:
        // growth of value if no convert: 20.1*0.001*600=12.06
        // growth of value if convert: 19.1*0.00105*600-600*0.001*2=10.833
        def plan = planProducer.get()

        then:
        1 * candidateAssetProducer.getCandidates() >> candidates
        1 * tradingPathFinder.findPathOfOrders(
            new Currency("USDT"),
            new Currency("ETH"),
            new BigDecimal("20.1")
        ) >> Optional.of(usdtToEthOrders)
        1 * tradingPathFinder.getOrderTargetQuantity(usdtToEthOrders[1]) >> new BigDecimal("19.1")
        (1 .. _) * tradingConfig.getMeasuringSec() >> 600
        (1 .. _) * tradingConfig.getTradingFeeRate() >> new BigDecimal("0.001")
        plan.getOrders().isEmpty()
    }

    def "Get plan, and the source has negative growth rate"() {
        given:
        def usdtToEthOrders = [Mock(Order), Mock(Order)]
        def candidates = [
                // source
                Mock(CandidateAsset) {
                    isEligibleForSource() >> true
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("-0.00001")
                    }
                    getAsset() >> new Asset(
                            new Currency("USDT"), new BigDecimal("22.2"))
                    getFreeQuantity() >> new BigDecimal("20.1")
                    getFreeValue() >> new BigDecimal("600")
                },
                // target
                // Profitable
                Mock(CandidateAsset) {
                    isEligibleForSource() >> false
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.00105")
                    }
                    getAsset() >> new Asset(
                            new Currency("ETH"), new BigDecimal("10"))
                }
        ]

        when:
        // growth of value if no convert: 20.1*-0.00001*600=-0.1206
        // growth of value if convert: 0.01*0.00105*600=12.033
        def plan = planProducer.get()

        then:
        1 * candidateAssetProducer.getCandidates() >> candidates
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT"),
                new Currency("ETH"),
                new BigDecimal("20.1")
        ) >> Optional.of(usdtToEthOrders)
        1 * tradingPathFinder.getOrderTargetQuantity(usdtToEthOrders[1]) >> new BigDecimal("0.01")
        (1 .. _) * tradingConfig.getMeasuringSec() >> 600
        0 * tradingConfig.getTradingFeeRate()
        plan.getOrders() == usdtToEthOrders
    }

    def "Get plan, and the post profitable target is chosen"() {
        given:
        def usdtToEthOrders = [Mock(Order), Mock(Order), Mock(Order)]
        def usdtToEth2Orders = [Mock(Order), Mock(Order)]
        def candidates = [
                // source
                Mock(CandidateAsset) {
                    isEligibleForSource() >> true
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.001")
                    }
                    getAsset() >> new Asset(
                            new Currency("USDT"), new BigDecimal("22.2"))
                    getFreeQuantity() >> new BigDecimal("20.1")
                    getFreeValue() >> new BigDecimal("600")
                },
                // target
                // Profitable
                Mock(CandidateAsset) {
                    isEligibleForSource() >> false
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.002")
                    }
                    getAsset() >> new Asset(
                            new Currency("ETH"), new BigDecimal("10"))
                },
                Mock(CandidateAsset) {
                    isEligibleForSource() >> false
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.002")
                    }
                    getAsset() >> new Asset(
                            new Currency("ETH2"), new BigDecimal("10"))
                }
        ]

        when:
        // growth of value if no convert: 20.1*0.001*600=12.06
        // growth of value if convert to ETH: 19.1*0.002*600-600*0.001*3=21.12
        // growth of value if convert to ETH2: 19.1*0.002*600-600*0.001*2=21.72
        def plan = planProducer.get()

        then:
        1 * candidateAssetProducer.getCandidates() >> candidates
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT"),
                new Currency("ETH"),
                new BigDecimal("20.1")
        ) >> Optional.of(usdtToEthOrders)
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT"),
                new Currency("ETH2"),
                new BigDecimal("20.1")
        ) >> Optional.of(usdtToEth2Orders)
        1 * tradingPathFinder.getOrderTargetQuantity(usdtToEthOrders[2]) >> new BigDecimal("19.1")
        1 * tradingPathFinder.getOrderTargetQuantity(usdtToEth2Orders[1]) >> new BigDecimal("19.1")
        (1 .. _) * tradingConfig.getMeasuringSec() >> 600
        (1 .. _) * tradingConfig.getTradingFeeRate() >> new BigDecimal("0.001")
        plan.getOrders() == usdtToEth2Orders
    }

    def "Get plan, and the there're two sources"() {
        given:
        def usdtToEthOrders = [Mock(Order)]
        def usdt2ToEthOrders = [Mock(Order), Mock(Order)]
        def candidates = [
                // source
                Mock(CandidateAsset) {
                    isEligibleForSource() >> true
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.001")
                    }
                    getAsset() >> new Asset(
                            new Currency("USDT"), new BigDecimal("22.2"))
                    getFreeQuantity() >> new BigDecimal("20.1")
                    getFreeValue() >> new BigDecimal("600")
                },
                // source
                Mock(CandidateAsset) {
                    isEligibleForSource() >> true
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.001")
                    }
                    getAsset() >> new Asset(
                            new Currency("USDT2"), new BigDecimal("22.2"))
                    getFreeQuantity() >> new BigDecimal("21.1")
                    getFreeValue() >> new BigDecimal("600")
                },
                // target
                // Profitable
                Mock(CandidateAsset) {
                    isEligibleForSource() >> false
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.002")
                    }
                    getAsset() >> new Asset(
                            new Currency("ETH"), new BigDecimal("10"))
                }
        ]

        when:
        // growth of value if no convert: 20.1*0.001*600=12.06
        // growth of value if convert to ETH: 19.1*0.002*600-600*0.001*3=21.12
        def plan = planProducer.get()

        then:
        1 * candidateAssetProducer.getCandidates() >> candidates
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT"),
                new Currency("ETH"),
                new BigDecimal("20.1")
        ) >> Optional.of(usdtToEthOrders)
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT"),
                new Currency("USDT2"),
                new BigDecimal("20.1")
        ) >> Optional.empty()
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT2"),
                new Currency("USDT"),
                new BigDecimal("21.1")
        ) >> Optional.of([])
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT2"),
                new Currency("ETH"),
                new BigDecimal("21.1")
        ) >> Optional.of(usdt2ToEthOrders)
        1 * tradingPathFinder.getOrderTargetQuantity(usdtToEthOrders[0]) >> new BigDecimal("19.1")
        1 * tradingPathFinder.getOrderTargetQuantity(usdt2ToEthOrders[1]) >> new BigDecimal("19.1")
        (1 .. _) * tradingConfig.getMeasuringSec() >> 600
        (1 .. _) * tradingConfig.getTradingFeeRate() >> new BigDecimal("0.001")
        plan.getOrders() == usdtToEthOrders + usdt2ToEthOrders
    }

    def "Get plan, and the unable to find trading path"() {
        given:
        def candidates = [
                // source
                Mock(CandidateAsset) {
                    isEligibleForSource() >> true
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.001")
                    }
                    getAsset() >> new Asset(
                            new Currency("USDT"), new BigDecimal("22.2"))
                    getFreeQuantity() >> new BigDecimal("20.1")
                    getFreeValue() >> new BigDecimal("600")
                },
                // target
                // Profitable
                Mock(CandidateAsset) {
                    isEligibleForSource() >> false
                    getPricePrediction() >> Mock(PricePrediction) {
                        isProfitable() >> true
                        getGrowthPerSec() >> new BigDecimal("0.002")
                    }
                    getAsset() >> new Asset(
                            new Currency("ETH"), new BigDecimal("10"))
                }
        ]

        when:
        // growth of value if no convert: 20.1*0.001*600=12.06
        // growth of value if convert to ETH: 19.1*0.002*600-600*0.001*3=21.12
        def plan = planProducer.get()

        then:
        1 * candidateAssetProducer.getCandidates() >> candidates
        1 * tradingPathFinder.findPathOfOrders(
                new Currency("USDT"),
                new Currency("ETH"),
                new BigDecimal("20.1")
        ) >> {throw new ValueException("")}
        plan.getOrders().isEmpty()
    }
}

