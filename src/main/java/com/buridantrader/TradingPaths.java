package com.buridantrader;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@ThreadSafe
@Immutable
public class TradingPaths {
    private final Map<Currency, Map<Currency, PathStep>> pathGraph;

    public TradingPaths(@Nonnull Map<Currency, Map<Currency, PathStep>> pathGraph) {

        // Make a deep copy to ensure the structure won't be modified
        Collector<Map.Entry<Currency, PathStep>, ?, Map<Currency, PathStep>> secondLevelCollector =
            Collectors.toMap(Map.Entry::getKey,
                (entry) -> new PathStep(
                    entry.getValue().getSymbolToNext(),
                    entry.getValue().getLength()));

        this.pathGraph = pathGraph.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                (entry) -> entry.getValue().entrySet().stream()
                    .collect(secondLevelCollector)));
    }

    @Nonnull
    public Optional<PathStep> getNextStep(
            @Nonnull Currency sourceCurrency,
            @Nonnull Currency targetCurrency) {

        // Return a copy of the instance to prevent caller from modifying
        return Optional.ofNullable(pathGraph.getOrDefault(sourceCurrency, Collections.emptyMap())
                .get(targetCurrency))
                .map((p) -> new PathStep(p.getSymbolToNext(), p.getLength()));
    }
}
