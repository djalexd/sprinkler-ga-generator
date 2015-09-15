package org.house.sprinklers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.Population;
import org.house.sprinklers.genetics.SprinklersChromosome;
import org.house.sprinklers.metrics.InMemorySprinklerMetricsExtractor;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SprinklerCommonsGARunner {

    public static void main(String[] args) throws InterruptedException {
        // UI stuff
        System.setProperty("org.lwjgl.librarypath", "/Users/alexdobjanschi/workspace/sprinkler-ga-generator/target/natives");

        // Read a couple of Beans
        ApplicationContext appCtx = new AnnotationConfigApplicationContext(SprinklerConfiguration.class);

        /* Retrieve the ExecutorService so we can start rendering immediately */
        final ExecutorService executorService = appCtx.getBean(ExecutorService.class);

        /* Setup rendering pipeline */
        GameRenderer gm = appCtx.getBean(GameRenderer.class);
        executorService.submit(gm);

        final RecorderService recorderService = appCtx.getBean(RecorderService.class);

        final GeneticAlgorithm ga = appCtx.getBean(GeneticAlgorithm.class);

        final Population initial = appCtx.getBean(Population.class);

        final int numGenerations = Integer.valueOf(appCtx.getEnvironment().getProperty("geneticAlgorithm.generations"));

        final long start = System.currentTimeMillis();

        // run the algorithm
        // stopping condition
        final Population finalPopulation = ga.evolve(initial, new FixedGenerationCount(numGenerations));

        // best chromosome from the final population
        final SprinklersChromosome fittestChromosome = (SprinklersChromosome) finalPopulation.getFittestChromosome();

        final long end = System.currentTimeMillis();

        log.info("Best chromosome after {} generations, computed in {} ms: {}", numGenerations, end - start, fittestChromosome);

        // Display statistics
        final String separator = "----------------------------";
        final InMemorySprinklerMetricsExtractor extractor = appCtx.getBean(InMemorySprinklerMetricsExtractor.class);
        log.info("[Terrain-Sprinkler ops] Total number = {} / Total exec time = {} millis / average exec time = {} millis",
                extractor.numberOfTerrainSprinklerOps(),
                extractor.totalExecTimeTerrainSprinklerOps(),
                extractor.averageExecTimeTerrainSprinklerOps());
        log.info("{}", separator);
        log.info("[Genetic algorithm] Generations count = {} / Total individuals = {}",
                extractor.generationsCount(),
                extractor.totalIndividualsCount());
        log.info("{}", separator);
        log.info("[Errors] {}", extractor.getErrors());

        log.info("{}", separator);
        log.info("[Fitness over generations]");
        recorderService.getMetricValues(MetricsConstants.METRIC_GA_GENERATION_FITNESS)
                .stream()
                .forEach(v -> {
                    log.info("{}", v);
                });

        executorService.awaitTermination(100, TimeUnit.MINUTES);
    }
}
