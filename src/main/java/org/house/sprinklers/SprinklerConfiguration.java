package org.house.sprinklers;

import com.google.common.collect.ImmutableSet;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.fitness.SimpleFitnessCalculator;
import org.house.sprinklers.population.CommonSenseSprinklerValidator;
import org.house.sprinklers.population.CompositeSprinklerValidator;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.population.TerrainSprinklerValidator;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
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
    FitnessInputCalculator fitnessInputCalculator() {
        return new FitnessInputCalculator(executor());
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
}
