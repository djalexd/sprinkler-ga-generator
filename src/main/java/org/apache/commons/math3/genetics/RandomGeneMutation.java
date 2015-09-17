package org.apache.commons.math3.genetics;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mutates chromosomes based on {@link java.util.function.Function}.
 *
 * <p/>This implementation can perform many mutations (i.e. a mutation is
 * considered change(s) to a genome, not to a single gene). For example,
 * it can add new genes, remove existing genes or change existing genes
 */
public class RandomGeneMutation<T> implements MutationPolicy {

    private static RandomGenerator randomGenerator = new JDKRandomGenerator();

    private Function<T, T> geneGenerator;

    private GeneGeneratorConfiguration config;

    public RandomGeneMutation(Function<T, T> geneGenerator,
                              GeneGeneratorConfiguration config) {
        this.geneGenerator = geneGenerator;
        this.config = config;
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
        final List<T> newRepr = new ArrayList<>(repr);

        double p1 = config.getProbabilityInsertGenes(),
                p2 = p1 + config.getProbabilityChangeGenes(),
                p3 = p2 + config.getProbabilityRemoveGene();
        double prob = randomGenerator.nextDouble();

        if (prob < p1) {
            // Insert new genes at the end of chromosome.
            int newGenesCount = config.getMinGenesToInsert() +
                    randomGenerator.nextInt(config.getMaxGenesToInsert() - config.getMinGenesToInsert());
            newRepr.addAll(
                    IntStream.range(0, newGenesCount)
                            .mapToObj(i -> geneGenerator.apply(null))
                            .collect(Collectors.toList()));
        } else if (prob < p2) {
            // Change existing genes.
            int changeGenesCount = config.getMinGenesToChange() +
                    randomGenerator.nextInt(config.getMaxGenesToChange() - config.getMinGenesToChange());
            // it's safe to change each gene because indices are unique.
            Arrays.asList(generateUniqueIndices(repr.size(), changeGenesCount))
                    .stream()
                    .forEach(i -> newRepr.set(i, geneGenerator.apply(repr.get(i))));
        } else if (prob < p3) {
            // Remove existing genes from anywhere in genome.
            int removeGenesCount = config.getMinGenesToRemove() +
                    randomGenerator.nextInt(config.getMaxGenesToRemove() - config.getMinGenesToRemove());
            // special case here, we will remove from max index to min index.
            final List<Integer> list = Arrays.asList(generateUniqueIndices(repr.size(), removeGenesCount));
            Collections.sort(list);
            Collections.reverse(list);
            list.stream().forEach(newRepr::remove);
        }

        return originalRk.newFixedLengthChromosome(newRepr);
    }

    /**
     * Generates an array of unique indices.
     */
    private static Integer[] generateUniqueIndices(int size, int count) {
        List<Integer> integers = IntStream.range(0, size).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        while (integers.size() > count) {
            integers.remove(randomGenerator.nextInt(integers.size()));
        }
        return integers.toArray(new Integer[integers.size()]);
    }
}
