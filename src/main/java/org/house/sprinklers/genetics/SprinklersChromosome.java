package org.house.sprinklers.genetics;

import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.house.sprinklers.GeneticAlgorithmProperties;
import org.house.sprinklers.fitness.FitnessCalculator;
import org.house.sprinklers.fitness.FitnessInput;
import org.house.sprinklers.fitness.FitnessInputCalculator;
import org.house.sprinklers.population.InvalidSprinklerException;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;

import java.util.List;

/**
 * Chromosome representation using a list of
 * {@link org.house.sprinklers.sprinkler_system.Sprinkler sprinklers}.
 */
public class SprinklersChromosome extends AbstractListChromosome<Sprinkler> {

    private SprinklerValidator sprinklerValidator;
    private FitnessCalculator fitnessCalculator;
    private FitnessInputCalculator fitnessInputCalculator;
    private Terrain terrain;
    private GeneticAlgorithmProperties.ChromosomeProperties chromosomeProperties;

    public SprinklersChromosome(final List<Sprinkler> representation,
                                final SprinklerValidator sprinklerValidator,
                                final FitnessCalculator fitnessCalculator,
                                final FitnessInputCalculator fitnessInputCalculator,
                                final Terrain terrain,
                                GeneticAlgorithmProperties.ChromosomeProperties chromosomeProperties)
            throws InvalidRepresentationException {

        super(representation);
        this.sprinklerValidator = sprinklerValidator;
        this.fitnessCalculator = fitnessCalculator;
        this.fitnessInputCalculator = fitnessInputCalculator;
        this.terrain = terrain;
        this.chromosomeProperties = chromosomeProperties;

        checkValidity(representation);
    }

    @Override
    protected void checkValidity(final List<Sprinkler> chromosomeRepresentation)
            throws InvalidRepresentationException {
        // Unfortunately, checkValidity is checked in super constructor and
        // sprinklerValidator field is not yet initialized.
        if (sprinklerValidator == null) {
            return;
        }
        for (final Sprinkler sprinkler: chromosomeRepresentation) {
            try {
                sprinklerValidator.validate(sprinkler);
            } catch (InvalidSprinklerException e) {
                throw new InvalidRepresentationException(LocalizedFormats.ILLEGAL_STATE);
            }
        }
    }

    @Override
    public AbstractListChromosome<Sprinkler> newFixedLengthChromosome(
            final List<Sprinkler> chromosomeRepresentation) {
        ensureChromosomeValidSize(chromosomeRepresentation);
        return new SprinklersChromosome(chromosomeRepresentation, sprinklerValidator, fitnessCalculator, fitnessInputCalculator, terrain, chromosomeProperties);
    }

    @Override
    public double fitness() {
        try {
            ensureChromosomeValidSize(getRepresentation());
            final FitnessInput fitnessInput = fitnessInputCalculator.computeFitnessInput(getRepresentation(), terrain);
            return fitnessCalculator.computeFitness(fitnessInput);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to compute Chromosome fitness", e);
        }
    }

    private void ensureChromosomeValidSize(List<Sprinkler> representation) {
        if (representation.size() > chromosomeProperties.getMaxLength()) {
            throw new IllegalStateException(String.format("Cannot have more than %d chromosomes, but found %d",
                    chromosomeProperties.getMaxLength(),representation.size()));
        }
    }

    @Override
    public List<Sprinkler> getRepresentation() {
        return super.getRepresentation();
    }
}
