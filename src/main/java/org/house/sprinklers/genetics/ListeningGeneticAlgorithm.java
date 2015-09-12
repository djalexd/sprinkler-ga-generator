package org.house.sprinklers.genetics;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.SelectionPolicy;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;

/**
 * Extends {@link org.apache.commons.math3.genetics.GeneticAlgorithm} in order
 * to provide listeners for population evolution and chromosomes.
 */
public class ListeningGeneticAlgorithm extends GeneticAlgorithm {

    private PopulationListener populationListener;

    /** the number of generations evolved to reach {@link StoppingCondition} in the last run. */
    private int generationsEvolved = 0;

    private RecorderService recorderService;

    public ListeningGeneticAlgorithm(CrossoverPolicy crossoverPolicy,
                                     double crossoverRate,
                                     MutationPolicy mutationPolicy,
                                     double mutationRate,
                                     SelectionPolicy selectionPolicy,
                                     final RecorderService recorderService,
                                     PopulationListener populationListener) throws OutOfRangeException {
        super(crossoverPolicy, crossoverRate, mutationPolicy, mutationRate, selectionPolicy);
        this.recorderService = recorderService;
        this.populationListener = populationListener;
    }

    @Override
    public Population evolve(Population initial, StoppingCondition condition) {
        Population current = initial;
        generationsEvolved = 0;
        while (!condition.isSatisfied(current)) {
            populationListener.onPopulation(current, generationsEvolved);
            current = nextGeneration(current);
            generationsEvolved++;
        }

        recorderService.increment(MetricsConstants.COUNTER_GA_GENERATIONS, generationsEvolved);
        return current;
    }
}
