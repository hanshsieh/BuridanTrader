package com.buridantrader;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ThreadSafe
@Immutable
public class TradingPaths {
    private final Map<Currency, Map<Currency, PathStep>> pathGraph;

    public TradingPaths(@Nonnull Map<Currency, Map<Currency, PathStep>> pathGraph) {

        // Make a deep copy to ensure the structure won't be modified
        this.pathGraph = pathGraph.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    (entry) -> ImmutableMap.copyOf(entry.getValue())));
    }

    @Nonnull
    public Optional<PathStep> getNextStep(
            @Nonnull Currency sourceCurrency,
            @Nonnull Currency targetCurrency) {
        return Optional.ofNullable(pathGraph.getOrDefault(sourceCurrency, Collections.emptyMap())
                .get(targetCurrency));
    }
}
