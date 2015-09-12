package org.house.sprinklers.genetics;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.Population;
import org.house.sprinklers.fitness.FitnessInput;
import org.house.sprinklers.metrics.GenerationAndValue;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;

import java.util.stream.StreamSupport;

public class DefaultPopulationListener implements PopulationListener {

    private RecorderService recorderService;

    public DefaultPopulationListener(RecorderService recorderService) {
        this.recorderService = recorderService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPopulation(Population current, final int generation) {
        recorderService.increment(MetricsConstants.COUNTER_GA_INDIVIDUALS, current.getPopulationSize());

        final double averageFitness = StreamSupport.stream(current.spliterator(), false)
                .mapToDouble(Chromosome::getFitness)
                .sum() / current.getPopulationSize();
        recorderService.submit(MetricsConstants.METRIC_GA_GENERATION_FITNESS,
                new GenerationAndValue(generation, averageFitness));

        double generationCoveredArea = StreamSupport.stream(current.spliterator(), false)
                .mapToDouble(c -> {
                    if (c instanceof DataAwareChromosome) {
                        DataAwareChromosome<FitnessInput> dac = (DataAwareChromosome<FitnessInput>) c;
                        return dac.getRawFitnessData().getCoveredArea();
                    } else {
                        return 0;
                    }
                })
                .sum() / current.getPopulationSize();
        recorderService.submit(MetricsConstants.METRIC_GA_GENERATION_COVERED_AREA,
                new GenerationAndValue(generation, generationCoveredArea));

    }
}
