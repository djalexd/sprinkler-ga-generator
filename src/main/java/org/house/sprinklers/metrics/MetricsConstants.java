package org.house.sprinklers.metrics;

public final class MetricsConstants {

    private MetricsConstants() {}

    public static final String COUNTER_SPRINKLER_TERRAIN_INTERSECTIONS        = "counter.terrain-sprinkler.intersection";
    public static final String METRIC_SPRINKLER_TERRAIN_INTERSECTION          = "histogram.terrain-sprinkler.intersection";
    public static final String COUNTER_SPRINKLER_TERRAIN_ERRORS_TIMEOUT       = "errors.terrain-sprinkler.intersection.timeout";
    public static final String COUNTER_SPRINKLER_TERRAIN_ERRORS_MULTIPLEAREAS = "errors.terrain-sprinkler.intersection.multiple-intersection-areas";
    public static final String COUNTER_SPRINKLER_TERRAIN_NO_INTERSECTION      = "counter.terrain-sprinkler.no-intersection";

    public static final String COUNTER_GA_GENERATIONS                         = "counter.genetic-algorithm.generations";
    public static final String COUNTER_GA_INDIVIDUALS                         = "counter.genetic-algorithm.individuals";
    public static final String COUNTER_GA_CROSSOVERS                          = "counter.genetic-algorithm.crossovers";
    public static final String COUNTER_GA_MUTATIONS                           = "counter.genetic-algorithm.mutations";
    public static final String COUNTER_GA_MUTATIONS_INSERT                    = "counter.genetic-algorithm.mutations-insert";
    public static final String COUNTER_GA_MUTATIONS_CHANGE                    = "counter.genetic-algorithm.mutations-change";
    public static final String COUNTER_GA_MUTATIONS_DELETE                    = "counter.genetic-algorithm.mutations-delete";
    public static final String COUNTER_GA_MUTATIONS_CHANGE_ERRORS_IMPOSSIBLE  = "errors.genetic-algorithm.mutations-change.no-solution";
    public static final String METRIC_GA_GENERATION_FITNESS                   = "histogram.genetic-algorithm.fitness";
    public static final String METRIC_GA_GENERATION_DIVERSITY                 = "histogram.genetic-algorithm.diversity";
    public static final String METRIC_GA_GENERATION_COVERED_AREA              = "histogram.genetic-algorithm.covered-area";
}
