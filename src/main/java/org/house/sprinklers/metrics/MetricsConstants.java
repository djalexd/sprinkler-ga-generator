package org.house.sprinklers.metrics;

public final class MetricsConstants {
    private MetricsConstants() {}

    public static final String COUNTER_SPRINKLER_TERRAIN_INTERSECTIONS =
            "counter.terrain-sprinkler.intersection";

    public static final String METRIC_SPRINKLER_TERRAIN_INTERSECTION =
            "histogram.terrain-sprinkler.intersection";

    public static final String COUNTER_SPRINKLER_TERRAIN_ERRORS_TIMEOUT =
            "counter.errors.terrain-sprinkler.intersection.timeout";

    public static final String COUNTER_SPRINKLER_TERRAIN_ERRORS_MULTIPLEAREAS =
            "counter.errors.terrain-sprinkler.intersection.multiple-intersection-areas";

    public static final String COUNTER_GA_GENERATIONS =
            "gauge.genetic-algorithm.generations";

    public static final String COUNTER_GA_INDIVIDUALS =
            "counter.genetic-algorithm.individuals";

    public static final String METRIC_GA_GENERATION_FITNESS =
            "histogram.genetic-algorithm.fitness";

    public static final String METRIC_GA_GENERATION_COVERED_AREA =
            "histogram.genetic-algorithm.covered-area";

}
