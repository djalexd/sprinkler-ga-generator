package org.house.sprinklers.sprinkler_system;

import org.house.sprinklers.population.InvalidSprinklerException;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.awt.*;
import java.awt.geom.Point2D;

public class ValidSprinklerValidator implements ConstraintValidator<ValidSprinkler, Sprinkler> {

    private double minRange;
    private double maxRange;

    @Override
    public void initialize(ValidSprinkler constraintAnnotation) {
        minRange = constraintAnnotation.minRange();
        maxRange = constraintAnnotation.maxRange();
    }

    @Override
    public boolean isValid(Sprinkler sprinkler, ConstraintValidatorContext context) {
        boolean valid = true;

        final double range = sprinkler.getRange();
        if (range < minRange) {
            context.buildConstraintViolationWithTemplate("Invalid sprinkler range, below minimum allowed")
                    .addPropertyNode("range");
            valid = false;
        } else if (range > maxRange) {
            context.buildConstraintViolationWithTemplate("Invalid sprinkler range, above maximum allowed")
                    .addPropertyNode("range");
            valid = false;
        }

        if (sprinkler.getStartAngle() < 0.0 || sprinkler.getStartAngle() > 2 * Math.PI) {
            context.buildConstraintViolationWithTemplate("Invalid sprinkler startAngle")
                    .addPropertyNode("startAngle");
            valid = false;
        }

        if (sprinkler.getEndAngle() < 0.0 || sprinkler.getEndAngle() > 2 * Math.PI ) {
            context.buildConstraintViolationWithTemplate("Invalid sprinkler endAngle")
                    .addPropertyNode("startAngle");
            valid = false;
        }

        if (sprinkler.getEndAngle() > sprinkler.getStartAngle()) {
            context.buildConstraintViolationWithTemplate("Invalid sprinkler angle, startAngle is greater than endAngle");
            valid = false;
        }

        // TODO Validate terrain

        return valid;
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
