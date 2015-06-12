package org.house.sprinklers.sprinkler_system;

import java.awt.geom.Point2D;

public interface Polygon {

    public Point2D[] getPolygonPoints();

    /**
     * A polygon with no edges.
     */
    Polygon EMPTY = new Polygon() {
        @Override
        public Point2D[] getPolygonPoints() {
            return new Point2D[0];
        }
    };
}
