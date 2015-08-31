package org.house.sprinklers.population;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInput;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.genetics.Crossover;
import org.house.sprinklers.genetics.Mutation;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.SprinklerSystemFitness;
import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;

import java.util.*;
import java.util.concurrent.Callable;


/**
 * Performs all GA specific steps:
 * - fitness
 * - selection
 * - crossover + mutation
 * - generate a new population
 */
@Slf4j
@Data
public class PopulationSimulator implements Callable<Population> {

    private final int expectedPopulationSize;

    private final Population currentPopulation;

    private final Terrain terrain;

    private final FitnessInputCalculator fitnessInputCalculator;

    private final FitnessCalculator fitnessCalculator;

    private final SprinklerValidator validator;

    private final Crossover crossover;

    private final Mutation mutation;

    @Override
    public Population call() throws Exception {

        log.info("Perform GA for population {}", currentPopulation.getPopulation().size());

        log.info("Step1. Perform fitness for all candidates");

        PopulationFitness populationFitness = computePopulationFitness(currentPopulation);

        final double currentPopulationFitness = populationFitness.computeAverageFitness();

        log.info("Total fitness of this population is {}, average fitness={}", currentPopulationFitness,
                String.format("%.2f", currentPopulationFitness / currentPopulation.getPopulation().size()));

        List<SprinklerSystem> newPopulation = Lists.newArrayList();

        log.info("Average genome size of this population is {}", averagePopulationGenomeSize(currentPopulation));

        final PopulationRoulette roulette = populationFitness.buildRoulette();

        while (newPopulation.size() < expectedPopulationSize) {
            // Pick 2 random individuals, but different
            //
            SprinklerSystem individual1 = roulette.pickBasedOnIndividualChance();
            SprinklerSystem individual2 = null;
            do {
                individual2 = roulette.pickBasedOnIndividualChance();
            } while (individual2 == individual1);

            /* Crossover + mutation */
            final List<SprinklerSystem> children = crossover.generateChildren(individual1, individual2);

            for (SprinklerSystem g : children) {
                final SprinklerSystem mutated = mutation.generateMutation(g);
                // if mutation is not valid, skip it.
                try {
                    for (Sprinkler s1 : mutated.getSprinklers()) {
                        validator.validate(s1);
                    }

                    newPopulation.add(mutated);
                } catch (InvalidSprinklerException e) {
                    // Do nothing
                    log.warn("Mutated sprinkler system is no longer valid, skipping it.");
                }
            }
        }

        return new Population(newPopulation);
    }

    private PopulationFitness computePopulationFitness(Population currentPopulation) throws InterruptedException {
        List<SprinklerSystemFitness> internalList = Lists.newArrayList();

        for (final SprinklerSystem sprinklerSystem : currentPopulation.getPopulation()) {

            final FitnessInput input = fitnessInputCalculator.computeFitnessInput(sprinklerSystem, terrain);
            final double fitness = fitnessCalculator.computeFitness(input);

            internalList.add(new SprinklerSystemFitness(sprinklerSystem, fitness));
        }

        return new PopulationFitness(ImmutableList.copyOf(internalList));
    }

    private Integer averagePopulationGenomeSize(Population currentPopulation) throws Exception {

        final SprinklerSystemGenome.GenomeStore store = new SprinklerSystemGenome.GenomeStore();
        int genomeSize = 0;

        for (SprinklerSystem system : currentPopulation.getPopulation()) {
            genomeSize += store.save(system).getGenes().length;
        }

        return genomeSize / currentPopulation.getPopulation().size();
    }
}
