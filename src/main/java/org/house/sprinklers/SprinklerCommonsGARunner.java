package org.house.sprinklers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.OnePointVariableLengthCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.RandomGeneMutation;
import org.apache.commons.math3.genetics.SmallChangeGeneGenerator;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.genetics.SprinklersChromosome;
import org.house.sprinklers.population.InvalidSprinklerException;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Slf4j
public class SprinklerCommonsGARunner {

    private static final Random random = new Random();
    private static final int NUM_GENERATIONS = 100;
    private static final int TOURNAMENT_ARITY = 2;

    public static void main(String[] args) {
        // UI stuff
        System.setProperty("org.lwjgl.librarypath", "/Users/alexdobjanschi/workspace/sprinkler-ga-generator/target/natives");

        // Read a couple of Beans
        ApplicationContext appCtx = new AnnotationConfigApplicationContext(
                SprinklerCommonsGARunner.class.getPackage().getName());

        // initial population
        final SprinklerValidator sprinklerValidator = appCtx.getBean(SprinklerValidator.class);
        // Generates
        final Function<Sprinkler, Sprinkler> geneGenerator = new SmallChangeGeneGenerator(sprinklerValidator);

        // initialize a new genetic algorithm
        @SuppressWarnings("unchecked")
        final GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointVariableLengthCrossover<Sprinkler>(),
                1,
                new RandomGeneMutation(geneGenerator),
                0.10,
                new TournamentSelection(TOURNAMENT_ARITY));

        final Population initial = getInitialPopulation(
                50, 200,
                sprinklerValidator,
                appCtx.getBean(FitnessCalculator.class),
                appCtx.getBean(FitnessInputCalculator.class),
                appCtx.getBean(Terrain.class));

        // stopping condition
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        final long start = System.currentTimeMillis();

        // run the algorithm
        final Population finalPopulation = ga.evolve(initial, stopCond);

        // best chromosome from the final population
        final SprinklersChromosome fittestChromosome = (SprinklersChromosome) finalPopulation.getFittestChromosome();

        final long end = System.currentTimeMillis();

        log.info("Best chromosome after {} generations, computed in {} ms: {}", NUM_GENERATIONS, end - start, fittestChromosome);

        //
        GameRenderer gm = appCtx.getBean(GameRenderer.class);
        gm.setSprinklers(fittestChromosome.getRepresentation());

        //final ExecutorService executorService = appCtx.getBean(ExecutorService.class);
        //executorService.submit(gm);

        gm.run();

        //executorService.awaitTermination()
    }

    private static Population getInitialPopulation(final int populationInitialSize,
                                                   final int populationLimit,
                                                   SprinklerValidator validator,
                                                   FitnessCalculator fitnessCalculator,
                                                   FitnessInputCalculator fitnessInputCalculator,
                                                   Terrain terrain) {

        final Chromosome[] chromosomes = new Chromosome[populationInitialSize];
        Arrays.setAll(chromosomes, i -> randomChromosome(validator, fitnessCalculator, fitnessInputCalculator, terrain));
        return new ElitisticListPopulation(Arrays.asList(chromosomes), populationLimit, 0.9);
    }


    private static SprinklersChromosome randomChromosome(SprinklerValidator validator, FitnessCalculator fitnessCalculator, FitnessInputCalculator fitnessInputCalculator, Terrain terrain) {
        return new SprinklersChromosome(randomSprinklers(validator), validator, fitnessCalculator, fitnessInputCalculator, terrain);
    }

    private static List<Sprinkler> randomSprinklers(SprinklerValidator validator) {
        // Random count of sprinklers
        final Sprinkler[] sprinklers = new Sprinkler[3 + random.nextInt(10)];

        if (validator != null) {
            Arrays.setAll(sprinklers, i -> randomValidSprinkler(validator));
        } else {
            Arrays.setAll(sprinklers, i -> randomSprinkler());
        }

        return Arrays.asList(sprinklers);
    }

    private static Sprinkler randomValidSprinkler(SprinklerValidator validator) {
        while (true) {
            Sprinkler aRandomSprinkler = randomSprinkler();
            try {
                validator.validate(aRandomSprinkler);
                return aRandomSprinkler;
            } catch (InvalidSprinklerException e) {
                // Thrown if sprinkler is invalid, this means the sprinkler was
                // not found yet (it's not even valid.
            }
        }
    }

    private static Sprinkler randomSprinkler() {
        return new Sprinkler(
                new Point2D.Double(10 * random.nextDouble(), 10 * random.nextDouble()),
                10 * random.nextDouble(),
                Math.PI * 2 * random.nextDouble(),
                Math.PI * 2 * random.nextDouble());
    }
}
