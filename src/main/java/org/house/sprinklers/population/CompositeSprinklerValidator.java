package org.house.sprinklers.population;

import org.house.sprinklers.sprinkler_system.Sprinkler;

import java.util.Set;

public class CompositeSprinklerValidator implements SprinklerValidator {
    private final Set<SprinklerValidator> validators;

    public CompositeSprinklerValidator(Set<SprinklerValidator> validators) {
        this.validators = validators;
    }

    @Override
    public void validate(Sprinkler sprinkler) throws InvalidSprinklerException {
        validators.forEach(v -> v.validate(sprinkler));
    }
}
