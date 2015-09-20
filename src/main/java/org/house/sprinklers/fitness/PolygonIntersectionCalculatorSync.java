package org.house.sprinklers.fitness;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.lighti.clipper.Clipper;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.math.Polygon;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class PolygonIntersectionCalculatorSync {

    private static final ThreadLocal<Clipper> CLIPPER = new InheritableThreadLocal<>();
    static {
        CLIPPER.set(new DefaultClipper());
    }

    private ExecutorService executorService;

    private RecorderService recorderService;

    public PolygonIntersectionCalculatorSync(ExecutorService executorService, RecorderService recorderService) {
        this.executorService = executorService;
        this.recorderService = recorderService;
    }

    public Polygon intersection(final Polygon a, final Polygon b)
            throws InterruptedException {

        try {
            return polygonOp(a, b, Clipper.ClipType.INTERSECTION);
        } catch (Exception e) {
            recorderService.increment(MetricsConstants.COUNTER_SPRINKLER_TERRAIN_ERRORS_COMPUTATION);
            return new SimplePolygon(Collections.<Point2D>emptyList());
        }
        /*try {

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
            recorderService.increment(MetricsConstants.COUNTER_SPRINKLER_TERRAIN_ERRORS_COMPUTATION);
            return new SimplePolygon(Collections.<Point2D>emptyList());
        }
        */
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
