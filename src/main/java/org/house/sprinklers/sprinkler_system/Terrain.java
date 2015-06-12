package org.house.sprinklers.sprinkler_system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.house.sprinklers.PolygonIntersect;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Terrain implements Polygon {

    private Point2D[] points;

    private Point2D[][] innerPenaltyDomains;

    private transient Double area = null;

    public double getArea() {

        if (area == null) {
            area = PolygonIntersect.intersectionArea(points, points);
            for (Point2D[] penalty : innerPenaltyDomains) {
                area -= PolygonIntersect.intersectionArea(penalty, penalty);
            }
        }

        return area;
    }

    @Override
    public Point2D[] getPolygonPoints() {
        return this.points;
    }

    public static class TerrainLoader implements Loader<InputStream, Terrain> {
        @Override
        public Terrain load(InputStream inputStream) throws Exception {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            int numPoints = Integer.parseInt(reader.readLine());
            Point2D[] points = new Point2D[numPoints];

            for (int i = 0; i < numPoints; i++) {
                String[] tokens = reader.readLine().split(" ");
                points[i] = new Point2D.Double(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
            }

            int numPenalties = Integer.parseInt(reader.readLine());
            Point2D[][] innerPenaltyPoints = new Point2D[numPenalties][];

            for (int i = 0; i < numPenalties; i++) {
                int penaltyPointsNum = Integer.parseInt(reader.readLine());
                innerPenaltyPoints[i] = new Point2D[penaltyPointsNum];
                for (int j = 0; j < penaltyPointsNum; j++) {
                    String[] tokens = reader.readLine().split(" ");
                    innerPenaltyPoints[i][j] = new Point2D.Double(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
                }
            }

            return new Terrain(points, innerPenaltyPoints, null);
        }
    }
}
