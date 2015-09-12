package org.house.sprinklers.genetics;

import org.apache.commons.math3.genetics.Population;

public interface PopulationListener {
    void onPopulation(Population current, int generation);
}
