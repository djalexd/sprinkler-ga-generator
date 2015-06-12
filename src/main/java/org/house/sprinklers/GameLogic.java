package org.house.sprinklers;

import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.genetics.Crossover;
import org.house.sprinklers.genetics.Mutation;
import org.house.sprinklers.population.Population;
import org.house.sprinklers.population.PopulationSimulator;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.Terrain;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class GameLogic implements Callable<Void> {

    private volatile Population currentPopulation;

    private final int expectedPopulationSize;
    private final Terrain terrain;
    private final FitnessInputCalculator fitnessInputCalculator;
    private final FitnessCalculator fitnessCalculator;
    private final SprinklerValidator sprinklerValidator;
    private final Crossover crossover;
    private final Mutation mutation;

    private final ExecutorService executorService;

    public GameLogic(int expectedPopulationSize,
                    Terrain terrain,
                     FitnessInputCalculator fitnessInputCalculator,
                     FitnessCalculator fitnessCalculator,
                     SprinklerValidator sprinklerValidator,
                     Crossover crossover,
                     Mutation mutation,
                     ExecutorService executorService) {
        this.expectedPopulationSize = expectedPopulationSize;
        this.terrain = terrain;
        this.fitnessInputCalculator = fitnessInputCalculator;
        this.fitnessCalculator = fitnessCalculator;
        this.sprinklerValidator = sprinklerValidator;
        this.crossover = crossover;
        this.mutation = mutation;
        this.executorService = executorService;

    }

    public Population getCurrentPopulation() {
        return this.currentPopulation;
    }

    public void setInitialPopulation(Population population) {
        this.currentPopulation = population;
    }

    @Override
    public Void call() throws Exception {

        for (int i = 0; i < 1000; i++) {

            log.info("Submit generation {}", i);

            Future<Population> c = executorService.submit(new PopulationSimulator(
                    expectedPopulationSize,
                    currentPopulation,
                    terrain,
                    fitnessInputCalculator,
                    fitnessCalculator,
                    sprinklerValidator,
                    crossover, mutation));

            // Retrieve next population
            currentPopulation = c.get();
        }

        return null;
    }
}
