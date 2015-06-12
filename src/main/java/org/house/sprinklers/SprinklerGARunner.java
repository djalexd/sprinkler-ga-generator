package org.house.sprinklers;

import com.google.common.base.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.fitness.SimpleFitnessCalculator;
import org.house.sprinklers.genetics.Crossover;
import org.house.sprinklers.genetics.CutAndSpliceCrossover;
import org.house.sprinklers.genetics.Mutation;
import org.house.sprinklers.genetics.RandomMutation;
import org.house.sprinklers.population.CommonSenseSprinklerValidator;
import org.house.sprinklers.population.Population;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.Terrain;

import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.*;

@Slf4j
@Data
public class SprinklerGARunner  {

    private static final Random random = new Random();

    public static void main(String[] args) throws Exception {

        final ExecutorService executor = Executors.newFixedThreadPool(20);

        final Terrain terrain = new Terrain.TerrainLoader().load(resourceAsStream("terrain.in"));

        SprinklerValidator validator = new CommonSenseSprinklerValidator(Optional.of(terrain));

        FitnessInputCalculator fitnessInputCalculator = new FitnessInputCalculator(executor);

        FitnessCalculator fitnessCalculator = new SimpleFitnessCalculator();

        Crossover crossover = new CutAndSpliceCrossover();

        Mutation mutation = new RandomMutation();

        // UI stuff
        System.setProperty("org.lwjgl.librarypath", "/Users/alexdobjanschi/workspace/sprinkler-ga-generator/target/natives");

        int populationSize = 100;
        final Population population = new Population(populationSize, false);
        population.initializePopulation(Optional.of(validator));

        final GameLogic gameLogic = new GameLogic(
                populationSize,
                terrain, fitnessInputCalculator, fitnessCalculator,
                validator, crossover, mutation, executor);
        gameLogic.setInitialPopulation(population);

        final GameRenderer gameRenderer = new GameRenderer(terrain);

        executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                while (true) {

                    // A runner that keeps on syncing between logic and UI.
                    Population population = gameLogic.getCurrentPopulation();
                    //int anInt = random.nextInt(population.getPopulation().length);
                    int anInt = 0;
                    gameRenderer.setSprinklerSystem(population.getPopulation()[anInt]);

                    TimeUnit.MILLISECONDS.sleep(50);
                }

                /*return null;*/
            }
        });

        executor.submit(gameRenderer);
        executor.submit(gameLogic);


    }

    private static InputStream resourceAsStream(String byName) {
        return SprinklerGARunner.class.getResourceAsStream(byName);
    }
}
