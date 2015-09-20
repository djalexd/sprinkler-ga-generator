package org.apache.commons.math3.genetics;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.house.sprinklers.GeneticAlgorithmProperties;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an one-point crossover policy, similar to
 * {@link org.apache.commons.math3.genetics.OnePointCrossover},
 * but this allows pairs to have any length, including different
 * values.
 *
 * <p/>Note that any length is possible, including zero-length or
 * 1-length chromosomes. In these cases:
 * <ul>
 *     <li>first is zero, second is greater than 2: select a crossover point
 *     from second chromosome only.</li>
 *     <li>first is one, second is greater than 2: </li>
 * </ul>
 */
public class OnePointVariableLengthCrossover<T> implements CrossoverPolicy {

    private static final int MAX_ATTEMPTS = 50;

    private GeneticAlgorithmProperties.CrossoverProperties crossoverProperties;

    private GeneticAlgorithmProperties.ChromosomeProperties chromosomeProperties;

    private RecorderService recorderService;

    public OnePointVariableLengthCrossover(GeneticAlgorithmProperties.CrossoverProperties crossoverProperties,
                                           GeneticAlgorithmProperties.ChromosomeProperties chromosomeProperties,
                                           RecorderService recorderService) {
        this.crossoverProperties = crossoverProperties;
        this.chromosomeProperties = chromosomeProperties;
        this.recorderService = recorderService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChromosomePair crossover(Chromosome first, Chromosome second) throws MathIllegalArgumentException {
        if (! (first instanceof AbstractListChromosome<?> && second instanceof AbstractListChromosome<?>)) {
            throw new MathIllegalArgumentException(LocalizedFormats.INVALID_FIXED_LENGTH_CHROMOSOME);
        }
        return crossover((AbstractListChromosome<T>) first, (AbstractListChromosome<T>) second);
    }

    private ChromosomePair crossover(final AbstractListChromosome<T> first,
                                     final AbstractListChromosome<T> second) throws DimensionMismatchException {
        // select a crossover point at random (0 and length makes no sense)
        final int minimumAllowedLength = crossoverProperties.getMinimumLength();
        final int lengthParent1 = first.getLength(),
                  lengthParent2 = second.getLength(),
                  minLengthParents = Math.min(lengthParent1, lengthParent2);
        if (lengthParent1 < minimumAllowedLength || lengthParent2 < minimumAllowedLength) {
            throw new DimensionMismatchException(minLengthParents, minimumAllowedLength);
        }

        // Now that we're sure a crossover is performed, increment counter
        recorderService.increment(MetricsConstants.COUNTER_GA_CROSSOVERS);

        final RandomGenerator rnd = GeneticAlgorithm.getRandomGenerator();

        /*
        final int crossoverIndexParent1 = rnd.nextInt(lengthParent1),
                  crossoverIndexParent2 = rnd.nextInt(lengthParent2);
        */

        int crossoverIndexParent1, crossoverIndexParent2;
        int counter = 0;
        while (true) {
            crossoverIndexParent1 = rnd.nextInt(lengthParent1);
            crossoverIndexParent2 = rnd.nextInt(lengthParent2);

            int l1 = crossoverIndexParent1 + lengthParent2 - crossoverIndexParent2,
                    l2 = crossoverIndexParent2 + lengthParent1 - crossoverIndexParent1;

            // Validate bounds, if min/max are valid, we have found valid crossover points.
            if (l1 > chromosomeProperties.getMinLength() && l1 < chromosomeProperties.getMaxLength() &&
                    l2 > chromosomeProperties.getMinLength() && l2 < chromosomeProperties.getMaxLength()) {
                break;
            }

            if (counter >= MAX_ATTEMPTS) {
                // TODO Metric.
                // Could not find any match in max attempts, just return
                // the 2 parents.
                return new ChromosomePair(first, second);
            }
            counter++;
        }

        // array representations of the parents
        final List<T> parent1Rep = first.getRepresentation();
        final List<T> parent2Rep = second.getRepresentation();
        // and of the children
        final List<T> child1Rep = new ArrayList<T>(minLengthParents);
        final List<T> child2Rep = new ArrayList<T>(minLengthParents);

        // copy the first part
        child1Rep.addAll(parent1Rep.subList(0, crossoverIndexParent1));
        child1Rep.addAll(parent2Rep.subList(crossoverIndexParent2, lengthParent2));

        child2Rep.addAll(parent2Rep.subList(0, crossoverIndexParent2));
        child2Rep.addAll(parent1Rep.subList(crossoverIndexParent1, lengthParent1));

        // Both children need to have valid chromosomes (length)

        return new ChromosomePair(first.newFixedLengthChromosome(child1Rep),
                second.newFixedLengthChromosome(child2Rep));
    }
}
