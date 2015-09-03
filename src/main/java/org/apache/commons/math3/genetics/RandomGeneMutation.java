package org.apache.commons.math3.genetics;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Mutates a chromosome based on {@link java.util.function.Function}
 */
public class RandomGeneMutation<T> implements MutationPolicy {

    private Function<T, T> geneGenerator;

    public RandomGeneMutation(Function<T, T> geneGenerator) {
        this.geneGenerator = geneGenerator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Chromosome mutate(Chromosome original) throws MathIllegalArgumentException {
        // Make sure we have an AbstractListChromosome, so we
        // can extract its List representation
        if (!(original instanceof AbstractListChromosome<?>)) {
            throw new MathIllegalArgumentException(LocalizedFormats.RANDOMKEY_MUTATION_WRONG_CLASS,
                    original.getClass().getSimpleName());
        }

        final AbstractListChromosome<T> originalRk = (AbstractListChromosome<T>) original;
        final List<T> repr = originalRk.getRepresentation();
        int rInd = GeneticAlgorithm.getRandomGenerator().nextInt(repr.size());

        List<T> newRepr = new ArrayList<>(repr);
        newRepr.set(rInd, geneGenerator.apply(repr.get(rInd)));

        return originalRk.newFixedLengthChromosome(newRepr);
    }
}
