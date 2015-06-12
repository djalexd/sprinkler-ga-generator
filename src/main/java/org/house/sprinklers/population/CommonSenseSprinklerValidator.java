package org.house.sprinklers.population;

import com.google.common.base.Optional;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.Terrain;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Performs common sense validations of the sprinkler.
 */
public class CommonSenseSprinklerValidator implements SprinklerValidator {

    private Optional<Terrain> terrain;

    private final double minRange = 1.0, maxRange = 8.0;

    public CommonSenseSprinklerValidator() {
        this(Optional.<Terrain>absent());
    }

    public CommonSenseSprinklerValidator(Optional<Terrain> terrain) {
        this.terrain = terrain;
    }

    @Override
    public void validate(Sprinkler sprinkler) throws InvalidSprinklerException {
        if (terrain.isPresent()) {
            validateSprinklerBounds(sprinkler, terrain.get());
        }

        validateSprinklerRadius(sprinkler);
        validateSprinklerAngles(sprinkler);
    }

    private void validateSprinklerAngles(Sprinkler sprinkler) {
        if (sprinkler.getEndAngle() > 2 * Math.PI ||
                sprinkler.getStartAngle() > sprinkler.getEndAngle()) {
            throw new InvalidSprinklerException(String.format("Invalid sprinkler angles (%.2f, %.2f)",
                    sprinkler.getStartAngle(), sprinkler.getEndAngle()));
        }
    }

    private void validateSprinklerRadius(Sprinkler sprinkler) {
        if (sprinkler.getRange() < minRange || sprinkler.getRange() > maxRange) {
            throw new InvalidSprinklerException(String.format("Invalid sprinkler range (%.2f)", sprinkler.getRange()));
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
