package org.house.sprinklers.metrics;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.house.sprinklers.metrics.MetricsConstants.*;

import java.util.List;

@SuppressWarnings("unchecked")
@Component
public class InMemorySprinklerMetricsExtractor {

    @Autowired
    private RecorderService recorderService;

    public long numberOfTerrainSprinklerOps() {
        return safeGetOrZero(COUNTER_SPRINKLER_TERRAIN_INTERSECTIONS, 0L);
    }

    private <T extends Number> T safeGetOrZero(String metricName, T zero) {
        return recorderService.getMetricValue(metricName, zero);
    }

    @SuppressWarnings("all")
    public double totalExecTimeTerrainSprinklerOps() {
        final double invScale = 1.0 / 1000000;
        final List<Integer> execTimes = recorderService.getMetricValues(MetricsConstants.METRIC_SPRINKLER_TERRAIN_INTERSECTION);
        return execTimes.stream()
                .mapToDouble(x -> x * invScale)
                .sum();
    }

    public double averageExecTimeTerrainSprinklerOps() {
        final long counts = numberOfTerrainSprinklerOps();
        if (counts == 0) {
            return 0;
        }
        return totalExecTimeTerrainSprinklerOps() / counts;
    }

    public long generationsCount() {
        return safeGetOrZero(COUNTER_GA_GENERATIONS, 0L);
    }

    public long totalIndividualsCount() {
        return safeGetOrZero(COUNTER_GA_INDIVIDUALS, 0L);
    }

    public Errors getErrors() {
        return new Errors(
                safeGetOrZero(COUNTER_SPRINKLER_TERRAIN_ERRORS_TIMEOUT, 0L),
                safeGetOrZero(COUNTER_SPRINKLER_TERRAIN_ERRORS_MULTIPLEAREAS, 0L));
    }

    @Data
    public static class Errors {
        private final long intersectionTimeoutCount;
        private final long intersectionMultipleAreasCount;
    }

}
