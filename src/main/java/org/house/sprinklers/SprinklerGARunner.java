package org.house.sprinklers;

import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.population.Population;
import org.house.sprinklers.population.SprinklerValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.*;

@Slf4j
public class SprinklerGARunner {

    public static void main(String[] args) throws Exception {
        // UI stuff
        System.setProperty("org.lwjgl.librarypath", "/Users/alexdobjanschi/workspace/sprinkler-ga-generator/target/natives");

        ApplicationContext appCtx = new AnnotationConfigApplicationContext(
                SprinklerGARunner.class.getPackage().getName());

        ExecutorService executor = appCtx.getBean(ExecutorService.class);

        final GameLogic gameLogic = appCtx.getBean(GameLogic.class);

        SprinklerValidator validator = appCtx.getBean(SprinklerValidator.class);

        int populationSize = 10;
        final Population population = new Population(populationSize, true);
        population.initializePopulation(Optional.of(validator));

        gameLogic.setInitialPopulation(population);

        final GameRenderer gameRenderer = appCtx.getBean(GameRenderer.class);

        executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                while (true) {

                    // A runner that keeps on syncing between logic and UI.
                    Population population = gameLogic.getCurrentPopulation();
                    //int anInt = random.nextInt(population.getPopulation().length);
                    int anInt = 0;
                    gameRenderer.setSprinklerSystem(population.getPopulation().get(anInt));

                    TimeUnit.MILLISECONDS.sleep(50);
                }

                /*return null;*/
            }
        });

        executor.submit(gameRenderer);
        executor.submit(gameLogic);


    }
}
