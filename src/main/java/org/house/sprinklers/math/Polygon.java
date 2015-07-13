package org.house.sprinklers.math;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

public interface Polygon {

    public List<Point2D> getPolygonPoints();

    /**
     * A polygon with no edges.
     */
    Polygon EMPTY = new Polygon() {
        @Override
        public List<Point2D> getPolygonPoints() {
            return Collections.emptyList();
        }
    };
}
