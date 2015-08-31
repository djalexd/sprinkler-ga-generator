package org.house.sprinklers.genetics;

import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;

import java.util.List;

/**
 * Performs the crossover of 2 parents to spawn 2 children.
 */
public interface Crossover {

    List<SprinklerSystem> generateChildren(SprinklerSystem parentA, SprinklerSystem parentB);

    SprinklerSystemGenome[] generateChildren(SprinklerSystemGenome parentA, SprinklerSystemGenome parentB);
}
