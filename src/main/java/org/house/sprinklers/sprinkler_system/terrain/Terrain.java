package org.house.sprinklers.sprinkler_system.terrain;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.house.sprinklers.math.PolygonIntersect;
import org.house.sprinklers.sprinkler_system.Loader;
import org.house.sprinklers.math.Polygon;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Terrain implements Polygon {

    private List<Point2D> points;

    private transient Double area = null;

    public double getArea() {

        if (area == null) {
            area = PolygonIntersect.intersectionArea(this, this);
            /*for (Point2D[] penalty : innerPenaltyDomains) {
                area -= PolygonIntersect.intersectionArea(penalty, penalty);
            }
            */
        }

        return area;
    }

    @Override
    public List<Point2D> getPolygonPoints() {
        return this.points;
    }

    public static class TerrainLoader implements Loader<InputStream, Terrain> {
        @Override
        public Terrain load(InputStream inputStream) {

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                int numPoints = Integer.parseInt(reader.readLine());
                List<Point2D> points = Lists.newArrayListWithExpectedSize(numPoints);

                for (int i = 0; i < numPoints; i++) {
                    String[] tokens = reader.readLine().split(" ");
                    points.add(new Point2D.Double(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1])));
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

                //return new Terrain(points, innerPenaltyPoints, null);
                return new Terrain(points, null);

            } catch (IOException e) {
                throw new RuntimeException("Unable to load Terrain from given InputStream", e);
            }
        }
    }
}
