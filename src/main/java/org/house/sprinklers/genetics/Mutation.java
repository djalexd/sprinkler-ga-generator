package org.house.sprinklers.genetics;

import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;

/**
 * Performs mutation for an individual genome.
 */
public interface Mutation {

    SprinklerSystem generateMutation(SprinklerSystem original);

    SprinklerSystemGenome generateMutation(SprinklerSystemGenome original);
}
