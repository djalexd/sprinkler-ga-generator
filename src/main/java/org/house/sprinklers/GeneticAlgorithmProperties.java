package org.house.sprinklers;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "geneticAlgorithm")
@Data
@Component
public class GeneticAlgorithmProperties {

    private int generations;
    private int tournamentArity;
    private double crossoverRate;
    private double mutationRate;
    @NotNull
    private CrossoverProperties crossover;
    private double elitismRate;
    @NotNull
    private PopulationProperties population;

    @Data
    public static class CrossoverProperties {
        private int minimumLength;
    }

    @Data
    public static class PopulationProperties {
        private int initialSize;
        private int maximumSize;
    }
}
