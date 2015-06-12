package org.house.sprinklers.genetics;

import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;

/**
 * Performs the crossover of 2 parents to spawn 2 children.
 */
public interface Crossover {

    SprinklerSystemGenome[] generateChildren(SprinklerSystemGenome parentA, SprinklerSystemGenome parentB);
}
