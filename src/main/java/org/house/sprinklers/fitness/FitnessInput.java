package org.house.sprinklers.fitness;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FitnessInput {

    private int numSprinklers;

    private double terrainArea;

    private double coveredArea; // % * terrainArea

    /* Overlap is considered inside, any surface that gets hit by at least 2 sprinklers */
    private double overlapArea;

    private double outsideArea;
}
