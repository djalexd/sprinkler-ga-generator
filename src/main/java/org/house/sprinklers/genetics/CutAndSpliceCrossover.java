package org.house.sprinklers.genetics;

import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;

import java.util.Arrays;
import java.util.Random;

/**
 * This simple crossover determines one cut-point for each parent and
 * splices each genome around that point.
 */
public class CutAndSpliceCrossover implements Crossover {

    private static final Random random = new Random(45125123L);

    @Override
    public SprinklerSystemGenome[] generateChildren(SprinklerSystemGenome parentA, SprinklerSystemGenome parentB) {
        int cutPointA = 40 * random.nextInt(parentA.getGenes().length / 40),
                cutPointB = 40 * random.nextInt(parentB.getGenes().length / 40);

        return new SprinklerSystemGenome[] {
                new SprinklerSystemGenome(mergeGenome(lowerCut(parentA, cutPointA), upperCut(parentB, cutPointB))),
                new SprinklerSystemGenome(mergeGenome(lowerCut(parentB, cutPointB), upperCut(parentA, cutPointA)))
        };
    }

    private byte[] lowerCut(SprinklerSystemGenome genome, int cutPoint) {
        return Arrays.copyOfRange(genome.getGenes(), 0, cutPoint);
    }

    private byte[] upperCut(SprinklerSystemGenome genome, int cutPoint) {
        return Arrays.copyOfRange(genome.getGenes(), cutPoint, genome.getGenes().length);
    }

    private byte[] mergeGenome(byte[] lower, byte[] upper) {
        byte[] copy = new byte[lower.length + upper.length];

        System.arraycopy(lower, 0, copy, 0, lower.length);
        System.arraycopy(upper, 0, copy, lower.length, upper.length);

        return copy;
    }
}
