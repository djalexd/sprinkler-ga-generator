package org.house.sprinklers.population;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;

@Slf4j
@Data
public class Population {

    private static final Random random = new Random(7983L);

    private List<SprinklerSystem> population;
    private int expectedSize;

    public Population(List<SprinklerSystem> population) {
        this.population = population;
        this.expectedSize = population.size();
    }

    public Population(int size, boolean initialize) {
        this.population = Lists.newArrayList();
        this.expectedSize = size;
        if (initialize) {
            initializePopulation(Optional.<SprinklerValidator>absent());
        }
    }

    public void initializePopulation(Optional<SprinklerValidator> validator) {
        log.info("Initialize population of {} with random data", expectedSize);
        for (int i = 0; i < expectedSize; i++) {
            population.add(randomSprinklerSystem(validator));
        }
    }

    private SprinklerSystem randomSprinklerSystem(Optional<SprinklerValidator> validator) {
        // Random count of sprinklers
        int numSprinklers = random.nextInt(10);
        List<Sprinkler> sprinklers = Lists.newArrayListWithExpectedSize(numSprinklers);
        for (int i = 0; i < numSprinklers; i++) {

            boolean found = false;

            if (validator.isPresent()) {

                while (!found) {
                    Sprinkler aRandomSprinkler = randomSprinkler();
                    try {
                        validator.get().validate(aRandomSprinkler);
                        found = true;
                        sprinklers.add(aRandomSprinkler);

                    } catch (InvalidSprinklerException e) {
                        // Thrown if sprinkler is invalid, this means the sprinkler was
                        // not found yet (it's not even valid.
                    }
                }

            } else {
                sprinklers.add(randomSprinkler());
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
