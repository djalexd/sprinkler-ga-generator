package org.house.sprinklers.population;

import com.google.common.base.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;

import java.awt.geom.Point2D;
import java.util.Random;

@Slf4j
@Data
public class Population {

    private static final Random random = new Random(7983L);

    private SprinklerSystem[] population;

    public Population(int size, boolean initialize) {
        this.population = new SprinklerSystem[size];
        if (initialize) {
            initializePopulation(Optional.<SprinklerValidator>absent());
        }
    }

    public void initializePopulation(Optional<SprinklerValidator> validator) {
        log.info("Initialize population of {} with random data", population.length);
        for (int i = 0; i < population.length; i++) {
            population[i] = randomSprinklerSystem(validator);
        }
    }

    private SprinklerSystem randomSprinklerSystem(Optional<SprinklerValidator> validator) {
        // Random count of sprinklers
        Sprinkler[] sprinklers = new Sprinkler[random.nextInt(10)];
        for (int i = 0; i < sprinklers.length; i++) {

            boolean found = false;

            if (validator.isPresent()) {

                while (!found) {
                    sprinklers[i] = randomSprinkler();
                    try {
                        validator.get().validate(sprinklers[i]);
                        found = true;
                    } catch (InvalidSprinklerException e) {
                        // Thrown if sprinkler is invalid, this means the sprinkler was
                        // not found yet (it's not even valid.
                    }
                }

            } else {
                sprinklers[i] = randomSprinkler();
            }
        }

        return new SprinklerSystem(sprinklers);
    }

    private Sprinkler randomSprinkler() {
        return new Sprinkler(
                new Point2D.Double(10 * random.nextDouble(), 10 * random.nextDouble()),
                10 * random.nextDouble(),
                Math.PI * 2 * random.nextDouble(),
                Math.PI * 2 * random.nextDouble(),
                null);
    }
}
