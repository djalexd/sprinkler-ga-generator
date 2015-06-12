package org.house.sprinklers.population;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInput;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.genetics.Crossover;
import org.house.sprinklers.genetics.Mutation;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;
import org.house.sprinklers.sprinkler_system.Terrain;

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
    private static final Random random = new Random(62534L);

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

        log.info("Perform GA for population {}", currentPopulation.getPopulation().length);

        double currentPopulationFitness = 0.0;
        Double[] fitnesses = new Double[currentPopulation.getPopulation().length];

        /* Calculate fitness of each individual */
        for (int i = 0; i < currentPopulation.getPopulation().length; i++) {
            SprinklerSystem system = currentPopulation.getPopulation()[i];

            FitnessInput input = fitnessInputCalculator.computeFitnessInput(system, terrain);
            final double fitness = fitnessCalculator.computeFitness(input);

            // Associate individual sprinkler system with this fitness
            fitnesses[i] = fitness;
            currentPopulationFitness += fitness;

            log.debug("Fitness with {} sprinklers is {}", system.getSprinklers().length, fitness);
        }

        log.info("Fitness of this population is {}, average fitness={}, most fit candidates={}", currentPopulationFitness,
                String.format("%.2f", currentPopulationFitness / currentPopulation.getPopulation().length),
                0);

        /* Perform selection */
        final SprinklerSystemGenome.GenomeStore store = new SprinklerSystemGenome.GenomeStore();
        final SprinklerSystemGenome.GenomeLoader loader = new SprinklerSystemGenome.GenomeLoader(Optional.<SprinklerValidator>absent());

        List<SprinklerSystemGenome> genomes = Lists.newArrayList();

        log.info("Average genome size of this population is {}", averagePopulationGenomeSize(currentPopulation));

        int size = currentPopulation.getPopulation().length;
        while (genomes.size() < expectedPopulationSize) {
            // Pick 2 random individuals, but different
            //
            int individual1 = weightedRandom(size, fitnesses);

            int individual2 = -1;
            do {
                individual2 = weightedRandom(size, fitnesses);
            } while (individual2 == individual1);

            /* Crossover + mutation */
            SprinklerSystemGenome[] children = crossover.generateChildren(
                    store.save(currentPopulation.getPopulation()[individual1]),
                    store.save(currentPopulation.getPopulation()[individual2]));

            for (SprinklerSystemGenome g : children) {
                SprinklerSystemGenome mutated = mutation.generateMutation(g);
                // if mutation is not valid, skip it.
                try {
                    SprinklerSystem s = loader.load(mutated);
                    for (Sprinkler s1 : s.getSprinklers()) {
                        validator.validate(s1);
                    }

                    genomes.add(mutated);
                } catch (InvalidSprinklerException e) {
                    // Do nothing
                }
            }
        }

        SprinklerSystem[] systems = new SprinklerSystem[genomes.size()];
        for (int i = 0; i < genomes.size(); i++) {
            systems[i] = loader.load(genomes.get(i));
        }

        Population population = new Population(currentPopulation.getPopulation().length, false);
        population.setPopulation(systems);
        return population;
    }

    private Double[] mostFitCandidates(Double[] fitnesses, int i) {
        List<Double> copy = Arrays.asList(fitnesses);
        Collections.sort(copy, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return -1 * o1.compareTo(o2);
            }
        });

        List<Double> subList = copy.subList(0, i);
        return subList.toArray(new Double[subList.size()]);
    }

    private Integer averagePopulationGenomeSize(Population currentPopulation) throws Exception {

        final SprinklerSystemGenome.GenomeStore store = new SprinklerSystemGenome.GenomeStore();
        int genomeSize = 0;

        for (SprinklerSystem system : currentPopulation.getPopulation()) {
            genomeSize += store.save(system).getGenes().length;
        }

        return genomeSize / currentPopulation.getPopulation().length;
    }


    private int weightedRandom(int n, Double[] fitnesses) {
        HashMap<Integer, Integer> chance = Maps.newHashMap();

        for (int number = 0; number < n; number++) {
            chance.put(number, (int) (fitnesses[number] * 1000));
        }

        int chanceTotal = 0;
        for (Integer number : chance.keySet()) {
            chanceTotal += chance.get(number);
        }

        int choice = random.nextInt(chanceTotal), subtotal = 0;
        for (Integer number : chance.keySet()) {
            subtotal += chance.get(number);
            if (choice < subtotal) {
                return number;
            }
        }

        throw new RuntimeException("Should not happen");
    }
}