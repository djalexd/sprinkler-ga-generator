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
import org.house.sprinklers.genetics.SmallChangeGeneGenerator;
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
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

@Configuration
@ComponentScan
@EnableConfigurationProperties
@PropertySource("classpath:ga.properties")
public class SprinklerConfiguration {

    private static RandomGenerator randomGenerator = new JDKRandomGenerator();

    @Autowired
    private GeneticAlgorithmProperties geneticAlgorithmProperties;
    @Autowired
    private Terrain terrain;
    @Autowired
    private ExecutorService executorService;

    @Bean
    RecorderService recorderService() {
        return new InMemoryRecorderService();
    }

    @Bean
    FitnessInputCalculator fitnessInputCalculator() {
        return new FitnessInputCalculator(executorService, recorderService());
    }

    @Bean
    FitnessCalculator fitnessCalculator() {
        return new SimpleFitnessCalculator();
    }

    @Bean
    SprinklerValidator sprinklerValidator() {
        return new CompositeSprinklerValidator(ImmutableSet.of(
                new CommonSenseSprinklerValidator(),
                new TerrainSprinklerValidator(terrain))
        );
    }

    @Bean
    GameRenderer gameRenderer() {
        return new GameRenderer(terrain);
    }

    @Bean
    PopulationListener populationListener() {
        return new DefaultPopulationListener(recorderService(), gameRenderer());
    }

    @Bean
    Function<Sprinkler, Sprinkler> geneGenerator() {
        return new SmallChangeGeneGenerator(sprinklerValidator(), geneticAlgorithmProperties.getMutation(), recorderService());
    }

    @Bean
    MutationPolicy mutation() {
        return new RandomGeneMutation<>(
                geneGenerator(),
                geneticAlgorithmProperties.getMutation(),
                geneticAlgorithmProperties.getChromosome(),
                recorderService());
    }

    @Bean
    CrossoverPolicy crossover() {
        return new OnePointVariableLengthCrossover<>(geneticAlgorithmProperties.getCrossover().getMinimumLength(), recorderService());
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
        Arrays.setAll(chromosomes, i -> randomChromosome(sprinklerValidator(), geneGenerator(), fitnessCalculator(), fitnessInputCalculator(), terrain));
        return new ElitisticListPopulation(
                Arrays.asList(chromosomes),
                geneticAlgorithmProperties.getPopulation().getMaximumSize(),
                geneticAlgorithmProperties.getElitismRate());
    }

    private SprinklersChromosome randomChromosome(final SprinklerValidator validator,
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

    private List<Sprinkler> randomSprinklers(Function<Sprinkler, Sprinkler> geneGenerator) {
        // Random count of sprinklers
        final GeneticAlgorithmProperties.ChromosomeProperties props = geneticAlgorithmProperties.getChromosome();
        final Sprinkler[] sprinklers = new Sprinkler[props.getMinLength() + randomGenerator.nextInt(props.getMaxLength() - props.getMinLength())];
        Arrays.setAll(sprinklers, i -> geneGenerator.apply(null));
        return Arrays.asList(sprinklers);
    }
}
