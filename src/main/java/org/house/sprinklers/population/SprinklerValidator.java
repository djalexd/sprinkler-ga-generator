package org.house.sprinklers.population;

import org.house.sprinklers.sprinkler_system.Sprinkler;

public interface SprinklerValidator {

    void validate(Sprinkler sprinkler)
        throws InvalidSprinklerException;
}
