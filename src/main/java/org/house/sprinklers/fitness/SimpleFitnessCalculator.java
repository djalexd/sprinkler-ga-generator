package org.house.sprinklers.fitness;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleFitnessCalculator implements FitnessCalculator {

    @Override
    public double computeFitness(FitnessInput input) {
        if (input.getNumSprinklers() == 0) {
            return 0;
        }

        final double terrainArea = input.getTerrainArea();
        final double percentCover = input.getCoveredArea() / terrainArea;
        final double percentOutside = Math.min(1, input.getOutsideArea() / terrainArea);

        final double weightInside = 1.0, weightOutside = 0.5;

        return Math.max(0, weightInside * percentCover - weightOutside * percentOutside);
    }
}
