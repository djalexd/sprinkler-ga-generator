package org.house.sprinklers;

import lombok.Data;
import org.apache.commons.math3.genetics.GeneGeneratorConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "geneticAlgorithm")
@Data
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
    @NotNull
    private GeneGeneratorConfiguration mutation;

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
