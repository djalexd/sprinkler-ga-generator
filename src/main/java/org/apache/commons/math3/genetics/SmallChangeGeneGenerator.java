package org.apache.commons.math3.genetics;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.house.sprinklers.population.InvalidSprinklerException;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.Sprinkler;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * Generates a new gene, based on given original one.
 *
 * <p/>If original is <code>null</code>, a new gene is always created.
 *
 * <p/>This GeneGenerator implementation can produce slightly different
 * genes (sprinklers), or a completely new one.
 */
@Slf4j
public class SmallChangeGeneGenerator implements Function<Sprinkler, Sprinkler> {

    private static final int WARNING_COUNTER = 100;

    private static final RandomGenerator randomGenerator = new JDKRandomGenerator();

    private static final DoubleSupplier[] functions = new DoubleSupplier[] {
            () -> 10 * randomGenerator.nextDouble(),
            () -> 10 * randomGenerator.nextDouble(),
            () -> 10 * randomGenerator.nextDouble(),
            () -> 2 * Math.PI * randomGenerator.nextDouble(),
            () -> 2 * Math.PI * randomGenerator.nextDouble()
    };

    private SprinklerValidator sprinklerValidator;

    public SmallChangeGeneGenerator(SprinklerValidator sprinklerValidator) {
        this.sprinklerValidator = sprinklerValidator;
    }

    @Override
    public Sprinkler apply(Sprinkler original) {
        final boolean isNewGene = randomGenerator.nextBoolean();
        if (original == null || isNewGene) {
            return generateValidSprinkler();
        } else {
            return generateMutatedValidSprinkler(original);
        }
    }

    /** Internal methods, used to generate slight variations of a specific gene.
     *  The disadvantage of 'slight variation' is that we need to know internal
     *  representation. */

    private Sprinkler generateMutatedValidSprinkler(Sprinkler original) {
        // All fields can be changed, but individually
        final boolean[] newFields = new boolean[] {
                randomGenerator.nextBoolean(),
                randomGenerator.nextBoolean(),
                randomGenerator.nextBoolean(),
                randomGenerator.nextBoolean(),
                randomGenerator.nextBoolean()
        };

        final double[] fieldValues = new double[] {
                original.getPosition().getX(),
                original.getPosition().getY(),
                original.getRange(),
                original.getStartAngle(),
                original.getEndAngle()
        };

        double[] solution = Arrays.copyOf(fieldValues, fieldValues.length);
        for (int i = 0; i < newFields.length; i++) {
            if (newFields[i]) {
                boolean found = false;
                int attempts = 0;
                while (!found) {
                    final double[] attempt = Arrays.copyOf(solution, solution.length);
                    attempt[i] = functions[i].getAsDouble();

                    // Check that our slight modification still validates this gene.
                    final Sprinkler candidate = buildFromDoubles(attempt);
                    if (isValidSprinkler(candidate)) {
                        found = true;
                        solution = Arrays.copyOf(attempt, solution.length);
                    }

                    attempts ++;
                    if (attempts > WARNING_COUNTER) {
                        log.warn("Unable to find valid value for field {} in {} attempts. Solution is: {}",
                                i, attempts, Arrays.toString(solution));
                        // Break the loop and continue with whatever value was before
                        found = true;
                    }
                }

            }
        }

        return buildFromDoubles(solution);
    }

    private Sprinkler generateValidSprinkler() {
        while (true) {
            Sprinkler aRandomSprinkler = randomSprinkler();
            try {
                sprinklerValidator.validate(aRandomSprinkler);
                return aRandomSprinkler;
            } catch (InvalidSprinklerException e) {
                // Thrown if sprinkler is invalid, this means the sprinkler was
                // not found yet (it's not even valid.
            }
        }
    }

    private Sprinkler randomSprinkler() {
        return new Sprinkler(
                new Point2D.Double(
                        functions[0].getAsDouble(),
                        functions[1].getAsDouble()),
                functions[2].getAsDouble(),
                functions[3].getAsDouble(),
                functions[4].getAsDouble());
    }

    private Sprinkler buildFromDoubles(double[] solution) {
        return new Sprinkler(
                new Point2D.Double(solution[0], solution[1]),
                solution[2],
                solution[3],
                solution[4]);
    }

    private boolean isValidSprinkler(Sprinkler sprinkler) {
        try {
            sprinklerValidator.validate(sprinkler);
            return true;
        } catch (InvalidSprinklerException e) {
            return false;
        }
    }

}
