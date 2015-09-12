package org.house.sprinklers.genetics;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.Population;

public interface ChromosomeListener {
    void onChromosome(Population population, int generation, Chromosome chromosome);
}
