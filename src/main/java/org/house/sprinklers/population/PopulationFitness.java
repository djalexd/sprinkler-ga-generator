package org.house.sprinklers.population;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.sprinkler_system.SprinklerSystemFitness;

import java.util.List;

@Slf4j
@Data
public class PopulationFitness {

    private final List<SprinklerSystemFitness> sprinklerSystemFitnesses;

    public double computeAverageFitness() {

        if (sprinklerSystemFitnesses.isEmpty()) {
            return 0;
        }

        double total = 0.0;
        for (SprinklerSystemFitness f : sprinklerSystemFitnesses) {
            total += f.getFitness();
        }

        return total / sprinklerSystemFitnesses.size();
    }


    public PopulationRoulette buildRoulette() {
        return new PopulationRoulette(sprinklerSystemFitnesses);
    }
}
