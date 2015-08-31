package org.house.sprinklers.population;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.SprinklerSystemFitness;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Data
@Slf4j
public class PopulationRoulette {
    private final static Random random = new Random();

    private final List<SprinklerSystemFitness> sprinklerSystemFitnesses;

    public SprinklerSystem pickBasedOnRandom() {
        return random();
    }

    public SprinklerSystem pickBasedOnIndividualChance() {
        return weightedRandom();
    }

    private SprinklerSystem random() {
        int index = random.nextInt(sprinklerSystemFitnesses.size());
        return sprinklerSystemFitnesses.get(index).getSprinklerSystem();
    }

    private SprinklerSystem weightedRandom() {

        HashMap<Integer, Integer> chance = Maps.newHashMap();
        for (int i = 0; i < sprinklerSystemFitnesses.size(); i++) {
            SprinklerSystemFitness sprinklerSystemFitness = sprinklerSystemFitnesses.get(i);
            chance.put(i, (int) (sprinklerSystemFitness.getFitness() * 1000));
        }

        int chanceTotal = 0;
        for (Integer number : chance.keySet()) {
            chanceTotal += chance.get(number);
        }

        int choice = random.nextInt(chanceTotal), subtotal = 0;
        for (Integer number : chance.keySet()) {
            subtotal += chance.get(number);
            if (choice < subtotal) {
                return sprinklerSystemFitnesses.get(number).getSprinklerSystem();
            }
        }

        throw new RuntimeException("Should not happen");
    }
}
