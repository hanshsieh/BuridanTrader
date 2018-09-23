package com.buridantrader

import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class CandlestickTest extends Specification {

    @Unroll
    def "not equals and hashCode should be different"() {
        when:
        def equals = candlestick1.equals(candlestick2)
        def hashCode1 = candlestick1.hashCode()
        def hashCode2 = candlestick2.hashCode()

        then:
        !equals
        hashCode1 != hashCode2

        where:
        candlestick1 << [
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                ),
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                ),
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                )
        ]
        candlestick2 << [
                new Candlestick(
                        Instant.ofEpochMilli(101),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                ),
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(102),
                        new BigDecimal("102")
                ),
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("103")
                ),
        ]
    }

    @Unroll
    def "null or different class should be not equal"() {
        when:
        def equals = candlestick1.equals(candlestick2)

        then:
        !equals

        where:
        candlestick1 << [
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                ),
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                ),
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                )
        ]
        candlestick2 << [
                null,
                new Candlestick(
                        Instant.ofEpochMilli(100),
                        Instant.ofEpochMilli(101),
                        new BigDecimal("102")
                ) {},
                new Object()
        ]
    }

    @Unroll
    def "equal and hash code should be same"() {
        given:
        def candlestick1 = new Candlestick(
                Instant.ofEpochMilli(200),
                Instant.ofEpochMilli(201),
                new BigDecimal("202")
        )
        def candlestick2 = new Candlestick(
                Instant.ofEpochMilli(200),
                Instant.ofEpochMilli(201),
                new BigDecimal("202")
        )


        when:
        def equals1 = candlestick1.equals(candlestick2)
        def equals2 = candlestick1.equals(candlestick1)
        def hashCode1 = candlestick1.hashCode()
        def hashCode2 = candlestick2.hashCode()

        then:
        equals1
        equals2
        hashCode1 == hashCode2
    }
}

