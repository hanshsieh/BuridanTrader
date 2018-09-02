package com.buridantrader;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.time.Instant;
import java.util.Iterator;

public interface PriceViewer <Target> extends Closeable {
    @Nonnull
    Iterator<Candlestick> getPriceHistory(@Nonnull Target target, @Nonnull Instant startTime);
}
