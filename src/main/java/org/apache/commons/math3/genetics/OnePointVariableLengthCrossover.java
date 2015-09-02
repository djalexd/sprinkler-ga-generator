package org.apache.commons.math3.genetics;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an one-point crossover policy, similar to
 * {@link org.apache.commons.math3.genetics.OnePointCrossover},
 * but this allows pairs to have any length, including different
 * values.
 */
public class OnePointVariableLengthCrossover<T> implements CrossoverPolicy {
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
        final int lengthParent1 = first.getLength(),
                lengthParent2 = second.getLength(),
                minLengthParents = Math.min(lengthParent1, lengthParent2);
        if (minLengthParents < 3) {
            throw new DimensionMismatchException(minLengthParents, 3);
        }
        final int crossoverIndex = 1 + (GeneticAlgorithm.getRandomGenerator().nextInt(minLengthParents-2));

        // array representations of the parents
        final List<T> parent1Rep = first.getRepresentation();
        final List<T> parent2Rep = second.getRepresentation();
        // and of the children
        final List<T> child1Rep = new ArrayList<T>(minLengthParents);
        final List<T> child2Rep = new ArrayList<T>(minLengthParents);

        // copy the first part
        child1Rep.addAll(parent1Rep.subList(0, crossoverIndex));
        child1Rep.addAll(parent2Rep.subList(crossoverIndex, lengthParent2));

        child2Rep.addAll(parent2Rep.subList(0, crossoverIndex));
        child2Rep.addAll(parent1Rep.subList(crossoverIndex, lengthParent1));

        return new ChromosomePair(first.newFixedLengthChromosome(child1Rep),
                second.newFixedLengthChromosome(child2Rep));
    }
}
