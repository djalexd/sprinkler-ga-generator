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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Configuration
@ComponentScan
// TODO Can I do this?
/*@org.springframework.context.annotation.PropertySource("ga.yaml")*/
public class SprinklerConfiguration {
    private static RandomGenerator randomGenerator = new JDKRandomGenerator();

    private @Inject Environment environment;

    static PropertySource yamlPropertySource() throws IOException {
        // todo really, environment is injected but we don't read
        // all possible files in order. Long way of saying I'm lazy
        final String yamlResource = "ga.yaml";
        return new YamlPropertySourceLoader().load(yamlResource, new ClassPathResource(yamlResource), null);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        final MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(yamlPropertySource());

        final EnvironmentAwarePropertySourcesPlaceholderConfigurer placeholderConfigurer = new EnvironmentAwarePropertySourcesPlaceholderConfigurer();
        placeholderConfigurer.propertySources = sources;
        return placeholderConfigurer;
    }

    @Value("${executors.threadPool}")
    private int nThreads;

    @Bean
    ExecutorService executor() {
        return Executors.newFixedThreadPool(nThreads);
    }

    @Value("${terrain.data}")
    private String terrainResource;

    @Bean
    Terrain terrain() {
        try {
            return new Terrain.TerrainLoader().load(new ClassPathResource(terrainResource).getInputStream());
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

    @Value("${geneticAlgorithm.crossoverRate}")
    private double crossoverRate;
    @Value("${geneticAlgorithm.tournamentArity}")
    private int tournamentArity;
    @Value("${geneticAlgorithm.mutationRate}")
    private double mutationRate;

    @Bean
    Function<Sprinkler, Sprinkler> geneGenerator() {
        return new SmallChangeGeneGenerator(sprinklerValidator());
    }

    @Bean
    MutationPolicy mutation() {
        return new RandomGeneMutation<>(geneGenerator());
    }

    @Value("${geneticAlgorithm.crossover.minimumLength}")
    private int crossoverMinimumAllowedLength;

    @Bean
    CrossoverPolicy crossover() {
        return new OnePointVariableLengthCrossover<>(crossoverMinimumAllowedLength);
    }

    @SuppressWarnings("unchecked")
    @Bean
    GeneticAlgorithm geneticAlgorithm() {
        return new ListeningGeneticAlgorithm(
                crossover(),
                crossoverRate,
                mutation(),
                mutationRate,
                new TournamentSelection(tournamentArity),
                recorderService(),
                populationListener());
    }

    @Value("${geneticAlgorithm.population.initialSize}")
    private int populationInitialSize;
    @Value("${geneticAlgorithm.population.maximumSize}")
    private int populationLimit;
    @Value("${geneticAlgorithm.elitismRate}")
    private double elitismRate;

    @Bean
    Population initialPopulation() {
        final Chromosome[] chromosomes = new Chromosome[populationInitialSize];
        Arrays.setAll(chromosomes, i -> randomChromosome(sprinklerValidator(), geneGenerator(), fitnessCalculator(), fitnessInputCalculator(), terrain()));
        return new ElitisticListPopulation(Arrays.asList(chromosomes), populationLimit, elitismRate);
    }

    private static SprinklersChromosome randomChromosome(final SprinklerValidator validator,
                                                         final Function<Sprinkler, Sprinkler> geneGenerator,
                                                         final FitnessCalculator fitnessCalculator,
                                                         final FitnessInputCalculator fitnessInputCalculator,
                                                         final Terrain terrain) {
        return new SprinklersChromosome(randomSprinklers(geneGenerator), validator, fitnessCalculator, fitnessInputCalculator, terrain);
    }

    private static List<Sprinkler> randomSprinklers(Function<Sprinkler, Sprinkler> geneGenerator) {
        // Random count of sprinklers
        final Sprinkler[] sprinklers = new Sprinkler[1 + randomGenerator.nextInt(10)];
        Arrays.setAll(sprinklers, i -> geneGenerator.apply(null));
        return Arrays.asList(sprinklers);
    }


    private static class EnvironmentAwarePropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer
        implements EnvironmentAware, InitializingBean {

        MutablePropertySources propertySources;
        ConfigurableEnvironment environment;

        @Override
        public void setEnvironment(Environment environment) {
            super.setEnvironment(environment);
            if (environment instanceof ConfigurableEnvironment) {
                this.environment = (ConfigurableEnvironment) environment;
            }
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            if (environment == null) {
                return;
            }
            final MutablePropertySources envPropertySources =  environment.getPropertySources();
            propertySources.forEach(envPropertySources::addFirst);
        }
    }
}
