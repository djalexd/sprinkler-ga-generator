package org.house.sprinklers.genetics;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;

import java.util.Arrays;
import java.util.Random;

@Slf4j
public class RandomMutation implements Mutation {
    private static final double DEFAULT_CHANGE = 0.001;
    private static final Random random = new Random(561263L);

    private final double chance;

    public RandomMutation() {
        this(DEFAULT_CHANGE);
    }
    public RandomMutation(double chance) {
        Preconditions.checkArgument(chance > 0.0, "Mutation chance must be positive");
        this.chance = chance;
    }

    @Override
    public SprinklerSystemGenome generateMutation(SprinklerSystemGenome original) {

        byte[] copy = Arrays.copyOf(original.getGenes(), original.getGenes().length);
        for (int i = 0; i < copy.length; i++) {

            double r = Math.abs(random.nextDouble());
            if (r < chance) {
                // Really low chance of mutation, but it still exists.
                copy[i] = Integer.valueOf(random.nextInt(255)).byteValue();

                log.debug("Random gene mutation occurred at {}/{} with value {}",
                        i, copy.length, copy[i]);
            }

        }

        return new SprinklerSystemGenome(copy);
    }
}
