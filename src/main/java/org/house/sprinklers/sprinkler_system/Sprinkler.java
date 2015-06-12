package org.house.sprinklers.sprinkler_system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.awt.geom.Point2D;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "areaPolygons")
public class Sprinkler implements Polygon{

    private static final double SPLIT_ANGLE = 0.1;

    private Point2D position;

    private double range;

    private double startAngle;

    private double endAngle;

    private Point2D[] areaPolygons = null;

    @Override
    public Point2D[] getPolygonPoints() {
        if (areaPolygons == null) {
            try {
                areaPolygons = splitSprinklerAreaToPolygons(SPLIT_ANGLE);
            } catch (IllegalStateException e) {
                // Not enough radius to compute polygon, return an empty
                // array of points
                areaPolygons = new Point2D[0];
            }
        }

        return areaPolygons;
    }

    private Point2D[] splitSprinklerAreaToPolygons(double splitAngle) {

        int numPoints = (int) Math.round((this.endAngle - this.startAngle) / splitAngle);
        if (numPoints < 1) {
            throw new IllegalStateException("Sprinkler not enough radius");
        }

        Point2D[] array = new Point2D[numPoints + 2];

        array[0] = this.position;
        array[numPoints + 1] = this.position;
        for (int i = 0; i < numPoints; i++) {

            double angle = this.startAngle + i * splitAngle;
            array[1 + i] = new Point2D.Double(
                    this.position.getX() + this.range * Math.cos(angle),
                    this.position.getY() + this.range * Math.sin(angle));
        }

        return array;
    }
}
