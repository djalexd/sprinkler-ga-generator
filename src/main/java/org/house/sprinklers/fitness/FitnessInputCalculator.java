package org.house.sprinklers.fitness;

import de.lighti.clipper.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.PolygonIntersect;
import org.house.sprinklers.sprinkler_system.Polygon;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.Terrain;
import org.joda.time.DateTime;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.*;

@Data
@Slf4j
public class FitnessInputCalculator {

    private static final ThreadLocal<Clipper> CLIPPER = new InheritableThreadLocal<>();
    static {
        CLIPPER.set(new DefaultClipper());
    }

    private final ExecutorService executorService;

    public FitnessInput computeFitnessInput(SprinklerSystem sprinklerSystem, Terrain terrain) throws InterruptedException {

        DateTime start = DateTime.now();

        double terrainArea = terrain.getArea(), covered = 0.0, overlap = 0.0, outside = 0.0;
        log.debug("Running sprinklers for terrain with total size {}", terrainArea);

        final Sprinkler[] sprinklers = sprinklerSystem.getSprinklers();

        Polygon[] sprinklerIntersections = new Polygon[sprinklers.length];
        double[] contribution = new double[sprinklers.length];

        for (int i = 0; i < sprinklers.length; i++) {
            Sprinkler s = sprinklers[i];
            sprinklerIntersections[i] = intersection(s, terrain);
            log.debug("Intersection {} with terrain: {}",
                    i, intersectionArea(sprinklerIntersections[i]));
            contribution[i] = intersectionArea(sprinklerIntersections[i]);

            outside += intersectionArea(sprinklers[i]);

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
        }

        outside = Math.max(0, outside - covered);

        DateTime end = DateTime.now();

        log.info("Sprinkler system run -- terrain={}, covered={}, overlap={}, outside={}, time={}",
                terrainArea, covered, overlap, outside, end.getMillis() - start.getMillis());
        log.debug("Sprinkler system run --");
        log.debug("Total terrain area = {}", terrainArea);
        log.debug("Covered area       = {} ({}%)", covered, String.format("%.2f", 100.0 * covered / terrainArea));
        log.debug("Overlap area       = {} ({}%)", overlap, String.format("%.2f", 100.0 * overlap / terrainArea));
        log.debug("System run time    = {} millis", end.getMillis() - start.getMillis());

        return FitnessInput.builder()
                .numSprinklers(sprinklerSystem.getSprinklers().length)
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
        return PolygonIntersect.intersectionArea(a.getPolygonPoints(), b.getPolygonPoints());
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
            log.warn("Timeout when compute intersection between {} and {}", a, b);
            return new SimplePolygon(new Point2D[0]);

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
        if (a.getPolygonPoints().length == 0 || b.getPolygonPoints().length == 0) {
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
            log.warn("Polygons has multiple areas of intersection: {} (this should not happen!);" +
                    " only the first one will be returned", solution.size());
        }

        if (solution.isEmpty()) {
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
        Point2D[] points = new Point2D[path.size()];
        for (int i = 0; i < path.size(); i++) {
            points[i] = new Point2D.Double(path.get(i).getX() / scale, path.get(i).getY() / scale);
        }

        return new SimplePolygon(points);
    }


    static class SimplePolygon implements Polygon {
        private Point2D[] points;

        public SimplePolygon(Point2D[] points) {
            this.points = Arrays.copyOf(points, points.length);
        }

        @Override
        public Point2D[] getPolygonPoints() {
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
