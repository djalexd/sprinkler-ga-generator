package org.house.sprinklers.population;

import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;

import java.awt.*;
import java.awt.geom.Point2D;

public class TerrainSprinklerValidator implements SprinklerValidator {

    private Terrain terrain;

    public TerrainSprinklerValidator() {
        this(null);
    }

    public TerrainSprinklerValidator(Terrain terrain) {
        this.terrain = terrain;
    }

    @Override
    public void validate(Sprinkler sprinkler) throws InvalidSprinklerException {
        if (terrain != null) {
            validateSprinklerBounds(sprinkler, terrain);
        }
    }

    private void validateSprinklerBounds(Sprinkler sprinkler, Terrain terrain) {

        java.awt.Polygon poly = new Polygon();
        for (Point2D p : terrain.getPolygonPoints()) {
            poly.addPoint((int) p.getX(), (int) p.getY());
        }

        if (!poly.contains(sprinkler.getPosition())) {
            throw new InvalidSprinklerException(String.format("Invalid sprinkler position (%.2f, %.2f)", sprinkler.getPosition().getX(), sprinkler.getPosition().getY()));
        }
    }
}
