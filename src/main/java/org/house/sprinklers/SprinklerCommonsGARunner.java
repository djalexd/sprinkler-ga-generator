package org.house.sprinklers;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.BinaryChromosome;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.OnePointCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.RandomKeyMutation;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.house.sprinklers.population.InvalidSprinklerException;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SprinklerCommonsGARunner {

    private static final Random random = new Random();
    private static final int NUM_GENERATIONS = 5;
    private static final int TOURNAMENT_ARITY = 2;

    public static void main(String[] args) {

        // initialize a new genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                1,
                new RandomKeyMutation(),
                0.10,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        // initial population
        Population initial = getInitialPopulation();

        // stopping condition
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        // run the algorithm
        Population finalPopulation = ga.evolve(initial, stopCond);

        // best chromosome from the final population
        Chromosome bestFinal = finalPopulation.getFittestChromosome();

    }

    private static Population getInitialPopulation(SprinklerValidator validator) {

        final int chromosomeCount = 10;
        final List<Chromosome> chromosomes = new ArrayList<>(chromosomeCount);
        for (int i = 0; i < 10; i++) {
            final SprinklerSystem system = randomSprinklerSystem(Optional.of(validator));
            chromosomes.add(new BinaryChromosome(sprinklerSystemRepresentation(system)) {
                @Override
                public AbstractListChromosome<Integer> newFixedLengthChromosome(List<Integer> chromosomeRepresentation) {
                    return null;
                }

                @Override
                public double fitness() {
                    return 0;
                }
            });
        }

        //final ElitisticListPopulation population = new ElitisticListPopulation()


        return null;
    }


    private static List<Integer> sprinklerSystemRepresentation(SprinklerSystem sprinklerSystem) {

    }


    private static SprinklerSystem randomSprinklerSystem(Optional<SprinklerValidator> validator) {
        // Random count of sprinklers
        int numSprinklers = 2 + random.nextInt(10);
        List<Sprinkler> sprinklers = Lists.newArrayListWithExpectedSize(numSprinklers);
        for (int i = 0; i < numSprinklers; i++) {

            boolean found = false;

            if (validator.isPresent()) {

                while (!found) {
                    Sprinkler aRandomSprinkler = randomSprinkler();
                    try {
                        validator.get().validate(aRandomSprinkler);
                        found = true;
                        sprinklers.add(aRandomSprinkler);
                    } catch (InvalidSprinklerException e) {
                        // Thrown if sprinkler is invalid, this means the sprinkler was
                        // not found yet (it's not even valid.
                    }
                }

            } else {
                sprinklers.add(randomSprinkler());
            }
        }

        return new SprinklerSystem(sprinklers);
    }

    private static Sprinkler randomSprinkler() {
        return new Sprinkler(
                new Point2D.Double(10 * random.nextDouble(), 10 * random.nextDouble()),
                10 * random.nextDouble(),
                Math.PI * 2 * random.nextDouble(),
                Math.PI * 2 * random.nextDouble(),
                null);
    }
}
