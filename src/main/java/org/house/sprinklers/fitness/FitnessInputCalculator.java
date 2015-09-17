package org.house.sprinklers.fitness;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.lighti.clipper.*;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.math.PolygonIntersect;
import org.house.sprinklers.math.Polygon;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;

import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class FitnessInputCalculator {

    private static final ThreadLocal<Clipper> CLIPPER = new InheritableThreadLocal<>();
    static {
        CLIPPER.set(new DefaultClipper());
    }

    private ExecutorService executorService;

    private RecorderService recorderService;

    public FitnessInputCalculator(final ExecutorService executorService,
                                  final RecorderService recorderService) {
        this.executorService = executorService;
        this.recorderService = recorderService;
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

        try {

            return executorService.submit(new Callable<Polygon>() {
                @Override
                public Polygon call() throws Exception {
                    return polygonOp(a, b, Clipper.ClipType.INTERSECTION);
                }
            }).get(500, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            log.debug("Timeout when compute intersection between {} and {}", a, b);
            recorderService.increment(MetricsConstants.COUNTER_SPRINKLER_TERRAIN_ERRORS_TIMEOUT);
            return new SimplePolygon(Collections.<Point2D>emptyList());

        } catch (ExecutionException e) {
            throw new RuntimeException("This should not happen", e);
        }
    }

    /**
     * Intersects 2 polygons.
     * @return Intersection polygon. If the two polygons don't intersect,
     * the result is an empty polygon (zero points).
     */
    private Polygon polygonOp(Polygon a, Polygon b, Clipper.ClipType clipType) {

        // If either polygons contain no point (not even one), return
        // an empty polygon as well.
        if (a.getPolygonPoints().isEmpty() || b.getPolygonPoints().isEmpty()) {
            return Polygon.EMPTY;
        }

        final double scale = 1000.0;

        Clipper clipper = CLIPPER.get();

        // Clear whatever was previously stored.
        clipper.clear();

        // Set the first polygon to clip and the second one as subject
        clipper.addPath(convertToPath(a, scale), Clipper.PolyType.CLIP, true);
        clipper.addPath(convertToPath(b, scale), Clipper.PolyType.SUBJECT, true);

        Paths solution = new Paths();
        clipper.execute(clipType, solution);

        if (solution.size() > 1) {
            log.debug("Polygons has multiple areas of intersection: {} (this should not happen!);" +
                    " only the first one will be returned", solution.size());
            recorderService.increment(MetricsConstants.COUNTER_SPRINKLER_TERRAIN_ERRORS_MULTIPLEAREAS);
        }

        if (solution.isEmpty()) {
            recorderService.increment(MetricsConstants.COUNTER_SPRINKLER_TERRAIN_NO_INTERSECTION);
            return Polygon.EMPTY;
        } else {
            Collections.sort(solution, new Comparator<Path>() {
                @Override
                public int compare(Path o1, Path o2) {
                    return -Double.valueOf(o1.area()).compareTo(o2.area());
                }
            });
            return convertToPolygon(solution.get(0), scale);
        }
    }

    /**
     * Utility method that converts a polygon to path
     * @param scale Path only supports {@link de.lighti.clipper.Point.LongPoint} so a
     *              scaling is performed in order to achieve better precision (or better yet,
     *              to loose as little as possible).
     * @return
     */
    private Path convertToPath(Polygon polygon, double scale) {
        Path path = new Path();

        for (Point2D p : polygon.getPolygonPoints()) {
            path.add(new Point.LongPoint(
                    Double.valueOf(scale * p.getX()).longValue(),
                    Double.valueOf(scale * p.getY()).longValue()));
        }

        return path;
    }

    private Polygon convertToPolygon(Path path, double scale) {

        List<Point2D> points = Lists.newArrayListWithExpectedSize(path.size());
        for (int i = 0; i < path.size(); i++) {
            points.add(new Point2D.Double(path.get(i).getX() / scale, path.get(i).getY() / scale));
        }

        return new SimplePolygon(points);
    }


    static class SimplePolygon implements Polygon {
        private List<Point2D> points;

        public SimplePolygon(List<Point2D> points) {
            this.points = ImmutableList.copyOf(points);
        }

        @Override
        public List<Point2D> getPolygonPoints() {
            return this.points;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("polygon(");
            for (Point2D p : points) {
                b.append("{").append(p.getX()).append(",").append(p.getY()).append("}, ");
            }
            b.append(")");
            return b.toString();
        }
    }
}
