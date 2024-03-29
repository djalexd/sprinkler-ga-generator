package org.house.sprinklers.population;

import org.house.sprinklers.sprinkler_system.Sprinkler;

/**
 * Performs common sense validations of the sprinkler.
 */
public class CommonSenseSprinklerValidator implements SprinklerValidator {

    private final double minRange = 1.0, maxRange = 8.0;

    @Override
    public void validate(Sprinkler sprinkler) throws InvalidSprinklerException {
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
}
