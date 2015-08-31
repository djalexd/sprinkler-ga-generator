package org.house.sprinklers.genetics;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.house.sprinklers.MathUtils;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This simple crossover determines one cut-point for each parent and
 * splices each genome around that point.
 */
@Slf4j
public class CutAndSpliceCrossover implements Crossover {

    private static final Random random = new Random();

    // Create a new Store that converts from a sprinkler system to its genome representation
    private static final SprinklerSystemGenome.GenomeStore store = new SprinklerSystemGenome.GenomeStore();
    // Create a new Loader that convert from genome representation to sprinkler system
    private final SprinklerSystemGenome.GenomeLoader loader = new SprinklerSystemGenome.GenomeLoader(Optional.<SprinklerValidator>absent());

    @Override
    public List<SprinklerSystem> generateChildren(SprinklerSystem parentA, SprinklerSystem parentB) {
        // Delegate
        final SprinklerSystemGenome[] childrenGenomes = generateChildren(convertToGenome(parentA), convertToGenome(parentB));

        return ImmutableList.copyOf(Lists.transform(Arrays.asList(childrenGenomes), new Function<SprinklerSystemGenome, SprinklerSystem>() {
            @Nullable
            @Override
            public SprinklerSystem apply(SprinklerSystemGenome input) {
                return loader.load(input);
            }
        }));
    }

    private SprinklerSystemGenome convertToGenome(SprinklerSystem parentA) {
        return store.save(parentA);
    }

    @Override
    public SprinklerSystemGenome[] generateChildren(SprinklerSystemGenome parentA, SprinklerSystemGenome parentB) {
        int cutPointA = safelyComputeOffsetCutPoint(parentA), cutPointB = safelyComputeOffsetCutPoint(parentB);

        // Print both slices (children)

        log.debug("CutAndSplice take {} ({}%) from parent A and {} ({}%) from parent B",
                cutPointA,
                MathUtils.toPercentage((float) cutPointA / parentA.getGenes().length, 2),
                parentB.getGenes().length - cutPointB,
                MathUtils.toPercentage((float) (parentB.getGenes().length - cutPointB) / parentB.getGenes().length, 2));

        log.debug("CutAndSplice take {} ({}%) from parent B and {} ({}%) from parent A",
                cutPointB,
                MathUtils.toPercentage((float) cutPointB / parentB.getGenes().length, 2),
                parentA.getGenes().length - cutPointA,
                MathUtils.toPercentage((float) (parentA.getGenes().length - cutPointA) / parentA.getGenes().length, 2));

        return new SprinklerSystemGenome[] {
                new SprinklerSystemGenome(mergeGenome(lowerCut(parentA, cutPointA), upperCut(parentB, cutPointB))),
                new SprinklerSystemGenome(mergeGenome(lowerCut(parentB, cutPointB), upperCut(parentA, cutPointA)))
        };
    }

    private int safelyComputeOffsetCutPoint(SprinklerSystemGenome parent) {
        if (parent.getGenes().length > 40) {
            return 40 * (1 + random.nextInt(parent.getGenes().length / 40 - 1));
        } else {
            return 40 * random.nextInt(parent.getGenes().length / 40);
        }
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
