package org.house.sprinklers;

import com.google.common.collect.ImmutableSet;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.fitness.SimpleFitnessCalculator;
import org.house.sprinklers.genetics.DefaultPopulationListener;
import org.house.sprinklers.genetics.PopulationListener;
import org.house.sprinklers.metrics.InMemoryRecorderService;
import org.house.sprinklers.metrics.RecorderService;
import org.house.sprinklers.population.CommonSenseSprinklerValidator;
import org.house.sprinklers.population.CompositeSprinklerValidator;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.population.TerrainSprinklerValidator;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan
public class SprinklerConfiguration {

    @Bean
    ExecutorService executor() {
        return Executors.newFixedThreadPool(20);
    }

    @Bean
    Terrain terrain() throws Exception {
        return new Terrain.TerrainLoader().load(resourceAsStream("terrain.in"));
    }

    private static InputStream resourceAsStream(String byName) {
        return SprinklerConfiguration.class.getResourceAsStream(byName);
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
    SprinklerValidator sprinklerValidator() throws Exception {
        return new CompositeSprinklerValidator(ImmutableSet.of(
                        new CommonSenseSprinklerValidator(),
                        new TerrainSprinklerValidator(terrain()))
        );
    }

    @Bean
    GameRenderer gameRenderer(Terrain terrain) {
        return new GameRenderer(terrain);
    }

    @Bean
    PopulationListener populationListener() {
        return new DefaultPopulationListener(recorderService());
    }
}
