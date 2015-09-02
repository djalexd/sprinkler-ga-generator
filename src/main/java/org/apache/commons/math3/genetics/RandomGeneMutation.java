package org.apache.commons.math3.genetics;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutates a chromosome based on {@link GeneGenerator}
 */
public class RandomGeneMutation<T> implements MutationPolicy {

    private GeneGenerator<T> geneGenerator;

    public RandomGeneMutation(GeneGenerator<T> geneGenerator) {
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
        newRepr.set(rInd, geneGenerator.generateRandomValue());

        return originalRk.newFixedLengthChromosome(newRepr);
    }
}
