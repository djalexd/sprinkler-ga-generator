package org.house.sprinklers;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.OnePointVariableLengthCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.RandomGeneMutation;
import org.apache.commons.math3.genetics.SmallChangeGeneGenerator;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.fitness.SimpleFitnessCalculator;
import org.house.sprinklers.genetics.DefaultPopulationListener;
import org.house.sprinklers.genetics.ListeningGeneticAlgorithm;
import org.house.sprinklers.genetics.PopulationListener;
import org.house.sprinklers.genetics.SprinklersChromosome;
import org.house.sprinklers.metrics.InMemoryRecorderService;
import org.house.sprinklers.metrics.RecorderService;
import org.house.sprinklers.population.CommonSenseSprinklerValidator;
import org.house.sprinklers.population.CompositeSprinklerValidator;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.population.TerrainSprinklerValidator;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Configuration
@ComponentScan
@EnableConfigurationProperties
@org.springframework.context.annotation.PropertySource("classpath:ga.properties")
public class SprinklerConfiguration {

    private static RandomGenerator randomGenerator = new JDKRandomGenerator();

    @Autowired
    private GeneticAlgorithmProperties geneticAlgorithmProperties;
    @Autowired
    private ExecutorProperties executorProperties;
    @Autowired
    private TerrainProperties terrainProperties;

    @Bean
    ExecutorService executor() {
        return Executors.newFixedThreadPool(executorProperties.getNThreads());
    }

    @Bean
    Terrain terrain() {
        try {
            return new Terrain.TerrainLoader().load(terrainProperties.terrainAsResource().getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load Terrain", e);
        }
    }

    @Bean
    RecorderService recorderService() {
        return new InMemoryRecorderService();
    }

    @Bean
    FitnessInputCalculator fitnessInputCalculator() {
        return new FitnessInputCalculator(executor(), recorderService());
    }

    @Bean
    FitnessCalculator fitnessCalculator() {
        return new SimpleFitnessCalculator();
    }

    @Bean
    SprinklerValidator sprinklerValidator() {
        return new CompositeSprinklerValidator(ImmutableSet.of(
                        new CommonSenseSprinklerValidator(),
                        new TerrainSprinklerValidator(terrain()))
        );
    }

    @Bean
    GameRenderer gameRenderer() {
        return new GameRenderer(terrain());
    }

    @Bean
    PopulationListener populationListener() {
        return new DefaultPopulationListener(recorderService(), gameRenderer());
    }

    @Bean
    Function<Sprinkler, Sprinkler> geneGenerator() {
        return new SmallChangeGeneGenerator(sprinklerValidator());
    }

    @Bean
    MutationPolicy mutation() {
        return new RandomGeneMutation<>(geneGenerator());
    }

    @Bean
    CrossoverPolicy crossover() {
        return new OnePointVariableLengthCrossover<>(geneticAlgorithmProperties.getCrossover().getMinimumLength());
    }

    @SuppressWarnings("unchecked")
    @Bean
    GeneticAlgorithm geneticAlgorithm() {
        return new ListeningGeneticAlgorithm(
                crossover(),
                geneticAlgorithmProperties.getCrossoverRate(),
                mutation(),
                geneticAlgorithmProperties.getMutationRate(),
                new TournamentSelection(geneticAlgorithmProperties.getTournamentArity()),
                recorderService(),
                populationListener());
    }

    @Bean
    Population initialPopulation() {
        final Chromosome[] chromosomes = new Chromosome[geneticAlgorithmProperties.getPopulation().getInitialSize()];
        Arrays.setAll(chromosomes, i -> randomChromosome(sprinklerValidator(), geneGenerator(), fitnessCalculator(), fitnessInputCalculator(), terrain()));
        return new ElitisticListPopulation(
                Arrays.asList(chromosomes),
                geneticAlgorithmProperties.getPopulation().getMaximumSize(),
                geneticAlgorithmProperties.getElitismRate());
    }

    private static SprinklersChromosome randomChromosome(final SprinklerValidator validator,
                                                         final Function<Sprinkler, Sprinkler> geneGenerator,
                                                         final FitnessCalculator fitnessCalculator,
                                                         final FitnessInputCalculator fitnessInputCalculator,
                                                         final Terrain terrain) {
        return new SprinklersChromosome(
                randomSprinklers(geneGenerator),
                validator,
                fitnessCalculator,
                fitnessInputCalculator,
                terrain);
    }

    private static List<Sprinkler> randomSprinklers(Function<Sprinkler, Sprinkler> geneGenerator) {
        // Random count of sprinklers
        final Sprinkler[] sprinklers = new Sprinkler[1 + randomGenerator.nextInt(10)];
        Arrays.setAll(sprinklers, i -> geneGenerator.apply(null));
        return Arrays.asList(sprinklers);
    }
}
