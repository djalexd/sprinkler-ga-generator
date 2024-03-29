package org.apache.commons.math3.genetics;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.house.sprinklers.GeneticAlgorithmProperties;
import org.house.sprinklers.genetics.GeneGeneratorConfiguration;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;

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

    private GeneticAlgorithmProperties.ChromosomeProperties chromosomeConfig;

    private RecorderService recorderService;

    public RandomGeneMutation(Function<T, T> geneGenerator,
                              GeneGeneratorConfiguration config,
                              GeneticAlgorithmProperties.ChromosomeProperties chromosomeConfig,
                              RecorderService recorderService) {
        this.geneGenerator = geneGenerator;
        this.config = config;
        this.chromosomeConfig = chromosomeConfig;
        this.recorderService = recorderService;
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

        recorderService.increment(MetricsConstants.COUNTER_GA_MUTATIONS);

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
            newGenesCount = Math.min(newGenesCount, chromosomeConfig.getMaxLength() - repr.size());
            if (newGenesCount > 0) {
                newRepr.addAll(IntStream.range(0, newGenesCount)
                                .mapToObj(i -> geneGenerator.apply(null))
                                .collect(Collectors.toList()));
                recorderService.increment(MetricsConstants.COUNTER_GA_MUTATIONS_INSERT);
            }

        } else if (prob < p2) {
            // Change existing genes.
            int changeGenesCount = config.getMinGenesToChange() +
                    randomGenerator.nextInt(config.getMaxGenesToChange() - config.getMinGenesToChange());
            // it's safe to change each gene because indices are unique.
            Arrays.asList(generateUniqueIndices(repr.size(), changeGenesCount))
                    .stream()
                    .forEach(i -> newRepr.set(i, geneGenerator.apply(repr.get(i))));
            recorderService.increment(MetricsConstants.COUNTER_GA_MUTATIONS_CHANGE);

        } else if (prob < p3) {
            // Remove existing genes from anywhere in genome.
            int removeGenesCount = config.getMinGenesToRemove() +
                    randomGenerator.nextInt(config.getMaxGenesToRemove() - config.getMinGenesToRemove());
            removeGenesCount = Math.min(removeGenesCount, repr.size() - chromosomeConfig.getMinLength());
            if (removeGenesCount > 0) {
                // special case here, we will remove from max index to min index.
                final List<Integer> list = Arrays.asList(generateUniqueIndices(repr.size(), removeGenesCount));
                Collections.sort(list);
                Collections.reverse(list);
                list.stream().forEach(newRepr::remove);
                recorderService.increment(MetricsConstants.COUNTER_GA_MUTATIONS_DELETE);
            }
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
