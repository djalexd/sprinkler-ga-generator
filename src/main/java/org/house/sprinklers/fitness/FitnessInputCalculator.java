package org.house.sprinklers.fitness;

import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.math.PolygonIntersect;
import org.house.sprinklers.math.Polygon;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
public class FitnessInputCalculator {

    private RecorderService recorderService;

    private PolygonIntersectionCalculatorSync polygonIntersectionCalculator;

    public FitnessInputCalculator(final PolygonIntersectionCalculatorSync polygonIntersectionCalculator,
                                  final RecorderService recorderService) {
        this.recorderService = recorderService;
        this.polygonIntersectionCalculator = polygonIntersectionCalculator;
    }

    public FitnessInput computeFitnessInput(final List<Sprinkler> sprinklers, Terrain terrain) throws InterruptedException {

        final Instant start = Instant.now();

        double terrainArea = terrain.getArea(), covered = 0.0, overlap = 0.0, outside = 0.0;
        log.debug("Running sprinklers for terrain with total size {}", terrainArea);

        Polygon[] sprinklerIntersections = new Polygon[sprinklers.size()];
        double[] contribution = new double[sprinklers.size()];

        for (int i = 0; i < sprinklers.size(); i++) {

            final Instant sprinklerStart = Instant.now();

            sprinklerIntersections[i] = intersection(sprinklers.get(i), terrain);
            log.debug("Intersection {} with terrain: {}",
                    i, intersectionArea(sprinklerIntersections[i]));
            contribution[i] = intersectionArea(sprinklerIntersections[i]);

            outside += intersectionArea(sprinklers.get(i));

            double c = 0.0;
            for (int j = 0; j < i; j++) {
                // Intersection between sprinklers (i, j)
                Polygon z = intersection(sprinklerIntersections[i], sprinklerIntersections[j]);

                // TODO The problem in line below is that contributed area ends up
                // with eronous data if sprinklers overlap too much.
                c += intersectionArea(z);
            }

            contribution[i] = Math.max(0, contribution[i] - c);
            log.debug("Contribution {}: {}", i, contribution[i]);

            covered += contribution[i];
            overlap += c;

            final Instant sprinklerEnd = Instant.now();

            recorderService.submit(MetricsConstants.METRIC_SPRINKLER_TERRAIN_INTERSECTION,
                    Duration.between(sprinklerStart, sprinklerEnd).getNano());
            recorderService.increment(MetricsConstants.COUNTER_SPRINKLER_TERRAIN_INTERSECTIONS);
        }

        outside = Math.max(0, outside - covered);

        Instant end = Instant.now();

        if (log.isInfoEnabled()) {
            log.info("Sprinkler system run -- terrain={}, covered={}, overlap={}, outside={}, nanos={}",
                    terrainArea, covered, overlap, outside, Duration.between(start, end).getNano());
        } else if (log.isDebugEnabled()) {
            log.debug("Sprinkler system run --");
            log.debug("Total terrain area = {}", terrainArea);
            log.debug("Covered area       = {} ({}%)", covered, String.format("%.2f", 100.0 * covered / terrainArea));
            log.debug("Overlap area       = {} ({}%)", overlap, String.format("%.2f", 100.0 * overlap / terrainArea));
            log.debug("System run time    = {} nanos", Duration.between(start, end).getNano());
        }

        return FitnessInput.builder()
                .numSprinklers(sprinklers.size())
                .terrainArea(terrainArea)
                .coveredArea(covered)
                .outsideArea(outside)
                .overlapArea(overlap)
                .build();
    }


    private double intersectionArea(Polygon a) {
        return intersectionArea(a, a);
    }

    private double intersectionArea(Polygon a, Polygon b) {
        return PolygonIntersect.intersectionArea(a, b);
    }

    private Polygon intersection(final Polygon a, final Polygon b)
        throws InterruptedException {
        return polygonIntersectionCalculator.intersection(a, b);
    }
}
