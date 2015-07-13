package org.house.sprinklers.sprinkler_system;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.house.sprinklers.math.Polygon;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "areaPolygons")
public class Sprinkler implements Polygon {

    private static final double SPLIT_ANGLE = 0.1;

    private Point2D position;

    private double range;

    private double startAngle;

    private double endAngle;

    private List<Point2D> areaPolygons = null;

    @Override
    public List<Point2D> getPolygonPoints() {
        if (areaPolygons == null) {
            try {
                areaPolygons = splitSprinklerAreaToPolygons(SPLIT_ANGLE);
            } catch (IllegalStateException e) {
                // Not enough radius to compute polygon, return an empty
                // array of points
                areaPolygons = Collections.emptyList();
            }
        }

        return areaPolygons;
    }

    private List<Point2D> splitSprinklerAreaToPolygons(double splitAngle) {

        int numPoints = (int) Math.round((this.endAngle - this.startAngle) / splitAngle);
        if (numPoints < 1) {
            throw new IllegalStateException("Sprinkler not enough radius");
        }

        List<Point2D> pointList = Lists.newArrayList();

        pointList.add(this.position);
        for (int i = 0; i < numPoints; i++) {

            double angle = this.startAngle + i * splitAngle;
            pointList.add(new Point2D.Double(
                    this.position.getX() + this.range * Math.cos(angle),
                    this.position.getY() + this.range * Math.sin(angle)));
        }

        pointList.add(this.position);

        return pointList;
    }
}
