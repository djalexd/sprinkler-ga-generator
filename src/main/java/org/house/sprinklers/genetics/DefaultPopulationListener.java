package org.house.sprinklers.genetics;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.Population;
import org.house.sprinklers.GameRenderer;
import org.house.sprinklers.fitness.FitnessInput;
import org.house.sprinklers.metrics.GenerationAndValue;
import org.house.sprinklers.metrics.MetricsConstants;
import org.house.sprinklers.metrics.RecorderService;

import java.util.stream.StreamSupport;

@Slf4j
public class DefaultPopulationListener implements PopulationListener {

    private RecorderService recorderService;
    private GameRenderer gameRenderer;

    public DefaultPopulationListener(RecorderService recorderService, GameRenderer gameRenderer) {
        this.recorderService = recorderService;
        // TODO This is a bad dependency
        this.gameRenderer = gameRenderer;
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

        final SprinklersChromosome fittestChromosome = (SprinklersChromosome) current.getFittestChromosome();
        gameRenderer.setSprinklers(fittestChromosome.getRepresentation());

        log.info("Completed generation {}", generation);
    }
}
